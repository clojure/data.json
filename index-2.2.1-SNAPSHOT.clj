{:namespaces
 ({:doc
   "JavaScript Object Notation (JSON) parser/generator.\nSee http://www.json.org/",
   :author "Stuart Sierra",
   :name "clojure.data.json",
   :wiki-url "https://clojure.github.io/data.json/index.html",
   :source-url
   "https://github.com/clojure/data.json/blob/66f946edc83f59034f0c139cffe737367216c382/src/main/clojure/clojure/data/json.clj"}),
 :vars
 ({:raw-source-url
   "https://github.com/clojure/data.json/raw/d3cce5b200031cf22603c13a9a39e3939651d344/src/main/clojure/clojure/data/json_compat_0_1.clj",
   :name "json-str",
   :file "src/main/clojure/clojure/data/json_compat_0_1.clj",
   :source-url
   "https://github.com/clojure/data.json/blob/d3cce5b200031cf22603c13a9a39e3939651d344/src/main/clojure/clojure/data/json_compat_0_1.clj#L42",
   :line 42,
   :var-type "function",
   :arglists ([x & options]),
   :doc
   "DEPRECATED; replaced by 'write-str'.\n\nConverts x to a JSON-formatted string.\n\nValid options are:\n  :escape-unicode false\n      to turn of \\uXXXX escapes of Unicode characters.",
   :namespace "clojure.data.json",
   :wiki-url
   "https://clojure.github.io/data.json//index.html#clojure.data.json/json-str"}
  {:raw-source-url
   "https://github.com/clojure/data.json/raw/66f946edc83f59034f0c139cffe737367216c382/src/main/clojure/clojure/data/json.clj",
   :name "pprint",
   :file "src/main/clojure/clojure/data/json.clj",
   :source-url
   "https://github.com/clojure/data.json/blob/66f946edc83f59034f0c139cffe737367216c382/src/main/clojure/clojure/data/json.clj#L597",
   :line 597,
   :var-type "function",
   :arglists ([x & {:as options}]),
   :doc
   "Pretty-prints JSON representation of x to *out*. Options are the\nsame as for write except :value-fn, which is not supported.",
   :namespace "clojure.data.json",
   :wiki-url
   "https://clojure.github.io/data.json//index.html#clojure.data.json/pprint"}
  {:raw-source-url
   "https://github.com/clojure/data.json/raw/d3cce5b200031cf22603c13a9a39e3939651d344/src/main/clojure/clojure/data/json_compat_0_1.clj",
   :name "pprint-json",
   :file "src/main/clojure/clojure/data/json_compat_0_1.clj",
   :source-url
   "https://github.com/clojure/data.json/blob/d3cce5b200031cf22603c13a9a39e3939651d344/src/main/clojure/clojure/data/json_compat_0_1.clj#L64",
   :line 64,
   :var-type "function",
   :arglists ([x & options]),
   :doc
   "DEPRECATED; replaced by 'pprint'.\n\nPretty-prints JSON representation of x to *out*.\n\nValid options are:\n  :escape-unicode false\n      to turn off \\uXXXX escapes of Unicode characters.",
   :namespace "clojure.data.json",
   :wiki-url
   "https://clojure.github.io/data.json//index.html#clojure.data.json/pprint-json"}
  {:raw-source-url
   "https://github.com/clojure/data.json/raw/d3cce5b200031cf22603c13a9a39e3939651d344/src/main/clojure/clojure/data/json_compat_0_1.clj",
   :name "print-json",
   :file "src/main/clojure/clojure/data/json_compat_0_1.clj",
   :source-url
   "https://github.com/clojure/data.json/blob/d3cce5b200031cf22603c13a9a39e3939651d344/src/main/clojure/clojure/data/json_compat_0_1.clj#L53",
   :line 53,
   :var-type "function",
   :arglists ([x & options]),
   :doc
   "DEPRECATED; replaced by 'write' to *out*.\n\nWrite JSON-formatted output to *out*.\n\nValid options are:\n  :escape-unicode false\n      to turn off \\uXXXX escapes of Unicode characters.",
   :namespace "clojure.data.json",
   :wiki-url
   "https://clojure.github.io/data.json//index.html#clojure.data.json/print-json"}
  {:raw-source-url
   "https://github.com/clojure/data.json/raw/66f946edc83f59034f0c139cffe737367216c382/src/main/clojure/clojure/data/json.clj",
   :name "read",
   :file "src/main/clojure/clojure/data/json.clj",
   :source-url
   "https://github.com/clojure/data.json/blob/66f946edc83f59034f0c139cffe737367216c382/src/main/clojure/clojure/data/json.clj#L255",
   :line 255,
   :var-type "function",
   :arglists ([reader & {:as options}]),
   :doc
   "Reads a single item of JSON data from a java.io.Reader. Options are\nkey-value pairs, valid options are:\n\n   :eof-error? boolean\n\n      If true (default) will throw exception if the stream is empty.\n\n   :eof-value Object\n\n      Object to return if the stream is empty and eof-error? is\n      false. Default is nil.\n\n   :bigdec boolean\n\n      If true use BigDecimal for decimal numbers instead of Double.\n      Default is false.\n\n   :key-fn function\n\n      Single-argument function called on JSON property names; return\n      value will replace the property names in the output. Default\n      is clojure.core/identity, use clojure.core/keyword to get\n      keyword properties.\n\n   :value-fn function\n\n      Function to transform values in maps (\"objects\" in JSON) in\n      the output. For each JSON property, value-fn is called with\n      two arguments: the property name (transformed by key-fn) and\n      the value. The return value of value-fn will replace the value\n      in the output. If value-fn returns itself, the property will\n      be omitted from the output. The default value-fn returns the\n      value unchanged. This option does not apply to non-map\n      collections.",
   :namespace "clojure.data.json",
   :wiki-url
   "https://clojure.github.io/data.json//index.html#clojure.data.json/read"}
  {:raw-source-url
   "https://github.com/clojure/data.json/raw/d3cce5b200031cf22603c13a9a39e3939651d344/src/main/clojure/clojure/data/json_compat_0_1.clj",
   :name "read-json",
   :file "src/main/clojure/clojure/data/json_compat_0_1.clj",
   :source-url
   "https://github.com/clojure/data.json/blob/d3cce5b200031cf22603c13a9a39e3939651d344/src/main/clojure/clojure/data/json_compat_0_1.clj#L10",
   :line 10,
   :var-type "function",
   :arglists
   ([input]
    [input keywordize?]
    [input keywordize? eof-error? eof-value]),
   :doc
   "DEPRECATED; replaced by read-str.\n\nReads one JSON value from input String or Reader. If keywordize? is\ntrue (default), object keys will be converted to keywords. If\neof-error? is true (default), empty input will throw an\nEOFException; if false EOF will return eof-value.",
   :namespace "clojure.data.json",
   :wiki-url
   "https://clojure.github.io/data.json//index.html#clojure.data.json/read-json"}
  {:raw-source-url
   "https://github.com/clojure/data.json/raw/66f946edc83f59034f0c139cffe737367216c382/src/main/clojure/clojure/data/json.clj",
   :name "read-str",
   :file "src/main/clojure/clojure/data/json.clj",
   :source-url
   "https://github.com/clojure/data.json/blob/66f946edc83f59034f0c139cffe737367216c382/src/main/clojure/clojure/data/json.clj#L297",
   :line 297,
   :var-type "function",
   :arglists ([string & {:as options}]),
   :doc
   "Reads one JSON value from input String. Options are the same as for\nread.",
   :namespace "clojure.data.json",
   :wiki-url
   "https://clojure.github.io/data.json//index.html#clojure.data.json/read-str"}
  {:raw-source-url
   "https://github.com/clojure/data.json/raw/66f946edc83f59034f0c139cffe737367216c382/src/main/clojure/clojure/data/json.clj",
   :name "write",
   :file "src/main/clojure/clojure/data/json.clj",
   :source-url
   "https://github.com/clojure/data.json/blob/66f946edc83f59034f0c139cffe737367216c382/src/main/clojure/clojure/data/json.clj#L507",
   :line 507,
   :var-type "function",
   :arglists ([x writer & {:as options}]),
   :doc
   "Write JSON-formatted output to a java.io.Writer. Options are\nkey-value pairs, valid options are:\n\n :escape-unicode boolean\n\n    If true (default) non-ASCII characters are escaped as \\uXXXX\n\n :escape-js-separators boolean\n\n    If true (default) the Unicode characters U+2028 and U+2029 will\n    be escaped as \\u2028 and \\u2029 even if :escape-unicode is\n    false. (These two characters are valid in pure JSON but are not\n    valid in JavaScript strings.)\n\n :escape-slash boolean\n\n    If true (default) the slash / is escaped as \\/\n\n :sql-date-converter function\n\n    Single-argument function used to convert a java.sql.Date to\n    a java.time.Instant. As java.sql.Date does not have a\n    time-component (which is required by java.time.Instant), it needs\n    to be computed. The default implementation, `default-sql-date->instant-fn`\n    uses\n    ```\n       (.toInstant (.atStartOfDay (.toLocalDate sql-date) (java.time.ZoneId/systemDefault)))\n    ```\n\n :date-formatter\n\n     A java.time.DateTimeFormatter instance, defaults to DateTimeFormatter/ISO_INSTANT\n\n :key-fn function\n\n     Single-argument function called on map keys; return value will\n     replace the property names in the output. Must return a\n     string. Default calls clojure.core/name on symbols and\n     keywords and clojure.core/str on everything else.\n\n :value-fn function\n\n     Function to transform values in maps before writing. For each\n     key-value pair in an input map, called with two arguments: the\n     key (BEFORE transformation by key-fn) and the value. The\n     return value of value-fn will replace the value in the output.\n     If the return value is a number, boolean, string, or nil it\n     will be included literally in the output. If the return value\n     is a non-map collection, it will be processed recursively. If\n     the return value is a map, it will be processed recursively,\n     calling value-fn again on its key-value pairs. If value-fn\n     returns itself, the key-value pair will be omitted from the\n     output. This option does not apply to non-map collections.",
   :namespace "clojure.data.json",
   :wiki-url
   "https://clojure.github.io/data.json//index.html#clojure.data.json/write"}
  {:raw-source-url
   "https://github.com/clojure/data.json/raw/d3cce5b200031cf22603c13a9a39e3939651d344/src/main/clojure/clojure/data/json_compat_0_1.clj",
   :name "write-json",
   :file "src/main/clojure/clojure/data/json_compat_0_1.clj",
   :source-url
   "https://github.com/clojure/data.json/blob/d3cce5b200031cf22603c13a9a39e3939651d344/src/main/clojure/clojure/data/json_compat_0_1.clj#L35",
   :line 35,
   :var-type "function",
   :arglists ([x out escape-unicode?]),
   :doc
   "DEPRECATED; replaced by 'write'.\n\nPrint object to PrintWriter out as JSON",
   :namespace "clojure.data.json",
   :wiki-url
   "https://clojure.github.io/data.json//index.html#clojure.data.json/write-json"}
  {:raw-source-url
   "https://github.com/clojure/data.json/raw/66f946edc83f59034f0c139cffe737367216c382/src/main/clojure/clojure/data/json.clj",
   :name "write-str",
   :file "src/main/clojure/clojure/data/json.clj",
   :source-url
   "https://github.com/clojure/data.json/blob/66f946edc83f59034f0c139cffe737367216c382/src/main/clojure/clojure/data/json.clj#L564",
   :line 564,
   :var-type "function",
   :arglists ([x & {:as options}]),
   :doc
   "Converts x to a JSON-formatted string. Options are the same as\nwrite.",
   :namespace "clojure.data.json",
   :wiki-url
   "https://clojure.github.io/data.json//index.html#clojure.data.json/write-str"}
  {:raw-source-url
   "https://github.com/clojure/data.json/raw/66f946edc83f59034f0c139cffe737367216c382/src/main/clojure/clojure/data/json.clj",
   :name "JSONWriter",
   :file "src/main/clojure/clojure/data/json.clj",
   :source-url
   "https://github.com/clojure/data.json/blob/66f946edc83f59034f0c139cffe737367216c382/src/main/clojure/clojure/data/json.clj#L310",
   :line 310,
   :var-type "protocol",
   :arglists nil,
   :doc nil,
   :namespace "clojure.data.json",
   :wiki-url
   "https://clojure.github.io/data.json//index.html#clojure.data.json/JSONWriter"}
  {:raw-source-url nil,
   :name "-write",
   :file nil,
   :source-url nil,
   :var-type "function",
   :arglists ([object out options]),
   :doc "Print object to Appendable out as JSON",
   :namespace "clojure.data.json",
   :wiki-url
   "https://clojure.github.io/data.json//index.html#clojure.data.json/-write"})}
