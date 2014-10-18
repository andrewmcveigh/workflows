(ns workflows.core
  (:require
   [clojure.walk :refer [postwalk]]
   [schema.core :as s]))

(def Fn (s/pred fn? 'fn?))

(def Task
  {(s/required-key ::work) Fn
   (s/optional-key ::wait) Fn})

(def Workflow
  {(s/required-key :position) s/Int
   (s/required-key :flow) [Task]
   (s/optional-key :waiting?) s/Bool
   s/Keyword s/Any})

(defn workflow [& tasks]
  {:position 0 :flow (vec tasks)})

(defn task-fn* [form]
  (postwalk #(cond (symbol? %) (or (resolve %) %)
                   (list? %) (cons 'list (map task-fn* %))
                   :else %)
            form))

(defmacro task-fn [form]
  (task-fn* form))

(defn task
  ([wait work]
     (merge (when wait {::wait wait}) {::work work}))
  ([work]
     (task nil work)))

(defn complete? [{:keys [position flow] :as workflow}]
  (>= position (count flow)))

(s/defn ^:always-validate work :- Workflow
  "Put a Workflow to work, or restart a waiting Workflow. & args are
expected to be required only by the :waiting? ::work (fn [arg1 arg2 ...])."
  [workflow :- Workflow & args]
  (loop [{:keys [position flow waiting?] :as workflow} workflow]
    (if (complete? workflow)
      workflow
      (let [{:keys [::wait ::work] :as task} (flow position)]
        (if (and wait (not waiting?))
          (do
            (wait)
            (assoc workflow :waiting? true))
          (let [workflow (dissoc workflow :waiting?)]
            (if waiting?
              (apply (::work task) args)
              ((::work task)))
            (recur (update-in workflow [:position] inc))))))))
