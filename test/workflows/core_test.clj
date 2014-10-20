(ns workflows.core-test
  (:require
   [clojure.java.io :as io]
   [clojure.test :refer [deftest is run-tests]]
   [workflows.core :as w]))

(def sum (atom 0))

(def workflows (atom nil))

(defn create-new-workflow []
  {"id" (w/workflow
         {:state 0}
         (w/task (w/task-fn [state] (is (= 0 @sum))))
         (w/task (w/task-fn [state] (swap! sum + 5)))
         (w/task (w/task-fn [state] (is (= 5 @sum))))
         (w/task (w/task-fn [state] (swap! sum dec)))
         (w/task (w/task-fn [state] (swap! sum - 6))
                 (w/task-fn [state x] (is (= x @sum))))
         (w/task (w/task-fn [state] (swap! sum inc)))
         (w/task (w/task-fn [state] (swap! sum inc))
                 (w/task-fn [state x] (is (= x @sum)))))})

(deftest workflow-test
  (reset! workflows (create-new-workflow))
  (is (not (w/complete? ((swap! workflows update-in ["id"] w/work) "id"))))
  (is (:waiting? (@workflows "id")))
  (is (not (w/complete? ((swap! workflows update-in ["id"] w/work -2) "id"))))
  (is (:waiting? (@workflows "id")))
  (is (w/complete? ((swap! workflows update-in ["id"] w/work 0) "id"))))

(deftest round-trip-test
  (let [wf1 (w/workflow
             (w/task (w/task-fn [state] (println "Task 1")))
             (w/task (w/task-fn [state] (println "Waiting 1"))
                     (w/task-fn [state x] (+ 1 x)))
             (w/task (w/task-fn [state] (println "Waiting 2"))
                     (w/task-fn [state a b c] (+ a b c))))
        wf2 (load-string (pr-str wf1))
        wf1 (w/work wf1)
        wf2 (w/work wf2)
        jfn (juxt :waiting? :position)]
    (is (= (pr-str wf1) (pr-str wf2)))
    (is (= (jfn wf1) (jfn wf2)))
    (is (= (jfn (w/work wf1 5)) (jfn (w/work wf2 5))))))
