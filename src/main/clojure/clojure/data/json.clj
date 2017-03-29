;; Copyright (c) Stuart Sierra, 2012. All rights reserved.  The use
;; and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this
;; distribution.  By using this software in any fashion, you are
;; agreeing to be bound by the terms of this license.  You must not
;; remove this notice, or any other, from this software.

(ns ^{:author "Stuart Sierra"
      :doc "JavaScript Object Notation (JSON) parser/generator.
  See http://www.json.org/"}
  clojure.data.json
  (:refer-clojure :exclude (read))
  (:require [clojure.pprint :as pprint])
  (:import (java.io PrintWriter PushbackReader StringWriter
                    Writer StringReader EOFException Reader)))

;;; JSON READER

(def ^{:dynamic true :private true} *bigdec*)
(def ^{:dynamic true :private true} *key-fn*)
(def ^{:dynamic true :private true} *value-fn*)
(def ^{:dynamic true :private true} *track-pos*)

(defn- default-write-key-fn
  [x]
  (cond (instance? clojure.lang.Named x)
        (name x)
        (nil? x)
        (throw (Exception. "JSON object properties may not be nil"))
        :else (str x)))

(defn- default-value-fn [k v] v)

(declare -read)

(defmacro ^:private codepoint [c]
  (int c))

(def ^:private whitespace
  (list (codepoint \tab)
        (codepoint \newline)
        (codepoint \return)
        (codepoint \space)))

(defn- codepoint-clause [[test result]]
  (cond (list? test)
        [(map int test) result]
        (= test :whitespace)
        [whitespace result]
        (= test :simple-ascii)
        [(remove #{(codepoint \") (codepoint \\) (codepoint \/)}
                 (range 32 127))
         result]
        (= test :js-separators)
        ['(16r2028 16r2029) result]
        :else
        [(int test) result]))

(defmacro ^:private codepoint-case [e & clauses]
  `(case ~e
     ~@(mapcat codepoint-clause (partition 2 clauses))
     ~@(when (odd? (count clauses))
         [(last clauses)])))

(defn- create-reader [^Reader reader]
  {:reader (PushbackReader. reader)
   :position (atom [1 0])
   :start (atom '())})

(defn- advance-line [[line _]]
  [(inc line) 0])

(defn- reverse-line [[line _]]
  [(dec line) -1])

(defn- advance-column [[line column]]
  [line (inc column)])

(defn- reverse-column [[line column]]
  [line (dec column)])

(defn- read-char [stream]
  (let [{:keys [^PushbackReader reader position]} stream
        c (.read reader)]
    (when-let [c-char (when-not (neg? c) (char c))]
      (if (#{\newline \return} c-char)
        (swap! position advance-line)
        (swap! position advance-column))
      
      (when (= c-char \return) 
        (let [next-c (.read reader)]
          (when-not (= (char next-c) \newline)
            (.unread reader next-c)))))
    c))

(defn- unread-char [stream ^Integer c]
  (let [{:keys [^PushbackReader reader position]} stream]
    (when-let [c-char (when-not (neg? c) (char c))]
      (if (#{\newline \return} c-char)
        (swap! position reverse-line)
        (swap! position reverse-column)))
    (.unread reader c)))

(defn- exception [stream text]
  (let [[line column] @(:position stream)]
    (Exception. (printf "%s: line %d, column %d" text line column))))

(defn- eof-exception [stream text]
  (let [[line column] (peek @(:start stream))]
    (EOFException. (printf "%s: starting from line %d, column %d" text line column))))

(defn- push-position [stream & _]
  (swap! (:start stream) conj @(:position stream)))

(defn- pop-position [stream & _]
  (swap! (:start stream) pop))

(defmacro ^:private defnested [n args & body]
  `(def ^:private ~n
     (fn ~args 
       (apply push-position ~args)
       (try
         (apply (fn ~args ~@body) ~args)
         (finally (apply pop-position ~args))))))

(defn- track-pos [result positions]
  (if *track-pos*
    (vary-meta result assoc :pos positions)
    result))

(defnested read-array [stream]
  ;; Expects to be called with the head of the stream AFTER the
  ;; opening bracket.
  (loop [result (transient []) positions (transient [])]
    (let [c (read-char stream)
          start @(:position stream)]
      (when (neg? c)
        (throw (eof-exception stream "JSON error (end-of-file inside array)")))
      (codepoint-case c
        :whitespace (recur result positions)
        \, (recur result positions)
        \] (track-pos (persistent! result) (persistent! positions))
        (do (unread-char stream c)
            (let [element (-read stream true nil)]
              (recur (conj! result element) (conj! positions start))))))))

(defnested read-object [stream]
  ;; Expects to be called with the head of the stream AFTER the
  ;; opening bracket.
  (loop [key nil result (transient {}) positions (transient {})]
    (let [c (read-char stream)
          start @(:position stream)]
      (when (neg? c)
        (throw (eof-exception stream "JSON error (end-of-file inside object)")))
      (codepoint-case c
        :whitespace (recur key result positions)

        \, (recur nil result positions)

        \: (recur key result positions)

        \} (if (nil? key)
             (track-pos (persistent! result) (persistent! positions))
             (throw (exception stream "JSON error (key missing value in object)")))

        (do (unread-char stream c)
            (let [element (-read stream true nil)]
              (if (nil? key)
                (if (string? element)
                  (recur element result positions)
                  (throw (exception stream "JSON error (non-string key in object)")))
                (let [out-key (*key-fn* key)
                      out-value (*value-fn* out-key element)]
                  (recur nil
                         (if (= *value-fn* out-value)
                           result
                           (assoc! result out-key out-value))
                         (assoc! positions out-key start))))))))))

