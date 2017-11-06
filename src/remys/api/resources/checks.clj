(ns remys.api.resources.checks
  (:require [clojure.string :as string]
            [remys.api.resources.queries :as q]))

(defn table-exists?
  "Check if `table` exists in the `schema`."
  [schema table]
  (->> (keys schema)
       (some #{table})
       nil?
       not))

(defn column-exists?
  "Check if `column` exists in the `table` in `schema`."
  [schema table column]
  (let [col (string/replace column #"-" "_")]
    (-> #(= (:column-name %) col)
        (filter (get schema table))
        empty?
        not)))

(defn columns-exist?
  "Check if all the `columns` exists in the `table` in `schema`."
  [schema table columns]
  (->> (map #(column-exists? schema table %) columns)
       (every? true?)))

(defn composite-key?
  "Check if `id` is a composite key.
  To be a composite key, the values in `id` must be separated by three
  underscores."
  [id]
  (not (nil? (re-matches #"[a-zA-Z0-9]+___[a-zA-Z0-9]+" id))))

(defn record-exists?
  "Check if the record identified by `id` exists in the `table` in `schema`."
  [schema table id]
  (if (composite-key? id)
    (not (empty? (q/query-by-composite-key schema table id)))
    (not (empty? (q/query-by-key schema table id)))))

(defn valid-query?
  "Check if the query contains create/delete/drop/update instructions."
  [query]
  (nil? (re-matches #"(?i)^.*(create|delete|drop|update).*" query)))

(defn string->number?
  "Check if `s` is a string corresponding to number."
  [s]
  (not (nil? (re-matches #"\d+" s))))

(defn valid-query-fields?
  "Check that `fields` matches the columns in the `table` in `schema`.
  `fields` is a string with comma-separated values."
  [schema table fields]
  (->> (string/split fields #",")
       (columns-exist? schema table)))
