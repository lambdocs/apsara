(ns apsara.vdom
  (:require
   [reagent.core :as r]
   [clojure.string :as string]))

(def spec
  "The spec is our data structure to maintain editor state.
  spec is specified in hiccup.
  Note: Why we don't use zippers
  Zippers are neat. But they don't have a way to jump to
  a random node in the tree. We need to jump to a random
  node when the user clicks on a corresponding DOM element.
  For these cases, using zippers, we would have to search
  the tree for the given element, an O(log n) operation."
  (r/atom [:Para {:parent nil :id 0}]))
(def spec-list [])
(def node-lookup
  "To speed up random access to the tree we have this lookup
  table. The flow is like so:
  1. User clicks on a dom node on which we have registered a
     mouse-click event handler.
  2. The browser would have changed the cursor position which
     we get by calling the getSelection API. We store the
     selection in cursor/selection.
  3. When the next key is pressed we get the DOM node's id
     which we independently track in our hiccup data structure."
  (atom {}))
(def ROOT-ID 0)
(def id-counter (atom ROOT-ID))

(defn new-spec
  "Return a new spec instance presumably for a new editor instance."
  []
  (conj spec-list (r/atom nil)))

(defn reset-spec!
  "Reset spec"
  []
  (reset! spec [:para {:parent nil :id 0}])
  (reset! node-lookup {}))

(defn reset-id-counter! []
  (reset! id-counter ROOT-ID))

;; Spec modification functions
(defn new-id
  ([]
   "Return a unique id."
   ;;(str (cljs.core/random-uuid)))
   (str (reset! id-counter (inc @id-counter))))
  ([old-id]
   "Return old id if it is not being used."
   (println "lookup" "old-id" (get @node-lookup old-id))))
   ;;(if (not (get @node-lookup old-id))
    ;; old-id
    ;; (new-id))))

(defn attr?
  "Returns true if node has attributes."
  [node]
  (and
   (vector? node)
   (> (count node) 1)
   (map? (nth node 1))))

(defn attr
  "Return the node's attributes."
  [node]
  (if (attr? node)
    (nth node 1)
    {}))

(defn children?
  "Return if node has children, where a child is a string or a vector."
  [node]
  (if (attr? node)
    (>= (count node) 3)
    (>= (count node) 2)))

(defn children
  "Return children of node, where a child is a string or a vector. "
  [node]
  (if (attr? node)
    (subvec node 2 (count node))
    (subvec node 1 (count node))))

(defn tag
  "Returns the type of node."
  [node]
  (nth node 0))

(defn leaf?
  "Returns if node is a leaf node. This is different from (not (children? node)).
  (children? node) returns true even if there are child strings, (not (leaf? node))
  returns true if there is a child vector."
  [node]
  (if (attr? node)
    (let [t (type (nth node 2))]
      (and (not= t cljs.core/PersistentVector) (not= t cljs.core/Subvec)))
    (let [t (type (nth node 1))]
      (and (not= t cljs.core/PersistentVector) (not= t cljs.core/Subvec)))))

(defn with-attr
  "Merges nattr to node's attributes."
  [node nattr]
  (if (attr? node)
    (assoc node 1 (conj (attr node) nattr))
    (into (subvec node 0 1) (into [nattr] (rest node)))))

(defn node-from-id
  "Return the node with given picljid"
  [id]
  (get @node-lookup id))

(defn picljid
  "Return the piclj id for a given node"
  [node]
  (if node
    (get (attr node) :id)
    nil))

(defn cache-it!
  "Cache node in node-lookup table"
  [node]
  ;; (println "Cacheing" node)
  (reset! node-lookup (assoc @node-lookup (picljid node) node)))

(defn uncache-it!
  "Remove node from cache"
  [node]
  (reset! node-lookup (dissoc @node-lookup (picljid node))))

(defn annotate-node
  "Set node id and parent attributes for a node."
  [node, parent-id]
  (let [id (new-id)]
    (assert (not (node-from-id id)) (str id " is already an id"))
    (cache-it! (with-attr node {:parent parent-id :id id}))
    (get @node-lookup id)))

(defn parent-of
  "Get parent of a node."
  [node]
  (if (attr? node)
    (node-from-id (get (attr node) :parent))
    nil))

(defn root?
  "Returns if node is root of the tree."
  [node]
  (= (parent-of node) nil))

(defn spec-of
  "For a particular node in a (sub) tree get its spec."
  [node]
  (if (root? node)
    (:spec (attr node))))

(defn siblings
  "Return the siblings of node including node."
  [node]
  (children (parent-of node)))

(defn index-of
  "Return the child-index of node in parent, nil if node is not a child of
   the parent."
  [node]
  (let [same-depth (siblings node)
        res (filter
             #(= (picljid (second %)) (picljid node))
             (map-indexed list same-depth))]
    (if (empty? res)
      nil
      (first (first res)))))

