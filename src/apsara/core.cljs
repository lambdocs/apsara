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
   [apsara.plugins.tailwind.config :as tailwind]
   [apsara.parser :as parser]
   [apsara.implementation :as impl]))

(defn display-spec
  "Display the spec"
  [s]
  ;; [:div (impl/editor-maker defaults/default-config @vdom/spec)])
  (renderer/styled-hiccup s))

(defn display-code
  "Display the spec"
  [cursor]
  [:section.section>div.container>div.content
   [:span (str (vdom/node-from-id (:id cursor)))]])

(defn tabs
  "Tabs"
  [args]
  [:div {:class "grid sm:grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4 pt-3"}
   (for [item args]
     ^{:key (:title item)} [:div (:render item)])])

(defn main-editor []
  [:div {:class "container"}
   [:div {:class "flex flex-col"}
    [:div (impl/editor-maker tailwind/tailwind-config @vdom/spec)]
    [tabs [{:title "title1"
            :render (display-code @cursor/cursor)}
           {:title "title 1"
            :render (display-spec @vdom/spec)}]]]])

(defn init []
  (vdom/reset-id-counter!)
  (vdom/reset-spec!)
  (let [para1 (vdom/add-node! nil [:Para])
        para2 (vdom/add-node! para1 [:Para])]
    (vdom/add-node! para2 [:Text "Hello World!"])))
(init)

;; Initialize editor state
(def test-cases
  [[:para [:Text "Hello World!"]]
   [:para "Some text " [:bold "in bold "] [:italic ", italic"] " and " [:strike "strike through"]]
   [:para
    "Some text "
    [:Text {:style.color "red"} "in red, "]
    [:Text {:style {:color "green"}} "green, "]
    "and "
    [:Text {:style {:color "blue"}} "blue!"]]
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
