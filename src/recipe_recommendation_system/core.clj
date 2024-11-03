(ns recipe-recommendation-system.core
  (:gen-class)
  (:require [clojure.string :as str]))

(def filename "first-cleaned.csv")

(slurp filename)

(defn parse
  "Convert a CSV string into rows of columns, removing unwanted characters."
  [string]

  (let [cleaned-string (str/replace string #"\r" "")] ;; Removing \r  because it isn't necessary even though dataset contains it
    (map (fn [row]
           (let [trimmed-row (str/trim row)] ;; Removing all unnecessary space characters
             (when (not (str/blank? trimmed-row))
               (str/split trimmed-row #";"))))
         (str/split cleaned-string #"\n"))))
(rest (parse (slurp filename)))

(def initial-dataset (rest (parse (slurp filename))))

(def keys [:title :total-time :serving-size :ingr :instructions])

(defn vectors-to-maps [vectors]
  (map #(zipmap keys %) vectors))

(def map-dataset (vectors-to-maps initial-dataset))
(def initial-dataset (doall map-dataset))