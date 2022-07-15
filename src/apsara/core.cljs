(ns apsara.core
  (:require
   [reagent.core :as r]
   [clojure.string :as string]
   [apsara.cursor :as cursor]
   [apsara.elements :as elements]
   [apsara.keys :as keys]
   [apsara.vdom :as vdom]
   [apsara.renderer :as renderer]
   [apsara.defaults :as defaults]
   [apsara.parser :as parser]
   [apsara.implementation :as impl]))

(defn display-spec [s]
  "Display the spec"
  ;; [:div (impl/editor-maker defaults/default-config @vdom/spec)])
  (renderer/styled-hiccup s))

(defn main-editor []
  "Return an outer level editor"
  [:div
   [:div (impl/editor-maker defaults/default-config @vdom/spec)]
   [:div (cursor/display-spec)]
   [:div.container [display-spec @vdom/spec]]])

(defn init []
  (vdom/reset-id-counter!)
  (vdom/reset-spec!)
  (let [para1 (vdom/add-node! nil [:para] nil)]
    (vdom/add-node! para1 [:text "Hello World!"] nil)
    ;; (vdom/add-node! para1 [:editor ] nil)
    (let [para2 (vdom/add-node! para1 [:para] nil)]
      (vdom/add-node! para2 [:text "Some plain text"] nil)
      (vdom/add-node! para2 [:italic ", some in italic"] nil)
      (vdom/add-node! para2 [:bold ", some in bold and "] nil)
      (vdom/add-node! para2 [:strike "some stricken out"] nil))
    (let [para3 (vdom/add-node! para1 [:para] nil)]
      (vdom/add-node! para3 [:text  "Some text "] nil)
      (vdom/add-node! para3 [:text {:style {:color "red"}} "in red, "] nil)
      (vdom/add-node! para3 [:text {:style {:color "green"}} "green, "] nil)
      (vdom/add-node! para3 [:text {:style {:color "blue"}} "and blue."] nil))
    (let [para (vdom/add-node! para1 [:para])]
      (vdom/add-node! para [:quote  "This is a block"] nil)
      (vdom/add-node! para [:button [:text "click me!"]]))
      ))

;; Initialize editor state
;; (init)

(def test-cases
  [[:para [:text "Hello World!"]]
   [:para "Some text " [:bold "in bold "] [:italic ", italic"] " and " [:strike "strike through"]]
   [:para
    "Some text "
    [:text {:style.color "red"} "in red, "]
    [:text {:style {:color "green"}} "green, "]
    "and "
    [:text {:style {:color "blue"}} "blue!"]]
   [:para "Some text " [:break] "with a line break" [:break]  "or two"]
   [:para "We love to be the 1" [:super "st"] " to support superscript and subscript X" [:sub "1"]]
   [[:h1 "Heading1"] [:h2 "Heading2"] [:h3 "Heading 3"] [:h4 "heading4"] [:h5 "heading5"]]
   [:para
    "And if we want to quote"
    [:block
     "These violent delights have violent ends"
     [:break]
     "And in their triump die, like fire and powder"
     [:break]
     "Which, as they kiss, consume"
     [:break]
     " -- Will Shakespeare"]]
    [:para
     [:block "This is a block"
     [:block "This is a sub block"
      [:block "And we can nest them forever!"]]]]
   [:para
    "Playing with links "
    [:break]
    "We really don't know " [:a {:href "https://en.wikipedia.org/wiki/Satoshi_Nakamoto"} " who "]
    "wrote "
    [:a {:href "https://bitcoinwhitepaper.co/"} " this article."]]
   [:para
    "Not to forget Lists!"
    [:break]
    "Ordered Lists"
    [:ol
     [:li "First"]
     [:li "Second"]]
    [:break]
    "Unordered lists"
    [:ul
     [:li "First"]
     [:li "Second"]
     [:ol
      [:li "Nested list item 1"]
      [:li "Nested list item 2"]
      [:ul
       [:li "Nest them until your browser crashes!"]]]]]
   [:para
    "Images are beautiful! Add them using [:img {:src link}]."
    [:img {:src "https://cdn.pixabay.com/photo/2016/01/14/03/25/sunset-1139293_960_720.jpg"}]]
   ])

;;(defn t [idx, options]
;;  (reset! spec (nth test-cases idx)))
;;(defn t-all []
;;  (reset! spec (reduce conj [] test-cases)))
