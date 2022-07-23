(ns apsara.plugins.tailwind.renderer
  (:require
   [apsara.plugins.tailwind.elements :as elements]
   [apsara.vdom :as vdom]
   [reagent.core :as r]))

;; Default Renderer
(defn render
  "Render spec to view"
  [sp]
  (r/create-class
   {:display-name "Editor"
    :component-did-update (fn [this] (println "component-did-update"))
    :reagent-render (fn [sp] (elements/compile sp))}))

(defn transform-tag [tag]
  (conj [:span {:style {:color "#73207c"}}] (str ":" (name tag))))

(defn transform-attr [attr]
  (if (map? attr)
    (let [a (dissoc attr :id :parent)
          len (count (keys a))]
      (if (> len 0)
        (conj [:span {:style {:color "#207c53"}} (str a)])
        " "))
    ""))

(defn transform
  "Transform each node"
  [node]
  (let [r (-> (assoc node 0 (transform-tag (first node)))
              (assoc 1 (transform-attr (nth node 1))))]
    (if (vdom/leaf? node)
      (into [:div] r)
      (into [:div] r))))

(def left-border {:style {:border-left "1px solid #86EFAC" :padding-left 7}})

(defn walk
  "Walk the (sub)tree in depth first order, applying func"
  [node func]
  (if (not (vdom/leaf? node))
    (func (into [] (concat [(vdom/tag node)]
                           [(vdom/attr node)]
                           [(into [:div left-border] (map #(walk %1 func) (vdom/children node)))])))
    (func node)
    ))

(defn styled-hiccup
  "Apply styles on hiccup. The input hiccup is transformed to apply
   syntax styles. The output is again hiccup!"
  [sp]
  (walk sp transform))

(defn render-as-hiccup
  "Render spec as hiccup"
  [sp]
  (r/create-class
   {:display-name "Editor-Hiccup"
    :reagent-render (fn [sp] (elements/compile (styled-hiccup sp)))}))
