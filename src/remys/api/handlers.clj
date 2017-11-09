(ns remys.api.handlers
  (:require [clojure.walk :as w]
            [compojure.api.sweet :as api]
            [remys.api.resources
             [checks :as c]
             [queries :as q]]
            [remys.services.mysql :as db]
            [ring.util.http-response :as response]))

(defn query-by-fields-size-and-offset
  "Query `table` in `schema` extracting only `fields` for `size` and `offset`."
  [schema table fields size offset]
  (cond
    (not (c/valid-query-fields? schema table fields))
    (response/not-found {:msg "Fields invalid"})

    (not (c/string->number? size))
    (response/not-found {:msg "Size must be a number"})

    (not (c/string->number? offset))
    (response/not-found {:msg "Offset must be a number"})

    :else (-> (q/query-by-fields-size-and-offset table fields size offset)
              (response/ok))))

(defn query-by-fields-and-size
  "Query `table` in `schema` extracting only `fields` for `size`."
  [schema table fields size]
  (cond
    (not (c/valid-query-fields? schema table fields))
    (response/not-found {:msg "Fields invalid"})

    (not (c/string->number? size))
    (response/not-found {:msg "Size must be a number"})

    :else (-> (q/query-by-fields-and-size table fields size)
              (response/ok))))

(defn query-by-fields-and-offset
  "Query `table` in `schema` extracting only `fields` for `offset`."
  [schema table fields offset]
  (cond
    (not (c/valid-query-fields? schema table fields))
    (response/not-found {:msg "Fields invalid"})

    (not (c/string->number? offset))
    (response/not-found {:msg "Offset must be a number"})

    :else (-> (q/query-by-fields-and-offset table fields offset)
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
    (-> (q/query-by-size table size)
        (response/ok))
    (response/not-found {:msg "Size must be a number"})))

(defn query-by-offset
  "Extract only the results in `offset` from `table`."
  [table offset]
  (if (c/string->number? offset)
    (-> (q/query-by-offset table offset)
        (response/ok))
    (response/not-found {:msg "Offset must be a number"})))

(api/defapi apis
  (api/context "/api" [table id fields]
    (api/GET "/tables" []
      (response/ok (q/show-tables @db/schema)))

    (api/GET "/:table" [table]
      :query-params [{fields :- String ""}
                     {size :- String ""}
                     {offset :- String ""}]
      (if (c/table-exists? @db/schema table)
        (cond
          (and (not (empty? fields)) (not (empty? size)) (not (empty? offset)))
          (query-by-fields-size-and-offset @db/schema table fields size offset)

          (and (not (empty? fields)) (not (empty? size)))
          (query-by-fields-and-size @db/schema table fields size)

          (and (not (empty? fields)) (not (empty? offset)))
          (query-by-fields-and-offset @db/schema table fields offset)

          (not (empty? fields))
          (query-by-fields @db/schema table fields)

          (not (empty? size))
          (query-by-size table size)

          (not (empty? offset))
          (query-by-offset table offset)

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
