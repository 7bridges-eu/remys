(ns remys.core
  (:require [clojure.string :as string]
            [clojure.tools.cli :as cli])
  (:gen-class))

(def cli-options
  [["-h" "--host"
    :default "localhost"]
   ["-u" "--user"]
   ["-p" "--password"]
   ["-d" "--database"]])

(defn usage
  [options-summary]
  (->> ["remys - Rest APIs for MySQL databases"
        ""
        "Usage: remys [options] action"
        ""
        "Options:"
        options-summary
        ""
        "Actions:"
        "  start   Start server"
        "  stop    Stop server"]
       (string/join \newline)))

(defn error-msg
  [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn validate-args
  "Validate command line arguments. Either return a map indicating the program
  should exit (with a error message, and optional ok status), or a map
  indicating the action the program should take and the options provided."
  [args]
  (let [{:keys [options arguments errors summary]}
        (cli/parse-opts args cli-options)]
    (cond
      errors ; errors => exit with description of errors
      {:exit-message (error-msg errors)}
      ;; custom validation on arguments
      (and (= 1 (count arguments))
           (#{"start" "stop"} (first arguments)))
      {:action (first arguments) :options options}
      :else ; failed custom validation => exit with usage summary
      {:exit-message (usage summary)})))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn -main [& args]
  (let [{:keys [action options exit-message ok?]} (validate-args args)]
    (if exit-message
      (exit (if ok? 0 1) exit-message)
      (case action
        "start" (println "Starting server...")
        "stop"  (println "Stopping server...")))))
