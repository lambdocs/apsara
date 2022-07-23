(ns apsara.plugins.tailwind.config
  (:require
   [apsara.cursor :as cursor]
   [apsara.keys :as keys]
   [apsara.plugins.tailwind.renderer :as renderer]
   [apsara.parser :as parser]
   [apsara.vdom :as vdom]
   [apsara.utils.core :as utils]))

(def default-style {:background "white"
                    :border-radius 5
                    :border "1px solid #CBD5E1"
                    :padding 13})
(def default-click cursor/set-selection)
(def default-keydown (fn [e] (keys/keydown e)))
(def key-after-hooks {"Printable" parser/parse})

(defn init []
  (utils/load-script "https://cdn.tailwindcss.com"))
  ;; (vdom/reset-id-counter!)
  ;; (vdom/reset-spec!)
  ;; (let [para1 (vdom/add-node! nil [:para])
  ;;      para2 (vdom/add-node! para1 [:para])]
  ;;  (vdom/add-node! para2 [:text "Hello World!"])))

(def tailwind-config
  "This config displays the post rendered spec."
  {:init init
   :style default-style
   :mouse-click default-click
   :keydown default-keydown
   :renderer renderer/render
   :key-after-hooks key-after-hooks})
