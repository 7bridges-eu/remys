(ns remys.core
  (:require [clojure.string :as string]
            [clojure.tools.cli :as cli])
  (:gen-class))

(def cli-options
  [["-H" "--hostname HOST"
    :default "localhost"]
   ["-P" "--port PORT"
    :default 3306
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 65536) "Must be a number between 0 and 65536"]]
   ["-u" "--username USERNAME"]
   ["-p" "--password PASSWORD"]
   ["-d" "--database DATABASE"]])

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

(defn- print-msg
  [msg errors]
  (str msg (string/join \newline errors)))

(defn error-msg
  [errors]
  (print-msg "The following errors occurred while parsing your command:\n\n"
             errors))

(defn validation-msg
  [errors]
  (print-msg "Mandatory parameters missing:\n\n" errors))

(defn check-username
  [errors options]
  (when (nil? (get options :username nil))
    (conj errors "Please specify a USERNAME with `-u` or `--username`")))

(defn check-password
  [errors options]
  (when (nil? (get options :password nil))
    (conj errors "Please specify a PASSWORD with `-p` or `--password`")))

(defn check-database
  [errors options]
  (when (nil? (get options :database nil))
    (conj errors "Please specify a DATABASE with `-d` or `--databasea`")))

(defn check-mandatory-options
  [options]
  (-> []
      (check-username options)
      (check-password options)
      (check-database options)))

(defn validate-args
  "Validate command line arguments. Either return a map indicating the program
  should exit (with a error message, and optional ok status), or a map
  indicating the action the program should take and the options provided."
  [args]
  (let [{:keys [options arguments errors summary]}
        (cli/parse-opts args cli-options)
        errors (check-mandatory-options options)]
    (if (empty? errors)
      (cond
       errors ; errors => exit with description of errors
       {:exit-message (error-msg errors)}
       ;; custom validation on arguments
       (and (= 1 (count arguments))
            (#{"start" "stop"} (first arguments)))
       {:action (first arguments) :options options}
       :else ; failed custom validation => exit with usage summary
       {:exit-message (usage summary)})
      {:exit-message (validation-msg errors)})))

(defn exit
  [msg]
  (println msg)
  (System/exit 0))

(defn start-server!
  [options]
  (println "Starting server...")
  (println options))

(defn stop-server!
  []
  (println "Stopping server..."))

(defn -main [& args]
  (let [{:keys [action options exit-message]} (validate-args args)]
    (if exit-message
      (exit exit-message)
      (case action
        "start" (start-server! options)
        "stop"  (stop-server!)))))
