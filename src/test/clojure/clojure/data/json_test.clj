(ns clojure.data.json-test
  (:use clojure.test clojure.data.json-new))

#_(deftest can-read-from-pushback-reader
  (let [s (java.io.PushbackReader. (java.io.StringReader. "42"))]
    (is (= 42 (read-json s)))))

#_(deftest can-read-from-reader
  (let [s (java.io.StringReader. "42")]
    (is (= 42 (read-json s)))))

(deftest can-read-numbers
  (is (= 42 (parse-string "42")))
  (is (= -3 (parse-string "-3")))
  (is (= 3.14159 (parse-string "3.14159")))
  (is (= 6.022e23 (parse-string "6.022e23"))))

(deftest can-read-null
  (is (= nil (parse-string "null"))))

(deftest can-read-strings
  (is (= "Hello, World!" (parse-string "\"Hello, World!\""))))

(deftest handles-escaped-slashes-in-strings
  (is (= "/foo/bar" (parse-string "\"\\/foo\\/bar\""))))

(deftest handles-unicode-escapes
  (is (= " \u0beb " (parse-string "\" \\u0bEb \""))))

(deftest handles-unicode-outside-bmp
  (is (= "\"smiling face: \uD83D\uDE03\""
         (json-str "smiling face: \uD83D\uDE03" :escape-unicode false)))
  (is (= "\"smiling face: \\ud83d\\ude03\""
         (json-str "smiling face: \uD83D\uDE03" :escape-unicode true))))

(deftest handles-escaped-whitespace
  (is (= "foo\nbar" (parse-string "\"foo\\nbar\"")))
  (is (= "foo\rbar" (parse-string "\"foo\\rbar\"")))
  (is (= "foo\tbar" (parse-string "\"foo\\tbar\""))))

(deftest can-read-booleans
  (is (= true (parse-string "true")))
  (is (= false (parse-string "false"))))

(deftest can-ignore-whitespace
  (is (= nil (parse-string "\r\n   null"))))

(deftest can-read-arrays
  (is (= [1 2 3] (parse-string "[1,2,3]")))
  (is (= ["Ole" "Lena"] (parse-string "[\"Ole\", \r\n \"Lena\"]"))))

(deftest can-read-objects
  (is (= {:a 1, :b 2} (parse-string "{\"a\": 1, \"b\": 2}"
                                    :keywordize true))))

(deftest can-read-nested-structures
  (is (= {:a [1 2 {:b [3 "four"]} 5.5]}
         (parse-string "{\"a\":[1,2,{\"b\":[3,\"four\"]},5.5]}"
                       :keywordize true))))

(deftest disallows-non-string-keys
  (is (thrown? Exception (parse-string "{26:\"z\""))))

(deftest disallows-barewords
  (is (thrown? Exception (parse-string "  foo  "))))

(deftest disallows-unclosed-arrays
  (is (thrown? Exception (parse-string "[1, 2,  "))))

(deftest disallows-unclosed-objects
  (is (thrown? Exception (parse-string "{\"a\":1,  "))))

(deftest can-get-string-keys
  (is (= {"a" [1 2 {"b" [3 "four"]} 5.5]}
         (parse-string "{\"a\":[1,2,{\"b\":[3,\"four\"]},5.5]}"))))

(declare pass1-string)

(deftest pass1-test
  (let [input (parse-string pass1-string)]
    (is (= "JSON Test Pattern pass1" (first input)))
    (is (= "array with 1 element" (get-in input [1 "object with 1 member" 0])))
    (is (= 1234567890 (get-in input [8 "integer"])))
    (is (= "rosebud" (last input)))))

