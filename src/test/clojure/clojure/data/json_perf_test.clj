(ns clojure.data.json-perf-test
  (:require [cheshire.core :as cheshire]
            [clj-async-profiler.core :as prof]
            [clojure.data.json :as json]
            [criterium.core :refer :all]
            [jsonista.core :as jsonista])
  (:import com.jsoniter.JsonIterator))

(defmacro profiling [times & body]
  `(try
     (prof/start {})
     (dotimes [_# ~times]
       ~@body)
     (finally
       (println (str "file://" (:path (bean (prof/stop {}))))))))

(defn json-data [size]
  (slurp (str "dev-resources/json" size ".json")))

(defn do-read-bench [size]
  (let [json (json-data size)]
    (println "Results for"  size "json:")
    (println "data.json:")
    (println (with-out-str (quick-bench (json/read-str json))))
    (println "cheshire:")
    (println (with-out-str (quick-bench (cheshire/parse-string-strict json))))
    (println "jsonista:")
    (println (with-out-str (quick-bench (jsonista/read-value json))))
    (println "jsoniter:")
    (println (with-out-str (quick-bench (.read (JsonIterator/parse ^String json)))))))

(defn do-write-bench [size]
  (let [edn (json/read-str (json-data size))]
    (println "Results for"  size "json:")
    (println "data.json:")
    (println (with-out-str (quick-bench (json/write-str edn))))
    (println "cheshire:")
    (println (with-out-str (quick-bench (cheshire/generate-string edn))))
    (println "jsonista:")
    (println (with-out-str (quick-bench (jsonista/write-value-as-string edn))))))

(defn read-bench-all-sizes []
  (doseq [size ["10b" "100b" "1k" "10k" "100k"]]
    (do-read-bench size)))

(defn write-bench-all-sizes []
  (doseq [size ["10b" "100b" "1k" "10k" "100k"]]
    (do-write-bench size)))

(defn profile-read-all-sizes []
  (doseq [size ["10b" "100b" "1k" "10k" "100k"]]
    (let [json (json-data size)]
      (profiling 10000 (json/read-str json))
      (Thread/sleep 1000))))

(defn profile-write-all-sizes []
  (doseq [size ["10b" "100b" "1k" "10k" "100k"]]
    (let [edn (json/read-str (json-data size))]
      (profiling 10000 (json/write-str edn))
      (Thread/sleep 1000))))

(defn read-bench-data-json []
  (doseq [size ["10b" "100b" "1k" "10k" "100k"]]
    (let [json (json-data size)]
      (quick-bench (json/read-str json)))))

(defn write-bench-data-json []
  (doseq [size ["10b" "100b" "1k" "10k" "100k"]]
    (let [json (json/read-str (json-data size))]
      (quick-bench (json/write-str json)))))

(defn write-profiling []
  (doseq [size ["10b" "100b" "1k" "10k" "100k"]]
    (let [json (json/read-str (json-data size))]
      (profiling 10000 (json/write-str json)))))

(defn read-profiling []
  (doseq [size ["10b" "100b" "1k" "10k" "100k"]]
    (let [json (json-data size)]
      (profiling 10000 (json/read-str json)))))
