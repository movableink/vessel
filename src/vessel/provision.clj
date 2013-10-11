(ns vessel.provision
  (:require [pallet.core]
            [pallet.compute]
            [pallet.configure]
            [pallet.crate.java]

            [pallet.action.user             :as user]
            [pallet.action.directory        :as directory]
            [pallet.action.file             :as file]
            [pallet.action.remote-directory :as remote-directory]
            [pallet.action.remote-file      :as remote-file]
            [pallet.action.exec-script      :as exec-script]

            [me.raynes.fs :as fs]

            [vessel.log      :as log]
            [vessel.manifest :as manifest])

  (:use [pallet.crate.automated-admin-user :only [automated-admin-user]]
        [pallet.action.package             :only [package package-manager]]
        [pallet.phase                      :only [phase-fn]]))

(defn compute-service
  []
  (pallet.compute/compute-service ((manifest/pallet-config) :provider)
   :identity   ((manifest/pallet-config) :identity)
   :credential ((manifest/pallet-config) :credential)))

(defn nodes []
  (pallet.compute/nodes (compute-service)))

(defn launch
  "Launch an EC2 instance"
  []
  (do
    (log/info "launch vessel")
    (pallet.core/converge
      (pallet.core/group-spec (str "vessel-" @manifest/batch)
        :count 1
        :node-spec (pallet.core/node-spec

          :image    { :os-family     :ubuntu
                      :image-id      "us-east-1/ami-d726abbe"
                      :location-id   "us-east-1a"
                      :inbound-ports [22] }

          :hardware { :hardware-id   (manifest/deploy-hardware-id) })

        :phases {
          :bootstrap automated-admin-user
          :configure
            (phase-fn

              (user/group "deploy" :system true)
              (user/user  "deploy" :system      true
                                   :create-home true
                                   :home        "/home/deploy"
                                   :shell       "/bin/bash"
                                   :group       "deploy")

              (directory/directory "/home/deploy/"
                                   :owner "deploy"
                                   :group "deploy"
                                   :mode  "0755")

              (directory/directory "/home/deploy/.pallet/services/"
                                   :owner "deploy"
                                   :group "deploy"
                                   :mode  "0755")

              (remote-file/remote-file
                (str "/home/deploy/.pallet/services/"
                     (name (@manifest/config :pallet-config)) ".clj")
                :owner      "deploy"
                :group      "deploy"
                :mode       "0400"
                :content    { (@manifest/config :pallet-config)
                              (manifest/pallet-config) })

              (directory/directory "/mnt/vessel"
                                   :owner "deploy"
                                   :group "deploy"
                                   :mode  "0755")

              (directory/directory "/mnt/vessel/logs"
                                   :owner "deploy"
                                   :group "deploy"
                                   :mode  "0755")

              (remote-file/remote-file
                "/mnt/vessel/manifest.edn"
                :owner      "deploy"
                :group      "deploy"
                :mode       "0644"
                :content    @manifest/config)

              (package-manager :update)

              (exec-script/exec-script
                (apply (str
                "sudo aptitude install "
                "-o Aptitude::Cmdline::ignore-trust-violations=true "
                "-q -y openjdk-7-jdk+")))

              ;;(package "s3cmd")

              ;;(remote-file/remote-file
              ;;  "/home/deploy/.s3cfg"
              ;;  :owner      "deploy"
              ;;  :group      "deploy"
              ;;  :mode       "0400"
              ;;  :local-file (fs/expand-home "~/.s3cfg"))

              ;;(exec-script/exec-checked-script
              ;;  "Fetch Vessel standalone JAR"
              ;;  "cd /mnt/vessel"
              ;;  (apply (str
              ;;    "sudo -u deploy -H"
              ;;    " "
              ;;    "s3cmd"
              ;;    " "
              ;;    "--no-progress"
              ;;    " "
              ;;    "get"
              ;;    " "
              ;;    ~(manifest/deploy-jar-source))))

              (remote-file/remote-file
                (str "/mnt/vessel/" (manifest/deploy-jar))
                :owner      "deploy"
                :group      "deploy"
                :mode       "0400"
                :local-file (fs/normalized-path
                              (manifest/deploy-jar-local-path)))

              (exec-script/exec-script
                (apply (str
                  "cd /mnt/vessel &&"
                  " "
                  "sudo -u deploy -H"
                  " "
                  "nohup"
                  " "
                  "java -Xmx"
                  ~(manifest/deploy-jvm-memory)
                  "g -cp"
                  " "
                  ;;"vessel.jar"
                  ~(manifest/deploy-jar)
                  " "
                  "clojure.main -m vessel.perform"
                  " "
                  "/mnt/vessel/manifest.edn"
                  " "
                  ~(deref manifest/batch)
                  " "
                  "2>&1 &")))
              )})

        ;;:extends [(pallet.crate.java/java {:vendor :openjdk})])

      :compute (pallet.configure/compute-service
                 (@manifest/config :pallet-config)))

    (log/info "vessel launched")))

(defn decommission
  "Decommission an EC2 instance"
  []
  (do
    (log/info "decommission vessel")
    (pallet.core/converge
      (pallet.core/group-spec (str "vessel-" @manifest/batch) :count 0)
      :compute (pallet.configure/compute-service
                 (@manifest/config :pallet-config)))
    (log/info "vessel decommission")))

(defn -main
  []
  (do
    (launch)
    (System/exit 0)))
