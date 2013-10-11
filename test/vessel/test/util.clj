(ns vessel.test.util
  (:require vessel.util)
  (:use clojure.test))

(deftest dir-path-with-file-path
  (is (= (vessel.util/dir-path "vessel/test/data/hello.csv")
         "vessel/test/data")))

(deftest show-key-with-bucket-and-file-key
  (is (= (vessel.util/show-url "vessel-test-data" "hello.csv")
         "s3://vessel-test-data/hello.csv")))
