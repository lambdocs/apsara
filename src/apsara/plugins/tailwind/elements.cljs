(ns apsara.plugins.tailwind.elements
  ^{:doc "A subset of DOM elements for the editor."}
  (:require
   [apsara.vdom :as vdom]))

(def block-elements
  {:Para true
   :address true
   :article true
   :aside true
   :blockquote true
   :Quote true
   :details true
   :dialog true
   :div true
   :dl true
   :fieldset true
   :figcaption true
   :figure true
   :footer true
   :form true
   :h1 true
   :h2 true
   :h3 true
   :h4 true
   :h5 true
   :h6 true
   :header true
   :hr true
   :li true
   :main true
   :nav true
   :ol true
   :p true
   :pre true
   :section true
   :table true
   :ul true
   })

(defn stringify
  "Return a new node with the stringified representation of node."
  [node]
  (let [attr (vdom/attr node)
        {id :id} attr
        attr-printable (dissoc attr :parent :id)
        node-printable (into [(vdom/tag node) attr-printable] (vdom/children node))
        new-node [:Text (pr-str node-printable)]]
    (vdom/replace-node! id new-node)
    [:span]))

(defn- split-attr
  "Split attr into 2 maps. The first part contains the id and parent.
  The second part contains everything else."
  [attr]
  [{:id (:id attr) :parent (:parent attr)}
   (dissoc attr :id :parent)])

(defn- merge-attr
  "Merge attr into the attributes of node. id and parent keys are not over
  written. User set attributes are given priority over transformations."
  [node attr]
  (let [[base-attr old-attr] (split-attr (vdom/attr node))
        [b new-attr] (split-attr attr)]
  (into []
        (concat [(vdom/tag node) (merge base-attr old-attr new-attr)]
                (vdom/children node)))))

(defn block?
 "Returns if an element is a block type."
 [node]
 (block-elements (vdom/tag node) false))

(defn- style
  "Interpret a style node."
  [node]
  (let [[base-attr style-attr] (split-attr (vdom/attr node))]
  (into [] (concat [:span (merge base-attr {:style style-attr})] (vdom/children node)))))

(defn- transform
  "Transform a node so that the resulting node has the :tag and :att
  from tx."
  [node tx]
  (-> node
      (assoc 0 (:tag tx))
      (merge-attr (:attr tx))))

(defn- Para
  "Render a Para element. This is split into 2 elements, an inner div
  and an outer div like so:
  [:div {:class \"flex flex-col\"}
    [:div {:class \"flex-row\"}]]
  Then all original block type elements of :Para should be the children of
  the outer :div and the inline type elements should be the children of
  the inner :div."
  [node]
  (let [blocks (filterv block? (vdom/children node))
        inlines (filterv #(not (block? %1)) (vdom/children node))]
    (println blocks inlines)
    (-> [:div {:class "flex flex-col"}] ;; outer :div
        (into (or blocks nil))
        (into [(into [:div {:class "flex flex-row bg-slate-100"}] inlines)]))))

(defn- Text
  "Render a text element. By default preserve white spaces."
  [node]
  (transform node {:tag :span :attr {:class "whitespace-pre"}}))

(def supported-elements
  ^{:doc "Aliases mapped to DOM elements that take the form:
    :alias -> {:transform (fn [node]) :type block|inline} "}
  {;; Aliases that map to block level elements
   ;; https://developer.mozilla.org/en-US/docs/Web/HTML/Block-level_elements
   :Para {:transform Para :type "block"}
   :address {:transform #(identity %1) :type "block"}
   :article {:transform #(identity %1) :type "block"}
   :aside {:transform #(identity %1) :type "block"}
   :blockquote {:transform #(identity %1) :type "block"}
   :Quote {:transform #(assoc %1 0 :blockquote) :type "block"}
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
   :Style {:transform #(style %1) :type "inline"}
   :escape {:transform #(stringify %1) :type "inline"}
   :Text {:transform Text :type "inline"}
   :span {:transform #(identity %1) :type "inline"}
   :Highlight {:transform #(assoc %1 0 :span) :type "inline"}
   :Bold {:transform #(assoc %1 0 :b) :type "inline"}
   :b {:transform #(identity %1) :type "inline"}
   :Break {:transform #(assoc %1 0 :br) :type "inline"}
   :br {:transform #(identity %1) :type "inline"}
   :i {:transform #(identity %1) :type "inline"}
   :Italic {:transform #(transform %1
                                   {:tag :p
                                    :attr {:class "italic"}})
          :type "inline"}
   :Strike {:transform #(assoc %1 0 :s) :type "inline"}
   :s {:transform #(identity %1) :type "inline"}
   :Super {:transform #(assoc %1 0 :sup) :type "inline"}
   :sup {:transform #(identity %1) :type "inline"}
   :sub {:transform #(identity %1) :type "inline"}
   :a {:transform #(identity %1) :type "inline"}
   :abbr {:transform #(identity %1) :type "inline"}
   :acronym {:transform #(identity %1) :type "inline"}
   :audio {:transform #(identity %1) :type "inline"}
   :bdi {:transform #(identity %1) :type "inline"}
   :bdo {:transform #(identity %1) :type "inline"}
   :big {:transform #(identity %1) :type "inline"}
   :button {:transform #(identity %1) :type "inline"}
   :canvas {:transform #(identity %1) :type "inline"}
   :cite {:transform #(identity %1) :type "inline"}
   :code {:transform #(identity %1) :type "inline"}
   :img {:transform #(identity %1) :type "inline"}
   })

(defn- compile-node [node]
  ((:transform (supported-elements (vdom/tag node)
                                   {:transform #(stringify %1)})) node))

;; Tree = [:component {attributes} [child1] [child2]]
(defn compile
  "Compile a spec tree into a reagent tree."
  [tree]
  ;; (println "tree: " (meta tree))
  (cond
     ;; Return literals as they are
    (string? tree) tree
     ;; Return attributes as is
    (map? tree) tree
     ;; an escape hatch
    (vector? (nth tree 0)) (reduce conj [:div ] (map compile tree))
     ;; map apsara to reagent
    (false? (vdom/children? tree)) (compile-node tree)
    (true? (vdom/children? tree)) (let [parent (compile-node tree)] ;; compile parent node
                                    (into [(nth parent 0)] ;; merge compiled parent node with
                                          (map compile (into [] (rest parent))))) ;; compiled children
  ))
