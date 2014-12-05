(ns portfolio.slider-indicator
  (:require
    [cljs.core.async :as async
      :refer [<! >! chan put! sub]]
    [om.core :as om :include-macros true]
    [om-bootstrap.panel :as p]
    [portfolio.range-buttons :as range-buttons]
    [portfolio.util :as util]
    [om-tools.dom :as d :include-macros true])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(defn slider-indicator-view [app owner]
  (reify
    om/IWillMount
    (will-mount [this]
      (let [slide-chan       (sub (:notif-chan (om/get-shared owner)) :slide (chan) false)]
        (go-loop []
          (let [search-elem      (<! slide-chan)
                value            (int (:value search-elem))]
            (om/transact! app (fn []
                                  (range-buttons/update-range
                                    (util/date-delta-days->str
                                      (:final-date @app)
                                      (- value (:total-length @app)))
                                    (:range @app)))))

          (recur))))

    om/IRenderState
    (render-state [this state]
      (p/panel
        {:class "slider-indicator"
        :list-group (d/ul {:class "list-group"}
                           (d/li {:class "list-group-item"}
                                 (str "End date: " (:end-date app)))
                           (d/li {:class "list-group-item"}
                                 (str "Start date: "
                                      (util/date-delta-days->str (:end-date app)
                                                                 (:range app)))))}))))