(defn last-child?
  "Returns if node is the last (right most) child of its parent."
  [node]
  (let [same-depth (children (parent-of node))]
    (= (picljid node) (picljid (last same-depth)))))

(defn nth-child
  "Return the n'th child of node."
  [node, n]
  (if (attr? node)
    (if (> n (- (count node) 3))
      nil
      (nth node (+ n 2)))
    (if (> n (- (count node) 2))
      nil
      (nth node (+ n 1)))))

(defn- append-child
  "Append node as the right most child of parent."
  [parent node]
  (conj parent node))

(defn- replace-child
  "Insert node as a child of parent at index replacing the previous child."
  [parent index node]
  (assoc parent
         (+ index 2) ;; Account for key and attributes in the parent
         node))

(defn- splice-in
  "Splice in node at index of parent. Children that are at and after index
   are shifted right."
  [parent index node]
  (into []
        (into (subvec parent 0 (+ index 2))
              (into [node] (subvec parent (+ index 2) (count parent))))))

(defn- add-child
  "Add node as child of parent after right-sibling."
  [parent node right-sibling]
  (if (not (= (index-of node) nil))
    (replace-child parent (index-of node) node)
    (if (= right-sibling nil)
      (append-child parent node)
      (splice-in parent (index-of right-sibling) node))))

(defn- bubble-up
  "Called when a node is added/changed in the tree. "
  [parent node right-sibling]
  (if (= parent nil) ;; We're at root
    (reset! spec node)
    (let [grand-parent (parent-of parent)
          parent-mod (add-child parent node right-sibling)]
      (cache-it! parent-mod)
      (bubble-up grand-parent parent-mod nil))))

(defn add-node!
  ([parent-id node right-sibling-id]
   "Add node as a child of parent just before right-sibling. If
   right-sibling is null add node as the rightmost child of parent.
   Return the id of the added node."
   (let [annotated (annotate-node node parent-id)]
     (bubble-up (node-from-id parent-id) annotated (node-from-id right-sibling-id))
     (picljid annotated)))
  ([parent-id node]
   "right-sibling-id can be omitted."
   (add-node! parent-id node nil)))

(defn- remove-child
  "Remove child from parent."
  [parent node]
  (if (last-child? node)
    (into [] (subvec parent 0 (dec (count parent))))
    (into []
          (concat
           (subvec parent 0 (+ (index-of node) 2))
           (subvec parent (+ (index-of node) 3) (count parent))))))

(defn- bubble-up-remove
  "Called when a node is removed in the tree."
  [node]
  (if (= (parent-of node) nil)
    (reset! spec node)
    (let [parent (parent-of node) parent-mod (remove-child parent node)]
      (cache-it! parent-mod)
      (uncache-it! node)
      (bubble-up (parent-of parent-mod) parent-mod nil))))

(defn remove-node!
  "Remove node with picljid id."
  [id]
  (bubble-up-remove (node-from-id id)))

(defn replace-node!
  "Replace (node-from-id id) with new-node"
  [id new-node]
  (let [node (node-from-id id)
        index (index-of node)
        parent (parent-of node)
        right-sibling (nth-child parent (inc index))]
    (remove-node! id)
    (add-node! (picljid parent) new-node (picljid right-sibling))))

(defn mod-str!
  "Replace the string in a leaf node with str."
  [node, str]
  (if (leaf? node)
    (if (attr? node)
      (let [mod-node (assoc node 2 str)]
        (cache-it! mod-node)
        (bubble-up (parent-of node) mod-node nil))
      (let [mod-node (assoc node 2 str)]
        (cache-it! mod-node)
        (bubble-up (parent-of node) mod-node nil)))))

(defn strlen
  "Return string length of node. If not a leaf node return 0."
  [id]
  (let [node (node-from-id id)]
    (if (leaf? node)
      (count (nth-child node 0))
      0)))

(defn to-string
  "Return the string representation of node."
  [id]
  (let [node (node-from-id id)]
    (cond
      (leaf? node) (nth-child node 0)
      ;; (= (tag node) :Para) "\n"
      :else "")))

(defn rightmost-child-of
  "Return the right most child leaf node of id."
  [id]
  (let [node (node-from-id id)]
    (if (not (leaf? node))
      (let [ch (children node)]
        (rightmost-child-of (picljid (nth ch (dec (count ch))))))
      (picljid node))))

(defn left-of
  "Return the leaf node to the left of id."
  [id]
  (let [node (node-from-id id) index (index-of node)]
    (if (= index 0)
      (left-of (picljid (parent-of node))) ;; Check left of parent
      (rightmost-child-of (picljid (nth-child (parent-of node)
                                              (- index 1))))))) ;; Left sibling

