(ns vessel.decommission
  (:require [vessel.provision :as provision]
            [vessel.manifest  :as manifest]))

(defn -main
  "Decommission an EC2 Vessel"
  [manifest-file batch-name]
  (do
    (in-ns 'vessel.manifest)
    (manifest/reset-all! manifest-file batch-name)
    (in-ns 'vessel.decommission)
    (provision/decommission)
    (System/exit 0)))
