(defproject com.andrewmcveigh/workflows "0.2.0"
  :description "Workflow Schema and engine"
  :url "http://github.com/andrewmcveigh/workflows"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "same as Clojure"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [prismatic/schema "0.3.1"]]
  :profiles {:dev {:plugins [[lein-marginalia "0.8.0"]]}})
