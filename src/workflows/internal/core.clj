(ns workflows.internal.core
  "Adapted from https://gist.github.com/devn/c52a7f5f7cdd45d772a9")

(defn gen-nonvariadic-invokes [f]
  (for [arity (range 1 21),
        :let [args (repeatedly arity gensym)]]
    `(~'invoke [~@args] (~f ~@args))))

(defn gen-variadic-invoke [f]
  (let [args (repeatedly 22 gensym)]
    `(~'invoke [~@args] (apply ~f ~@args))))

(defn gen-apply-to [f]
  `(~'applyTo [this# args#] (apply ~f this# args#)))

(defn extend-IFn [f]
  `(clojure.lang.IFn
    ~@(gen-nonvariadic-invokes f)
    ~(gen-variadic-invoke f)
    ~(gen-apply-to f)))

(defmacro defrecordfn
  "Like defrecord, but accepts a function f before any specs that is
  used to implement clojure.lang.IFn. f should accept at least one
  argument, 'this'."
  [name [& fields] f & opts+specs]
  `(defrecord ~name [~@fields]
     ~@(extend-IFn f)
     ~@opts+specs))
