(ns remys.core
  (:require [clojure.tools.cli :as cli])
  (:gen-class))

(def cli-options
  [["-h" "--help"]])

(defn -main [& args]
  (cli/parse-opts args cli-options))
