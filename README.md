clojure.data.json
========================================

JSON parser/generator to/from Clojure data structures.

Follows the specification on http://json.org/



Releases and Dependency Information
========================================

Latest stable release: 0.1.3

* [All Released Versions](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22org.clojure%22%20AND%20a%3A%22data.json%22)

* [Development Snapshot Versions](https://oss.sonatype.org/index.html#nexus-search;gav~org.clojure~data.json~~~)

[Leiningen](https://github.com/technomancy/leiningen) dependency information:

    [org.clojure/data.json "0.1.3"]

[Maven](http://maven.apache.org/) dependency information:

    <dependency>
      <groupId>org.clojure</groupId>
      <artifactId>data.json</artifactId>
      <version>0.1.3</version>
    </dependency>



Usage
========================================

Refer to docstrings in the `clojure.data.json` namespace for
additional documentation.

[API Documentation](http://clojure.github.com/data.json/)



Developer Information
========================================

* [GitHub project](https://github.com/clojure/data.json)

* [Bug Tracker](http://dev.clojure.org/jira/browse/DJSON)

* [Continuous Integration](http://build.clojure.org/job/data.json/)

* [Compatibility Test Matrix](http://build.clojure.org/job/data.json-test-matrix/)



Change Log
====================

* Release 0.1.3 on 2012-03-09
  * Fix writing strings containing characters outside the BMP
* Release 0.1.2 on 2011-10-14
  * Better parsing of hexadecimal character escapes
  * Fix EOF-handling bug
  * Fix reflection warnings [DJSON-1](http://dev.clojure.org/jira/browse/DJSON-1)
* Release 0.1.1 on 2011-07-01
  * Ensure that printing to `*out*` always uses a PrintWriter.
* Release 0.1.0 on 2011-03-18
  * Initial release.
  * Source-compatible with clojure.contrib.json, except for the name change.



Copyright and License
========================================

Copyright (c) Stuart Sierra, 2012. All rights reserved.  The use and
distribution terms for this software are covered by the Eclipse Public
License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which can
be found in the file epl-v10.html at the root of this distribution.
By using this software in any fashion, you are agreeing to be bound by
the terms of this license.  You must not remove this notice, or any
other, from this software.
