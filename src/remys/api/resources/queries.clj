(ns remys.api.resources.queries
  (:require [clojure.string :as string]
            [remys.services.mysql :as db]))

(defn show-tables
  "Show all the tables in `schema`."
  [schema]
  (keys schema))

(defn describe-table
  "Describe the `table` in `schema`."
  [schema table]
  (get schema table))

(defn table-exists?
  "Check if `table` exists in the `schema`."
  [schema table]
  (->> (keys schema)
       (some #{table})
       nil?
       not))

(defn primary-key
  "Find the primary key of `table`."
  [schema table]
  (->> (get schema table)
       (filter #(= (:column-key %) "PRI"))
       (map :column-name)))

(defn escape-string
  "Enclose `s` in single quotes if `s` is a string. Useful for SQL."
  [s]
  (if (string? s)
    (str "'" s "'")
    s))

(defn query-all
  "Select all columns of all the records in `table`."
  [table]
  (-> (str "select * from " table)
      (db/query!)))

(defn query-by-key
  "Query `table` in `schema` by its primary key `id`."
  [schema table id]
  (let [pk (first (primary-key schema table))]
    (->> (escape-string id)
         (str "select * from " table " where " pk " = ")
         (db/query!))))

(defn create-where
  "Create the where conditions linking `pks` to `values.`
  E.g.: id = 1 and name = 'test'"
  [pks values]
  (->> (map escape-string values)
       (map #(str %1 " = " %2) pks)
       (interpose " and ")
       (apply str)))

(defn query-by-composite-key
  "Query the `table` in `schema` using its composite primary key `ids`.
  `ids` is a string where the values are separated by three underscores."
  [schema table ids]
  (let [pks (primary-key schema table)
        values (string/split ids #"___")
        where-cond (create-where pks values)]
    (-> (str "select * from " table " where " where-cond)
        (db/query!))))

(defn query-fields
  "Query `table` selecting only the given `fields`.
  `fields` is a string with comma-separated values."
  [table fields]
  (let [fs (->> (string/split fields #",")
                (map #(string/replace % #"-" "_"))
                (interpose ",")
                (apply str))]
    (-> (str "select " fs " from " table)
        (db/query!))))

(defn valid-query?
  "Check if the query contains create/delete/drop/update instructions."
  [query]
  (nil? (re-matches #"(?i)^.*(create|delete|drop|update).*" query)))

(defn format-params
  "Format `params` interposing 'and' between them."
  [params]
  (->> (interpose " and " params)
       (apply str)))

(defn execute-query
  "Execute `query` with `params`, if they are present."
  [query params]
  (if (empty? params)
    (db/query! query)
    (->> (format-params params)
         (str query " where ")
         (db/query!))))
