(ns portfolio.input
  (:require
    [cljs.core.async :as async
      :refer [>! put! sub chan]]
    [om.core :as om :include-macros true]
    [om.dom :as dom :include-macros true])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(defn submit [owner]
  (let [value         (.trim (.-value (om/get-node owner "term")))
        search-chan   (:search-chan  (om/get-shared owner))]
    (put! search-chan
          {:topic :search
           :value value})
    false))

(defn input-view [app owner]
  (reify
    om/IWillMount
    (will-mount [this]
      (let [click-chan (sub (:notif-chan (om/get-shared owner)) :search-click (chan) false)]
        (go-loop []
          (let [search-elem      (<! click-chan)]
            (set! (.-value (om/get-node owner "term")) ""))
          (recur))))

    om/IRenderState
    (render-state [this state]
      (dom/div #js {:className "search-view"}
        (dom/form #js
          {
            :className "navbar-form"
            :role: "search"
            :onSubmit (fn [] (submit owner))
            :onChange (fn [] (submit owner))}
          (dom/div #js {:className "input-group searchInput"}
            (dom/div #js {:className "input-group-btn"}
              [(dom/input #js {:type "text"
                              :autoComplete "off"
                              :className "form-control"
                              :placeholder "Instrument"
                              :name "srch-term"
                              :id "srch-term"
                              :ref "term"})
               (dom/button #js {:className "btn btn-default"
                                :type "submit"}
                 (dom/i #js {:className "glyphicon glyphicon-search"}))])))))))
