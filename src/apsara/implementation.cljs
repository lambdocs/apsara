(ns apsara.implementation
  (:require
      [apsara.keys :as keys]))
;; This module delibrately has no dependencies so that it may be
;; called from anywhere without creating circular dependencies.
(defn editor-maker
  "Return a content editable component."
  [config spec]
  [:section.section>div.container>div.content
   [:div
    {:content-editable true
     :style (:style config)
     :onClick (:mouse-click config)
     :on-key-down (:keydown config)
     }
    (let [renderer (:renderer config)
          key-after-hooks (:key-after-hooks config)
          init (:init config)]
      (init)
      (keys/set-key-after-hooks key-after-hooks)
      [renderer spec])]])
