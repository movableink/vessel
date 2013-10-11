(ns vessel.perform
  "Perform a single batch defined in manifest.edn and decommission"
  (:require [vessel.manifest  :as manifest]
            [vessel.ship      :as ship]
            [vessel.log       :as log]
            [vessel.provision :as provision]))

(defn -main
  "Perform a batch and decommission"
  [manifest-file batch-name]
  (do
    (in-ns 'vessel.manifest)
    (manifest/reset-all! manifest-file batch-name)
    (in-ns 'vessel.perform)
    (ship/run!)
    (log/put-s3!)
    (provision/decommission)
    (System/exit 0)))
