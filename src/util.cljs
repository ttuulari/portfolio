(ns portfolio.util)

(defn log [v & text]
  (let [vs (if (string? v)
             (apply str v text)
             v)]
    (. js/console (log (clj->js vs)))
    v))

(defn to-fixed [number precision]
  (.toFixed number precision))
