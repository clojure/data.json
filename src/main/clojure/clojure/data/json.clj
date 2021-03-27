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

(set! *warn-on-reflection* true)

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

(defn- codepoint-clause [[test result]]
  (cond (list? test)
        [(map int test) result]
        (= test :whitespace)
        ['(9 10 13 32) result]
        (= test :js-separators)
        ['(16r2028 16r2029) result]
        :else
        [(int test) result]))

(defmacro ^:private codepoint-case [e & clauses]
  `(case ~e
     ~@(mapcat codepoint-clause (partition 2 clauses))
     ~@(when (odd? (count clauses))
         [(last clauses)])))

(defn- read-array [^PushbackReader stream options]
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
            (let [element (-read stream true nil options)]
              (recur (conj! result element))))))))

(defn- read-object [^PushbackReader stream options]
  ;; Expects to be called with the head of the stream AFTER the
  ;; opening bracket.
  (let [key-fn (:key-fn options)
        value-fn (:value-fn options)]
    (loop [key nil, pending? false, result (transient {})]
      (let [c (.read stream)]
        (when (neg? c)
          (throw (EOFException. "JSON error (end-of-file inside object)")))
        (codepoint-case c
          :whitespace (recur key pending? result)

          \, (if pending?
               (throw (Exception. "JSON error (missing entry in object)"))
               (recur nil true result))

          \: (recur key false result)

          \} (if pending?
               (throw (Exception. "JSON error (missing entry in object)"))
               (if (nil? key)
                 (persistent! result)
                 (throw (Exception. "JSON error (key missing value in object)"))))

          (do (.unread stream c)
              (let [element (-read stream true nil options)]
                (if (nil? key)
                  (if (string? element)
                    (recur element false result)
                    (throw (Exception. "JSON error (non-string key in object)")))
                  (recur nil false
                         (let [out-key (key-fn key)
                               out-value (value-fn out-key element)]
                           (if (= value-fn out-value)
                             result
                             (assoc! result out-key out-value))))))))))))

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
    (when (neg? c)
      (throw (EOFException. "JSON error (end-of-file inside escaped char)")))
    (codepoint-case c
      (\" \\ \/) (char c)
      \b \backspace
      \f \formfeed
      \n \newline
      \r \return
      \t \tab
      \u (read-hex-char stream))))

(defn- slow-read-string [^PushbackReader stream ^String already-read]
  (let [buffer (StringBuilder. already-read)]
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

(defn- read-quoted-string [^PushbackReader stream]
  ;; Expects to be called with the head of the stream AFTER the
  ;; opening quotation mark.
  (let [buffer ^chars (char-array 64)
        read (.read stream buffer 0 64)]
    (when (neg? read)
      (throw (EOFException. "JSON error (end-of-file inside string)")))
    (loop [i (int 0)]
      (let [c (int (aget buffer i))]
        (codepoint-case c
          \" (let [off (unchecked-inc-int i)
                   len (unchecked-subtract-int read off)]
               (.unread stream buffer off len)
               (String. buffer 0 i))
          \\ (let [off i
                   len (unchecked-subtract-int read off)]
               (.unread stream buffer off len)
               (slow-read-string stream (String. buffer 0 i)))
          (if (= i 63)
            (do (.unread stream c)
                (slow-read-string stream (String. buffer 0 i)))
            (recur (unchecked-inc-int i))))))))

(defn- read-integer [^String string]
  (if (< (count string) 18)  ; definitely fits in a Long
    (Long/valueOf string)
    (or (try (Long/valueOf string)
             (catch NumberFormatException e nil))
        (bigint string))))

(defn- read-decimal [^String string bigdec?]
  (if bigdec?
    (bigdec string)
    (Double/valueOf string)))

(defn- read-number [^PushbackReader stream bigdec?]
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
      (read-decimal (str buffer) bigdec?)
      (read-integer (str buffer)))))

(defn- -read
  [^PushbackReader stream eof-error? eof-value options]
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
              (read-number stream (:bigdec options)))

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
          \{ (read-object stream options)

          ;; Read JSON arrays
          \[ (read-array stream options)

          (throw (Exception.
                  (str "JSON error (unexpected character): " (char c)))))))))

(def default-read-options {:bigdec false
                           :key-fn identity
                           :value-fn default-value-fn})
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
        collections."
  [reader & options]
  (let [{:keys [eof-error? eof-value]
         :or {eof-error? true}} options]
    (->> options
         (apply array-map)
         (merge default-read-options)
         (-read (PushbackReader. reader) eof-error? eof-value))))

(defn read-str
  "Reads one JSON value from input String. Options are the same as for
  read."
  [string & options]
  (let [{:keys [eof-error? eof-value]
         :or {eof-error? true}} options]
    (->> options
         (apply array-map)
         (merge default-read-options)
         (-read (PushbackReader. (StringReader. string) 64) eof-error? eof-value))))

;;; JSON WRITER


(defprotocol JSONWriter
  (-write [object out options]
    "Print object to Appendable out as JSON"))

(defn- ->hex-string [^Appendable out cp]
  (let [cpl (long cp)]
    (.append out "\\u")
    (cond
      (< cpl 16)
      (.append out "000")
      (< cpl 256)
      (.append out "00")
      (< cpl 4096)
      (.append out "0"))
    (.append out (Integer/toHexString cp))))

(def ^{:tag "[S"} codepoint-decoder
  (let [shorts (short-array 128)]
    (dotimes [i 128]
      (codepoint-case i
        \" (aset shorts i (short 1))
        \\ (aset shorts i (short 1))
        \/ (aset shorts i (short 2))
        \backspace (aset shorts i (short 3))
        \formfeed  (aset shorts i (short 4))
        \newline   (aset shorts i (short 5))
        \return    (aset shorts i (short 6))
        \tab       (aset shorts i (short 7))
        (if (< i 32)
          (aset shorts i (short 8))
          (aset shorts i (short 0)))))
    shorts))

(defn- write-string [^CharSequence s ^Appendable out options]
  (let [decoder codepoint-decoder]
    (.append out \")
    (dotimes [i (.length s)]
      (let [cp (int (.charAt s i))]
        (if (< cp 128)
          (case (aget decoder cp)
            0 (.append out (char cp))
            1 (do (.append out (char (codepoint \\))) (.append out (char cp)))
            2 (.append out (if (get options :escape-slash) "\\/" "/"))
            3 (.append out "\\b")
            4 (.append out "\\f")
            5 (.append out "\\n")
            6 (.append out "\\r")
            7 (.append out "\\t")
            8 (->hex-string out cp))
          (codepoint-case cp
            :js-separators (if (get options :escape-js-separators)
                             (->hex-string out cp)
                             (.append out (char cp)))
            (if (get options :escape-unicode)
              (->hex-string out cp) ; Hexadecimal-escaped
              (.append out (char cp)))))))
    (.append out \")))

(defn- write-object [m ^Appendable out options]
  (let [key-fn (:key-fn options)
        value-fn (:value-fn options)]
    (.append out \{)
    (loop [x m, have-printed-kv false]
      (when (seq x)
        (let [[k v] (first x)
              out-key (key-fn k)
              out-value (value-fn k v)
              nxt (next x)]
          (when-not (string? out-key)
            (throw (Exception. "JSON object keys must be strings")))
          (if-not (= value-fn out-value)
            (do
              (when have-printed-kv
                (.append out \,))
              (write-string out-key out options)
              (.append out \:)
              (-write out-value out options)
              (when (seq nxt)
                (recur nxt true)))
            (when (seq nxt)
              (recur nxt have-printed-kv)))))))
  (.append out \}))

(defn- write-array [s ^Appendable out options]
  (.append out \[)
  (loop [x s]
    (when (seq x)
      (let [fst (first x)
            nxt (next x)]
        (-write fst out options)
        (when (seq nxt)
          (.append out \,)
          (recur nxt)))))
  (.append out \]))

(defn- write-bignum [x ^Appendable out options]
  (.append out (str x)))

(defn- write-float [^Float x ^Appendable out options]
  (cond (.isInfinite x)
        (throw (Exception. "JSON error: cannot write infinite Float"))
        (.isNaN x)
        (throw (Exception. "JSON error: cannot write Float NaN"))
        :else
        (.append out (str x))))

(defn- write-double [^Double x ^Appendable out options]
  (cond (.isInfinite x)
        (throw (Exception. "JSON error: cannot write infinite Double"))
        (.isNaN x)
        (throw (Exception. "JSON error: cannot write Double NaN"))
        :else
        (.append out (str x))))

(defn- write-plain [x ^Appendable out options]
  (.append out (str x)))

(defn- write-null [x ^Appendable out options]
  (.append out "null"))

(defn- write-named [x out options]
  (write-string (name x) out options))

(defn- write-generic [x out options]
  (if (.isArray (class x))
    (-write (seq x) out options)
    (throw (Exception. (str "Don't know how to write JSON of " (class x))))))

(defn- write-ratio [x out options]
  (-write (double x) out options))

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

(def default-write-options {:escape-unicode true
                         :escape-js-separators true
                         :escape-slash true
                         :key-fn default-write-key-fn
                         :value-fn default-value-fn})
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
  (-write x writer (merge default-write-options (apply array-map options))))

(defn write-str
  "Converts x to a JSON-formatted string. Options are the same as
  write."
  ^String [x & options]
  (let [sw (StringWriter.)]
    (-write x sw (merge default-write-options (apply array-map options)))
    (.toString sw)))

;;; JSON PRETTY-PRINTER

;; Based on code by Tom Faulhaber

(defn- pprint-array [s] 
  ((pprint/formatter-out "~<[~;~@{~w~^, ~:_~}~;]~:>") s))

(defn- pprint-object [m options]
  (let [key-fn (:key-fn options)]
    ((pprint/formatter-out "~<{~;~@{~<~w:~_~w~:>~^, ~_~}~;}~:>")
     (for [[k v] m] [(key-fn k) v]))))

(defn- pprint-generic [x options]
  (if (.isArray (class x))
    (pprint-array (seq x))
    ;; pprint proxies Writer, so we can't just wrap it
    (print (with-out-str (-write x (PrintWriter. *out*) options)))))

(defn- pprint-dispatch [x options]
  (cond (nil? x) (print "null")
        (instance? java.util.Map x) (pprint-object x options)
        (instance? java.util.Collection x) (pprint-array x)
        (instance? clojure.lang.ISeq x) (pprint-array x)
        :else (pprint-generic x options)))

(defn pprint
  "Pretty-prints JSON representation of x to *out*. Options are the
  same as for write except :value-fn, which is not supported."
  [x & options]
  (let [opts (merge default-write-options (apply array-map options))]
    (pprint/with-pprint-dispatch #(pprint-dispatch % opts)
      (pprint/pprint x))))

(load "json_compat_0_1")

;; Local Variables:
;; mode: clojure
;; eval: (define-clojure-indent (codepoint-case (quote defun)))
;; End:
