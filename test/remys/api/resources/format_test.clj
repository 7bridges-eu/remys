(ns remys.api.resources.format-test
  (:require [remys.api.resources.format :as f]
            [remys.services.mysql :as db]
            [clojure.test :refer :all]))

(def schema {"test" [{:column-name "test1"} {:column-name "test2"}]})

(deftest wrap-string-test
  (testing "Testing wrap-string resource"
    (is (= "'test'" (f/wrap-string "test")))
    (is (= 1 (f/wrap-string 1)))))

(deftest create-where-test
  (testing "Testing create-where resource"
    (let [pks ["id" "text"]
          values [1 "test"]]
      (is (= (f/create-where pks values) "id = 1 and text = 'test'")))))

(deftest kebab-case->snake-case-test
  (testing "Testing kebab-case->snake-case resource"
    (let [v ["test-col" "Test"]]
      (is (= (f/kebab-case->snake-case v) ["test_col" "Test"])))))

(deftest format-as-test
  (testing "Testing format-as resource"
    (let [v ["test" "Test"]]
      (is (= (f/format-as v) "test as Test")))))

(deftest format-like
  (testing "Testing format-like resource"
    (is (= (f/format-like "id" "test") "id like '%test%'"))))

(deftest format-likes
  (testing "Testing format-likes resource"
    (let [fs "id,text:Text"
          like "test"]
      (is (f/format-likes fs like) "id like '%test%' or Text like '%test%'"))))

(deftest format-fields-test
  (testing "Testing format-fields resource"
    (let [fs "id,text:Text"]
      (is (= (f/format-fields fs) "id, text as Text")))))

(deftest format-params-test
  (testing "Testing format-params resource"
    (let [params {:id 1 :text "test"}]
      (is (= (f/format-params params) "id = 1 and text = 'test'")))))

(deftest format-column-value-test
  (testing "Testing format-column-value resource"
    (is (= (f/format-column-value schema "test" "test1" "text") "'text'"))
    (is (= (f/format-column-value schema "test" "test1" 1) 1))))

(deftest params->mysql-params-test
  (testing "Testing params->mysql-params resource"
    (is (= (f/params->mysql-params schema "test" {:test1 1}) {"test1" 1}))))

(deftest format-update-params-test
  (testing "Testing format-update-params resource"
    (is (= (f/format-update-params schema "test" {:test1 1}) "test1 = 1"))))
