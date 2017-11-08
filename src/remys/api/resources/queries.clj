(ns remys.api.resources.queries
  (:require [clojure.string :as string]
            [remys.api.resources.format :as f]
            [remys.services.mysql :as db]))

(defn show-tables
  "Show all the tables in `schema`."
  [schema]
  (keys schema))

(defn describe-table
  "Describe the `table` in `schema`."
  [schema table]
  (get schema table))

(defn primary-key
  "Find the primary key of `table`."
  [schema table]
  (->> (get schema table)
       (filter #(= (:column-key %) "PRI"))
       (map :column-name)))

(defn count-records
  "Return the number of records in `table`."
  [table]
  (-> (str "select count(*) as records from " table)
      (db/query!)))

(defn query-all
  "Select all columns of all the records in `table`."
  [table]
  (-> (str "select * from " table " limit 20")
      (db/query!)))

(defn query-by-key
  "Query `table` in `schema` by its primary key `id`."
  [schema table id]
  (let [pk (first (primary-key schema table))]
    (->> (f/wrap-string id)
         (str "select * from " table " where " pk " = ")
         (db/query!))))

(defn query-by-composite-key
  "Query the `table` in `schema` using its composite primary key `ids`.
  `ids` is a string where the values are separated by three underscores."
  [schema table ids]
  (let [pks (primary-key schema table)
        values (string/split ids #"___")
        where-cond (f/create-where pks values)]
    (-> (str "select * from " table " where " where-cond)
        (db/query!))))

(defn query-by-fields
  "Query `table` selecting only the given `fields`.
  `fields` is a string with comma-separated values."
  [table fields]
  (let [fs (f/format-fields fields)]
    (-> (str "select " fs " from " table " limit 20")
        (db/query!))))

(defn query-by-size
  "Query `table` selecting `size` records."
  [table size]
  (-> (str "select * from " table " limit " size)
      (db/query!)))

(defn query-by-offset
  "Query `table` selecting the given `offset` from the results."
  [table offset]
  (-> (str "select * from " table " limit " offset ", 20")
      (db/query!)))

(defn query-by-fields-and-offset
  "Query `table` selecting only the given `fields` and `offset`.
  `fields` is a string with comma-separated values."
  [table fields offset]
  (let [fs (f/format-fields fields)]
    (-> (str "select " fs " from " table " limit " offset ", 20")
        (db/query!))))

(defn query-by-fields-size-and-offset
  "Query `table` selecting only the given `fields`, `size` and `offset`.
  `fields` is a string with comma-separated values."
  [table fields size offset]
  (let [fs (f/format-fields fields)]
    (-> (str "select " fs " from " table " limit " offset ", " size)
        (db/query!))))

(defn execute-query
  "Execute `query` with `params`, if they are present."
  ([query]
   (db/query! query))
  ([query params]
   (if (empty? params)
     (db/query! query)
     (->> (f/format-params params)
          (str query " where ")
          (db/query!)))))

(defn update-table
  "Update `table` in `schema`, setting the values in `params` on the record
  identified by the primary key `id`."
  [schema table id params]
  (let [pk (first (primary-key schema table))
        vals (f/format-update-params schema table params)
        query (str "update " table " set " vals " where " pk " = '" id "'")]
    (db/update! [query])))
