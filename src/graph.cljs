(ns portfolio.graph
  (:require
    [om.core :as om :include-macros true]
    [om.dom :as dom :include-macros true]
    [portfolio.util :as util]))

(defn graph-input-data
  [input date-labels]
  (let [labels        (take-nth (/ (count date-labels) 9) date-labels)
        mult-price    (fn [[name amount]]
                        (map (fn [elem] (* amount elem))
                             (:prices (get (:data input) name))))

        prices        (map mult-price (:components input))]

    (if (empty? prices)
      {:labels   labels
       :series   [(repeat (count labels) 0.0)]}
      {:labels   labels
       :series   [(apply map + prices)]})))

(defn draw [element input-data opts]
  (let [constructor  (:constructor opts)]
    (constructor.
      element
      (clj->js input-data)
      (clj->js (:graph-opts opts))
      (clj->js (:responsive-opts opts)))))

(defn graph-view [app owner opts]
  (reify
    om/IDidMount
    (did-mount [this]
      (draw (om/get-node owner) app opts))

    om/IDidUpdate
    (did-update [this prev-props prev-state]
      (draw (om/get-node owner) app opts))

    om/IRenderState
    (render-state [this state]
      (dom/div (clj->js (:js opts))))))
