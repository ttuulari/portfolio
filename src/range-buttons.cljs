(ns portfolio.range-buttons
  (:require
    [om-bootstrap.button :as b]
    [om.core :as om :include-macros true]
    [cljs.core.async :as async
      :refer [<! put! sub chan]]
    [om-tools.dom :as d :include-macros true]
    [portfolio.util :as util])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(defn update-range
  [end-date delta]
    {:end-date     end-date
     :range        delta
     :total-length 7
     :final-date   "2014-05-30"})

(defn clicked [app owner delta]
   (let [search-chan   (:search-chan  (om/get-shared owner))]
     (put! search-chan
           {:topic   :range
            :value   (update-range (:end-date app) delta)})))

(defn create-button [app owner delta text]
  (b/button {:bs-style "danger"
             :on-click (fn[] (clicked @app owner delta))}
            text))

(defn range-buttons-view [app owner]
  (reify
    om/IWillMount
    (will-mount [this]
      (let [range-chan       (sub (:notif-chan (om/get-shared owner)) :range (chan) false)]
        (go-loop []
          (let [range-elem      (<! range-chan)
                update-state    (fn [] (update-range
                                         (get-in range-elem [:value :final-date])
                                         (get-in range-elem [:value :range])))]
            (om/transact! app update-state))
          (recur))))

    om/IRenderState
    (render-state [this state]
      (d/div {:class "range-buttons text-center"}
      (b/toolbar {:class "col-md-1"}
        (create-button app owner -7 "1 Week")
        (create-button app owner -30 "1 Month")
        (create-button app owner -180 "6 Months")
        (create-button app owner -365 "1 Year"))))))
