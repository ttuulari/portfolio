(ns portfolio.util)

(defn log [v & text]
  (let [vs (if (string? v)
             (apply str v text)
             v)]
    (. js/console (log (clj->js vs)))
    v))

(defn to-fixed [number precision]
  (.toFixed number precision))

(defn date-delta-days [date-string delta-days]
  (let [date   (js/Date. date-string)
        year   (.getFullYear date)
        month  (.getMonth date)
        day    (.getDate  date)]
    (js/Date. year, month, (+ (inc day) delta-days))))

(defn pad [number digits]
  (let [diff     (- digits (count (str number)))
        maxdiff  (max 0 diff)]
    (str (apply str (repeat maxdiff "0")) number)))

(defn date->str [date]
  (let [year   (.getFullYear date)
        month  (inc (.getMonth date))
        day    (.getDate  date)]
    (str year "-" (pad month 2) "-" (pad day 2))))

(defn date-delta-days->str [date-string delta-days]
  (date->str (date-delta-days date-string delta-days)))

(defn strip [coll chars]
  (apply str (remove #((set chars) %) coll)))

(defn l-trim [string character]
  (let [rgx-trim   (js/RegExp. (str "^" character "+"))]
    (.replace string rgx-trim, "")))

(defn filter-nans [string]
  (apply str (filter (fn [c] (not (js/isNaN c))) string)))

(defn scale-seq [price-seq]
  (let [first-val   (first price-seq)
        scaler      (fn [elem]
                      (-> elem
                          (/ first-val)
                          (- 1)
                          (* 100)))]
    (map scaler price-seq)))
