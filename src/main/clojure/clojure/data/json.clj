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
                    Writer StringReader EOFException)))

;;; JSON READER

(def ^{:dynamic true :private true} *bigdec*)
(def ^{:dynamic true :private true} *key-fn*)
(def ^{:dynamic true :private true} *value-fn*)
(def ^{:dynamic true :private true} *pair-fn*)

(defn- default-write-key-fn
  [x]
  (cond (instance? clojure.lang.Named x)
        (name x)
        (nil? x)
        (throw (Exception. "JSON object properties may not be nil"))
        :else (str x)))

(defn- default-value-fn [k v] v)

(defn- default-pair-fn [k v] [k v])

(declare -read)

(defmacro ^:private codepoint [c]
  (int c))

(defn- codepoint-clause [[test result]]
  (cond (list? test)
        [(map int test) result]
        (= test :whitespace)
        ['(9 10 13 32) result]
        (= test :simple-ascii)
        [(remove #{(codepoint \") (codepoint \\) (codepoint \/)}
                 (range 32 127))
         result]
        :else
        [(int test) result]))

(defmacro ^:private codepoint-case [e & clauses]
  `(case ~e
     ~@(mapcat codepoint-clause (partition 2 clauses))
     ~@(when (odd? (count clauses))
         [(last clauses)])))

(defn- read-array [^PushbackReader stream]
  ;; Expects to be called with the head of the stream AFTER the
  ;; opening bracket.
  (loop [result (transient [])]
    (let [c (.read stream)]
      (when (neg? c)
        (throw (EOFException. "JSON error (end-of-file inside array)")))
      (codepoint-case c
        :whitespace (recur result)
        \, (recur result)
        \] (persistent! result)
        (do (.unread stream c)
            (let [element (-read stream true nil)]
              (recur (conj! result element))))))))

(defn- read-object [^PushbackReader stream]
  ;; Expects to be called with the head of the stream AFTER the
  ;; opening bracket.
  (loop [key nil, result (transient {})]
    (let [c (.read stream)]
      (when (neg? c)
        (throw (EOFException. "JSON error (end-of-file inside object)")))
      (codepoint-case c
        :whitespace (recur key result)

        \, (recur nil result)

        \: (recur key result)

        \} (if (nil? key)
             (persistent! result)
             (throw (Exception. "JSON error (key missing value in object)")))

        (do (.unread stream c)
            (let [element (-read stream true nil)]
              (if (nil? key)
                (if (string? element)
                  (recur element result)
                  (throw (Exception. "JSON error (non-string key in object)")))
                (recur nil
                       (let [out-key (*key-fn* key)
                             out-value (*value-fn* out-key element)
                             [out-key out-value] (*pair-fn* out-key out-value)]
                         (if (= *value-fn* out-value)
                           result
                           (assoc! result out-key out-value)))))))))))

(defn- read-hex-char [^PushbackReader stream]
  ;; Expects to be called with the head of the stream AFTER the
  ;; initial "\u".  Reads the next four characters from the stream.
  (let [a (.read stream)
        b (.read stream)
        c (.read stream)
        d (.read stream)]
    (when (or (neg? a) (neg? b) (neg? c) (neg? d))
      (throw (EOFException.
              "JSON error (end-of-file inside Unicode character escape)")))
    (let [s (str (char a) (char b) (char c) (char d))]
      (char (Integer/parseInt s 16)))))

(defn- read-escaped-char [^PushbackReader stream]
  ;; Expects to be called with the head of the stream AFTER the
  ;; initial backslash.
  (let [c (.read stream)]
    (codepoint-case c
      (\" \\ \/) (char c)
      \b \backspace
      \f \formfeed
      \n \newline
      \r \return
      \t \tab
      \u (read-hex-char stream))))

(defn- read-quoted-string [^PushbackReader stream]
  ;; Expects to be called with the head of the stream AFTER the
  ;; opening quotation mark.
  (let [buffer (StringBuilder.)]
    (loop []
      (let [c (.read stream)]
        (when (neg? c)
          (throw (EOFException. "JSON error (end-of-file inside string)")))
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

(defn- read-number [^PushbackReader stream]
  (let [buffer (StringBuilder.)
        decimal? (loop [decimal? false]
                   (let [c (.read stream)]
                     (codepoint-case c
                       (\- \+ \0 \1 \2 \3 \4 \5 \6 \7 \8 \9)
                       (do (.append buffer (char c))
                           (recur decimal?))
                       (\e \E \.)
                       (do (.append buffer (char c))
                           (recur true))
                       (do (.unread stream c)
                           decimal?))))]
    (if decimal?
      (read-decimal (str buffer))
      (read-integer (str buffer)))))

(defn- -read
  [^PushbackReader stream eof-error? eof-value]
  (loop []
    (let [c (.read stream)]
      (if (neg? c) ;; Handle end-of-stream
        (if eof-error?
          (throw (EOFException. "JSON error (end-of-file)"))
          eof-value)
        (codepoint-case
          c
          :whitespace (recur)

          ;; Read numbers
          (\- \0 \1 \2 \3 \4 \5 \6 \7 \8 \9)
          (do (.unread stream c)
              (read-number stream))

          ;; Read strings
          \" (read-quoted-string stream)

          ;; Read null as nil
          \n (if (and (= (codepoint \u) (.read stream))
                      (= (codepoint \l) (.read stream))
                      (= (codepoint \l) (.read stream)))
               nil
               (throw (Exception. "JSON error (expected null)")))

          ;; Read true
          \t (if (and (= (codepoint \r) (.read stream))
                      (= (codepoint \u) (.read stream))
                      (= (codepoint \e) (.read stream)))
               true
               (throw (Exception. "JSON error (expected true)")))

          ;; Read false
          \f (if (and (= (codepoint \a) (.read stream))
                      (= (codepoint \l) (.read stream))
                      (= (codepoint \s) (.read stream))
                      (= (codepoint \e) (.read stream)))
               false
               (throw (Exception. "JSON error (expected false)")))

          ;; Read JSON objects
          \{ (read-object stream)

          ;; Read JSON arrays
          \[ (read-array stream)

          (throw (Exception.
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

        Function to transform values in the output. For each JSON
        property, value-fn is called with two arguments: the property
        name (transformed by key-fn) and the value. The return value
        of value-fn will replace the value in the output. If value-fn
        returns itself, the property will be omitted from the output.
        The default value-fn returns the value unchanged.

     :pair-fn  function

        Function to transform key/value pairs in the output.For each JSON
        property,pair-fn is called with two arguments: the property
        name (transformed by key-fn) and the value (transformed by
        value-fn).Ther return value of pair-fn must be a vector in the form
        of [key value].The return value of pair-fn will replace the key
        and value in the output.The default pair-fn returns the pair unchanged.
"
  [reader & options]
  (let [{:keys [eof-error? eof-value bigdec key-fn value-fn pair-fn]
         :or {bigdec false
              eof-error? true
              key-fn identity
              pair-fn default-pair-fn
              value-fn default-value-fn}} options]
    (binding [*bigdec* bigdec
              *key-fn* key-fn
              *value-fn* value-fn
              *pair-fn* pair-fn]
      (-read (PushbackReader. reader) eof-error? eof-value))))

(defn read-str
  "Reads one JSON value from input String. Options are the same as for
  read."
  [string & options]
  (apply read (StringReader. string) options))

;;; JSON WRITER

(def ^{:dynamic true :private true} *escape-unicode*)
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
          ;; Any other character is Unicode
          (if *escape-unicode*
            (.append sb (format "\\u%04x" cp)) ; Hexadecimal-escaped
            (.appendCodePoint sb cp)))))
    (.append sb \")
    (.print out (str sb))))

(defn- write-object [m ^PrintWriter out] 
  (.print out \{)
  (loop [x m]
    (when (seq m)
      (let [[k v] (first x)
            out-key (*key-fn* k)
            out-value (*value-fn* k v)]
        (when-not (string? out-key)
          (throw (Exception. "JSON object keys must be strings")))
        (when-not (= *value-fn* out-value)
          (write-string out-key out)
          (.print out \:)
          (-write out-value out)))
      (let [nxt (next x)]
        (when (seq nxt)
          (.print out \,)
          (recur nxt)))))
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
(extend java.lang.Number       JSONWriter {:-write write-plain})
(extend clojure.lang.Ratio     JSONWriter {:-write write-ratio})
(extend java.math.BigInteger   JSONWriter {:-write write-bignum})
(extend java.math.BigDecimal   JSONWriter {:-write write-bignum})
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

    :escape-slash boolean

       If true (default) the slash / is escaped as \\/

    :key-fn function

        Single-argument function called on map keys; return value will
        replace the property names in the output. Must return a
        string. Default calls clojure.core/name on symbols and
        keywords and clojure.core/str on everything else.

    :value-fn function

        Function to transform values before writing. For each
        key-value pair in the input, called with two arguments: the
        key (BEFORE transformation by key-fn) and the value. The
        return value of value-fn will replace the value in the output.
        If the return value is a number, boolean, string, or nil it
        will be included literally in the output. If the return value
        is a non-map collection, it will be processed recursively. If
        the return value is a map, it will be processed recursively,
        calling value-fn again on its key-value pairs. If value-fn
        returns itself, the key-value pair will be omitted from the
        output."
  [x ^Writer writer & options]
  (let [{:keys [escape-unicode escape-slash key-fn value-fn]
         :or {escape-unicode true
              escape-slash true
              key-fn default-write-key-fn
              value-fn default-value-fn}} options]
    (binding [*escape-unicode* escape-unicode
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
      (pprint/write x :dispatch pprint-dispatch))))

(load "json_compat_0_1")

;; Local Variables:
;; mode: clojure
;; eval: (define-clojure-indent (codepoint-case (quote defun)))
;; End:
