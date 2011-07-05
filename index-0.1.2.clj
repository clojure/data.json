{:namespaces
 ({:source-url
   "https://github.com/clojure/data.json/blob/ef6a05f6599862a8c5c7b0d00fe4ce418fc638be/src/main/clojure/clojure/data/json.clj",
   :wiki-url "http://clojure.github.com/data.json/json-api.html",
   :name "clojure.data.json",
   :author "Stuart Sierra",
   :doc
   "JavaScript Object Notation (JSON) parser/writer.\nSee http://www.json.org/\nTo write JSON, use json-str, write-json, or write-json.\nTo read JSON, use read-json."}),
 :vars
 ({:arglists ([x & options]),
   :name "json-str",
   :namespace "clojure.data.json",
   :source-url
   "https://github.com/clojure/data.json/blob/ef6a05f6599862a8c5c7b0d00fe4ce418fc638be/src/main/clojure/clojure/data/json.clj#L310",
   :raw-source-url
   "https://github.com/clojure/data.json/raw/ef6a05f6599862a8c5c7b0d00fe4ce418fc638be/src/main/clojure/clojure/data/json.clj",
   :wiki-url
   "http://clojure.github.com/data.json//json-api.html#clojure.data.json/json-str",
   :doc
   "Converts x to a JSON-formatted string.\n\nValid options are:\n  :escape-unicode false\n      to turn of \\uXXXX escapes of Unicode characters.",
   :var-type "function",
   :line 310,
   :file
   "/home/tom/src/clj/autodoc/../autodoc-work-area/data.json/src/src/main/clojure/clojure/data/json.clj"}
  {:arglists ([x & options]),
   :name "pprint-json",
   :namespace "clojure.data.json",
   :source-url
   "https://github.com/clojure/data.json/blob/ef6a05f6599862a8c5c7b0d00fe4ce418fc638be/src/main/clojure/clojure/data/json.clj#L357",
   :raw-source-url
   "https://github.com/clojure/data.json/raw/ef6a05f6599862a8c5c7b0d00fe4ce418fc638be/src/main/clojure/clojure/data/json.clj",
   :wiki-url
   "http://clojure.github.com/data.json//json-api.html#clojure.data.json/pprint-json",
   :doc
   "Pretty-prints JSON representation of x to *out*.\n\nValid options are:\n  :escape-unicode false\n      to turn off \\uXXXX escapes of Unicode characters.",
   :var-type "function",
   :line 357,
   :file
   "/home/tom/src/clj/autodoc/../autodoc-work-area/data.json/src/src/main/clojure/clojure/data/json.clj"}
  {:arglists ([x & options]),
   :name "print-json",
   :namespace "clojure.data.json",
   :source-url
   "https://github.com/clojure/data.json/blob/ef6a05f6599862a8c5c7b0d00fe4ce418fc638be/src/main/clojure/clojure/data/json.clj#L323",
   :raw-source-url
   "https://github.com/clojure/data.json/raw/ef6a05f6599862a8c5c7b0d00fe4ce418fc638be/src/main/clojure/clojure/data/json.clj",
   :wiki-url
   "http://clojure.github.com/data.json//json-api.html#clojure.data.json/print-json",
   :doc
   "Write JSON-formatted output to *out*.\n\nValid options are:\n  :escape-unicode false\n      to turn off \\uXXXX escapes of Unicode characters.",
   :var-type "function",
   :line 323,
   :file
   "/home/tom/src/clj/autodoc/../autodoc-work-area/data.json/src/src/main/clojure/clojure/data/json.clj"}
  {:arglists
   ([input]
    [input keywordize?]
    [input keywordize? eof-error? eof-value]),
   :name "read-json",
   :namespace "clojure.data.json",
   :source-url
   "https://github.com/clojure/data.json/blob/ef6a05f6599862a8c5c7b0d00fe4ce418fc638be/src/main/clojure/clojure/data/json.clj#L186",
   :raw-source-url
   "https://github.com/clojure/data.json/raw/ef6a05f6599862a8c5c7b0d00fe4ce418fc638be/src/main/clojure/clojure/data/json.clj",
   :wiki-url
   "http://clojure.github.com/data.json//json-api.html#clojure.data.json/read-json",
   :doc
   "Reads one JSON value from input String or Reader.\nIf keywordize? is true (default), object keys will be converted to\nkeywords.  If eof-error? is true (default), empty input will throw\nan EOFException; if false EOF will return eof-value. ",
   :var-type "function",
   :line 186,
   :file
   "/home/tom/src/clj/autodoc/../autodoc-work-area/data.json/src/src/main/clojure/clojure/data/json.clj"}
  {:raw-source-url nil,
   :source-url nil,
   :wiki-url
   "http://clojure.github.com/data.json//json-api.html#clojure.data.json/read-json-from",
   :namespace "clojure.data.json",
   :var-type "function",
   :arglists ([input keywordize? eof-error? eof-value]),
   :doc
   "Reads one JSON value from input String or Reader.\nIf keywordize? is true, object keys will be converted to keywords.\nIf eof-error? is true, empty input will throw an EOFException; if\nfalse EOF will return eof-value. ",
   :name "read-json-from"}
  {:raw-source-url nil,
   :source-url nil,
   :wiki-url
   "http://clojure.github.com/data.json//json-api.html#clojure.data.json/write-json",
   :namespace "clojure.data.json",
   :var-type "function",
   :arglists ([object out escape-unicode?]),
   :doc "Print object to PrintWriter out as JSON",
   :name "write-json"})}
