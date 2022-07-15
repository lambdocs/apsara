(ns apsara.defaults
  (:require
   [apsara.cursor :as cursor]
   [apsara.keys :as keys]
   [apsara.renderer :as renderer]
   [apsara.parser :as parser]
   [apsara.vdom :as vdom]))

(def default-style {:background "white"
                    :border-radius 5
                    :padding 13})
(def default-click cursor/set-selection)
(def default-keydown (fn [e] (keys/keydown e)))
(def default-spec @vdom/spec)
(def key-after-hooks {"Printable" parser/parse})

(def default-config
  "This config displays the post rendered spec."
  {:style default-style
   :mouse-click default-click
   :keydown default-keydown
   :renderer renderer/render
   :spec default-spec
   :key-after-hooks key-after-hooks})

(def inspector-config
  "This config displays the hiccup as is."
  {:style default-style
   :mouse-click default-click
   :keydown default-keydown
   :renderer renderer/render-as-hiccup
   :spec default-spec
   :key-after-hooks nil})
