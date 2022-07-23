(ns apsara.utils.core)

(defn has-script?
  "See if current HTML page's <HEAD> has a <SCRIPT> tag with src set to url"
  [url]
  (if (= (. js/document getElementById (str (hash url))) nil)
  false
  true))

(defn load-script
  "Push an script tag with url as a child of the head tag. This is
  equivalent to the html:
  <Head>
    <Script type=\"text/javascript\" src=\"url\" />
  </Head>"
  [url]
  (let [h (aget (. js/document getElementsByTagName "head") 0)
        ]
    (if (has-script? url)
      nil
      (let [s (. js/document createElement "script")]
        (set! (. s -src) url)
        (set! (. s -type) "text/javascript")
        (set! (. s -id) (str (hash url)))
        (. h (appendChild s))))))
