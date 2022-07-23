(ns apsara.parser
  (:require
   [instaparse.core :as insta]
   [apsara.vdom :as vdom]
   [apsara.grammar.hiccup :as hic]))

(defn get-parser []
  (insta/parser hic/grammar))

(def hiccup-parser (get-parser))

(defn- seq-to-map [v]
  "Convert a seq, like [:color \"red\" :font \"aria\"] to a map
   {:color \"red\" :font \"aria\"}."
  (let [keys (for [[x y] (keep-indexed #(vector %1 %2) v) :when (even? x)] y)
        vals (for [[x y] (keep-indexed #(vector %1 %2) v) :when (odd? x)] y)]
    (zipmap keys vals)))

(defn- parse-hiccup [c]
  (->> (hiccup-parser c)
       (insta/transform
        {:hiccup vector
         :keyword (fn [& s] (keyword (reduce str s)))
         :string str
         :map (fn [& args] (seq-to-map args))
         :mixed-string (fn [& args] (vector :Text (apply str args)))
         :mixed-hiccup (fn [& args] args)
         :instaparse/failure #(vector :Text %1)})))

(defn parse-impl [txt]
  "If the user enters hiccup in the editor, then parse"
  (let [ast (parse-hiccup txt)]
    (if (insta/failure? ast)
      nil
      ast)))

(defn parse [changed-node]
  "Check for embedded code and if found modify the DOM."
  (let [id (:id changed-node)
        ast (parse-impl (vdom/to-string id))]
    (println ast)
    (and ast (vdom/substitute! id ast))))
