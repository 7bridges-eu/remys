(ns remys.api.resources.checks-test
  (:require [clojure.test :refer :all]
            [remys.api.resources
             [checks :as c]
             [queries :as q]]))

(def schema {"test" [{:column-name "test1"} {:column-name "test2"}]})

(deftest table-exists?-test
  (testing "Testing table-exists? resource"
    (is (true? (c/table-exists? schema "test")))
    (is (false? (c/table-exists? schema "testing")))))

(deftest column-exists?-test
  (testing "Testing column-exists? resource"
    (is (true? (c/column-exists? schema "test" "test1")))
    (is (false? (c/column-exists? schema "test" "testing")))))

(deftest columns-exist?-test
  (testing "Testing columns-exist? resource"
    (is (true? (c/columns-exist? schema "test" ["test1" "test2"])))
    (is (false? (c/columns-exist? schema "test" ["testing"])))))

(deftest composite-key?-test
  (testing "Testing composite-key? resource"
    (is (true? (c/composite-key? "1___1")))
    (is (false? (c/composite-key? "1a")))))

(deftest record-exists?-test
  (testing "Testing record-exists? resource"
    (with-redefs [q/query-by-composite-key (fn [s t id] {:test "test"})
                  q/query-by-key (fn [s t id] {:test "test"})]
      (is (true? (c/record-exists? schema "test" "1")))
      (is (true? (c/record-exists? schema "test" "1___1"))))))

(deftest valid-query?-test
  (testing "Testing valid-query? resource"
    (let [valid-query "select * from test"
          invalid-query "drop table test"]
      (is (true? (c/valid-query? valid-query)))
      (is (false? (c/valid-query? invalid-query))))))

(deftest string->number?-test
  (testing "Testing string->number? resource"
    (is (true? (c/string->number? "42")))
    (is (false? (c/string->number? "test")))))

(deftest valid-query-fields?-test
  (testing "Testing valid-query-fields? resource"
    (is (true? (c/valid-query-fields? schema "test" "test1,test2")))
    (is (false? (c/valid-query-fields? schema "test" "testing1,testing2")))))
