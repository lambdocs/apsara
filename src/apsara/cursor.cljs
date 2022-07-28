(ns apsara.cursor
  (:require
   [reagent.core :as r]
   [apsara.vdom :as vdom]))

;; Cursor and selection
(def selection (r/atom nil))
(def cursor (r/atom nil))

(defn container-from-id
  "Return the dom container for node."
  [id]
  (. (. js/document getElementById id) -lastChild))

;; Cursor functions
(defn current-cursor
  "Get params related to current cursor position."
  []
  (if (= @selection nil)
    {:range nil :container nil :offset 0 :id -1 :length 0}
    (let [range (. @selection (getRangeAt 0))
          container (. range -startContainer)
          offset (. range -startOffset)
          id (js->clj (. (. container -parentNode) -id))
          length (vdom/strlen id)]
      {:range range :container container :offset offset :id id :length length})))

(defn set-cursor [id offset]
  "Set the cursor to the given node and offset."
  (let [container (container-from-id id)
        range (. @selection (getRangeAt 0))]
    (. range (setStart container offset))
    (. range (collapse true))
    (reset! cursor (current-cursor))))

(defn set-cursor-pessimistic [id offset]
  "Set cursor before and after next render. If :disable-immediate is true in options
  then set cursor only after render."
  ;; (println "set-cursor-pessimistic" id offset)
  (set-cursor id offset)
  (r/after-render #(set-cursor id offset)))

(defn set-cursor-post-render [id offset]
  "Set cursor only after render is complete."
  )

(defn set-selection []
  "Called from mouse click events."
  (let [sel (. js/window getSelection)]
    ;; (. js/console log sel)
    (reset! selection sel)
    (reset! cursor (current-cursor))))

(defn set-selection-left-of [id]
  "Set selection to a range to the left of id."
  (let [left-id (vdom/left-of id)
        offset (vdom/strlen left-id)]
    (set-cursor left-id offset)))

(defn move-cursor-left []
  "Move cursor count characters to the left."
  (let [{offset :offset id :id} (current-cursor)]
    (if (> offset 0)
      (set-cursor id (dec offset))
      (set-selection-left-of id))))

(defn set-selection-right-of [id]
  "Set selection to a range to the left of id."
     (set-cursor (vdom/right-of id) 0))

(defn move-cursor-right []
  "Move cursor count characters to the right."
  (let [{offset :offset length :length id :id} (current-cursor)]
    (if (< offset length)
      (set-cursor id (inc offset))
      (set-selection-right-of id))))

(defn delete-char-at [id offset]
  "Delete char in node(id) at offset."
  (let [{length :length} (current-cursor)]
    ;; (println id offset length)
    (if (= length 1)
      (let [left-id (vdom/left-of id)
            left-len (vdom/strlen left-id)
            parent (vdom/parent-of (vdom/node-from-id id))]
        (vdom/remove-node! id)
        (and (= (count (vdom/children parent)) 1) (vdom/remove-node! (vdom/picljid parent)))
        (set-cursor-pessimistic left-id left-len))
      (let []
        (vdom/delete-char-at! id offset)
        (set-cursor-pessimistic id offset)))))

(defn backspace []
  "Delete 1 char to the left."
  (let [{id :id offset :offset length :length} (current-cursor)]
    ;; (println "backspace" id offset length)
    (if (> offset 0)
      (delete-char-at id (dec offset))
      (let [left-id (vdom/left-of id)
            length (vdom/strlen left-id)]
        (delete-char-at left-id (dec length))))))

(defn delete []
  "Delete 1 char to the right of."
    (let [{id :id offset :offset length :length} (current-cursor)]
      ;; (println offset length)
      (if (<= offset (dec length))
        (delete-char-at id offset)
        (let []
          (delete-char-at (vdom/right-of id) 0)))))

(defn set-cursor-timed [id offset ms]
  "Call set-cursor after ms milliseconds."
  ;; (println "set-cursor-timed" id offset)
  (js/setTimeout #(set-cursor id offset) ms))

(defn emit [key]
  "Emit the string represented by key into current cursor position"
  (let [{id :id offset :offset} (current-cursor)
        mod-str (vdom/insert-char-at! id key offset)]
    ;; (. js/console log range lcontainer ":offset" offset ":id" id ":length" length)
    (if (< (inc offset) (count mod-str))
      (set-cursor-pessimistic id (inc offset))
       ;; A hack for react rendering time. This will break if a sequence of keys are
       ;; pressed very fast extending the lenghth of a string. An ideal fix would be to
       ;; set the cursor offset using the componentDidMount callback.
      (set-cursor-timed id (inc offset) 5))))

(defn enter []
  "Insert a new :para element."
  (let [{id :id offset :offset} (current-cursor)
        part (vdom/split-subtree-at id offset)
        node (vdom/node-from-id id)
        parent (vdom/parent-of node)
        grandparent (vdom/parent-of parent)
        right-node (vdom/nth-child grandparent (+ (vdom/index-of parent) 1))
        new-parent-id (vdom/add-node! (vdom/picljid grandparent)
                                      [:Para]
                                      (vdom/picljid right-node))]
    ;; (println right-node new-parent-id)
    (vdom/transplant! (last part) new-parent-id)
    (vdom/add-node! (vdom/picljid parent) (last (first part)))
    (set-cursor-timed (vdom/leftmost-child-of new-parent-id) 0 100)))
    ;; (set-cursor-pessimistic new-parent-id 0)))
