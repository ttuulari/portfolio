(ns portfolio.range-buttons
  "Om component for radio style button bank for selecting date range."
  (:require
    [om-bootstrap.button :as b]
    [om.core :as om :include-macros true]
    [cljs.core.async :as async
      :refer [<! put! sub chan]]
    [om-tools.dom :as d :include-macros true]
    [portfolio.prices :as prices]
    [portfolio.util :as util])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(defn update-range
  "Construct date range map."
  [end-date delta]
    {:end-date     end-date
     :range        delta
     :total-length (prices/total-length)
     :final-date   (prices/final-date)})

(defn delta->data [delta]
  "Map delta days to interval map."
  (condp = delta
    -7     {:text "1 Week" :index 0}
    -30    {:text "1 Month" :index 1}
    -180   {:text "6 Months" :index 2}
    -365   {:text "1 Year" :index 3}))

(defn clicked
  "Range button click handler."
  [app owner delta index]
  (let [search-chan   (:search-chan  (om/get-shared owner))]
    (put! search-chan
          {:topic   :range
           :value   (update-range (:end-date app) delta)})))

(defn create-button
  "Create range button based on app state."
  [app owner delta]
  (let [delta-data    (delta->data delta)
        text          (:text delta-data)
        index         (:index delta-data)
        class-data    "btn btn-xs "
        button-data   {:on-click (fn[] (clicked @app owner delta index))}
        state-index   (:index (delta->data (:range app)))
        active        (= index state-index)]
    (if active
      (d/a (assoc button-data :class (str class-data "btn-material-indigo")) text)
      (d/a (assoc button-data :class (str class-data "btn-material-bluegrey")) text))))

(defn range-buttons-view
  "Range buttons Om component."
  [app owner]
  (reify
    om/IWillMount
    (will-mount
     [this]
     (let [range-chan       (sub (:notif-chan (om/get-shared owner)) :range (chan) false)
           slide-chan       (sub (:notif-chan (om/get-shared owner)) :slide (chan) false)]
       (go-loop
        []
        (let [[v c]           (alts! [range-chan slide-chan])
              range-update    (fn [] (update-range
                                      (get-in v [:value :final-date])
                                      (get-in v [:value :range])))
              int-value       (int (:value v))
              slide-update    (fn []
                                (update-range
                                 (util/date-delta-days->str
                                  (:final-date @app)
                                  (- int-value (:total-length @app)))
                                 (:range @app)))]

          (condp = c
            range-chan   (om/transact! app range-update)
            slide-chan   (om/transact! app slide-update)))
        (recur))))

    om/IRenderState
    (render-state [this state]
      (d/div {:class "range-buttons text-center"}
      (b/toolbar {:class "col-md-1"}
        (create-button app owner -7)
        (create-button app owner -30)
        (create-button app owner -180)
        (create-button app owner -365))))))
