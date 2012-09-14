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
  clojure.data.json-new
  (:require [clojure.pprint :as pprint])
  (:import (java.io PrintWriter PushbackReader StringWriter
                    Writer StringReader EOFException)))

;;; JSON READER

(def ^:dynamic ^:private *bigdec*)
(def ^:dynamic ^:private *key-fn*)
(def ^:dynamic ^:private *value-fn*)

(declare -parse)

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

(defn- parse-array [^PushbackReader stream]
  ;; Expects to be called with the head of the stream AFTER the
  ;; opening bracket.
  (loop [c (.read stream), result (transient [])]
    (when (neg? c)
      (throw (EOFException. "JSON error (end-of-file inside array)")))
    (codepoint-case c
      :whitespace (recur (.read stream) result)
      \, (recur (.read stream) result)
      \] (persistent! result)
      (do (.unread stream c)
          (let [element (-parse stream true nil)]
            (recur (.read stream) (conj! result element)))))))

(defn- parse-object [^PushbackReader stream]
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
            (let [element (-parse stream true nil)]
              (if (nil? key)
                (if (string? element)
                  (recur element result)
                  (throw (Exception. "JSON error (non-string key in object)")))
                (recur nil
                       (let [out-key (*key-fn* key)
                             out-value (*value-fn* out-key element)]
                         (if (= *value-fn* out-value)
                           result
                           (assoc! result out-key out-value)))))))))))

(defn- parse-hex-char [^PushbackReader stream]
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

(defn- parse-escaped-char [^PushbackReader stream]
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
      \u (parse-hex-char stream))))

(defn- parse-quoted-string [^PushbackReader stream]
  ;; Expects to be called with the head of the stream AFTER the
  ;; opening quotation mark.
  (let [buffer (StringBuilder.)]
    (loop []
      (let [c (.read stream)]
        (when (neg? c)
          (throw (EOFException. "JSON error (end-of-file inside array)")))
        (codepoint-case c
          \" (str buffer)
          \\ (do (.append buffer (parse-escaped-char stream))
                 (recur))
          (do (.append buffer (char c))
              (recur)))))))

(defn- parse-integer [^String string]
  (if (< (count string) 18)  ; definitely fits in a Long
    (Long/valueOf string)
    (or (try (Long/valueOf string)
             (catch NumberFormatException e nil))
        (bigint string))))

(defn- parse-decimal [^String string]
  (if *bigdec*
    (bigdec string)
    (Double/valueOf string)))

(defn- parse-number [^PushbackReader stream]
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
      (parse-decimal (str buffer))
      (parse-integer (str buffer)))))

(defn- -parse
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
              (parse-number stream))

          ;; Read strings
          \" (parse-quoted-string stream)

          ;; Read null as nil
          \n (if (and (= (codepoint \u) (.read stream))
                      (= (codepoint \l) (.read stream))
                      (= (codepoint \l) (.read stream)))
               nil
               (throw (Exception. (str "JSON error (expected null)"))))

          ;; Read true
          \t (if (and (= (codepoint \r) (.read stream))
                      (= (codepoint \u) (.read stream))
                      (= (codepoint \e) (.read stream)))
               true
               (throw (Exception. (str "JSON error (expected true)"))))

          ;; Read false
          \f (if (and (= (codepoint \a) (.read stream))
                      (= (codepoint \l) (.read stream))
                      (= (codepoint \s) (.read stream))
                      (= (codepoint \e) (.read stream)))
               false
               (throw (Exception. (str "JSON error (expected false)"))))

          ;; Read JSON objects
          \{ (parse-object stream)

          ;; Read JSON arrays
          \[ (parse-array stream)

          (throw (Exception.
                  (str "JSON error (unexpected character): " (char c)))))))))

(defn parse
  "Parse a single item of JSON data from a java.io.Reader. Options are
  key-value pairs, valid options are:

     :bigdec boolean

        If true use BigDecimal for decimal numbers instead of Double.
        Default is false.

     :eof-error? boolean

        If true (default) will throw exception if the stream is empty.

     :eof-value Object

        Object to return if the stream is empty and eof-error? is
        false. Default is nil.

     :key-fn function

        Single-argument function called on JSON property names; return
        value will replace the property names in the output. Default
        is clojure.core/keyword.

     :value-fn function

        Function to transform values in the output. For each JSON
        property, value-fn is called with two arguments: the property
        name (transformed by key-fn) and the value. The return value
        of value-fn will replace the value in the output. If value-fn
        returns itself, the property will be omitted from the output.

        Finally, value-fn is called with one argument, the whole
        object, and may return any any substitute value.

        The default value-fn is (fn ([v] v) ([k v] v))."
  [reader & options]
  (let [{:keys [eof-error? eof-value bigdec key-fn value-fn]
         :or {bigdec false
              eof-error? true
              key-fn keyword
              value-fn (fn ([v] v) ([k v] v))}} options]
    (binding [*bigdec* bigdec
              *key-fn* key-fn
              *value-fn* value-fn]
      (let [out-value 
            (*value-fn* (-parse reader eof-error? eof-value))]
        (when-not (= *value-fn* out-value)
          out-value)))))

