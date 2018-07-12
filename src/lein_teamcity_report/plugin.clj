(ns lein-teamcity-report.plugin
  (:require [lein-teamcity-report.core :as report]
            [leiningen.core.project :as project]
            leiningen.core.main
            leiningen.jar
            leiningen.test
            leiningen.uberjar
            robert.hooke))

(defn hooks []
  (robert.hooke/add-hook #'leiningen.jar/write-jar
                         report/add-teamcity-jar-artifact-reporting)
  (robert.hooke/add-hook #'leiningen.uberjar/uberjar
                         report/add-teamcity-uberjar-artifact-reporting)
  (robert.hooke/add-hook #'leiningen.core.main/apply-task
                         report/add-teamcity-task-reporting))

(defn add-profile [project profile]
  (if (some #{profile} (-> project meta :included-profiles))
    project
    (project/merge-profiles project [profile])))

(defn middleware [project]
  (add-profile project {:injections `[(require '[lein-teamcity-report.core])
                                      (require '[robert.hooke])
                                      (robert.hooke/add-hook #'clojure.test/report
                                                             (fn [f# & [m#]]
                                                               (apply lein-teamcity-report.core/report
                                                                      [(merge m# {:lein-teamcity-report.core/f f#})])))]
                        :dependencies [['robert/hooke "1.3.0"]
                                       ['lein-teamcity-report "0.3.0-SNAPSHOT"]]}))
