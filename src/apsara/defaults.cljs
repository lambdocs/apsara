(ns apsara.defaults
  (:require
   [apsara.cursor :as cursor]
   [apsara.keys :as keys]
   [apsara.renderer :as renderer]
   [apsara.parser :as parser]
   [apsara.vdom :as vdom]))

(def default-style {:background "white"
                    :border-radius 5
                    :border "1px solid #CBD5E1"
                    :padding 13})
(def default-click cursor/set-selection)
(def default-keydown (fn [e] (keys/keydown e)))
(def key-after-hooks {"Printable" parser/parse})

(defn init []
  (vdom/reset-id-counter!)
  (vdom/reset-spec!)
  (let [para1 (vdom/add-node! nil [:para] nil)]
    (vdom/add-node! para1 [:Text "Hello World!"] nil)
    ;; (vdom/add-node! para1 [:editor ] nil)
    (let [para2 (vdom/add-node! para1 [:para] nil)]
      (vdom/add-node! para2 [:Text "Some plain text"] nil)
      (vdom/add-node! para2 [:italic ", some in italic"] nil)
      (vdom/add-node! para2 [:bold ", some in bold and "] nil)
      (vdom/add-node! para2 [:strike "some stricken out"] nil))
    (let [para3 (vdom/add-node! para1 [:para] nil)]
      (vdom/add-node! para3 [:Text  "Some text "] nil)
      (vdom/add-node! para3 [:Text {:style {:color "red"}} "in red, "] nil)
      (vdom/add-node! para3 [:Text {:style {:color "green"}} "green, "] nil)
      (vdom/add-node! para3 [:Text {:style {:color "blue"}} "and blue."] nil))
    (let [para (vdom/add-node! para1 [:para])]
      (vdom/add-node! para [:quote  "This is a block"] nil)
      (vdom/add-node! para [:button [:Text "click me!"]]))
      ))

(def default-config
  "This config displays the post rendered spec."
  {:init init
   :style default-style
   :mouse-click default-click
   :keydown default-keydown
   :renderer renderer/render
   :key-after-hooks key-after-hooks})
