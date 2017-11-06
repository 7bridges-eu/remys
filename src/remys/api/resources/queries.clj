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

(defn primary-key
  "Find the primary key of `table`."
  [schema table]
  (->> (get schema table)
       (filter #(= (:column-key %) "PRI"))
       (map :column-name)))

(defn wrap-string
  "Wrap `s` in single quotes if `s` is a string. Useful for SQL."
  [s]
  (if (string? s)
    (str "'" s "'")
    s))

(defn count-records
  "Return the number of records in `table`."
  [table]
  (-> (str "select count(*) as records from " table)
      (db/query!)))

(defn query-all
  "Select all columns of all the records in `table`."
  [table]
  (-> (str "select * from " table)
      (db/query!)))

(defn query-by-key
  "Query `table` in `schema` by its primary key `id`."
  [schema table id]
  (let [pk (first (primary-key schema table))]
    (->> (wrap-string id)
         (str "select * from " table " where " pk " = ")
         (db/query!))))

(defn create-where
  "Create the where conditions linking `pks` to `values.`
  E.g.: id = 1 and name = 'test'"
  [pks values]
  (->> (map wrap-string values)
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

(defn format-params
  "Format `params` map as where conditions."
  [params]
  (let [ks (->> (keys params) (map name) (map #(string/replace % #"-" "_")))
        vs (->> (vals params) (map wrap-string))]
    (->> (map #(str %1 " = " %2) ks vs)
         (interpose " and ")
         (apply str))))

(defn execute-query
  "Execute `query` with `params`, if they are present."
  ([query]
   (db/query! query))
  ([query params]
   (if (empty? params)
     (db/query! query)
     (->> (format-params params)
          (str query " where ")
          (db/query!)))))

(defn format-column-value
  "Convert `value` in format suitable for MySQL."
  [schema table column value]
  (if (number? value)
    (-> (db/format-value schema table column (.longValue value))
        (wrap-string))
    (wrap-string value)))

(defn params->mysql-params
  "Convert `params` in a format suitable for MySQL."
  [schema table params]
  (reduce-kv
   (fn [m k v]
     (let [column (-> (name k) (string/replace #"-" "_"))
           value (format-column-value schema table column v)]
       (assoc m column value)))
   {}
   params))

(defn format-update-params
  "Format `params` map as update set values."
  [schema table params]
  (let [mysql-params (params->mysql-params schema table params)
        ks (keys mysql-params)
        vs (vals mysql-params)]
    (->> (map #(str %1 " = " %2) ks vs)
         (interpose ",")
         (apply str))))

(defn update-table
  "Update `table` in `schema`, setting the values in `params` on the record
  identified by the primary key `id`."
  [schema table id params]
  (let [pk (first (primary-key schema table))
        vals (format-update-params schema table params)
        query (str "update " table " set " vals " where " pk " = " id)]
    (db/update! [query])))
