(ns portfolio.graph
  (:require
    [om.core :as om :include-macros true]
    [portfolio.price-utils :as price]
    [portfolio.util :as util]
    [om-bootstrap.grid :as g]
    [om-tools.dom :as d :include-macros true]))

(defn app-state->date-labels [app]
  (-> app
      :data
      first
      second
      :dates))

(defn window-input
  [date-labels selected-date input]
  (let [sorted-labels (apply sorted-set date-labels)
        sub-labels    (subseq (apply sorted-set date-labels)
                              <=
                              (:end-date selected-date))
        to-take       (Math/abs (:range selected-date))]
    (take-last to-take (take (count sub-labels) input))))

(defn construct-prices [labels prices sel-prices]
    (if (empty? prices)
      {:labels   labels
       :series   [(repeat (count labels) 0.0)]}
      (let [cat-prices     (concat [(apply map + prices)] sel-prices)
            all-prices     (filter (complement price/all-zeros?) cat-prices)
            scaled-prices  (map util/scale-seq all-prices)]

        (if (> (count all-prices) 1)
          {:labels   labels
           :series   scaled-prices}
          {:labels   labels
           :series   all-prices}))))

(defn graph-input-data
  [input date-labels]
  (let [window-labels (window-input date-labels (:selected-date input) date-labels)
        labels        (take-nth (/ (count window-labels)
                                   (min (count window-labels) 9))
                                window-labels)

        mult-price    (fn [[name data]]
                        (map (fn [elem] (* (:amount data) elem))
                             (window-input date-labels
                                           (:selected-date input)
                                           (:prices (get (:data input) name)))))

        prices        (map mult-price (:components input))

        selected      (filter (fn [[name data]]
                                (:selected data))
                              (:components input))

        sel-prices    (map (fn [[name data]]
                             (window-input date-labels
                                           (:selected-date input)
                                           (:prices (get (:data input) name))))
                           selected)]

    (construct-prices labels prices sel-prices)))

(defn draw [element input-data opts]
  (let [constructor  (:constructor opts)]
    (constructor.
      element
      (clj->js input-data)
      (clj->js (:graph-opts opts))
      (clj->js (:responsive-opts opts)))))

(defn output-legends [present selected]
  (if (empty? present)
    selected
    (concat ["Portfolio"] selected)))

(defn build-legend [components]
  (let [name-mapper     (map (fn [[a-name _]] a-name))
        amount-filter   (filter (fn [[_ elem]] (> (:amount elem) 0)))
        selected-filter (filter (fn [[_ elem]] (:selected elem)))
        name-amount     (comp amount-filter name-mapper)
        name-selected   (comp selected-filter name-mapper)
        present         (transduce name-amount conj [] components)
        selected        (transduce name-selected conj [] components)]
  (d/div {:class "ct-legend list-group"}
         (map-indexed (fn [index legend]
                        (d/li {:class (str "list-group-item ct-legend-text ct-series-" index)} legend))
                      (output-legends present selected)))))

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
      (d/div (clj->js (:js opts))))))