(defn parse-string
  "Reads one JSON value from input String. Options are the same as for
  parse."
  [string & options]
  (apply parse (PushbackReader. (StringReader. string)) options))

;;; JSON WRITER

(def ^:dynamic ^:private *escape-unicode*)
(def ^:dynamic ^:private *escape-slash*)

(defprotocol JSONWriter
  (-write-json [object out]
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

(defn- as-str
  [x]
  (if (instance? clojure.lang.Named x)
    (name x)
    (str x)))

(defn- write-object [m ^PrintWriter out] 
  (.print out \{)
  (loop [x m]
    (when (seq m)
      (let [[k v] (first x)]
        (when (nil? k)
          (throw (Exception. "JSON object keys cannot be nil/null")))
	(write-string (as-str k) out)
        (.print out \:)
        (-write-json v out))
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
        (-write-json fst out)
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
    (-write-json (seq x) out)
    (throw (Exception. (str "Don't know how to write JSON of " (class x))))))

(defn- write-ratio [x out]
  (-write-json (double x) out))

;; nil, true, false
(extend nil                    JSONWriter {:-write-json write-null})
(extend java.lang.Boolean      JSONWriter {:-write-json write-plain})

;; Numbers
(extend java.lang.Number       JSONWriter {:-write-json write-plain})
(extend clojure.lang.Ratio     JSONWriter {:-write-json write-ratio})
(extend clojure.lang.BigInt    JSONWriter {:-write-json write-bignum})
(extend java.math.BigInteger   JSONWriter {:-write-json write-bignum})
(extend java.math.BigDecimal   JSONWriter {:-write-json write-bignum})

;; Symbols, Keywords, and Strings
(extend clojure.lang.Named     JSONWriter {:-write-json write-named})
(extend java.lang.CharSequence JSONWriter {:-write-json write-string})

;; Collections
(extend java.util.Map          JSONWriter {:-write-json write-object})
(extend java.util.Collection   JSONWriter {:-write-json write-array})

;; Maybe a Java array, otherwise fail
(extend java.lang.Object       JSONWriter {:-write-json write-generic})

(defn write-json
  "Write JSON-formatted output to a java.io.Writer.
   Options are key-value pairs, valid options are:

    :escape-unicode boolean

       If true (default) non-ASCII characters are escaped as \\uXXXX

    :escape-slash boolean

       If true (default) the slash / is escaped as \\/"
  [x ^Writer writer & options]
  (let [{:keys [escape-unicode escape-slash]
         :or {escape-unicode true
              escape-slash true}} options]
    (binding [*escape-unicode* escape-unicode
              *escape-slash* escape-slash]
      (-write-json x (PrintWriter. writer)))))

(defn json-str
  "Converts x to a JSON-formatted string. Options are the same as
  write-json."
  [x & options]
  (let [sw (StringWriter.)]
    (apply write-json x sw options)
    (.toString sw)))

;;; JSON PRETTY-PRINTER

;; Based on code by Tom Faulhaber

(defn- pprint-json-array [s escape-unicode] 
  ((pprint/formatter-out "~<[~;~@{~w~^, ~:_~}~;]~:>") s))

(defn- pprint-json-object [m escape-unicode]
  ((pprint/formatter-out "~<{~;~@{~<~w:~_~w~:>~^, ~_~}~;}~:>") 
   (for [[k v] m] [(as-str k) v])))

(defn- pprint-json-generic [x escape-unicode]
  (if (.isArray (class x))
    (pprint-json-array (seq x) escape-unicode)
    (print (json-str x :escape-unicode escape-unicode))))

(defn- pprint-json-dispatch [x escape-unicode]
  (cond (nil? x) (print "null")
        (instance? java.util.Map x) (pprint-json-object x escape-unicode)
        (instance? java.util.Collection x) (pprint-json-array x escape-unicode)
        (instance? clojure.lang.ISeq x) (pprint-json-array x escape-unicode)
        :else (pprint-json-generic x escape-unicode)))

(defn pprint-json
  "Pretty-prints JSON representation of x to *out*.

  Valid options are:
    :escape-unicode false
        to turn off \\uXXXX escapes of Unicode characters."
  [x & options]
  (let [{:keys [escape-unicode] :or {escape-unicode true}} options]
    (pprint/write x :dispatch #(pprint-json-dispatch % escape-unicode))))

;; Local Variables:
;; mode: clojure
;; eval: (define-clojure-indent (codepoint-case (quote defun)))
;; End:
