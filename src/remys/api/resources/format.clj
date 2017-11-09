(ns remys.api.resources.format
  (:require [clojure.string :as string]
            [remys.services.mysql :as db]))

(defn wrap-string
  "Wrap `s` in single quotes if `s` is a string. Useful for SQL."
  [s]
  (if (string? s)
    (str "'" s "'")
    s))

(defn create-where
  "Create the where conditions linking `pks` to `values.`
  E.g.: id = 1 and name = 'test'"
  [pks values]
  (->> (map wrap-string values)
       (map #(str %1 " = " %2) pks)
       (interpose " and ")
       (apply str)))

(defn kebab-case->snake-case
  "Transform the first element of `v` from kebab-case to snake_case."
  [v]
  (let [col (first v)
        as (second v)]
    (vector (db/kebab-case->snake-case col) as)))

(defn format-as
  "Format the elements in `v` as SQL query parameters.
  E.g.: [test Test] => test as test
        [test] => test"
  [v]
  (let [col (first v)
        as (second v)]
    (if as
      (str col " as " as)
      col)))

(defn format-like
  "Format `column` and `like` as an SQL where condition."
  [column like]
  (str column " like '%" like "%'"))

(defn format-likes
  "Format the elements in `fs` as SQL where conditions with `like`."
  [fs like]
  (->> (string/split fs #",")
       (map #(string/split % #":"))
       (map kebab-case->snake-case)
       (map first)
       (map #(format-like % like))
       (interpose " or ")
       (apply str)))

(defn format-fields
  "Transform `fs` in a format suitable for MySQL.
  E.g.: id,text:Text => id, text as Text"
  [fs]
  (->> (string/split fs #",")
       (map #(string/split % #":"))
       (map kebab-case->snake-case)
       (map format-as)
       (interpose ", ")
       (apply str)))

(defn format-params
  "Format `params` map as where conditions."
  [params]
  (let [ks (->> (keys params) (map name) (map db/kebab-case->snake-case))
        vs (->> (vals params) (map wrap-string))]
    (->> (map #(str %1 " = " %2) ks vs)
         (interpose " and ")
         (apply str))))

(defn format-column-value
  "Convert `value` in a format suitable for MySQL."
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
     (let [column (-> (name k) (db/kebab-case->snake-case))
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
