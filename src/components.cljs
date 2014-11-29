(ns portfolio.components
  (:require
    [cljs.core.async :as async
      :refer [<! >! chan put! sub]]
    [om.core :as om :include-macros true]
    [om.dom :as dom :include-macros true]
    [portfolio.slider :as slider])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(def columns ["Component" "# Amount" "$ Price"])

(defn portfolio-component-view [app owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (dom/div
        #js {:className   "list-group-item portfolio-component"}
        (dom/div #js {:className   "portfolio-list-column col-sm-4"} app)
        (dom/div #js {:className   "portfolio-list-column col-sm-4"} 616)
        (om/build slider/slider-view
          app
          {:opts
            {:js       {:className "slider-material-pink shor col-sm-4"}
             :slider   {:start 0
                        :connect "lower"
                        :range {:min 0
                                :max 100}}}})))))

(defn portfolio-list-column [app owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (dom/div
        #js {:className   "portfolio-list-column col-sm-4"}
        (dom/div nil app)))))

(defn portfolio-list-columns [app owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (apply dom/div #js {:className "portfolio-list-columns"}
        (om/build-all
          portfolio-list-column
          (map (fn [elem] elem) columns))))))

(defn portfolio-list-view [app owner]
  (reify
    om/IWillMount
    (will-mount [this]
      (let [click-chan (sub (:notif-chan (om/get-shared owner)) :search-click (chan) false)]
        (go-loop []
          (let [search-elem      (<! click-chan)
                add-component    (fn []
                                   (conj @app (:value search-elem)))]
            (om/transact! app add-component))
          (recur))))

    om/IRenderState
    (render-state [this state]
      (dom/div #js {:className "portfolio-container"}
        (om/build portfolio-list-columns true)
        (apply dom/div #js {:className "list-group portfolio-list"}
          (om/build-all
            portfolio-component-view
            (map
              (fn [elem] elem)
              app)))))))
