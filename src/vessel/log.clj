(ns vessel.log
  (:require [taoensso.timbre :as timbre]
            [clojure.java.io :as io]
            [aws.sdk.s3      :as s3]
            [vessel.manifest :as manifest]))

(timbre/set-config! [:timestamp-pattern]
                    "yyyy-MM-dd HH:mm:ss ZZ")

(timbre/set-config! [:appenders :spit :enabled?]
                    true)

(timbre/set-config! [:shared-appender-config :spit-filename]
                    "logs/vessel.log")

(defn info  [arg] (timbre/info  arg))
(defn error [arg] (timbre/error arg))

(defn put-s3!
  "Put Vessel log to S3"
  []
  (let [s3-key (str "vessel/batches/"
                    @manifest/env "/"
                    @manifest/batch "/"
                    "logs/vessel.log")
        content (io/file "logs/vessel.log")]
    (do
      (info "put vessel.log on s3")
      (s3/put-object @manifest/cred @manifest/bucket s3-key content))))

(defn -main
  "Put Vessel log to S3"
  [manifest-file batch-name]
  (do
    (in-ns 'vessel.manifest)
    (manifest/reset-all! manifest-file batch-name)
    (in-ns 'vessel.log)
    (put-s3!)))
