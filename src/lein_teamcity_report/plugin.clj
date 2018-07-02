(ns lein-teamcity-report.plugin
  (:require [lein-teamcity-report.core :as report]
            leiningen.core.main
            leiningen.jar
            leiningen.test
            leiningen.uberjar
            robert.hooke))

(defn hooks []
  (robert.hooke/add-hook #'leiningen.test/form-for-testing-namespaces
                         report/add-teamcity-test-reporting)
  (robert.hooke/add-hook #'leiningen.jar/write-jar
                         report/add-teamcity-jar-artifact-reporting)
  (robert.hooke/add-hook #'leiningen.uberjar/uberjar
                         report/add-teamcity-uberjar-artifact-reporting)
  (robert.hooke/add-hook #'leiningen.core.main/apply-task
                         report/add-teamcity-task-reporting))
