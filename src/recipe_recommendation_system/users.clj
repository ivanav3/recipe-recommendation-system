(ns recipe-recommendation-system.users
  (:require [recipe-recommendation-system.core :as c]
            [recipe-recommendation-system.utils :as u]
            [clojure.string :as str]))


(defn users-recommend [selected-recipe username]
  (let [users-with-selected (filter
                             (fn [user]
                               (and
                                (some #(= (:title %) (:title selected-recipe)) (:favs user))
                                (not= (:username user) username)))
                             @c/registered-users)
        all-favs (mapcat :favs users-with-selected)
        without-selected (remove #(= (:title %) (:title selected-recipe)) all-favs)
        shuffled (shuffle without-selected)
        top-3 (take 3 shuffled)]
    top-3))

(defn by-users-recipe [username]
  (println "Enter recipe title or part of title:")
  (let [title (read-line)
        results (u/find-by-title title (u/get-favs-by-username username @c/registered-users))]
    (if (seq results)
      (do
        (println "Found the following recipes:")
        (doseq [result results]
          (println (:title result)))

        (println "Please enter the full title of the recipe you're interested in:")
        (let [chosen-title (str/lower-case (read-line))
              chosen-recipe (some #(if (= (str/lower-case (:title %)) chosen-title) %) results)]
          (if chosen-recipe
            (println (users-recommend (first (filter #(= (:title %) (:title chosen-recipe)) @c/initial-dataset)) username))
            (println "Error. Recipe not found or invalid input."))))
      (println "No recipes found."))))
