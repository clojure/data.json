;; Copyright (c) Stuart Sierra, 2012. All rights reserved. The use and
;; distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; By using this software in any fashion, you are agreeing to be bound
;; by the terms of this license. You must not remove this notice, or
;; any other, from this software.

(in-ns 'clojure.data.json)

(defn read-json
  "DEPRECATED; replaced by read-str.

  Reads one JSON value from input String or Reader. If keywordize? is
  true (default), object keys will be converted to keywords. If
  eof-error? is true (default), empty input will throw an
  EOFException; if false EOF will return eof-value."
  ([input]
     (read-json input true true nil))
  ([input keywordize?]
     (read-json input keywordize? true nil))
  ([input keywordize? eof-error? eof-value]
     (let [key-fn (if keywordize? keyword identity)]
       (condp instance? input
         String
         (read-str input
                   :key-fn key-fn
                   :eof-error? eof-error?
                   :eof-value eof-value)
         java.io.Reader
         (read input
               :key-fn key-fn
               :eof-error? eof-error?
               :eof-value eof-value)))))

(defn write-json
  "DEPRECATED; replaced by 'write'.

  Print object to PrintWriter out as JSON"
  [x out escape-unicode?]
  (write x out :escape-unicode escape-unicode?))

(defn json-str
  "DEPRECATED; replaced by 'write-str'.

  Converts x to a JSON-formatted string.

  Valid options are:
    :escape-unicode false
        to turn of \\uXXXX escapes of Unicode characters."
  [x & options]
  (apply write-str x options))

(defn print-json
  "DEPRECATED; replaced by 'write' to *out*.

  Write JSON-formatted output to *out*.

  Valid options are:
    :escape-unicode false
        to turn off \\uXXXX escapes of Unicode characters."
  [x & options]
  (apply write x *out* options))

(defn pprint-json
  "DEPRECATED; replaced by 'pprint'.

  Pretty-prints JSON representation of x to *out*.

  Valid options are:
    :escape-unicode false
        to turn off \\uXXXX escapes of Unicode characters."
  [x & options]
  (apply pprint x options))
