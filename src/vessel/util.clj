(ns vessel.util
  (:require [clojure.java.io :as io]
            [vessel.log      :as log]
            [clojure.string  :as string]
            [me.raynes.fs    :as fs])
  (:import java.util.zip.GZIPOutputStream))

(defn mkdir
  [dir-path]
  (.mkdirs (io/file dir-path)))

(defn dir-path
  [file-path]
  (string/join "/" (butlast (string/split file-path #"/"))))

(defn file-list
  [start-dir]
  (remove #(empty? (% 2)) (vec (fs/iterate-dir start-dir))))

(defn gzip-file
  [input output & opts]
  (do
    (log/info (str "compress" " " input))
    (with-open [output (-> output io/output-stream GZIPOutputStream.)]
      (apply io/copy (io/reader input) output opts))))

(defn gzip-and-delete
  [input output]
  (do
    (gzip-file input output))
    (fs/delete input))

(defn gzip-and-delete-files
  [inputs & opts]
  (let [outputs (map #(str % ".gz") inputs)]
    (doall (pmap gzip-and-delete inputs outputs))))

(defn show-url
  [bucket file-key]
  (str "s3://" bucket "/" file-key))
