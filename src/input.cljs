(ns portfolio.input
  (:require
    [om.core :as om :include-macros true]
    [om.dom :as dom :include-macros true]))

(defn submit [owner]
  (let [value   (.trim (.-value (om/get-node owner "term")))]
    (.log js/console value)
    false))

(defn input-view [app owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (dom/div #js {:className "search-view"}
        (dom/form #js
          {
            :className "navbar-form"
            :role: "search"
            :onSubmit (fn [] (submit owner))
            :onChange (fn [_] (.log js/console "onChange"))}
          (dom/div #js {:className "input-group searchInput"}
            (dom/div #js {:className "input-group-btn"}
              [(dom/input #js {:type "text"
                              :className "form-control"
                              :placeholder "Instrument"
                              :name "srch-term"
                              :id "srch-term"
                              :ref "term"})
               (dom/button #js {:className "btn btn-default"
                                :type "submit"}
                 (dom/i #js {:className "glyphicon glyphicon-search"}))])))))))
