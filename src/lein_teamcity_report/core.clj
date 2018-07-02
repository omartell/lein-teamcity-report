(ns lein-teamcity-report.core
  (:require [clojure.test]
            [clojure.string :as str]))

(defn escape-val [s]
  (str/replace s #"['|\n\r\[\]]"
               (fn [x]
                 (cond (= x "\n") "|n"
                       (= x "\r") "|r"
                       :else (str "|" x)))))

(defn tc-msg-attrs [attrs]
  (if (seq (rest attrs))
    (->> attrs
         (partition 2)
         (map (fn [[n v]] (str (name n) "='" (escape-val v) "'")))
         (str/join " "))
    (str "'" (first attrs) "'")))

(defn tc-msg [message & attrs]
  (str "##teamcity[" (name message) " " (tc-msg-attrs attrs) "]"))

(defn add-teamcity-jar-artifact-reporting [f & [_ out-file :as args]]
  (apply f args)
  (println (tc-msg :publishArtifacts out-file)))

(defn add-teamcity-uberjar-artifact-reporting [f & args]
  (let [artifact (apply f args)]
    (println (tc-msg  :publishArtifacts artifact))))

(defn add-teamcity-task-reporting [f & [name :as args]]
  (println (tc-msg :blockOpened :name name))
  (apply f args)
  (println (tc-msg :blockClosed :name name)))

(defn add-teamcity-test-reporting [f & args]
  `(do
     (require '[lein-teamcity-report.core])
     (binding [clojure.test/report report]
       ~(apply f args))))

(defn test-name []
  (let [names (reverse (map #(:name (meta %)) clojure.test/*testing-vars*))]
    (if (= (count names) 1)
      (str (first names))
      (str names))))

(defn fail-msg [event]
  (str (if (:message event) (str :message " " (:message event) "\n") "")
       :expected " " (:expected event) "\n"
       :actual " " (:actual event)))

(defn escape-fail-msg [s]
  (-> s
      (.replaceAll "\\|" "||")
      (.replaceAll "'"   "|'")
      (.replaceAll "\n"  "|n")
      (.replaceAll "\r"  "|r")
      (.replaceAll "\\[" "|[")
      (.replaceAll "\\]" "|]")))

(def original-test-report
  clojure.test/report)

(defmulti report :type)

(defmethod report :default [m]
  (original-test-report m))

(defmethod report :begin-test-ns [m]
  (println (str "##teamcity[testSuiteStarted name='" (ns-name (:ns m)) "']"))
  (original-test-report m))

(defmethod report :end-test-ns [m]
  (original-test-report m)
  (println (str "##teamcity[testSuiteFinished name='" (ns-name (:ns m)) "']")))

(defmethod report :begin-test-var [m]
  (println (str "##teamcity[testStarted name='" (test-name)  "' captureStandardOutput='true']"))
  (original-test-report m))

(defmethod report :end-test-var [m]
  (original-test-report m)
  (println (str "##teamcity[testFinished name='" (test-name) "']")))

(defmethod report :fail [m]
  (original-test-report m)
  (println (str "##teamcity[testFailed name='" (test-name) "' message='" (escape-fail-msg (fail-msg m)) "']")))

(defmethod report :error [m]
  (original-test-report m)
  (println (str "##teamcity[testFailed name='" (test-name) "' message='" (escape-fail-msg (fail-msg m)) "']")))
