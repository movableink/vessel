(ns vessel.output
  (:require [me.raynes.fs    :as fs]
            [clojure.java.io :as io]
            [clojure.string  :as string]
            [aws.sdk.s3      :as s3]
            [vessel.log      :as log]
            [vessel.util     :as util]
            [vessel.manifest :as manifest]))

(defn local
  []
  (util/file-list @manifest/output-dir))

(defn paths-from-parts
  [parts]
  (for [file-name (parts 2)]
    (let [dir-path (str (parts 0))]
      (str dir-path "/" file-name))))

(defn local-paths
  []
  (doall (map paths-from-parts (local))))

(defn make-key
  [path]
  (str @manifest/env
       "/"
       (last (string/split path
                (re-pattern (str "/" @manifest/output-dir "/"))))))

(defn put-remote
  [path]
  (let [s3-key (make-key path)
        content (io/file path)]
      (log/info (str "put output" " " s3-key))
      (s3/put-object @manifest/cred @manifest/bucket s3-key content
        {:content-disposition (str "attachment;"
                                   " "
                                   "filename=\""
                                   (fs/base-name s3-key) "\"")})))

(defn put-all!
  "Put all output to S3"
  []
  (do
    (log/info "start putting output")
    (doall (pmap put-remote (flatten (local-paths))))
    (log/info "output done")))

(defn -main
  "Put all input for a manifest & batch"
  [manifest-file batch-name]
  (do
    (in-ns 'vessel.manifest)
    (manifest/reset-all! manifest-file batch-name)
    (in-ns 'vessel.output)
    (put-all!)))
