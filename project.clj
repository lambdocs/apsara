(defproject apsara "0.1.0"
  :description "Editor macros in Clojure"
  :url "https://github.com/"
  :license {:name "EPL-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [org.clojure/clojurescript "1.10.896" :scope "provided"]
                 [reagent "1.1.0"]
                 [instaparse "1.4.10"]]
  :repl-options {:init-ns apsara.core})
