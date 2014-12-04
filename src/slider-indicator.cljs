(ns portfolio.slider-indicator
  (:require
    [cljs.core.async :as async
      :refer [<! >! chan put! sub]]
    [om.core :as om :include-macros true]
    [om.dom :as dom :include-macros true]
    [om-bootstrap.panel :as p]
    [portfolio.util :as util])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(defn slider-indicator-view [app owner]
  (reify
    om/IWillMount
    (will-mount [this]
      (let [slide-chan       (sub (:notif-chan (om/get-shared owner)) :slide (chan) false)]
        (go-loop []
          (let [search-elem      (<! slide-chan)
                value            (get
                                  (get-in search-elem [:data :labels])
                                  (int (:value search-elem)))]
            (om/update! app [:selected-point :date] value))
          (recur))))

    om/IRenderState
    (render-state [this state]
      (p/panel {:class "slider-indicator col-sm-1"} (get-in app [:selected-point :date])))))
