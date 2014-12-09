(ns portfolio.navbar
  (:require
    [om.core :as om :include-macros true]
    [om-bootstrap.button :as b]
    [om-bootstrap.nav :as n]
    [om-tools.dom :as d :include-macros true]))

(defn navbar-view [app owner]
  (reify
    om/IRenderState
    (render-state [this state]
      (d/div {:class "navbar navbar-danger"}
             (d/div {:class "navbar-collapse collapse navbar-responsive-collapse"}
                    (d/a {:class "navbar-brand"} "Portfol.io"))))))