; from http://www.json.org/JSON_checker/test/pass1.json
(def pass1-string
     "[
    \"JSON Test Pattern pass1\",
    {\"object with 1 member\":[\"array with 1 element\"]},
    {},
    [],
    -42,
    true,
    false,
    null,
    {
        \"integer\": 1234567890,
        \"real\": -9876.543210,
        \"e\": 0.123456789e-12,
        \"E\": 1.234567890E+34,
        \"\":  23456789012E66,
        \"zero\": 0,
        \"one\": 1,
        \"space\": \" \",
        \"quote\": \"\\\"\",
        \"backslash\": \"\\\\\",
        \"controls\": \"\\b\\f\\n\\r\\t\",
        \"slash\": \"/ & \\/\",
        \"alpha\": \"abcdefghijklmnopqrstuvwyz\",
        \"ALPHA\": \"ABCDEFGHIJKLMNOPQRSTUVWYZ\",
        \"digit\": \"0123456789\",
        \"0123456789\": \"digit\",
        \"special\": \"`1~!@#$%^&*()_+-={':[,]}|;.</>?\",
        \"hex\": \"\\u0123\\u4567\\u89AB\\uCDEF\\uabcd\\uef4A\",
        \"true\": true,
        \"false\": false,
        \"null\": null,
        \"array\":[  ],
        \"object\":{  },
        \"address\": \"50 St. James Street\",
        \"url\": \"http://www.JSON.org/\",
        \"comment\": \"// /* <!-- --\",
        \"# -- --> */\": \" \",
        \" s p a c e d \" :[1,2 , 3

,

4 , 5        ,          6           ,7        ],\"compact\":[1,2,3,4,5,6,7],
        \"jsontext\": \"{\\\"object with 1 member\\\":[\\\"array with 1 element\\\"]}\",
        \"quotes\": \"&#34; \\u0022 %22 0x22 034 &#x22;\",
        \"\\/\\\\\\\"\\uCAFE\\uBABE\\uAB98\\uFCDE\\ubcda\\uef4A\\b\\f\\n\\r\\t`1~!@#$%^&*()_+-=[]{}|;:',./<>?\"
: \"A key can be any string\"
    },
    0.5 ,98.6
,
99.44
,

1066,
1e1,
0.1e1,
1e-1,
1e00,2e+00,2e-00
,\"rosebud\"]")


(deftest can-print-json-strings
  (is (= "\"Hello, World!\"" (json-str "Hello, World!")))
  (is (= "\"\\\"Embedded\\\" Quotes\"" (json-str "\"Embedded\" Quotes"))))

(deftest can-print-unicode
  (is (= "\"\\u1234\\u4567\"" (json-str "\u1234\u4567"))))

(deftest can-print-nonescaped-unicode
  (is (= "\"\u1234\u4567\"" (json-str "\u1234\u4567" :escape-unicode false))))

(deftest can-print-json-null
  (is (= "null" (json-str nil))))

(deftest can-print-ratios-as-doubles
  (is (= "0.75" (json-str 3/4))))

(deftest can-print-bigints
  (is (= "12345678901234567890" (json-str 12345678901234567890))))

(deftest can-print-json-arrays
  (is (= "[1,2,3]" (json-str [1 2 3])))
  (is (= "[1,2,3]" (json-str (list 1 2 3))))
  (is (= "[1,2,3]" (json-str (sorted-set 1 2 3))))
  (is (= "[1,2,3]" (json-str (seq [1 2 3])))))

(deftest can-print-java-arrays
 (is (= "[1,2,3]" (json-str (into-array [1 2 3])))))

(deftest can-print-empty-arrays
  (is (= "[]" (json-str [])))
  (is (= "[]" (json-str (list))))
  (is (= "[]" (json-str #{}))))

(deftest can-print-json-objects
  (is (= "{\"a\":1,\"b\":2}" (json-str (sorted-map :a 1 :b 2)))))

(deftest object-keys-must-be-strings
  (is (= "{\"1\":1,\"2\":2") (json-str (sorted-map 1 1 2 2))))

(deftest can-print-empty-objects
  (is (= "{}" (json-str {}))))

(deftest accept-sequence-of-nils
  (is (= "[null,null,null]" (json-str [nil nil nil]))))

(deftest error-on-nil-keys
  (is (thrown? Exception (json-str {nil 1}))))

(deftest characters-in-symbols-are-escaped
  (is (= "\"foo\\u1b1b\"" (json-str (symbol "foo\u1b1b")))))

(deftest default-throws-on-eof
  (is (thrown? java.io.EOFException (parse-string ""))))

(deftest can-accept-eof
  (is (= ::eof (parse-string "" :eof-error? false :eof-value ::eof))))

(deftest characters-in-map-keys-are-escaped
  (is (= (json-str {"\"" 42}) "{\"\\\"\":42}")))

;;; Pretty-printer

(deftest pretty-printing
  (let [x (parse-string pass1-string)]
    (is (= x (parse-string (with-out-str (pprint-json x)))))))

(deftest can-pretty-print-nonescaped-unicode
  (is (= "\"\u1234\u4567\"" (with-out-str (pprint-json "\u1234\u4567" :escape-unicode false)))))

(defn benchmark []
  (dotimes [_ 8]
    (time
     (dotimes [_ 100]
       (assert (= (parse-string pass1-string false)
                  (parse-string (json-str (parse-string pass1-string false)) false)))))))
