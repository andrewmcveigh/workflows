(ns workflows.core
  (:require
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

(defn workflow [flow & {:as kvs}]
  (merge kvs {:position 0 :flow flow}))

(defn task [work & [wait]]
  (merge {::work work} (if wait {::wait wait})))

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
