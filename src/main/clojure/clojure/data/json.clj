;;; json.clj: JavaScript Object Notation (JSON) parser/writer

;; by Stuart Sierra, http://stuartsierra.com/
;; January 30, 2010

;; Copyright (c) Stuart Sierra, 2010. All rights reserved.  The use
;; and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this
;; distribution.  By using this software in any fashion, you are
;; agreeing to be bound by the terms of this license.  You must not
;; remove this notice, or any other, from this software.

(ns ^{:author "Stuart Sierra"
       :doc "JavaScript Object Notation (JSON) parser/writer.
  See http://www.json.org/
  To write JSON, use json-str, write-json, or print-json.
  To read JSON, use read-json."}
    clojure.data.json
  (:use [clojure.pprint :only (write formatter-out)])
  (:import (java.io PrintWriter PushbackReader StringWriter
                    StringReader Reader EOFException)))

;;; JSON READER

(defmacro ^:private codepoint [c]
  (int c))

(declare read-json-reader)

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

(defn- read-json-array [^PushbackReader stream keywordize?]
  ;; Expects to be called with the head of the stream AFTER the
  ;; opening bracket.
  (loop [c (.read stream), result (transient [])]
    (when (neg? c) (throw (EOFException. "JSON error (end-of-file inside array)")))
    (codepoint-case c
      :whitespace (recur (.read stream) result)
      \, (recur (.read stream) result)
      \] (persistent! result)
      (do (.unread stream c)
          (let [element (read-json-reader stream keywordize? true nil)]
            (recur (.read stream) (conj! result element)))))))

(defn- read-json-object [^PushbackReader stream keywordize?]
  ;; Expects to be called with the head of the stream AFTER the
  ;; opening bracket.
  (loop [c (.read stream), key nil, result (transient {})]
    (when (neg? c) (throw (EOFException. "JSON error (end-of-file inside array)")))
    (codepoint-case c
     :whitespace (recur (.read stream) key result)

     \, (recur (.read stream) nil result)

     \: (recur (.read stream) key result)

     \} (if (nil? key)
           (persistent! result)
           (throw (Exception. "JSON error (key missing value in object)")))

     (do (.unread stream c)
         (let [element (read-json-reader stream keywordize? true nil)]
           (if (nil? key)
             (if (string? element)
               (recur (.read stream) element result)
               (throw (Exception. "JSON error (non-string key in object)")))
             (recur (.read stream) nil
                    (assoc! result (if keywordize? (keyword key) key)
                            element))))))))

(defn- read-json-hex-character [^PushbackReader stream]
  ;; Expects to be called with the head of the stream AFTER the
  ;; initial "\u".  Reads the next four characters from the stream.
  (let [a (.read stream)
        b (.read stream)
        c (.read stream)
        d (.read stream)]
    (when (or (neg? a) (neg? b) (neg? c) (neg? d))
      (throw (EOFException. "JSON error (end-of-file inside Unicode character escape)")))
    (let [s (str (char a) (char b) (char c) (char d))]
      (char (Integer/parseInt s 16)))))

(defn- read-json-escaped-character [^PushbackReader stream]
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
      \u (read-json-hex-character stream))))

(defn- read-json-quoted-string [^PushbackReader stream]
  ;; Expects to be called with the head of the stream AFTER the
  ;; opening quotation mark.
  (let [buffer (StringBuilder.)]
    (loop [c (.read stream)]
      (when (neg? c) (throw (EOFException. "JSON error (end-of-file inside array)")))
      (codepoint-case c
          \" (str buffer)
          \\ (do (.append buffer (read-json-escaped-character stream))
                 (recur (.read stream)))
          (do (.append buffer (char c))
              (recur (.read stream)))))))

