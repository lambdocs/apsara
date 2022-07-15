(ns apsara.elements
  ^{:doc "A subset of DOM elements for the editor."}
  (:require
   [apsara.vdom :as vdom]))

(defn stringify [node]
  "Return a new node with the stringified representation of node."
  (let [attr (vdom/attr node)
        {id :id} attr
        attr-printable (dissoc attr :parent :id)
        node-printable (into [(vdom/tag node) attr-printable] (vdom/children node))
        new-node [:text (pr-str node-printable)]]
    (vdom/replace-node! id new-node)
    [:span]))

(defn- split-attr [attr]
  "Split attr into 2 maps. The first part contains the id and parent. The second part
  contains everything else."
  [{:id (:id attr) :parent (:parent attr)}
   (dissoc attr :id :parent)])

(defn- style [node]
  "Interpret a style node."
  (let [[base-attr style-attr] (split-attr (vdom/attr node))]
  (into [] (concat [:span (merge base-attr {:style style-attr})] (vdom/children node)))))

(def supported-elements
  ^{:doc "Aliases mapped to DOM elements that take the form:
    :alias -> {:transform (fn [node]) :type block|inline} "}
  {;; Aliases that map to block level elements
   ;; https://developer.mozilla.org/en-US/docs/Web/HTML/Block-level_elements
   :para {:transform #(assoc %1 0 :div) :type "block"}
   :address {:transform #(identity %1) :type "block"}
   :article {:transform #(identity %1) :type "block"}
   :aside {:transform #(identity %1) :type "block"}
   :blockquote {:transform #(identity %1) :type "block"}
   :quote {:transform #(assoc %1 0 :blockquote) :type "block"}
   :details {:transform #(identity %1) :type "block"}
   :dialog {:transform #(identity %1) :type "block"}
   :div {:transform #(identity %1) :type "block"}
   :dl {:transform #(identity %1) :type "block"}
   :fieldset {:transform #(identity %1) :type "block"}
   :figcaption {:transform #(identity %1) :type "block"}
   :figure {:transform #(identity %1) :type "block"}
   :footer {:transform #(identity %1) :type "block"}
   :form {:transform #(identity %1) :type "block"}
   :h1 {:transform #(identity %1) :type "block"}
   :h2 {:transform #(identity %1) :type "block"}
   :h3 {:transform #(identity %1) :type "block"}
   :h4 {:transform #(identity %1) :type "block"}
   :h5 {:transform #(identity %1) :type "block"}
   :h6 {:transform #(identity %1) :type "block"}
   :header {:transform #(identity %1) :type "block"}
   :hr {:transform #(identity %1) :type "block"}
   :li {:transform #(identity %1) :type "block"}
   :main {:transform #(identity %1) :type "block"}
   :nav {:transform #(identity %1) :type "block"}
   :ol {:transform #(identity %1) :type "block"}
   :p {:transform #(identity %1) :type "block"}
   :pre {:transform #(identity %1) :type "block"}
   :section {:transform #(identity %1) :type "block"}
   :table {:transform #(identity %1) :type "block"}
   :ul {:transform #(identity %1) :type "block"}
   ;; Aliases that map to inline elements
   ;; https://developer.mozilla.org/en-US/docs/Web/HTML/Inline_elements
   :style {:transform #(style %1) :type "inline"}
   :escape {:transform #(stringify %1) :type "inline"}
   :text {:transform #(assoc %1 0 :span) :type "inline"}
   :highlight {:transform #(assoc %1 0 :span) :type "inline"}
   :bold {:transform #(assoc %1 0 :b) :type "inline"}
   :break {:transform #(assoc %1 0 :br) :type "inline"}
   :italic {:transform #(assoc %1 0 :i) :type "inline"}
   :strike {:transform #(assoc %1 0 :s) :type "inline"}
   :super {:transform #(assoc %1 0 :sup) :type "inline"}
   :sub {:transform #(identity %1) :type "inline"}
   :a {:transform #(identity %1) :type "inline"}
   :abbr {:transform #(identity %1) :type "inline"}
   :acronym {:transform #(identity %1) :type "inline"}
   :audio {:transform #(identity %1) :type "inline"}
   :b {:transform #(identity %1) :type "inline"}
   :bdi {:transform #(identity %1) :type "inline"}
   :bdo {:transform #(identity %1) :type "inline"}
   :big {:transform #(identity %1) :type "inline"}
   :br {:transform #(identity %1) :type "inline"}
   :button {:transform #(identity %1) :type "inline"}
   :canvas {:transform #(identity %1) :type "inline"}
   :cite {:transform #(identity %1) :type "inline"}
   :code {:transform #(identity %1) :type "inline"}
   :img {:transform #(identity %1) :type "inline"}
   })

(defn- compile-node [node]
  ((:transform (supported-elements (vdom/tag node)
                                   {:transform #(stringify %1)}))
   node))

;; Tree = [:component {attributes} [child1] [child2]]
(defn compile [tree]
  "Compile a spec tree into a reagent tree."
  ;; (println "tree: " (meta tree))
  (cond
    (string? tree) tree ;; Return literals as they are
    (map? tree) tree ;; Return attributes as is
    (vector? (nth tree 0)) (reduce conj [:div ] (map compile tree)) ;; an escape hatch
    (false? (vdom/children? tree)) (compile-node tree) ;; map from piclojure to reagent components
    (true? (vdom/children? tree)) (let [parent (compile-node tree)] ;; compile parent node
                               (into [(nth parent 0)] ;; merge compiled parent node with
                                     (map compile (into [] (rest parent))))) ;; compiled children
  ))
