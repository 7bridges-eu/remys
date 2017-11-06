(ns remys.api.handlers
  (:require [clojure.walk :as w]
            [compojure.api.sweet :as api]
            [remys.api.resources
             [checks :as c]
             [queries :as q]]
            [remys.services.mysql :as db]
            [ring.util.http-response :as response]))

(defn query-by-fields-and-page
  "Query `table` in `schema` extracting only `fields` for `page`."
  [schema table fields page]
  (cond
    (not (c/valid-query-fields? schema table fields))
    (response/not-found {:msg "Fields invalid"})

    (not (c/string->number? page))
    (response/not-found {:msg "Page must be a number"})

    :else (->> (Integer/parseInt page)
               (q/query-by-fields-and-page table fields)
               (response/ok))))

(defn query-by-fields
  "Extract only `fields` (columns) from `table` in `schema`."
  [schema table fields]
  (if (c/valid-query-fields? schema table fields)
    (response/ok (q/query-by-fields table fields))
    (response/not-found {:msg "Fields invalid"})))

(defn query-by-size
  "Extract only `size` records from `table`."
  [table size]
  (if (c/string->number? size)
    (->> (Integer/parseInt size)
         (q/query-by-size table)
         (response/ok))
    (response/not-found {:msg "Size must be a number"})))

(defn query-by-page
  "Extract only the results in `page` from `table`."
  [table page]
  (if (c/string->number? page)
    (->> (Integer/parseInt page)
         (q/query-by-page table)
         (response/ok))
    (response/not-found {:msg "Page must be a number"})))

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
          (and (not (empty? fields)) (not (empty? page)))
          (query-by-fields-and-page @db/schema table fields page)

          (not (empty? fields))
          (query-by-fields @db/schema table fields)

          (not (empty? size))
          (query-by-size table size)

          (not (empty? page))
          (query-by-page table page)

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
        (if (c/composite-key? id)
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
