(ns portfolio.portfolio-summary
  (:require
    [cljs.core.async :as async
      :refer [<! >! chan put! sub]]
    [om.core :as om :include-macros true]
    [om-bootstrap.panel :as p]
    [portfolio.util :as util]
    [om-tools.dom :as d :include-macros true])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(defn yield [first-price last-price]
  (if (zero? last-price)
    0
    (/ last-price first-price)))

(defn build-summary
  [prices]
  (let [total-prices   (apply map + prices)]
    {:value   (last total-prices)
     :yield   (yield
                (first total-prices)
                (last total-prices))}))

(defn portolio-summary-data
  [input]
  (let [mult-price    (fn [[name amount]]
                        (map (fn [elem] (* amount elem))
                             (:prices (get (:data input) name))))

        prices        (map mult-price (:components input))]
    (if (empty? prices)
      {:value   0.0
       :yield   0.0}
      (build-summary prices))))

(defn portfolio-summary-view [app owner]
  (reify
    om/IRenderState
    (render-state [this state]
    (let [data   (portolio-summary-data app)]
      (d/div {:class "portfolio-summary"}
             (p/panel
              {:header "Your portfolio"
               :list-group (d/ul {:class "list-group"}
                                 (d/li {:class "list-group-item"}
                                       (str "Value $ " (util/to-fixed (:value data) 2)))
                                 (d/li {:class "list-group-item"}
                                       (str "Yield % " (util/to-fixed (:yield data) 2)))
                                 )}
              nil))))))
