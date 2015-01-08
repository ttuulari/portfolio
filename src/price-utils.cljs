(ns portfolio.price-utils
  (:require
   [portfolio.util :as util]))

(defn yield [first-price last-price]
  (if (zero? last-price)
    0
    (-> last-price
        (/ first-price)
        (- 1)
        (* 100))))

(defn average [coll]
  (/ (reduce + coll) (count coll)))

(defn sample-variance [coll]
  (let [avg     (average coll)
        diffs   (map (fn [e] (- e avg)) coll)
        squared (map (fn [e] (* e e)) diffs)]
    (/ (reduce + squared)
       (- (count coll) 1))))

(defn sample-std [coll]
  (Math.pow (sample-variance coll) 0.5))

(defn return-ratio [coll]
  (let [nume   (drop 1 coll)
        deno   (drop-last coll)]
    (map / nume deno)))

(defn difference-seq [coll]
  (let [nume   (drop 1 coll)
        deno   (drop-last coll)]
    (map - nume deno)))

(defn sharpe
  "Sharpe ratio between coll and index-coll"
  [coll index-coll]
  (let [diff-coll   (return-ratio coll)
        diff-index  (return-ratio index-coll)
        diff        (map - diff-coll diff-index)
        e-diff      (average diff)
        std-diff    (Math.pow (sample-variance diff) 0.5)]
    (/ e-diff std-diff)))

(defn gain-to-pain
  "Gain-to-pain ratio of coll"
  [coll]
  (let [diff   (difference-seq coll)
        pos    (reduce + (filter (fn [e] (> e 0)) diff))
        neq    (Math.abs (reduce + (filter (fn [e] (< e 0)) diff)))]
    (/ pos neq)))

(defn all-zeros? [seq]
  (= (count (filter zero? seq))
     (count seq)))

(defn scale-seq
  "Transform sequence to percentage diff between elements and first value."
  [price-seq]
  (let [first-val   (first price-seq)
        scaler      (fn [elem]
                      (-> elem
                          (/ first-val)
                          (- 1)
                          (* 100)))]
    (map scaler price-seq)))
