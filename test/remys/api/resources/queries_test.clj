(ns remys.api.resources.queries-test
  (:require [remys.api.resources.queries :as q]
            [remys.services.mysql :as db]
            [clojure.test :refer :all]))

(def schema {"test" [{:column-key "PRI" :column-name "id"}]
             "test2" [{:column-key "PRI" :column-name "id1"}
                      {:column-key "PRI" :column-name "id2"}]})

(deftest show-tables-test
  (testing "Testing show-tables resource"
    (is (= (q/show-tables schema) (list "test" "test2")))))

(deftest describe-table-test
  (testing "Testing describe-table resource"
    (is (= (q/describe-table schema "test")
           [{:column-key "PRI" :column-name "id"}]))))

(deftest primary-key-test
  (testing "Testing primary-key resource"
    (is (= (q/primary-key schema "test") (list "id")))))

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
    (with-redefs [db/query! (fn [s] {:test "test"})]
      (is (= (q/query-by-key schema :test 1) {:test "test"})))))

(deftest query-by-composite-key-test
  (testing "Testing query-by-composite-key resource"
    (with-redefs [db/query! (fn [s] {:test "test"})]
      (is (= (q/query-by-composite-key schema "test2" "1___1")
             {:test "test"})))))

(deftest query-by-fields-test
  (testing "Testing query-by-fields resource"
    (with-redefs [db/query! (fn [s] {:test "test"})]
      (is (= (q/query-by-fields :test "id") {:test "test"})))))

(deftest query-by-size-test
  (testing "Testing query-by-size resource"
    (with-redefs [db/query! (fn [s] {:test "test"})]
      (is (= (q/query-by-size :test 20) {:test "test"})))))

(deftest query-by-page-test
  (testing "Testing query-by-page resource"
    (with-redefs [db/query! (fn [s] {:test "test"})]
      (is (= (q/query-by-page :test 1) {:test "test"})))))

(deftest query-by-fields-and-page-test
  (testing "Testing query-by-fields-and-page resource"
    (with-redefs [db/query! (fn [s] {:test "test"})]
      (is (= (q/query-by-fields-and-page :test "id" 1) {:test "test"})))))

(deftest execute-query-test
  (testing "Testing execute-query resource"
    (with-redefs [db/query! (fn [s] {:test "test"})]
      (is (= (q/execute-query "test" {:id 1}) {:test "test"})))))

(deftest update-table-test
  (testing "Testing update-table resource"
    (with-redefs [db/update! (fn [s] {:test "test"})]
      (is (= (q/update-table schema "test" 1 {:id 1}) {:test "test"})))))
