# workflows [![Build Status](https://travis-ci.org/andrewmcveigh/workflows.png?branch=master)](https://travis-ci.org/andrewmcveigh/workflows)

A Clojure library designed to make writing workflows simple.

## Artifacts

`workflows` artifacts are [released to Clojars](https://clojars.org/com.andrewmcveigh/workflows).

If you are using Maven, add the following repository definition to your `pom.xml`:

``` xml
<repository>
  <id>clojars.org</id>
  <url>http://clojars.org/repo</url>
</repository>
```

### The Most Recent Release

With Leiningen:

``` clj
[com.andrewmcveigh/workflows "0.1.0-SNAPSHOTS"]
```

The most recent release [can be found on Clojars](https://clojars.org/com.andrewmcveigh/workflows).

## Documentation

The complete [API documentation](http://andrewmcveigh.github.io/workflows/uberdoc.html)
is also available (marginalia generated).

## Usage

A workflow is a map conforming to the `workflows.core/Workflow`
schema. The constructor function `workflow` can be used to create a
workflow. It doesn't do much, you can create one manually if you like.

Workflows need tasks. Tasks are maps conforming to the
`workflows.core/Task` schema. If a task has a `:workflows.core/wait`
key, it's a waiting task.

```clojure
(use 'workflows.core)

(workflow (task #(prn 5)) (task #(prn 6) #(prn 7)))
;;=> {:position 0,
      :flow
      [{:workflows.core/work #<fn>
       {:workflows.core/wait #<fn> :workflows.core/work #<fn>}]}
```

Workflows are started with `#'workflows.core/work`. #'work called with
no args starts a workflow. It's presumed that non-waiting tasks are
no-args functions.

```clojure

(work (workflow (task #(prn 5)) (task #(prn 6) #(prn 7))))
;   5
;   6
;=> {:waiting? true,
     :position 1,
     :flow [{:workflows.core/work #<fn>}
            {:workflows.core/work #<fn>, :workflows.core/wait #<fn>}]}
```

When a `:workflows.core/wait` task is run, it pauses the workflow. The
workflow is restarted by calling work on it again.

A `:waiting?` workflow is pretty useless if it isn't stored
somewhere. They can be kept pretty easily in a ref, or atom.

```clojure
(def workflows
  (atom
   {"id" (workflow (task #(prn 5)) (task #(prn 6) #(prn %)))}))

;=> #'user/workflows
```

Waiting tasks are often waiting for input. #'work called with args
supplies this input to the task's work function by applying the
args. If you're keeping a workflow in an atom, it can be restarted
pretty easily too.

```clojure

(swap! workflows update-in ["id"] work)
;   5
;   6
;=> {"id" {:waiting? true,
           :position 1,
           :flow [{:workflows.core/work #<fn>}
                  {:workflows.core/work #<fn>, :workflows.core/wait #<fn>}]}}

(swap! workflows update-in ["id"] work "finished!")
;   "finished!"
;=> {"id" {:position 2,
           :flow [{:workflows.core/work #<fn>}
                  {:workflows.core/work #<fn>, :workflows.core/wait #<fn>}]}}
```

You can check the status of a workflow pretty easily with `complete?`
or `:waiting?` aswell.

## License

Copyright © 2014 Andrew Mcveigh

Distributed under the Eclipse Public License, the same as Clojure.
