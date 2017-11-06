(ns remys.api.resources.queries-test
  (:require [remys.api.resources.queries :as q]
            [remys.services.mysql :as db]
            [clojure.test :refer :all]))

(deftest show-tables-test
  (testing "Testing show-tables resource"
    (let [schema {:test "test"}]
      (is (= (q/show-tables schema) (list :test))))))

(deftest describe-table-test
  (testing "Testing describe-table resource"
    (let [schema {:test "test"}]
      (is (= (q/describe-table schema :test) "test")))))

(deftest primary-key-test
  (testing "Testing primary-key resource"
    (let [schema {:test [{:column-key "PRI" :column-name "id"}]}]
      (is (= (q/primary-key schema :test) (list "id"))))))

(deftest count-records-test
  (testing "Testing count-records resource"
    (with-redefs [db/query! (fn [s] {:test "test"})]
      (is (= (q/count-records :test) {:test "test"})))))

(deftest query-all-test
  (testing "Testing query-all resource"
    (with-redefs [db/query! (fn [s] {:test "test"})]
      (is (= (q/query-all :test) {:test "test"})))))

(deftest query-by-key-test
  (testing "Testing query-by-key resource"
    (let [schema {:test [{:column-key "PRI" :column-name "id"}]}]
      (with-redefs [db/query! (fn [s] {:test "test"})]
        (is (= (q/query-by-key schema :test 1) {:test "test"}))))))

(deftest query-by-composite-key-test
  (testing "Testing query-by-composite-key resource"
    (let [schema {:test [{:column-key "PRI" :column-name "id1"}
                         {:column-key "PRI" :column-name "id2"}]}]
      (with-redefs [db/query! (fn [s] {:test "test"})]
        (is (= (q/query-by-composite-key schema :test "1___1")
               {:test "test"}))))))

(deftest query-fields-test
  (testing "Testing query-fields resource"
    (with-redefs [db/query! (fn [s] {:test "test"})]
      (is (= (q/query-fields :test "id") {:test "test"})))))

(deftest execute-query-test
  (testing "Testing execute-query resource"
    (with-redefs [db/query! (fn [s] {:test "test"})]
      (is (= (q/execute-query "test" {:id 1}) {:test "test"})))))

(deftest update-table-test
  (testing "Testing update-table resource"
    (let [schema {:test [{:column-key "PRI" :column-name "id"}]}]
      (with-redefs [db/update! (fn [s] {:test "test"})]
        (is (= (q/update-table schema "test" 1 {:id 1}) {:test "test"}))))))
