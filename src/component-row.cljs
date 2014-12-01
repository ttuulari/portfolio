(ns portfolio.component-row
  (:require
    [cljs.core.async :as async
      :refer [chan put!]]
    [om.core :as om :include-macros true]
    [om.dom :as dom :include-macros true]
    [portfolio.slider :as slider]))

(defn remove-button-view [app owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (dom/a #js {:className   "btn btn-danger btn-xs remove-button"
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
