(ns vessel.ship
  "Fetch input, process, and put output"
  (:require [vessel.input    :as input]
            [vessel.process  :as process]
            [vessel.output   :as output]
            [vessel.manifest :as manifest]))

(defn run!
  []
  (do
    (input/get-all!)
    (process/run!)
    (output/put-all!)))

(defn -main
  "Fetch input, process, and put output"
  [manifest-file batch-name]
  (do
    (in-ns 'vessel.manifest)
    (manifest/reset-all! manifest-file batch-name)
    (in-ns 'vessel.ship)
    (run!)))
