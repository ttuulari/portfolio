(ns portfolio.components
  (:require
    [cljs.core.async :as async
      :refer [<! >! chan put! sub]]
    [om.core :as om :include-macros true]
    [om.dom :as dom :include-macros true]
    [portfolio.slider :as slider])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(def columns ["Component" "# Amount" "$ Price"])

(defn remove-button-view [app owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (dom/a #js {:className   "btn btn-danger btn-raised remove-button col-sm-2"
                  :onClick     (fn []
                                 (put!
                                   (:search-chan (om/get-shared owner))
                                   {:topic :remove-click
                                    :value app}))}

        "Remove"))))

(defn portfolio-component-view [app owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (dom/div
        #js {:className   "list-group-item portfolio-component"}
        (dom/div #js {:className "row"}
          (dom/div #js {:className   "portfolio-list-column col-sm-2"} app)
          (dom/div #js {:className   "portfolio-list-column col-sm-2"} 616)
          (om/build slider/slider-view
            app
            {:opts
              {:js       {:className "slider-material-red shor col-sm-2"}
               :slider   {:start 0
                          :connect "lower"
                          :range {:min 0
                                  :max 100}}}})
          (om/build remove-button-view app))))))

(defn portfolio-list-column [app owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (dom/h4
        #js {:className   "portfolio-list-column col-sm-2"}
        (dom/div nil app)))))

(defn portfolio-list-columns [app owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (dom/div #js {:className "portfolio-list-columns"}
        (apply dom/div #js {:className "row"}
          (om/build-all
            portfolio-list-column
            (map (fn [elem] elem) columns)))))))



(defn portfolio-list-view [app owner]
  (reify
    om/IWillMount
    (will-mount [this]
      (let [click-chan       (sub (:notif-chan (om/get-shared owner)) :search-click (chan) false)
            remove-chan      (sub (:notif-chan (om/get-shared owner)) :remove-click (chan) false)]

        (go-loop []
          (let [[v c]            (alts! [remove-chan click-chan]
                                   {:as {:default true}})
                add-component    (fn []
                                   (conj @app (:value v)))

                remove-component (fn []
                                   (remove (fn [elem] (= elem (:value v))) @app))]

            (condp = c
              remove-chan   (om/transact! app remove-component)
              click-chan    (om/transact! app add-component)
              :default      nil))
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
