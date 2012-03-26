{:namespaces
 ({:source-url
   "https://github.com/clojure/data.json/blob/f0dd129f31b1f7cd26c3d9483696cade74d4e832/src/main/clojure/clojure/data/json.clj",
   :wiki-url
   "http://clojure.github.com/data.json/clojure.data.json-api.html",
   :name "clojure.data.json",
   :author "Stuart Sierra",
   :doc
   "JavaScript Object Notation (JSON) parser/writer.\nSee http://www.json.org/\nTo write JSON, use json-str, write-json, or print-json.\nTo read JSON, use read-json."}),
 :vars
 ({:arglists ([x & options]),
   :name "json-str",
   :namespace "clojure.data.json",
   :source-url
   "https://github.com/clojure/data.json/blob/f0dd129f31b1f7cd26c3d9483696cade74d4e832/src/main/clojure/clojure/data/json.clj#L306",
   :raw-source-url
   "https://github.com/clojure/data.json/raw/f0dd129f31b1f7cd26c3d9483696cade74d4e832/src/main/clojure/clojure/data/json.clj",
   :wiki-url
   "http://clojure.github.com/data.json//clojure.data.json-api.html#clojure.data.json/json-str",
   :doc
   "Converts x to a JSON-formatted string.\n\nValid options are:\n  :escape-unicode false\n      to turn of \\uXXXX escapes of Unicode characters.",
   :var-type "function",
   :line 306,
   :file "src/main/clojure/clojure/data/json.clj"}
  {:arglists ([x & options]),
   :name "pprint-json",
   :namespace "clojure.data.json",
   :source-url
   "https://github.com/clojure/data.json/blob/f0dd129f31b1f7cd26c3d9483696cade74d4e832/src/main/clojure/clojure/data/json.clj#L353",
   :raw-source-url
   "https://github.com/clojure/data.json/raw/f0dd129f31b1f7cd26c3d9483696cade74d4e832/src/main/clojure/clojure/data/json.clj",
   :wiki-url
   "http://clojure.github.com/data.json//clojure.data.json-api.html#clojure.data.json/pprint-json",
   :doc
   "Pretty-prints JSON representation of x to *out*.\n\nValid options are:\n  :escape-unicode false\n      to turn off \\uXXXX escapes of Unicode characters.",
   :var-type "function",
   :line 353,
   :file "src/main/clojure/clojure/data/json.clj"}
  {:arglists ([x & options]),
   :name "print-json",
   :namespace "clojure.data.json",
   :source-url
   "https://github.com/clojure/data.json/blob/f0dd129f31b1f7cd26c3d9483696cade74d4e832/src/main/clojure/clojure/data/json.clj#L319",
   :raw-source-url
   "https://github.com/clojure/data.json/raw/f0dd129f31b1f7cd26c3d9483696cade74d4e832/src/main/clojure/clojure/data/json.clj",
   :wiki-url
   "http://clojure.github.com/data.json//clojure.data.json-api.html#clojure.data.json/print-json",
   :doc
   "Write JSON-formatted output to *out*.\n\nValid options are:\n  :escape-unicode false\n      to turn off \\uXXXX escapes of Unicode characters.",
   :var-type "function",
   :line 319,
   :file "src/main/clojure/clojure/data/json.clj"}
  {:arglists
   ([input]
    [input keywordize?]
    [input keywordize? eof-error? eof-value]),
   :name "read-json",
   :namespace "clojure.data.json",
   :source-url
   "https://github.com/clojure/data.json/blob/f0dd129f31b1f7cd26c3d9483696cade74d4e832/src/main/clojure/clojure/data/json.clj#L182",
   :raw-source-url
   "https://github.com/clojure/data.json/raw/f0dd129f31b1f7cd26c3d9483696cade74d4e832/src/main/clojure/clojure/data/json.clj",
   :wiki-url
   "http://clojure.github.com/data.json//clojure.data.json-api.html#clojure.data.json/read-json",
   :doc
   "Reads one JSON value from input String or Reader.\nIf keywordize? is true (default), object keys will be converted to\nkeywords.  If eof-error? is true (default), empty input will throw\nan EOFException; if false EOF will return eof-value. ",
   :var-type "function",
   :line 182,
   :file "src/main/clojure/clojure/data/json.clj"}
  {:file "src/main/clojure/clojure/data/json.clj",
   :raw-source-url
   "https://github.com/clojure/data.json/raw/f0dd129f31b1f7cd26c3d9483696cade74d4e832/src/main/clojure/clojure/data/json.clj",
   :source-url
   "https://github.com/clojure/data.json/blob/f0dd129f31b1f7cd26c3d9483696cade74d4e832/src/main/clojure/clojure/data/json.clj#L160",
   :wiki-url
   "http://clojure.github.com/data.json//clojure.data.json-api.html#clojure.data.json/Read-JSON-From",
   :namespace "clojure.data.json",
   :line 160,
   :var-type "protocol",
   :doc nil,
   :name "Read-JSON-From"}
  {:file "src/main/clojure/clojure/data/json.clj",
   :raw-source-url
   "https://github.com/clojure/data.json/raw/f0dd129f31b1f7cd26c3d9483696cade74d4e832/src/main/clojure/clojure/data/json.clj",
   :source-url
   "https://github.com/clojure/data.json/blob/f0dd129f31b1f7cd26c3d9483696cade74d4e832/src/main/clojure/clojure/data/json.clj#L197",
   :wiki-url
   "http://clojure.github.com/data.json//clojure.data.json-api.html#clojure.data.json/Write-JSON",
   :namespace "clojure.data.json",
   :line 197,
   :var-type "protocol",
   :doc nil,
   :name "Write-JSON"}
  {:file nil,
   :raw-source-url nil,
   :source-url nil,
   :wiki-url
   "http://clojure.github.com/data.json//clojure.data.json-api.html#clojure.data.json/read-json-from",
   :namespace "clojure.data.json",
   :var-type "function",
   :arglists ([input keywordize? eof-error? eof-value]),
   :doc
   "Reads one JSON value from input String or Reader.\nIf keywordize? is true, object keys will be converted to keywords.\nIf eof-error? is true, empty input will throw an EOFException; if\nfalse EOF will return eof-value. ",
   :name "read-json-from"}
  {:file nil,
   :raw-source-url nil,
   :source-url nil,
   :wiki-url
   "http://clojure.github.com/data.json//clojure.data.json-api.html#clojure.data.json/write-json",
   :namespace "clojure.data.json",
   :var-type "function",
   :arglists ([object out escape-unicode?]),
   :doc "Print object to PrintWriter out as JSON",
   :name "write-json"})}
