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
       first
       :column-name))

(defn query-all
  [table]
  (-> (str "select * from " table)
      (db/query!)))

(defn query-by-id
  [schema table id]
  (let [pk (primary-key schema table)]
    (-> (str "select * from " table " where " pk " = " id)
        (db/query!))))

(defn query-fields
  [table fields]
  (let [fs (string/split fields #",")]
    (-> (str "select " fields " from " table)
        (db/query!))))
