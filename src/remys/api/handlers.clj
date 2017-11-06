(ns remys.api.handlers
  (:require [clojure.walk :as w]
            [compojure.api.sweet :as api]
            [remys.api.resources
             [checks :as c]
             [queries :as q]]
            [remys.services.mysql :as db]
            [ring.util.http-response :as response]))

(api/defapi apis
  (api/context "/api" [table id fields]
    (api/GET "/tables" []
      (response/ok (q/show-tables @db/schema)))

    (api/GET "/:table" [table]
      :query-params [{fields :- String ""}
                     {size :- String ""}
                     {page :- String ""}]
      (if (c/table-exists? @db/schema table)
        (cond
          ;; Query by fields and page
          (and (not (empty? fields)) (not (empty? page)))
          (if (re-matches #"\d+" page)
            (->> (Integer/parseInt page)
                 (q/query-by-fields-and-page table fields)
                 (response/ok))
            (response/not-found {:msg "Page must be a number"}))

          ;; Query by fields
          (not (empty? fields))
          (response/ok (q/query-by-fields table fields))

          ;; Query by size
          (not (empty? size))
          (if (re-matches #"\d+" size)
            (->> (Integer/parseInt size)
                 (q/query-by-size table)
                 (response/ok))
            (response/not-found {:msg "Size must be a number"}))

          ;; Query by page
          (not (empty? page))
          (if (re-matches #"\d+" page)
            (->> (Integer/parseInt page)
                 (q/query-by-page table)
                 (response/ok))
            (response/not-found {:msg "Page must be a number"}))

          ;; Query all records
          :else (response/ok (q/query-all table)))
        (response/not-found {:msg "Table not found"})))

    (api/GET "/:table/describe" [table]
      (if (c/table-exists? @db/schema table)
        (response/ok (q/describe-table @db/schema table))
        (response/not-found {:msg "Table not found"})))

    (api/GET "/:table/count" [table]
      (if (c/table-exists? @db/schema table)
        (response/ok (q/count-records table))
        (response/not-found {:msg "Table not found"})))

    (api/GET "/:table/:id" [table id]
      (if (c/table-exists? @db/schema table)
        (if (re-matches #"[a-zA-Z0-9]+___[a-zA-Z0-9]+" id)
          (response/ok (q/query-by-composite-key @db/schema table id))
          (response/ok (q/query-by-key @db/schema table id)))
        (response/not-found {:msg "Table not found"})))

    (api/POST "/dynamic" []
      :body-params [query :- String
                    {params :- clojure.lang.PersistentArrayMap {}}]
      (if (c/valid-query? query)
        (try
          (response/ok (q/execute-query query params))
          (catch Exception e
            (response/not-found {:msg (str "Error: " e)})))
        (response/not-found {:msg "Query not valid"})))

    (api/PUT "/:table/:id" req
      (let [table (get-in req [:route-params :table])
            id (get-in req [:route-params :id])
            cols (keys (get req :body-params))
            params (w/keywordize-keys (get req :body-params))]
        (cond
          (not (c/table-exists? @db/schema table))
          (response/not-found {:msg "Table not found"})

          (not (c/record-exists? @db/schema table id))
          (response/not-found {:msg "Record not present"})

          (not (c/columns-exist? @db/schema table cols))
          (response/not-found {:msg "Invalid columns"})

          :else (response/ok (q/update-table @db/schema table id params)))))))
