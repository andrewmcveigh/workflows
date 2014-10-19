(ns workflows.core
  (:require
   [clojure.walk :refer [postwalk]]
   [schema.core :as s]
   [workflows.internal.core :refer [defrecordfn]]))

(defrecordfn TFn [form f]
  (fn [_ & args] (apply f args)))

(defmethod print-dup TFn [o w]
  (.write w (pr-str (:form o))))

(defmethod print-method TFn [o w]
  (.write w (pr-str (:form o))))

(defn qualify [s]
  (when-let [m (some-> (resolve s) (meta))]
    (list 'quote (symbol (str (:ns m)) (name (:name m))))))

(defn task-fn* [args form]
  (postwalk (fn [f]
              (cond (symbol? f) (if (args f) `'~f (or (qualify f) f))
                    (list? f) (cons 'list f)
                    :else f))
            form))

(defmacro task-fn [args & forms]
  `(->TFn (list `task-fn '~args ~@(task-fn* (set args) forms))
          (fn ~args ~@forms)))

(def Fn (s/pred fn? 'fn?))

(def Task
  {(s/required-key ::work) (s/either Fn TFn)
   (s/optional-key ::wait) (s/either Fn TFn)})

(def Workflow
  {(s/required-key :position) s/Int
   (s/required-key :flow) [Task]
   (s/required-key :state) s/Any
   (s/optional-key :waiting?) s/Bool})

(s/defn ^:always-validate workflow :- Workflow [init-state & tasks :- [Task]]
  {:position 0 :state init-state :flow (vec tasks)})

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
  (loop [{:keys [position state flow waiting?] :as workflow} workflow]
    (if (complete? workflow)
      workflow
      (let [{:keys [::wait ::work] :as task} (flow position)]
        (if (and wait (not waiting?))
          (assoc workflow :state (wait state) :waiting? true)
          (recur (-> workflow
                     (dissoc :waiting?)
                     (assoc :state (if waiting?
                                     (apply work state args)
                                     (work state)))
                     (update-in [:position] inc))))))))