(defn- read-json-reader
  ([^PushbackReader stream keywordize? eof-error? eof-value]
     (loop [c (.read stream)]
       (if (neg? c) ;; Handle end-of-stream
	 (if eof-error?
	   (throw (EOFException. "JSON error (end-of-file)"))
	   eof-value)
	 (codepoint-case c
           :whitespace (recur (.read stream))

           ;; Read numbers with Clojure reader
           (\- \0 \1 \2 \3 \4 \5 \6 \7 \8 \9)
           (do (.unread stream c)
               (read stream true nil))

           ;; Read strings
           \" (read-json-quoted-string stream)

           ;; Read null as nil
           \n (let [ull [(.read stream)
                         (.read stream)
                         (.read stream)]]
                (if (= ull [(codepoint \u) (codepoint \l) (codepoint \l)])
                  nil
                  (throw (Exception. (str "JSON error (expected null): " c ull)))))

           ;; Read true
           \t (let [rue [(.read stream)
                         (.read stream)
                         (.read stream)]]
                (if (= rue [(codepoint \r) (codepoint \u) (codepoint \e)])
                  true
                  (throw (Exception. (str "JSON error (expected true): " c rue)))))

           ;; Read false
           \f (let [alse [(.read stream)
                          (.read stream)
                          (.read stream)
                          (.read stream)]]
                (if (= alse [(codepoint \a) (codepoint \l) (codepoint \s) (codepoint \e)])
                  false
                  (throw (Exception. (str "JSON error (expected false): " c alse)))))

           ;; Read JSON objects
           \{ (read-json-object stream keywordize?)

           ;; Read JSON arrays
           \[ (read-json-array stream keywordize?)

           (throw (Exception. (str "JSON error (unexpected character): " (char c)))))))))

(defprotocol Read-JSON-From
  (read-json-from [input keywordize? eof-error? eof-value]
                  "Reads one JSON value from input String or Reader.
  If keywordize? is true, object keys will be converted to keywords.
  If eof-error? is true, empty input will throw an EOFException; if
  false EOF will return eof-value. "))

(extend-protocol
 Read-JSON-From
 String
 (read-json-from [input keywordize? eof-error? eof-value]
                 (read-json-reader (PushbackReader. (StringReader. input))
                                   keywordize? eof-error? eof-value))
 PushbackReader
 (read-json-from [input keywordize? eof-error? eof-value]
                 (read-json-reader input
                                   keywordize? eof-error? eof-value))
 Reader
 (read-json-from [input keywordize? eof-error? eof-value]
                 (read-json-reader (PushbackReader. input)
                                   keywordize? eof-error? eof-value)))

(defn read-json
  "Reads one JSON value from input String or Reader.
  If keywordize? is true (default), object keys will be converted to
  keywords.  If eof-error? is true (default), empty input will throw
  an EOFException; if false EOF will return eof-value. "
  ([input]
     (read-json-from input true true nil))
  ([input keywordize?]
     (read-json-from input keywordize? true nil))
  ([input keywordize? eof-error? eof-value]
     (read-json-from input keywordize? eof-error? eof-value)))


;;; JSON PRINTER

(defprotocol Write-JSON
  (write-json [object out escape-unicode?]
              "Print object to PrintWriter out as JSON"))

(defn- write-json-string [^CharSequence s ^PrintWriter out escape-unicode?]
  (let [sb (StringBuilder. ^Integer (count s))]
    (.append sb \")
    (dotimes [i (count s)]
      (let [cp (int (.charAt s i))]
        (codepoint-case cp
          ;; Handle printable JSON escapes before ASCII
          \" (.append sb "\\\"")
          \\ (.append sb "\\\\")
          \/ (.append sb "\\/")
          ;; Print simple ASCII characters
          :simple-ascii (.append sb (.charAt s i))
          ;; Handle JSON escapes
          \backspace (.append sb "\\b")
          \formfeed  (.append sb "\\f")
          \newline   (.append sb "\\n")
          \return    (.append sb "\\r")
          \tab       (.append sb "\\t")
          ;; Any other character is Unicode
          (if escape-unicode?
            ;; Hexadecimal-escaped
            (.append sb (format "\\u%04x" cp))
            (.appendCodePoint sb cp)))))
    (.append sb \")
    (.print out (str sb))))

(defn- as-str
  [x]
  (if (instance? clojure.lang.Named x)
    (name x)
    (str x)))

(defn- write-json-object [m ^PrintWriter out escape-unicode?] 
  (.print out \{)
  (loop [x m]
    (when (seq m)
      (let [[k v] (first x)]
        (when (nil? k)
          (throw (Exception. "JSON object keys cannot be nil/null")))
	(write-json-string (as-str k) out escape-unicode?)
        (.print out \:)
        (write-json v out escape-unicode?))
      (let [nxt (next x)]
        (when (seq nxt)
          (.print out \,)
          (recur nxt)))))
  (.print out \}))

(defn- write-json-array [s ^PrintWriter out escape-unicode?]
  (.print out \[)
  (loop [x s]
    (when (seq x)
      (let [fst (first x)
            nxt (next x)]
        (write-json fst out escape-unicode?)
        (when (seq nxt)
          (.print out \,)
          (recur nxt)))))
  (.print out \]))

(defn- write-json-bignum [x ^PrintWriter out escape-unicode]
  (.print out (str x)))

(defn- write-json-plain [x ^PrintWriter out escape-unicode?]
  (.print out x))

(defn- write-json-null [x ^PrintWriter out escape-unicode?]
  (.print out "null"))

(defn- write-json-named [x ^PrintWriter out escape-unicode?]
  (write-json-string (name x) out escape-unicode?))

(defn- write-json-generic [x out escape-unicode?]
  (if (.isArray (class x))
    (write-json (seq x) out escape-unicode?)
    (throw (Exception. (str "Don't know how to write JSON of " (class x))))))

(defn- write-json-ratio [x out escape-unicode?]
  (write-json (double x) out escape-unicode?))

(extend nil Write-JSON
        {:write-json write-json-null})
(extend clojure.lang.Named Write-JSON
        {:write-json write-json-named})
(extend java.lang.Boolean Write-JSON
        {:write-json write-json-plain})
(extend java.lang.Number Write-JSON
        {:write-json write-json-plain})
(extend java.math.BigInteger Write-JSON
        {:write-json write-json-bignum})
(extend java.math.BigDecimal Write-JSON
        {:write-json write-json-bignum})
(extend clojure.lang.Ratio Write-JSON
        {:write-json write-json-ratio})
(extend java.lang.CharSequence Write-JSON
        {:write-json write-json-string})
(extend java.util.Map Write-JSON
        {:write-json write-json-object})
(extend java.util.Collection Write-JSON
        {:write-json write-json-array})
(extend clojure.lang.ISeq Write-JSON
        {:write-json write-json-array})
(extend java.lang.Object Write-JSON
        {:write-json write-json-generic})

(defn json-str
  "Converts x to a JSON-formatted string.

  Valid options are:
    :escape-unicode false
        to turn of \\uXXXX escapes of Unicode characters."
  [x & options]
  (let [{:keys [escape-unicode] :or {escape-unicode true}} options
	sw (StringWriter.)
        out (PrintWriter. sw)]
    (write-json x out escape-unicode)
    (.toString sw)))

(defn print-json
  "Write JSON-formatted output to *out*.

  Valid options are:
    :escape-unicode false
        to turn off \\uXXXX escapes of Unicode characters."
  [x & options]
  (let [{:keys [escape-unicode] :or {escape-unicode true}} options]
    (write-json x (PrintWriter. *out*) escape-unicode)))


;;; JSON PRETTY-PRINTER

;; Based on code by Tom Faulhaber

(defn- pprint-json-array [s escape-unicode] 
  ((formatter-out "~<[~;~@{~w~^, ~:_~}~;]~:>") s))

(defn- pprint-json-object [m escape-unicode]
  ((formatter-out "~<{~;~@{~<~w:~_~w~:>~^, ~_~}~;}~:>") 
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
    (write x :dispatch #(pprint-json-dispatch % escape-unicode))))
