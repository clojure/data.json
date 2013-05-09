{:namespaces
 ({:source-url
   "https://github.com/clojure/data.json/blob/20dac1124fbeb0723cd6d349adfd4421f7241f53/src/main/clojure/clojure/data/json.clj",
   :wiki-url
   "http://clojure.github.com/data.json/clojure.data.json-api.html",
   :name "clojure.data.json",
   :author "Stuart Sierra",
   :doc
   "JavaScript Object Notation (JSON) parser/generator.\nSee http://www.json.org/"}),
 :vars
 ({:arglists ([x & options]),
   :name "json-str",
   :namespace "clojure.data.json",
   :source-url
   "https://github.com/clojure/data.json/blob/d3cce5b200031cf22603c13a9a39e3939651d344/src/main/clojure/clojure/data/json_compat_0_1.clj#L42",
   :raw-source-url
   "https://github.com/clojure/data.json/raw/d3cce5b200031cf22603c13a9a39e3939651d344/src/main/clojure/clojure/data/json_compat_0_1.clj",
   :wiki-url
   "http://clojure.github.com/data.json//clojure.data.json-api.html#clojure.data.json/json-str",
   :doc
   "DEPRECATED; replaced by 'write-str'.\n\nConverts x to a JSON-formatted string.\n\nValid options are:\n  :escape-unicode false\n      to turn of \\uXXXX escapes of Unicode characters.",
   :var-type "function",
   :line 42,
   :file "src/main/clojure/clojure/data/json_compat_0_1.clj"}
  {:arglists ([x & options]),
   :name "pprint",
   :namespace "clojure.data.json",
   :source-url
   "https://github.com/clojure/data.json/blob/20dac1124fbeb0723cd6d349adfd4421f7241f53/src/main/clojure/clojure/data/json.clj#L463",
   :raw-source-url
   "https://github.com/clojure/data.json/raw/20dac1124fbeb0723cd6d349adfd4421f7241f53/src/main/clojure/clojure/data/json.clj",
   :wiki-url
   "http://clojure.github.com/data.json//clojure.data.json-api.html#clojure.data.json/pprint",
   :doc
   "Pretty-prints JSON representation of x to *out*. Options are the\nsame as for write except :value-fn, which is not supported.",
   :var-type "function",
   :line 463,
   :file "src/main/clojure/clojure/data/json.clj"}
  {:arglists ([x & options]),
   :name "pprint-json",
   :namespace "clojure.data.json",
   :source-url
   "https://github.com/clojure/data.json/blob/d3cce5b200031cf22603c13a9a39e3939651d344/src/main/clojure/clojure/data/json_compat_0_1.clj#L64",
   :raw-source-url
   "https://github.com/clojure/data.json/raw/d3cce5b200031cf22603c13a9a39e3939651d344/src/main/clojure/clojure/data/json_compat_0_1.clj",
   :wiki-url
   "http://clojure.github.com/data.json//clojure.data.json-api.html#clojure.data.json/pprint-json",
   :doc
   "DEPRECATED; replaced by 'pprint'.\n\nPretty-prints JSON representation of x to *out*.\n\nValid options are:\n  :escape-unicode false\n      to turn off \\uXXXX escapes of Unicode characters.",
   :var-type "function",
   :line 64,
   :file "src/main/clojure/clojure/data/json_compat_0_1.clj"}
  {:arglists ([x & options]),
   :name "print-json",
   :namespace "clojure.data.json",
   :source-url
   "https://github.com/clojure/data.json/blob/d3cce5b200031cf22603c13a9a39e3939651d344/src/main/clojure/clojure/data/json_compat_0_1.clj#L53",
   :raw-source-url
   "https://github.com/clojure/data.json/raw/d3cce5b200031cf22603c13a9a39e3939651d344/src/main/clojure/clojure/data/json_compat_0_1.clj",
   :wiki-url
   "http://clojure.github.com/data.json//clojure.data.json-api.html#clojure.data.json/print-json",
   :doc
   "DEPRECATED; replaced by 'write' to *out*.\n\nWrite JSON-formatted output to *out*.\n\nValid options are:\n  :escape-unicode false\n      to turn off \\uXXXX escapes of Unicode characters.",
   :var-type "function",
   :line 53,
   :file "src/main/clojure/clojure/data/json_compat_0_1.clj"}
  {:arglists ([reader & options]),
   :name "read",
   :namespace "clojure.data.json",
   :source-url
   "https://github.com/clojure/data.json/blob/20dac1124fbeb0723cd6d349adfd4421f7241f53/src/main/clojure/clojure/data/json.clj#L224",
   :raw-source-url
   "https://github.com/clojure/data.json/raw/20dac1124fbeb0723cd6d349adfd4421f7241f53/src/main/clojure/clojure/data/json.clj",
   :wiki-url
   "http://clojure.github.com/data.json//clojure.data.json-api.html#clojure.data.json/read",
   :doc
   "Reads a single item of JSON data from a java.io.Reader. Options are\nkey-value pairs, valid options are:\n\n   :eof-error? boolean\n\n      If true (default) will throw exception if the stream is empty.\n\n   :eof-value Object\n\n      Object to return if the stream is empty and eof-error? is\n      false. Default is nil.\n\n   :bigdec boolean\n\n      If true use BigDecimal for decimal numbers instead of Double.\n      Default is false.\n\n   :key-fn function\n\n      Single-argument function called on JSON property names; return\n      value will replace the property names in the output. Default\n      is clojure.core/identity, use clojure.core/keyword to get\n      keyword properties.\n\n   :value-fn function\n\n      Function to transform values in the output. For each JSON\n      property, value-fn is called with two arguments: the property\n      name (transformed by key-fn) and the value. The return value\n      of value-fn will replace the value in the output. If value-fn\n      returns itself, the property will be omitted from the output.\n      The default value-fn returns the value unchanged.",
   :var-type "function",
   :line 224,
   :file "src/main/clojure/clojure/data/json.clj"}
  {:arglists
   ([input]
    [input keywordize?]
    [input keywordize? eof-error? eof-value]),
   :name "read-json",
   :namespace "clojure.data.json",
   :source-url
   "https://github.com/clojure/data.json/blob/d3cce5b200031cf22603c13a9a39e3939651d344/src/main/clojure/clojure/data/json_compat_0_1.clj#L10",
   :raw-source-url
   "https://github.com/clojure/data.json/raw/d3cce5b200031cf22603c13a9a39e3939651d344/src/main/clojure/clojure/data/json_compat_0_1.clj",
   :wiki-url
   "http://clojure.github.com/data.json//clojure.data.json-api.html#clojure.data.json/read-json",
   :doc
   "DEPRECATED; replaced by read-str.\n\nReads one JSON value from input String or Reader. If keywordize? is\ntrue (default), object keys will be converted to keywords. If\neof-error? is true (default), empty input will throw an\nEOFException; if false EOF will return eof-value.",
   :var-type "function",
   :line 10,
   :file "src/main/clojure/clojure/data/json_compat_0_1.clj"}
  {:arglists ([string & options]),
   :name "read-str",
   :namespace "clojure.data.json",
   :source-url
   "https://github.com/clojure/data.json/blob/20dac1124fbeb0723cd6d349adfd4421f7241f53/src/main/clojure/clojure/data/json.clj#L268",
   :raw-source-url
   "https://github.com/clojure/data.json/raw/20dac1124fbeb0723cd6d349adfd4421f7241f53/src/main/clojure/clojure/data/json.clj",
   :wiki-url
   "http://clojure.github.com/data.json//clojure.data.json-api.html#clojure.data.json/read-str",
   :doc
   "Reads one JSON value from input String. Options are the same as for\nread.",
   :var-type "function",
   :line 268,
   :file "src/main/clojure/clojure/data/json.clj"}
  {:arglists ([x writer & options]),
   :name "write",
   :namespace "clojure.data.json",
   :source-url
   "https://github.com/clojure/data.json/blob/20dac1124fbeb0723cd6d349adfd4421f7241f53/src/main/clojure/clojure/data/json.clj#L387",
   :raw-source-url
   "https://github.com/clojure/data.json/raw/20dac1124fbeb0723cd6d349adfd4421f7241f53/src/main/clojure/clojure/data/json.clj",
   :wiki-url
   "http://clojure.github.com/data.json//clojure.data.json-api.html#clojure.data.json/write",
   :doc
   "Write JSON-formatted output to a java.io.Writer. Options are\nkey-value pairs, valid options are:\n\n :escape-unicode boolean\n\n    If true (default) non-ASCII characters are escaped as \\uXXXX\n\n :escape-slash boolean\n\n    If true (default) the slash / is escaped as \\/\n\n :key-fn function\n\n     Single-argument function called on map keys; return value will\n     replace the property names in the output. Must return a\n     string. Default calls clojure.core/name on symbols and\n     keywords and clojure.core/str on everything else.\n\n :value-fn function\n\n     Function to transform values before writing. For each\n     key-value pair in the input, called with two arguments: the\n     key (BEFORE transformation by key-fn) and the value. The\n     return value of value-fn will replace the value in the output.\n     If the return value is a number, boolean, string, or nil it\n     will be included literally in the output. If the return value\n     is a non-map collection, it will be processed recursively. If\n     the return value is a map, it will be processed recursively,\n     calling value-fn again on its key-value pairs. If value-fn\n     returns itself, the key-value pair will be omitted from the\n     output.",
   :var-type "function",
   :line 387,
   :file "src/main/clojure/clojure/data/json.clj"}
  {:arglists ([x out escape-unicode?]),
   :name "write-json",
   :namespace "clojure.data.json",
   :source-url
   "https://github.com/clojure/data.json/blob/d3cce5b200031cf22603c13a9a39e3939651d344/src/main/clojure/clojure/data/json_compat_0_1.clj#L35",
   :raw-source-url
   "https://github.com/clojure/data.json/raw/d3cce5b200031cf22603c13a9a39e3939651d344/src/main/clojure/clojure/data/json_compat_0_1.clj",
   :wiki-url
   "http://clojure.github.com/data.json//clojure.data.json-api.html#clojure.data.json/write-json",
   :doc
   "DEPRECATED; replaced by 'write'.\n\nPrint object to PrintWriter out as JSON",
   :var-type "function",
   :line 35,
   :file "src/main/clojure/clojure/data/json_compat_0_1.clj"}
  {:arglists ([x & options]),
   :name "write-str",
   :namespace "clojure.data.json",
   :source-url
   "https://github.com/clojure/data.json/blob/20dac1124fbeb0723cd6d349adfd4421f7241f53/src/main/clojure/clojure/data/json.clj#L431",
   :raw-source-url
   "https://github.com/clojure/data.json/raw/20dac1124fbeb0723cd6d349adfd4421f7241f53/src/main/clojure/clojure/data/json.clj",
   :wiki-url
   "http://clojure.github.com/data.json//clojure.data.json-api.html#clojure.data.json/write-str",
   :doc
   "Converts x to a JSON-formatted string. Options are the same as\nwrite.",
   :var-type "function",
   :line 431,
   :file "src/main/clojure/clojure/data/json.clj"}
  {:file "src/main/clojure/clojure/data/json.clj",
   :raw-source-url
   "https://github.com/clojure/data.json/raw/20dac1124fbeb0723cd6d349adfd4421f7241f53/src/main/clojure/clojure/data/json.clj",
   :source-url
   "https://github.com/clojure/data.json/blob/20dac1124fbeb0723cd6d349adfd4421f7241f53/src/main/clojure/clojure/data/json.clj#L279",
   :wiki-url
   "http://clojure.github.com/data.json//clojure.data.json-api.html#clojure.data.json/JSONWriter",
   :namespace "clojure.data.json",
   :line 279,
   :var-type "protocol",
   :doc nil,
   :name "JSONWriter"}
  {:file nil,
   :raw-source-url nil,
   :source-url nil,
   :wiki-url
   "http://clojure.github.com/data.json//clojure.data.json-api.html#clojure.data.json/-write",
   :namespace "clojure.data.json",
   :var-type "function",
   :arglists ([object out]),
   :doc "Print object to PrintWriter out as JSON",
   :name "-write"})}