(defnested read-hex-char [stream]
  ;; Expects to be called with the head of the stream AFTER the
  ;; initial "\u".  Reads the next four characters from the stream.
  (let [a (read-char stream)
        b (read-char stream)
        c (read-char stream)
        d (read-char stream)]
    (when (or (neg? a) (neg? b) (neg? c) (neg? d))
      (throw (eof-exception
               stream
               "JSON error (end-of-file inside Unicode character escape)")))
    (let [s (str (char a) (char b) (char c) (char d))]
      (char (Integer/parseInt s 16)))))

(defnested read-escaped-char [stream]
  ;; Expects to be called with the head of the stream AFTER the
  ;; initial backslash.
  (let [c (read-char stream)]
    (when (neg? c)
      (throw (eof-exception stream "JSON error (end-of-file inside escaped char)")))
    (codepoint-case c
      (\" \\ \/) (char c)
      \b \backspace
      \f \formfeed
      \n \newline
      \r \return
      \t \tab
      \u (read-hex-char stream))))

(defnested read-quoted-string [stream]
  ;; Expects to be called with the head of the stream AFTER the
  ;; opening quotation mark.
  (let [buffer (StringBuilder.)]
    (loop []
      (let [c (read-char stream)]
        (when (neg? c)
          (throw (eof-exception stream "JSON error (end-of-file inside string)")))
        (codepoint-case c
          \" (str buffer)
          \\ (do (.append buffer (read-escaped-char stream))
                 (recur))
          (do (.append buffer (char c))
              (recur)))))))

(defn- read-integer [^String string]
  (if (< (count string) 18)  ; definitely fits in a Long
    (Long/valueOf string)
    (or (try (Long/valueOf string)
             (catch NumberFormatException e nil))
        (bigint string))))

(defn- read-decimal [^String string]
  (if *bigdec*
    (bigdec string)
    (Double/valueOf string)))

(defn- read-number [stream]
  (let [buffer (StringBuilder.)
        decimal? (loop [decimal? false]
                   (let [c (read-char stream)]
                     (codepoint-case c
                       (\- \+ \0 \1 \2 \3 \4 \5 \6 \7 \8 \9)
                       (do (.append buffer (char c))
                           (recur decimal?))
                       (\e \E \.)
                       (do (.append buffer (char c))
                           (recur true))
                       (do (unread-char stream c)
                           decimal?))))]
    (if decimal?
      (read-decimal (str buffer))
      (read-integer (str buffer)))))

(defnested -read
  [stream eof-error? eof-value]
  (loop []
    (let [c (read-char stream)]
      (if (neg? c) ;; Handle end-of-stream
        (if eof-error?
          (throw (eof-exception stream "JSON error (end-of-file)"))
          eof-value)
        (codepoint-case
          c
          :whitespace (recur)

          ;; Read numbers
          (\- \0 \1 \2 \3 \4 \5 \6 \7 \8 \9)
          (do (unread-char stream c)
              (read-number stream))

          ;; Read strings
          \" (read-quoted-string stream)

          ;; Read null as nil
          \n (if (and (= (codepoint \u) (read-char stream))
                      (= (codepoint \l) (read-char stream))
                      (= (codepoint \l) (read-char stream)))
               nil
               (throw (exception stream "JSON error (expected null)")))

          ;; Read true
          \t (if (and (= (codepoint \r) (read-char stream))
                      (= (codepoint \u) (read-char stream))
                      (= (codepoint \e) (read-char stream)))
               true
               (throw (exception stream "JSON error (expected true)")))

          ;; Read false
          \f (if (and (= (codepoint \a) (read-char stream))
                      (= (codepoint \l) (read-char stream))
                      (= (codepoint \s) (read-char stream))
                      (= (codepoint \e) (read-char stream)))
               false
               (throw (exception stream "JSON error (expected false)")))

          ;; Read JSON objects
          \{ (read-object stream)

          ;; Read JSON arrays
          \[ (read-array stream)

          (throw (exception
                   stream
                   (str "JSON error (unexpected character): " (char c)))))))))

(defn read
  "Reads a single item of JSON data from a java.io.Reader. Options are
  key-value pairs, valid options are:

     :eof-error? boolean

        If true (default) will throw exception if the stream is empty.

     :eof-value Object

        Object to return if the stream is empty and eof-error? is
        false. Default is nil.

     :bigdec boolean

        If true use BigDecimal for decimal numbers instead of Double.
        Default is false.

     :key-fn function

        Single-argument function called on JSON property names; return
        value will replace the property names in the output. Default
        is clojure.core/identity, use clojure.core/keyword to get
        keyword properties.

     :value-fn function

        Function to transform values in maps (\"objects\" in JSON) in
        the output. For each JSON property, value-fn is called with
        two arguments: the property name (transformed by key-fn) and
        the value. The return value of value-fn will replace the value
        in the output. If value-fn returns itself, the property will
        be omitted from the output. The default value-fn returns the
        value unchanged. This option does not apply to non-map
        collections.
  
     :track-pos? boolean
  
        If true, positional metadata will be attached to each vector and map
        that composes the result.  This information is attached under the
        keyword :pos in the metadata map.  The positional data is stored as
        two element vectors, [line column], that reflects the starting
        position in the stream where a given member began.
  
        For arrays, an array of equal length is stored under :pos such
        that the position corresponds to the data in the same index.  For 
        maps, a map containing the same keys is store under :pos where
        the key values correspond to the starting position of the map value.
  
        By default, :track-pos? is set to false."

  [reader & options]
  (let [{:keys [eof-error? eof-value bigdec key-fn value-fn track-pos?]
         :or {bigdec false
              eof-error? true
              key-fn identity
              value-fn default-value-fn
              track-pos? false}} options]
    (binding [*bigdec* bigdec
              *key-fn* key-fn
              *value-fn* value-fn
              *track-pos* track-pos?]
      (-read (create-reader reader) eof-error? eof-value))))

(defn read-str
  "Reads one JSON value from input String. Options are the same as for
  read."
  [string & options]
  (apply read (StringReader. string) options))

;;; JSON WRITER

(def ^{:dynamic true :private true} *escape-unicode*)
(def ^{:dynamic true :private true} *escape-js-separators*)
(def ^{:dynamic true :private true} *escape-slash*)

(defprotocol JSONWriter
  (-write [object out]
    "Print object to PrintWriter out as JSON"))

(defn- write-string [^CharSequence s ^PrintWriter out]
  (let [sb (StringBuilder. (count s))]
    (.append sb \")
    (dotimes [i (count s)]
      (let [cp (int (.charAt s i))]
        (codepoint-case cp
          ;; Printable JSON escapes
          \" (.append sb "\\\"")
          \\ (.append sb "\\\\")
          \/ (.append sb (if *escape-slash* "\\/" "/"))
          ;; Simple ASCII characters
          :simple-ascii (.append sb (.charAt s i))
          ;; JSON escapes
          \backspace (.append sb "\\b")
          \formfeed  (.append sb "\\f")
          \newline   (.append sb "\\n")
          \return    (.append sb "\\r")
          \tab       (.append sb "\\t")
          ;; Unicode characters that Javascript forbids raw in strings
          :js-separators (if *escape-js-separators*
                           (.append sb (format "\\u%04x" cp))
                           (.appendCodePoint sb cp))
          ;; Any other character is Unicode
          (if *escape-unicode*
            (.append sb (format "\\u%04x" cp)) ; Hexadecimal-escaped
            (.appendCodePoint sb cp)))))
    (.append sb \")
    (.print out (str sb))))

(defn- write-object [m ^PrintWriter out] 
  (.print out \{)
  (loop [x m, have-printed-kv false]
    (when (seq m)
      (let [[k v] (first x)
            out-key (*key-fn* k)
            out-value (*value-fn* k v)
            nxt (next x)]
        (when-not (string? out-key)
          (throw (Exception. "JSON object keys must be strings")))
        (if-not (= *value-fn* out-value)
          (do
            (when have-printed-kv
              (.print out \,))
            (write-string out-key out)
            (.print out \:)
            (-write out-value out)
            (when (seq nxt)
              (recur nxt true)))
          (when (seq nxt)
            (recur nxt have-printed-kv))))))
  (.print out \}))

(defn- write-array [s ^PrintWriter out]
  (.print out \[)
  (loop [x s]
    (when (seq x)
      (let [fst (first x)
            nxt (next x)]
        (-write fst out)
        (when (seq nxt)
          (.print out \,)
          (recur nxt)))))
  (.print out \]))

(defn- write-bignum [x ^PrintWriter out]
  (.print out (str x)))

(defn- write-float [^Float x ^PrintWriter out]
  (cond (.isInfinite x)
        (throw (Exception. "JSON error: cannot write infinite Float"))
        (.isNaN x)
        (throw (Exception. "JSON error: cannot write Float NaN"))
        :else
        (.print out x)))

(defn- write-double [^Double x ^PrintWriter out]
  (cond (.isInfinite x)
        (throw (Exception. "JSON error: cannot write infinite Double"))
        (.isNaN x)
        (throw (Exception. "JSON error: cannot write Double NaN"))
        :else
        (.print out x)))

(defn- write-plain [x ^PrintWriter out]
  (.print out x))

(defn- write-null [x ^PrintWriter out]
  (.print out "null"))

(defn- write-named [x out]
  (write-string (name x) out))

(defn- write-generic [x out]
  (if (.isArray (class x))
    (-write (seq x) out)
    (throw (Exception. (str "Don't know how to write JSON of " (class x))))))

(defn- write-ratio [x out]
  (-write (double x) out))

;; nil, true, false
(extend nil                    JSONWriter {:-write write-null})
(extend java.lang.Boolean      JSONWriter {:-write write-plain})

;; Numbers
(extend java.lang.Byte         JSONWriter {:-write write-plain})
(extend java.lang.Short        JSONWriter {:-write write-plain})
(extend java.lang.Integer      JSONWriter {:-write write-plain})
(extend java.lang.Long         JSONWriter {:-write write-plain})
(extend java.lang.Float        JSONWriter {:-write write-float})
(extend java.lang.Double       JSONWriter {:-write write-double})
(extend clojure.lang.Ratio     JSONWriter {:-write write-ratio})
(extend java.math.BigInteger   JSONWriter {:-write write-bignum})
(extend java.math.BigDecimal   JSONWriter {:-write write-bignum})
(extend java.util.concurrent.atomic.AtomicInteger JSONWriter {:-write write-plain})
(extend java.util.concurrent.atomic.AtomicLong    JSONWriter {:-write write-plain})
;; Attempt to support Clojure 1.2.x:
(when-let [class (try (.. Thread currentThread getContextClassLoader
                          (loadClass "clojure.lang.BigInt"))
                      (catch ClassNotFoundException _ false))]
  (extend class JSONWriter {:-write write-bignum}))


;; Symbols, Keywords, and Strings
(extend clojure.lang.Named     JSONWriter {:-write write-named})
(extend java.lang.CharSequence JSONWriter {:-write write-string})

;; Collections
(extend java.util.Map          JSONWriter {:-write write-object})
(extend java.util.Collection   JSONWriter {:-write write-array})

;; Maybe a Java array, otherwise fail
(extend java.lang.Object       JSONWriter {:-write write-generic})

(defn write
  "Write JSON-formatted output to a java.io.Writer. Options are
   key-value pairs, valid options are:

    :escape-unicode boolean

       If true (default) non-ASCII characters are escaped as \\uXXXX

    :escape-js-separators boolean

       If true (default) the Unicode characters U+2028 and U+2029 will
       be escaped as \\u2028 and \\u2029 even if :escape-unicode is
       false. (These two characters are valid in pure JSON but are not
       valid in JavaScript strings.)

    :escape-slash boolean

       If true (default) the slash / is escaped as \\/

    :key-fn function

        Single-argument function called on map keys; return value will
        replace the property names in the output. Must return a
        string. Default calls clojure.core/name on symbols and
        keywords and clojure.core/str on everything else.

    :value-fn function

        Function to transform values in maps before writing. For each
        key-value pair in an input map, called with two arguments: the
        key (BEFORE transformation by key-fn) and the value. The
        return value of value-fn will replace the value in the output.
        If the return value is a number, boolean, string, or nil it
        will be included literally in the output. If the return value
        is a non-map collection, it will be processed recursively. If
        the return value is a map, it will be processed recursively,
        calling value-fn again on its key-value pairs. If value-fn
        returns itself, the key-value pair will be omitted from the
        output. This option does not apply to non-map collections."
  [x ^Writer writer & options]
  (let [{:keys [escape-unicode escape-js-separators escape-slash key-fn value-fn]
         :or {escape-unicode true
              escape-js-separators true
              escape-slash true
              key-fn default-write-key-fn
              value-fn default-value-fn}} options]
    (binding [*escape-unicode* escape-unicode
              *escape-js-separators* escape-js-separators
              *escape-slash* escape-slash
              *key-fn* key-fn
              *value-fn* value-fn]
      (-write x (PrintWriter. writer)))))

(defn write-str
  "Converts x to a JSON-formatted string. Options are the same as
  write."
  [x & options]
  (let [sw (StringWriter.)]
    (apply write x sw options)
    (.toString sw)))

;;; JSON PRETTY-PRINTER

;; Based on code by Tom Faulhaber

(defn- pprint-array [s] 
  ((pprint/formatter-out "~<[~;~@{~w~^, ~:_~}~;]~:>") s))

(defn- pprint-object [m]
  ((pprint/formatter-out "~<{~;~@{~<~w:~_~w~:>~^, ~_~}~;}~:>") 
   (for [[k v] m] [(*key-fn* k) v])))

(defn- pprint-generic [x]
  (if (.isArray (class x))
    (pprint-array (seq x))
    ;; pprint proxies Writer, so we can't just wrap it
    (print (with-out-str (-write x (PrintWriter. *out*))))))

(defn- pprint-dispatch [x]
  (cond (nil? x) (print "null")
        (instance? java.util.Map x) (pprint-object x)
        (instance? java.util.Collection x) (pprint-array x)
        (instance? clojure.lang.ISeq x) (pprint-array x)
        :else (pprint-generic x)))

(defn pprint
  "Pretty-prints JSON representation of x to *out*. Options are the
  same as for write except :value-fn, which is not supported."
  [x & options]
  (let [{:keys [escape-unicode escape-slash key-fn]
         :or {escape-unicode true
              escape-slash true
              key-fn default-write-key-fn}} options]
    (binding [*escape-unicode* escape-unicode
              *escape-slash* escape-slash
              *key-fn* key-fn]
      (pprint/with-pprint-dispatch pprint-dispatch
        (pprint/pprint x)))))

(load "json_compat_0_1")

;; Local Variables:
;; mode: clojure
;; eval: (define-clojure-indent (codepoint-case (quote defun)))
;; End:
