;; NOTE: Used only for perf testing - this project is built with Maven (see pom.xml)
(defproject clojure.data.json "1.1.1-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.12.4"]]
  :source-paths ["src/main/clojure"]
  :java-source-paths ["src/main/java"]
  :java-test-paths ["src/test/java"]
  :test-paths ["src/test/clojure" "src/test/clojure-perf"]
  :profiles {:dev {:dependencies [[com.clojure-goes-fast/clj-async-profiler "1.6.2"]
                                  [com.clojure-goes-fast/clj-java-decompiler "0.3.7"]
                                  [org.clojure/test.check "1.1.3"]
                                  [criterium/criterium "0.4.6"]
                                  [metosin/jsonista "0.3.13"]
                                  [cheshire/cheshire "6.1.0"]
                                  [org.openjdk.jmh/jmh-core "1.37"]
                                  [jmh-clojure "0.4.1"]
                                  [com.jsoniter/jsoniter "0.9.23"]]
                   :resource-paths ["dev-resources"]
                   :global-vars {*warn-on-reflection* true}}}
  :jvm-opts ["-Djdk.attach.allowAttachSelf=true"])
