(ns vessel.manifest
  (require [pallet.configure]
           [clojure.tools.reader.edn :as edn]
           [clojure.tools.reader     :as r]
           [clojure.string           :as string]
           [clojure.core.memoize     :as memo]
           [me.raynes.fs             :as fs])
  (:use clj-time.format))

(def file-path
  (atom ""))

(defn reset-file-path!
  [path]
  (reset! file-path path))

(defn file
  []
  (if @file-path (fs/normalized-path @file-path)))

(defn raw
  "Read config string from config-file"
  []
  (cond
    (.isFile (file)) (string/trim (slurp (file)))
    :else            "{}"))

(def config
  (atom {}))

(defn reset-config!
  []
  (reset! config (edn/read-string (raw))))

(def batch
  "Batch to be processed"
  (atom nil))

(defn set-batch!
  "Reset batch to be processed"
  [batch-name]
  (swap! config assoc :batch batch-name))

(defn reset-batch!
  "Reset batch to be processed"
  []
  (reset! batch (@config :batch)))

(defn map-str
  [input]
  (map #(if (keyword? %) (@config %) %) input))

(defn join-str
  [input]
  (apply str (map-str input)))

(def env
  "Vessel environment"
  (atom "development"))

(defn reset-env!
  "Vessel environment"
  []
  (reset! env (@config :env)))

(defn pallet-config
  "Pallet configuration map set in ~/.pallet/services/{:pallet-config}.clj"
  []
  (((pallet.configure/pallet-config) :services) (@config :pallet-config)))

(def cred
  "AWS authentication credentials"
  (atom { :access-key nil
         :secret-key nil }))

(defn reset-cred!
  "AWS authentication credentials"
  []
  (reset! cred
    { :access-key ((pallet-config) :identity)
      :secret-key ((pallet-config) :credential) }))

(def bucket
  "S3 bucket"
  (atom ""))

(defn reset-bucket!
  "S3 bucket"
  []
  (reset! bucket (@config :input-bucket)))

(defn deploy-version
  []
  "Vessel version to deploy"
  (@config :deploy-version))

(defn deploy-jar
  "Vessel JAR to deploy"
  []
  (join-str (@config :deploy-jar)))

(defn deploy-jar-source
  "Vessel JAR source key on S3"
  []
  (apply str
    (interpose "/"
      (flatten ["s3:" ""
                (map-str (@config :deploy-jar-source))
                (deploy-jar)] ))))

(defn deploy-jar-local-path
  []
  (apply str
    (interpose "/"
      [(@config :deploy-jar-local-dir ".") (deploy-jar)])))

(defn deploy-jvm-memory
  "Memory in GBs to allocate to JVM (default: 6)"
  []
  (@config :deploy-jvm-memory 6))

(defn log-destination
  "Log destination on S3"
  []
  (apply str
    (interpose "/"
      (flatten ["s3:" "" (map-str (@config :log-destination))]))))

(defn processing-dir
  "Local processing directory"
  []
  (@config :processing-dir))

(def input-dir
  "Local input directory"
  (atom "data/input"))

(defn reset-input-dir!
  "Reset Local input directory"
  []
  (reset! input-dir (@config :input-dir)))

(def input-prefix
  "Path to remote & local input files"
  (atom ""))

(defn reset-input-prefix!
  "Path to remote & local input files"
  []
  (reset! input-prefix
    (apply str (interpose "/" (map-str (@config :input-prefix))))))

(def input-file-prefix
  "Path to remote & local input files"
  (atom ""))

(defn reset-input-file-prefix!
  "Path to remote & local input files"
  []
  (reset! input-file-prefix (@config :input-file-prefix)))

(def output-dir
  "Local output directory"
  (atom ""))

(defn reset-output-dir!
  "Local output directory"
  []
  (reset! output-dir (@config :output-dir)))

(def input-row
  "Ordered column names of input rows"
  (atom []))

(defn reset-input-row!
  []
  (reset! input-row 
    (vec (flatten (map keys (@config :input-row))))))

(defn output-cols-indexes
  []
  (remove #(nil? (first (vals %))) (@config :input-row)))

(defn output-cols-sorted
  []
  (sort-by #(first (vals %)) (output-cols-indexes)))

(defn output-cols-fn
  []
  (vec (map #(first (keys %)) (output-cols-sorted))))

(def output-row
  "Ordered selection of input rows to be included in output CSV"
  (atom []))

(defn reset-output-row!
  "Ordered selection of input rows to be included in output CSV"
  []
  (reset! output-row (output-cols-fn)))

(defn deploy-hardware-id
  "EC2 instance type: m1.large (default) or m1.xlarge etc..."
  []
  (@config :deploy-hardware-id "m1.large"))

(def output-indexes
  (atom []))

(defn reset-output-indexes!
  []
  (reset! output-indexes
    (apply vector (map #(.indexOf @input-row %) @output-row))))

(def header-row
  (atom []))

(defn reset-header-row!
  []
  (reset! header-row (vec (map name @output-row))))

(defn output-path [cols file-path file-key]
  (str @output-dir "/" file-key ".csv"))

(defn reset-ffns!
  []
  (doall (map #(eval %) (@config :ffns))))

(def col-fns
  {})

(def input-delimiter
  "\t")

(defn col-ffns
  []
  (vec (map #(col-fns % (fn [x] str x)) @output-row)))

(def col-ffns
  (atom (vec (map #(col-fns % (fn [x] str x)) @output-row))))

(defn reset-col-ffns!
  []
  (reset! col-ffns (vec (map #(col-fns % (fn [x] str x)) @output-row))))

(defn apply-col-ffns
  [cols]
  (map-indexed (fn [idx itm] ((nth @col-ffns idx) itm)) cols))

(defn apply-col-non-ffns
  [cols]
  cols)

(defn reset-all!
  "Reset all configuration values"
  [path batch-name]
  (do
    (reset-file-path! path)
    (reset-config!)
    (set-batch! batch-name)
    (reset-env!)
    (reset-cred!)
    (reset-bucket!)
    (reset-batch!)
    (reset-input-dir!)
    (reset-input-prefix!)
    (reset-input-file-prefix!)
    (reset-output-dir!)
    (reset-input-row!)
    (reset-output-row!)
    (reset-output-indexes!)
    (reset-header-row!)
    (reset-ffns!)
    (reset-col-ffns!)
    @config))

(defn -main
  "Set configuration file path"
  [path batch-name]
  (do
    (in-ns 'vessel.manifest)
    (reset-all! path batch-name)))
