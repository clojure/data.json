data.json
========================================

JSON parser/generator to/from Clojure data structures.

Follows the specification on http://json.org/



Releases and Dependency Information
----------------------------------------

Latest stable release is [0.2.6]

[Leiningen] dependency information:

    [org.clojure/data.json "0.2.6"]

[Maven] dependency information:

    <dependency>
      <groupId>org.clojure</groupId>
      <artifactId>data.json</artifactId>
      <version>0.2.6</version>
    </dependency>

[Gradle] dependency information:

    compile "org.clojure:data.json:0.2.6"

[Leiningen]: http://leiningen.org/
[Maven]: http://maven.apache.org/
[Gradle]: http://www.gradle.org/


Other versions:

* [All Released Versions](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22org.clojure%22%20AND%20a%3A%22data.json%22)

* [Development Snapshots](https://oss.sonatype.org/index.html#nexus-search;gav~org.clojure~data.json~~~)

* [Development Snapshot Repositories](http://dev.clojure.org/display/doc/Maven+Settings+and+Repositories)



Usage
----------------------------------------

[API Documentation](http://clojure.github.com/data.json/)

Example usage:

    (ns example
      (:require [clojure.data.json :as json]))

To convert to/from JSON strings, use `json/write-str` and `json/read-str`:

    (json/write-str {:a 1 :b 2})
    ;;=> "{\"a\":1,\"b\":2}"

    (json/read-str "{\"a\":1,\"b\":2}")
    ;;=> {"a" 1, "b" 2}

Note that these operations are not symmetric: converting Clojure data
into JSON is lossy.


### Converting Key/Value Types

You can specify a `:key-fn` to convert map keys on the way in or out:

    (json/read-str "{\"a\":1,\"b\":2}"
                   :key-fn keyword)
    ;;=> {:a 1, :b 2}

    (json/write-str {:a 1 :b 2}
                    :key-fn #(.toUpperCase %))
    ;;=> "{\"A\":1,\"B\":2}"

    (json/read-str "{\"a\":1,\"b\":2}"
                   :key-fn #(keyword "com.example" %))
    ;;=> {:com.example/a 1, :com.example/b 2}

You can specify a `:value-fn` to convert map values on the way in or
out. The value-fn will be called with two arguments, the key and the
value, and it returns the updated value.

    (defn my-value-reader [key value]
      (if (= key :date)
        (java.sql.Date/valueOf value)
        value))

    (json/read-str "{\"number\":42,\"date\":\"2012-06-02\"}"
                   :value-fn my-value-reader
                   :key-fn keyword) 
    ;;=> {:number 42, :date #inst "2012-06-02T04:00:00.000-00:00"}

Be aware that `:value-fn` only works on maps (JSON objects). If your
root data structure is, for example, a vector of dates, you will need
to pre- or post-process it outside of data.json. [clojure.walk] may be
useful for this.

[clojure.walk]: http://clojure.github.io/clojure/clojure.walk-api.html


### Order of key-fn / value-fn

If you specify both a `:key-fn` and a `:value-fn` when **reading**,
the value-fn is called **after** the key has been processed by the
key-fn.

The **reverse** is true when **writing**:

    (defn my-value-writer [key value]
      (if (= key :date)
        (str (java.sql.Date. (.getTime value)))
        value))

    (json/write-str {:number 42, :date (java.util.Date. 112 5 2)}
                    :value-fn my-value-writer
                    :key-fn name) 
    ;;=> "{\"number\":42,\"date\":\"2012-06-02\"}"


### Reading/Writing a Stream

You can also read JSON directly from a java.io.Reader with `json/read`
and write JSON directly to a java.io.Writer with `json/write`.


### More

Other options are available. Refer to the [API Documentation] for details.

[API Documentation]: http://clojure.github.com/data.json/



Developer Information
----------------------------------------

* [GitHub project](https://github.com/clojure/data.json)

* [How to contribute](http://dev.clojure.org/display/community/Contributing)

* [Bug Tracker](http://dev.clojure.org/jira/browse/DJSON)

* [Continuous Integration](http://build.clojure.org/job/data.json/)

* [Compatibility Test Matrix](http://build.clojure.org/job/data.json-test-matrix/)



Change Log
----------------------------------------

* Development version 0.2.7-SNAPSHOT (current Git `master`)
* Release [0.2.6] on 2015-Mar-6
  * Modify build to produce an AOT package with classifier "aot"
* Release [0.2.5] on 2014-Jun-13
  * Fix [DJSON-17]: throw exception on Infinite or NaN floating-point
    values. Old behavior could produce invalid JSON.
* Release [0.2.4] on 2014-Jan-10
  * Small change in behavior: `clojure.data.json/pprint` now adds a
    newline after its output just like `clojure.core/pprint`
  * Fix [DJSON-13]: flush output after pprint
  * Fix [DJSON-14]: handle EOF inside character escape
  * Fix [DJSON-15]: bad syntax in test
* Release [0.2.3] on 2013-Aug-30
  * Enhancement [DJSON-9]: option to escape U+2028 and U+2029
  * Fix [DJSON-11]: printing unnecessary commas with value-fn
* Release [0.2.2] on 2013-Apr-07
  * Fix [DJSON-7]: extra commas when removing key/value pairs)
  * Fix [DJSON-8]: wrong output stream in `write-json`
* Release [0.2.1] on 2012-Oct-26
  * Restores backwards-compatibility with 0.1.x releases. The older
    0.1.x APIs are marked as deprecated in their documentation. They
    will be removed in a future release.
* Release [0.2.0] on 2012-Oct-12
  * **Not recommended for use**: this release introduced breaking API
    changes (renaming core functions) without any path for
    backwards-compatibility. Applications with transitive dependencies
    on both the 0.2.x and 0.1.x APIs cannot use this version.
  * New :key-fn and :value-fn permit flexible transformation
    of values when reading & writing JSON
  * Support for reading large integers as BigInt
  * Optional support for reading decimals as BigDecimal
  * Performance improvements
* Release [0.1.3] on 2012-Mar-09
  * Fix writing strings containing characters outside the BMP
* Release [0.1.2] on 2011-Oct-14
  * Better parsing of hexadecimal character escapes
  * Fix EOF-handling bug
  * Fix [DJSON-1]: reflection warnings
* Release [0.1.1] on 2011-Jul-01
  * Ensure that printing to `*out*` always uses a PrintWriter.
* Release [0.1.0] on 2011-Mar-18
  * Initial release.
  * Source-compatible with clojure.contrib.json, except for the name change.

[DJSON-17]: http://dev.clojure.org/jira/browse/DJSON-17
[DJSON-15]: http://dev.clojure.org/jira/browse/DJSON-15
[DJSON-14]: http://dev.clojure.org/jira/browse/DJSON-14
[DJSON-13]: http://dev.clojure.org/jira/browse/DJSON-13
[DJSON-11]: http://dev.clojure.org/jira/browse/DJSON-11
[DJSON-9]: http://dev.clojure.org/jira/browse/DJSON-9
[DJSON-8]: http://dev.clojure.org/jira/browse/DJSON-8
[DJSON-7]: http://dev.clojure.org/jira/browse/DJSON-7
[DJSON-1]: http://dev.clojure.org/jira/browse/DJSON-1

[0.2.6]: https://github.com/clojure/data.json/tree/data.json-0.2.6
[0.2.5]: https://github.com/clojure/data.json/tree/data.json-0.2.5
[0.2.4]: https://github.com/clojure/data.json/tree/data.json-0.2.4
[0.2.3]: https://github.com/clojure/data.json/tree/data.json-0.2.3
[0.2.2]: https://github.com/clojure/data.json/tree/data.json-0.2.2
[0.2.1]: https://github.com/clojure/data.json/tree/data.json-0.2.1
[0.2.0]: https://github.com/clojure/data.json/tree/data.json-0.2.0
[0.1.3]: https://github.com/clojure/data.json/tree/data.json-0.1.3
[0.1.2]: https://github.com/clojure/data.json/tree/data.json-0.1.2
[0.1.1]: https://github.com/clojure/data.json/tree/data.json-0.1.1
[0.1.0]: https://github.com/clojure/data.json/tree/data.json-0.1.0



Copyright and License
----------------------------------------

Copyright (c) Stuart Sierra, 2012. All rights reserved.  The use and
distribution terms for this software are covered by the Eclipse Public
License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which can
be found in the file epl-v10.html at the root of this distribution.
By using this software in any fashion, you are agreeing to be bound by
the terms of this license.  You must not remove this notice, or any
other, from this software.
