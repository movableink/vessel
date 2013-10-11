(ns vessel.launch
  (:require [vessel.provision :as provision]
            [vessel.manifest  :as manifest]))

(defn -main
  "Launch an EC2 Vessel"
  [manifest-file batch-name]
  (do
    (in-ns 'vessel.manifest)
    (manifest/reset-all! manifest-file batch-name)
    (in-ns 'vessel.launch)
    (provision/launch)
    (System/exit 0)))
