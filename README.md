**clojure.data.json** : JavaScript Object Notation (JSON) parser/generator for Clojure

Copyright (c) Stuart Sierra, 2010. All rights reserved.  The use and distribution terms for this software are covered by the Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which can be found in the file epl-v10.html at the root of this distribution.  By using this software in any fashion, you are agreeing to be bound by the terms of this license.  You must not remove this notice, or any other, from this software.


Adding clojure.data.json to Your Project
========================================

clojure.data.json is available in the Maven central repositories.  Add it to your Maven project's `pom.xml` as:

    <dependency>
      <groupId>org.clojure</groupId>
      <artifactId>data.json</artifactId>
      <version>0.1.0</version>
    </dependency>

or your Leiningen `project.clj` as:

    [org.clojure/data.json "0.1.0"]

Then depend on it in a Clojure namespace as:

    (ns your.project.namespace
      (:use [clojure.data.json :only (json-str write-json read-json)]))



Comparison to clojure.contrib.json
========================================

This project is the continuation of clojure.contrib.json.  As of version 0.1.0, it is source-compatible with clojure.contrib.json, except for the name change.



Downloading and Building from Source
========================================

Prerequisites:

* [Git](http://git-scm.com/)
* [Apache Maven](http://maven.apache.org/) 2 or 3

Instructions:

* Download the source: `git clone git://github.com/clojure/data.json.git`
* Enter the directory: `cd data.json`
* Compile, test, and install locally: `mvn install`
