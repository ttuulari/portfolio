(ns portfolio.portfolio-summary
  (:require
    [cljs.core.async :as async
      :refer [<! >! chan put! sub]]
    [om.core :as om :include-macros true]
    [om-bootstrap.panel :as p]
    [portfolio.util :as util]
    [portfolio.graph :as graph]
    [om-tools.dom :as d :include-macros true])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(defn yield [first-price last-price]
  (if (zero? last-price)
    0
    (/ last-price first-price)))

(defn build-summary
  [prices]
    {:value   (last prices)
     :yield   (yield
                (first prices)
                (last prices))})

(defn portolio-summary-data
  [input]
  (let [mult-price    (fn [[name amount]]
                        (map (fn [elem] (* amount elem))
                             (:prices (get-in input [:data name]))))

        prices        (map mult-price (:components input))
        total-prices  (if (empty? prices)
                        []
                        (apply map + prices))
        window-prices (graph/window-input (graph/app-state->date-labels input)
                                          (:selected-date input)
                                          total-prices)]
    (if (empty? window-prices)
      {:value   0.0
       :yield   0.0}
      (build-summary window-prices))))

(defn portfolio-summary-view [app owner]
  (reify
    om/IRenderState
    (render-state [this state]
    (let [data           (portolio-summary-data app)
          selected-date  (:selected-date app)]
      (d/div {:class "portfolio-summary"}
             (p/panel
              {:header "Your Portfolio"
               :list-group (d/ul {:class "list-group"}
                                 (d/li {:class "list-group-item"}
                                       (str "End date: " (:end-date selected-date)))
                                 (d/li {:class "list-group-item"}
                                       (str "Start date: "
                                       (util/date-delta-days->str (:end-date selected-date)
                                                                  (:range selected-date))))
                                 (d/li {:class "list-group-item"}
                                       (str "Value $ " (util/to-fixed (:value data) 2)))
                                 (d/li {:class "list-group-item"}
                                       (str "Yield % " (util/to-fixed (:yield data) 2)))
                                 )}
              nil))))))
