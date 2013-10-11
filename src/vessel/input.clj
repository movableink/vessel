(ns vessel.input
  (:require [me.raynes.fs    :as fs]
            [clojure.java.io :as io]
            [clojure.string  :as string]
            [aws.sdk.s3      :as s3]
            [vessel.util     :as util]
            [vessel.log      :as log]
            [vessel.manifest :as manifest]))

(defn local
  []
  (util/file-list @manifest/input-dir))

(defn remote []
  (s3/list-objects
    @manifest/cred
    @manifest/bucket
    { :prefix @manifest/input-prefix }))

(defn remote-input-files
  []
  (vec (map :key ((remote) :objects))))

(defn local-input-dir [path]
  (apply str (interpose "/" (cons @manifest/input-dir (butlast (fs/split path))))))

(defn local-input-path [key]
  (str (local-input-dir key) "/" (fs/base-name key)))

(defn get-remote [key]
  (log/info (str "get input" " " (util/show-url @manifest/bucket key)))
  (let [resp (s3/get-object @manifest/cred @manifest/bucket key)]
    (util/mkdir (local-input-dir key))
    (io/copy
      (-> resp :content)
      (io/file (local-input-path key)))))

(defn get-all!
  "Get all input for the configured batch"
  []
  (do
    (log/info "start fetching input")
    (doall (pmap get-remote (remote-input-files)))
    (log/info "input fetched")))

(defn -main
  "Get all input for a manifest & batch"
  [manifest-file batch-name]
  (do
    (in-ns 'vessel.manifest)
    (manifest/reset-all! manifest-file batch-name)
    (in-ns 'vessel.input)
    (get-all!)))