(defn leftmost-child-of
  "Return the left most leaf child node of id."
  [id]
  (let [node (node-from-id id)]
    (if (not (leaf? node))
      (let [ch (children node)]
        (leftmost-child-of (picljid (nth ch 0))))
      (picljid node))))

(defn right-of
  "Return the leaf node to the right of id. If no node exists to the right then return id."
  [id]
  (let [node (node-from-id id)
        index (index-of node)
        parent (parent-of node)]
    (if (last-child? node)
      (right-of (picljid parent)) ;; Check right of parent
      (leftmost-child-of (picljid (nth-child parent
                                             (+ index 1))))))) ;; Right sibling

(defn right-sibling
  "Return the right sibling of node "
  [id]
    (let [node (node-from-id id)
        index (index-of node)
        parent (parent-of node)]
      (if (last-child? node)
        nil
        (nth-child parent (+ index 1)))))

(defn insert-char-at!
  "Insert char in string for node represented by id at offset. Return the new string."
  [id c offset]
  (let [node (node-from-id id)
        s (to-string id)
        new-str (string/join [(subs s 0 offset) c (subs s offset (count s))])]
    (println "new-str" new-str)
    (mod-str! node new-str)
    new-str))

(defn delete-char-at!
  "Delete char in node represented by id at offset."
  [id offset]
  (let [node (node-from-id id) s (to-string id)]
    (mod-str! node (string/join [(subs s 0 offset)
                                 (subs s (inc offset) (count s))]))))

(defn shell-out!
  "Remove all text elements and children from node keeping only its type and attributes."
  [id]
  (let [node (node-from-id id)]
    (replace-node! id (into [] (subvec node 0 2)))))

(defn- split-string
  "Split string s into 2 parts. If any part is the null string, then
   return a single <space>."
  [s offset]
  (cond
    (= offset 0) ["_" s]
    (= offset (count s)) [s "_"]
    :else [(subs s 0 offset) (subs s offset (count s))]))

(defn- split-node
  "Split the node into 2 at (id, offset) and return the [left-node, right-node]
   pair without altering the vdom."
  [id offset]
  (let [node (node-from-id id)
        empty-node (into [] (subvec node 0 2))
        s (to-string id)
        str (split-string s offset)]
    [(conj empty-node (first str))
     (conj empty-node (last str))]))

(defn split-subtree-at
  "Split the tree into 2 at (id, offset) and return the [left-tree, right-tree]
   pair without altering the vdom."
  [id offset]
  (let [node (node-from-id id)
        index (index-of node)
        sibs (siblings node)
        [part1 part2] (partition-by #(< (index-of %) index) sibs)
        [leaf1 leaf2] (split-node id offset)]
    ;; (println "part1" part1 "\npart2" part2)
    ;; (println "leaf1" leaf1 "\nleaf2" leaf2)
    (if part2
      [(conj (into [] part1) leaf1) (assoc (into [] part2) 0 leaf2)]
      [[leaf1] (assoc (into [] part1) 0 leaf2)])))

(defn transplant!
  "Remove list of node(s) in nlist from their parent(s) and move them all
   under new-parent-id"
  [nlist new-parent-id]
  (doseq [node nlist] (remove-node! (picljid node)))
  (doseq [node nlist] (add-node! new-parent-id node)))

(defn substitute!
  "Substitute the node with list of node(s) in nlist while copying the attributes
   of node to nlist."
  [id nlist]
  (let [node (node-from-id id)
        parent-id (picljid (parent-of node))
        right (picljid (right-sibling id))]
    (remove-node! id)
    (doseq [n nlist] (add-node! parent-id (with-attr n (merge (attr node) (attr n))) right))))

(defn fork!
  "For the given node remove any text and create children with node(s) in nlist."
  [id nlist]
  (let [new-id (shell-out! id)]
    (doseq [n nlist] (add-node! new-id n))))

(defn enclose!
  "Enclose node (id) in enclosing-node. ie. make enclosing-node the new parent of id."
  [id enclosing-node]
  (let [enclosed-node (node-from-id id)
        parent (parent-of enclosed-node)
        right (picljid (right-sibling id))]
    (remove-node! id)
    (add-node!
     (add-node! (picljid parent) enclosing-node right)
     enclosed-node)))

(defn- walk-impl
  "Recursive implementation of walk"
  [node func]
  (if (leaf? node)
    (func (picljid node))
    (map #(walk-impl % func) (children node))
  ))

(defn walk
  "Walk the (sub)tree in depth first order, applying func"
  [id func]
  (walk-impl (node-from-id id) func))

(defn on-spec-change [key reference old new]
  ;; (println "spec changed")
  ;; (println "Old" old)
  ;; (println "New" new)
  ;; (walk "1" #(to-string %))
  )

(add-watch spec :decorator on-spec-change)
