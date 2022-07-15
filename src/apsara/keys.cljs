(ns apsara.keys
  (:require
   [apsara.cursor :as cursor]))

(defn prevent-default [event]
  "Block the default behavior."
  (. event preventDefault))

;; Lookup table for key press.
;; https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/key/Key_Values"
(def default-key-dispatch
  {
   ;; Navigation keys
   "ArrowRight" #() ;; Browser does its thing
   "ArrowLeft" #() ;; Browser does its thing
   "ArrowDown" #() ;; Browser does its thing
   "ArrowUp" #() ;; Browser does its thing
   "End" #() ;; Browser does its thing
   "Home" #() ;; Browser does its thing
   "PageDown" #() ;; Browser does its thing
   "PgDn" #() ;; Browser does its thing
   "PageUp" #() ;; Browser does its thing
   "PgUp" #() ;; Browser does its thing
   ;; Whitespace keys
   "Enter" (fn [event] (prevent-default event) (cursor/enter))
   "Tab" #(prevent-default %1)
   " " (fn [event] (prevent-default event) (cursor/emit " "))
   ;; Modifier keys
   "Alt" #() ;; Browser does its thing
   "AltGraph" #() ;; Browser does its thing
   "CapsLock" #() ;; Browser does its thing
   "Control" #() ;; Browser does its thing
   "Fn" #() ;; Browser does its thing
   "FnLoc" #() ;; Browser does its thing
   "Hyper" #() ;; Browser does its thing
   "Meta" #() ;; windows key  ;; Browser does its thing
   "NumLock" #() ;; Browser does its thing
   "ScrollLock" #() ;; Browser does its thing
   "Shift" #() ;; Browser does its thing
   "Super" #() ;; Browser does its thing
   "Symbol" #() ;; Browser does its thing
   "SymbolLock" #() ;; Browser does its thing
   ;; Editing keys
   "Backspace" (fn [event] (prevent-default event) (cursor/backspace))
   "Clear" #(prevent-default %1)
   "Copy" #(prevent-default %1)
   "CrSel" #(prevent-default %1)
   "Delete" (fn [event] (prevent-default event) (cursor/delete))
   "EraseEof" #(prevent-default %1)
   "ExSel" #(prevent-default %1)
   "Insert" #(prevent-default %1)
   "Paste" #(prevent-default %1)
   "Redo" #(prevent-default %1)
   "Undo" #(prevent-default %1)
   ;; UI Keys
   "Accept" #(prevent-default %1)
   "Again" #(prevent-default %1)
   "Attn" #(prevent-default %1)
   "Cancel" #(prevent-default %1)
   "ContextMenu" #(prevent-default %1)
   "Escape" #(prevent-default %1)
   "Execute" #(prevent-default %1)
   "Find" #(prevent-default %1)
   "Finish" #(prevent-default %1)
   "Help" #(prevent-default %1)
   "Pause" #(prevent-default %1)
   "Play" #(prevent-default %1)
   "Props" #(prevent-default %1)
   "Select" #(prevent-default %1)
   "ZoomIn" #(prevent-default %1)
   "ZoomOut" #(prevent-default %1)
   ;; Device keys
   "BrightnessDown" #() ;; Browser does its thing
   "BrightnessUp" #() ;; Browser does its thing
   "Eject" #() ;; Browser does its thing
   "LogOff" #() ;; Browser does its thing
   "Power" #() ;; Browser does its thing
   "PowerOff" #() ;; Browser does its thing
   "PrintScreen" #() ;; Browser does its thing
   "Hibernate" #() ;; Browser does its thing
   "Standby" #() ;; Browser does its thing
   "WakeUp" #() ;; Browser does its thing
   ;; Input Method Editor keys
   "AllCandidates" #() ;; Browser does its thing
   "Alphanumeric" #() ;; Browser does its thing
   "CodeInput" #() ;; Browser does its thing
   "Compose" #() ;; Browser does its thing
   "Convert" #() ;; Browser does its thing
   "Dead" #() ;; Browser does its thing
   "FinalMode" #() ;; Browser does its thing
   "GroupFirst" #() ;; Browser does its thing
   "GroupLast" #() ;; Browser does its thing
   "GroupNext" #() ;; Browser does its thing
   "GroupPrevious" #() ;; Browser does its thing
   "ModeChange" #() ;; Browser does its thing
   "NextCandidate" #() ;; Browser does its thing
   "NonConvert" #() ;; Browser does its thing
   "PreviousCandidate" #() ;; Browser does its thing
   "Process" #() ;; Browser does its thing
   "SingleCandidate" #() ;; Browser does its thing
   ;; Unidentified
   "Unidentified" #() ;; Browser does its thing
   })

;; Initialize key-dispatch to default key dispatch table
(def key-dispatch (clojure.core/atom default-key-dispatch))
;; key-after-hooks is a queue of user defined hooks called after
;; the given key's event is processed
(def key-after-hooks (clojure.core/atom {}))

(defn set-key-dispatch [table]
  "Set the key-dispatch to table."
  (reset! key-dispatch table))

(defn set-hooks [table hooks]
  "Enqueue hooks on table. hooks is a map from key -> fn. For example
   the following invokation will increment a counter when RightArrow is
   pressed and decrement it when LeftArrow is pressed.
   (set-key-after-hooks {\"ArrowRight\" #(reset! counter (inc @counter))
                         \"ArrowLeft\" #(reset! counter (dec @counter))})"
  (doseq [item hooks]
    ;; (println "Adding " item " to " @table)
    (reset! table
            (assoc @table
                   (key item)
                   (conj (get @table (key item) []) (val item))))))

(defn set-key-after-hooks [hooks]
  "Enqueue key-after hooks. hooks is a map from key -> fn."
  (set-hooks key-after-hooks hooks))

(defn printable? [event]
  "Is the key printable? See https://stackoverflow.com/questions/4194163/detect-printable-keys"
  (== event.key.length 1))

(defn call-hooks-for [table event]
  "Call enqueued hooks stored in table for event."
  ;; (println "call-hooks-for" event.key (get @table event.key []))
  (apply #(and %1 (%1 (assoc (cursor/current-cursor) :event event))) (get @table event.key []))
  (and ;; Call hooks for printable class of keys
   (printable? event)
   (apply #(and %1 (%1 (assoc (cursor/current-cursor) :event event))) (get @table "Printable" []))))

(defn key-action [event]
  "Key dispatcher."
  ;; Do work on keyed event
  ((get @key-dispatch event.key (fn [ev] (prevent-default ev) (cursor/emit ev.key)))
   event)
  ;; Call key-after hook
  (call-hooks-for key-after-hooks event))

(defn keydown [event]
  "Function to be called on key press event."
  ;; (. js/console log event.key)
  (key-action event))
