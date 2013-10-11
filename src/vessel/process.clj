(ns vessel.process
  (:refer-clojure :exclude [name parents])
  (:require [me.raynes.fs             :as fs]
            [me.raynes.fs.compression :as compression]
            [clojure.java.io          :as io]
            [clojure.string           :as string]
            [clojure.data.csv         :as csv]
            [vessel.log               :as log]
            [vessel.input             :as input]
            [vessel.util              :as util]
            [vessel.manifest          :as manifest])
  (:import java.io.File))

(def output-files (atom {}))

(defn add-output-files-key
  [file-key]
  (swap! output-files assoc-in [file-key] #{}))

(defn add-output-file-path
  [file-key file-path]
  (swap! output-files assoc-in [file-key]
         (clojure.set/union (get-in @output-files [file-key]) #{file-path})))

(defn split
  [line]
  (string/split line (re-pattern manifest/input-delimiter)))

(defn write-file
  [file-key file-path row]
  (do
    (util/mkdir (util/dir-path file-path))
    (with-open [wtr (io/writer file-path :append true)]
      (do
        (if (not (contains? (@output-files file-key) file-path))
            (csv/write-csv wtr [@manifest/header-row] :newline :cr+lf))
        (csv/write-csv wtr [row] :newline :cr+lf)))))

(defn process-file
  [processing-file date]
  (log/info (str "process input" " " processing-file))

  (add-output-files-key processing-file)

  (with-open [rdr (io/reader processing-file)]
    (doseq [line (line-seq rdr)]
      (try
        (let [cols             (split line)
              out              (map #(nth cols % "") @manifest/output-indexes)
              output-file-path (manifest/output-path cols processing-file date)]
        (write-file processing-file output-file-path
          (manifest/apply-col-ffns out))

        (add-output-file-path processing-file output-file-path))
        (catch Exception e (log/error (str "error" " " e))))))
  (do
    (fs/delete processing-file)
    (util/gzip-and-delete-files (@output-files processing-file))))

(defn prepare-input
  [path filename]
  (let [date            (last (string/split path #"/"))
        input-path      (str path "/" filename)
        processing-path (string/replace input-path "/input/" "/processing/") ;; TODO configure
        processing-dir  (util/dir-path processing-path)
        processing-file (string/replace processing-path #"\.gz$" "")]

    (do
      (util/mkdir processing-dir)
      (.renameTo (File. input-path) (File. processing-path))
      (log/info (str "uncompress input" " " processing-path))
      (compression/gunzip processing-path processing-file)
      (fs/delete processing-path)
      (process-file processing-file date))))

(defn run!
  []
  (doseq [input-path (input/local)]
    (doseq [file-name (sort (input-path 2))]
      (prepare-input (str (input-path 0)) file-name))))

(defn prun!
  []
  (doseq [input-path (input/local)]
    (doall
      (pmap #(prepare-input (str (input-path 0)) %) (sort (input-path 2))))))

(defn -main
  [manifest-file batch-name]
  (do
    (in-ns 'vessel.manifest)
    (manifest/reset-all! manifest-file batch-name)
    (in-ns 'vessel.process)
    (run!)))
