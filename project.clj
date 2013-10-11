(defproject vessel "0.4.0-SNAPSHOT"
  :description "Deploys an EC2 instance to ship and process CSV files on S3"
  :url         "https://github.com/movableink/vessel"
  :license     { :name "Eclipse Public License"
                 :url "http://www.eclipse.org/legal/epl-v10.html" }
  :dependencies [[org.clojure/clojure      "1.4.0"]
                 [org.clojure/tools.reader "0.7.5"]

                 [org.cloudhoist/pallet         "0.7.3"]
                 [org.cloudhoist/pallet-jclouds "1.4.3"]

                 ;; To get started we include all jclouds compute providers.
                 ;; You may wish to replace this with the specific jclouds
                 ;; providers you use, to reduce dependency sizes.
                 [org.jclouds/jclouds-allblobstore "1.4.2"]
                 [org.jclouds/jclouds-allcompute   "1.4.2"]
                 [org.jclouds.driver/jclouds-slf4j "1.4.2"
                  ;; the declared version is old and can overrule the
                  ;; resolved version
                   :exclusions [org.slf4j/slf4j-api]]
                 [org.jclouds.driver/jclouds-sshj "1.4.2"]
                 [ch.qos.logback/logback-core     "1.0.0"]
                 [ch.qos.logback/logback-classic  "1.0.0"]

                 ;;[com.palletops/java-crate "0.8.0-beta.2"]
                 [org.cloudhoist/java "0.7.2"]

                 [clj-time                  "0.4.5"]
                 [me.raynes/fs              "1.4.0"]
                 [clj-aws-s3                "0.3.3"]
                 [org.clojure/tools.logging "0.2.6"]
                 [com.taoensso/timbre       "1.5.3"]
                 [org.clojure/data.csv      "0.1.2"]
                 [org.clojure/core.memoize  "0.5.6"]]

  :dev-dependencies [[org.cloudhoist/pallet "0.7.3" :type "test-jar"]
                     [org.cloudhoist/pallet-lein "0.5.2"]]
  :profiles {:dev
             {:dependencies
                [[org.cloudhoist/pallet "0.7.3" :classifier "tests"]
                 [criterium             "0.4.1"]]
              :plugins [[org.cloudhoist/pallet-lein "0.5.2"]]}
             :leiningen/reply
             {:dependencies
                [[org.slf4j/jcl-over-slf4j "1.7.2"]]
              :exclusions [commons-logging]}}
  :local-repo-classpath true
  :jvm-opts ["-Xmx4g" "-server"]
  :repositories
    { "sonatype-snapshots"
        "https://oss.sonatype.org/content/repositories/snapshots"
      "sonatype"
        "https://oss.sonatype.org/content/repositories/releases/" }
  :aot :all
  :main vessel.manifest)
