(ns recipe-recommendation-system.utils
  (:require [clojure.string :as str]))


(defn get-user-by-username [username users]
  (first (filter #(= (:username %) username) @users)))

(defn find-by-title [title dataset]
  (let [lower-title (str/lower-case title)]
    (filter (fn [item]
              (let [title (:title item)]
                (str/includes? (str/lower-case title) lower-title))) dataset)))

(defn contains-more [my-map & keys]
  (every? #(contains? my-map %) keys))

(defn get-favs-by-username [username dataset]
  (some #(if (= (:username %) username) (:favs %)) dataset))

(defn print-recipe [recipe]
  (println "-----------------------\nRecipe: " (:title recipe)
           "\n\nTime: about " (:total-time recipe)
           " minutes \n\nIngredients: ")
  (let [ingredients (str/split (:ingr recipe) #", ")]
    (doseq [i ingredients]
      (println i)))
  (println "\n\nInstructions: " (:instructions recipe)
           "\nServing size: " (:serving-size recipe)
           "\n\nDifficulty: " (:difficulty recipe)))