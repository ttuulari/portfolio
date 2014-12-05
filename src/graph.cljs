(ns portfolio.graph
  (:require
    [om.core :as om :include-macros true]
    [om.dom :as dom :include-macros true]
    [portfolio.util :as util]))

(defn graph-input-data
  [input date-labels]
  (let [sorted-labels (apply sorted-set date-labels)
        sub-labels    (subseq (apply sorted-set date-labels)
                              <=
                              (get-in input [:selected-date :end-date]))
        to-take       (Math/abs (get-in input [:selected-date :range]))
        window-labels (take-last to-take sub-labels)
        labels        (take-nth (/ (count window-labels)
                                   (min (count window-labels) 9))
                                window-labels)

        mult-price    (fn [[name amount]]
                        (map (fn [elem] (* amount elem))
                             (take-last to-take
                                        (take (count sub-labels)
                                              (:prices (get (:data input) name))))))
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
