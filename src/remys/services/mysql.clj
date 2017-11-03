(ns remys.services.mysql
  (:require [clj-time.coerce :as c]
            [clj-time.format :as f]
            [clojure.java.jdbc :as jdbc]
            [clojure.string :as string]
            [hikari-cp.core :as hikari]
            [mount.core :as mount]))

(extend-protocol jdbc/IResultSetReadColumn
  java.sql.Date
  (result-set-read-column [col _ _]
    (c/to-long col))

  java.sql.Timestamp
  (result-set-read-column [col _ _]
    (c/to-long col)))

(defn column-type
  "For `table` in `schema`, get `column` type."
  [schema table column]
  (-> #(= (:column-name %) column)
      (filter (get schema table))
      first
      :column-type))

(defn date?
  [schema table column]
  (= "date" (column-type schema table column)))

(defn datetime?
  [schema table column]
  (= "datetime" (column-type schema table column)))

(defn timestamp?
  [schema table column]
  (= "timestamp" (column-type schema table column)))

(defn format-value
  "Coerce `value` to the needed type for the `column` in `table`."
  [schema table column value]
  (cond
    (date? schema table column) (->> (c/from-long value)
                                     (f/unparse (f/formatters :mysql)))
    (datetime? schema table column) (->> (c/from-long value)
                                         (f/unparse (f/formatters :mysql)))
    (timestamp? schema table column) (->> (c/from-long value)
                                          (f/unparse (f/formatters :mysql)))
    :else value))

(defn- make-datasource-options
  "Set up the necessary parameters in `options` to connect to a MySQL database."
  [options]
  (let [{:keys [hostname port username password database]} options]
    {:auto-commit        true
     :read-only          false
     :connection-timeout 30000
     :validation-timeout 5000
     :idle-timeout       600000
     :max-lifetime       1800000
     :minimum-idle       10
     :maximum-pool-size  10
     :pool-name          "db-pool"
     :adapter            "mysql"
     :username           username
     :password           password
     :database-name      database
     :server-name        hostname
     :port-number        port}))

(defn connect!
  "Create hikari datasource with the given `options`."
  [options]
  (-> (make-datasource-options options)
      (hikari/make-datasource)))

(defn disconnect!
  "Close the datasource."
  [datasource]
  (hikari/close-datasource datasource))

(defn init-database-connection
  "Create the database component with the given `options`."
  [options]
  (mount/defstate datasource
    :start (connect! options)
    :stop (disconnect! datasource)))

(defn snake-case->kebab-case
  [column]
  (when (keyword? column)
    (keyword (string/replace (name column) #"_" "-"))))

(defn kebab-case->snake-case
  [column]
  (when (keyword? column)
    (keyword (string/replace (name column) #"-" "_"))))

(defn format-input-keywords
  "Convert `input` keywords from kebab-case to snake_case."
  [input]
  (reduce-kv
   (fn [m k v]
     (assoc m (kebab-case->snake-case k) v))
   {}
   input))

(defn format-output-keywords
  "Convert `output` keywords from snake_case to kebab-case."
  [output]
  (reduce-kv
   (fn [m k v]
     (assoc m (snake-case->kebab-case k) v))
   {}
   output))

(defn query!
  "Execute a SELECT statement using the SQL in `query-sql`."
  [query-sql]
  (jdbc/with-db-connection [conn {:datasource datasource}]
    (map format-output-keywords (jdbc/query conn query-sql))))

(defn insert!
  "Insert `values` into `table`.
  `table` is a keyword (i.e.: :categories).
  `values` is a map of columns and values (i.e.: {:name \"Test\"})."
  [table values]
  (jdbc/with-db-connection [conn {:datasource datasource}]
    (let [input-values (format-input-keywords values)]
      (jdbc/insert! conn table input-values))))

(defn update!
  "Execute an update using the SQL in `query-sql`.
  `query-sql` is a vector with the SQL statement and the necessary parameters."
  [query-sql]
  (jdbc/with-db-connection [conn {:datasource datasource}]
    (jdbc/execute! conn query-sql)))

(defn delete!
  "Tiny wrapper around `update!`, just syntactic sugar."
  [query-sql]
  (update! query-sql))

(defn schema!
  "Load useful informations on all the tables in `db`."
  [db]
  (let [query (str "select c.table_name, c.column_name, c.ordinal_position,
                    c.column_key, c.data_type, c.column_type, c.extra
                    from information_schema.columns as c
                    where c.table_schema = '" db
                   "' order by c.table_name, c.ordinal_position")]
    (group-by :table-name (query! query))))

(def schema (atom {}))

(defn load-schema
  "Load in memory the schema identified by :database in `options`."
  [options]
  (->> (:database options)
       schema!
       (reset! schema)))
