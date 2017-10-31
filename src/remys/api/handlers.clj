(ns remys.api.handlers
  (:require [compojure.api.sweet :as api]
            [remys.services.mysql :as db]
            [ring.util.http-response :as response]))

(defn tables
  "Get a sequence of all the tables in the database."
  []
  (keys @db/schema))

(defn table-exists?
  "Check if `t` exists in the database."
  [t]
  (->> (tables)
       (some #{t})
       nil?
       not))

(defn primary-key
  "Find the primary key of the table `t`."
  [t]
  (->> (get @db/schema t)
       (filter #(= (:column-key %) "PRI"))
       first
       :column-name))

(defn query
  ([table]
   (-> (str "select * from " table)
       (db/query!)))
  ([table id]
   (let [pk (primary-key table)]
     (-> (str "select * from " table " where " pk " = " id)
         (db/query!)))))

(api/defapi apis
  (api/context
   "/api" [table id]
   (api/GET "/:table" [table]
            (if (table-exists? table)
              (response/ok (query table))
              (response/not-found {:msg "Table not found"})))
   (api/GET "/:table/:id" [table id]
            (if (table-exists? table)
              (response/ok (query table id))
              (response/not-found {:msg "Table not found"})))))
