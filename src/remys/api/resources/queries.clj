(ns remys.api.resources.queries
  (:require [clojure.string :as string]
            [remys.services.mysql :as db]))

(defn show-tables
  [schema]
  (keys schema))

(defn describe-table
  [schema t]
  (get schema t))

(defn table-exists?
  "Check if `t` exists in the database."
  [schema t]
  (->> (keys schema)
       (some #{t})
       nil?
       not))

(defn primary-key
  "Find the primary key of the table `t`."
  [schema t]
  (->> (get schema t)
       (filter #(= (:column-key %) "PRI"))
       (map :column-name)))

(defn escape-string
  [s]
  (if (string? s)
    (str "'" s "'")
    s))

(defn query-all
  [table]
  (-> (str "select * from " table)
      (db/query!)))

(defn query-by-key
  [schema table id]
  (let [pk (first (primary-key schema table))]
    (->> (escape-string id)
         (str "select * from " table " where " pk " = ")
         (db/query!))))

(defn create-where
  [pks values]
  (->> (map escape-string values)
       (map #(str %1 " = " %2) pks)
       (interpose " and ")
       (apply str)))

(defn query-by-composite-key
  [schema table ids]
  (let [pks (primary-key schema table)
        values (string/split ids #"___")
        where-cond (create-where pks values)]
    (-> (str "select * from " table " where " where-cond)
        (db/query!))))

(defn query-fields
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
  [params]
  (->> (interpose " and " params)
       (apply str)))

(defn execute-query
  [query params]
  (if (empty? params)
    (db/query! query)
    (->> (format-params params)
         (str query " where ")
         (db/query!))))
