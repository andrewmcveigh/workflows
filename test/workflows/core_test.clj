(ns workflows.core-test
  (:require
   [clojure.test :refer [deftest is run-tests]]
   [workflows.core :as w]))

(def sum (atom 0))

(def workflows
  (atom {"id" (w/workflow
               [(w/task #(is (= 0 @sum)))
                (w/task #(swap! sum + 5))
                (w/task #(is (= 5 @sum)))
                (w/task #(swap! sum dec))
                (w/task #(is (= % @sum)) #(swap! sum - 6))
                (w/task #(swap! sum inc))
                (w/task #(is (= % @sum)) #(swap! sum inc))])}))

(deftest workflow-test
  (is (not (w/complete? ((swap! workflows update-in ["id"] w/work) "id"))))
  (is (:waiting? (@workflows "id")))
  (is (not (w/complete? ((swap! workflows update-in ["id"] w/work -2) "id"))))
  (is (:waiting? (@workflows "id")))
  (is (w/complete? ((swap! workflows update-in ["id"] w/work 0) "id"))))
