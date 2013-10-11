(ns vessel.deploy
  "Launch an EC2 Vessel to perform a single batch process"
  (:require [vessel.provision :as provision]
            [vessel.manifest  :as manifest]))

(defn -main
  "Launch an EC2 Vessel to perform a single batch process"
  [manifest-file batch-name]
  (do
    (in-ns 'vessel.manifest)
    (manifest/reset-all! manifest-file batch-name)
    (in-ns 'vessel.deploy)
    (provision/launch)
    (System/exit 0)))
