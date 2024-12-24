(ns recipe-recommendation-system.users
  (:require [recipe-recommendation-system.core :as c]
            [recipe-recommendation-system.utils :as u]
            [clojure.string :as str]
            [clojure.set :as set]
            [recipe-recommendation-system.utils :as utils]))

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
              chosen-recipe (first (utils/find-by-title chosen-title results))]
          (if chosen-recipe
            (do
              (println "The following recipes were recommended by other users that chose" (:title chosen-recipe) "as well")
              (doseq [rec (users-recommend (first (utils/find-by-title (:title chosen-recipe) @c/initial-dataset))
                                           username)]
                (println rec)))

            (println "Error. Recipe not found or invalid input."))))
      (println "No recipes found."))))

(defn extract-favs [user]
  (set (map :title (:favs user))))

(defn jaccard-similarity [user1 user2]
  (let [favs1 (extract-favs user1)
        favs2 (extract-favs user2)]
    (if (or (empty? favs1) (empty? favs2))
      0.0
      (let
       [intersection (count (set/intersection favs1 favs2))
        union (count (set/union favs1 favs2))]
        (if (zero? union)
          0.0
          (let [similarity (float (/ intersection union))]
            (/ (Math/round (* similarity 1000)) 1000.0)))))))

(defn most-similar-user [users-to-compare target-user similarity-fn]
  (let [all-users (remove #(= (:username %) (:username target-user)) users-to-compare)
        similarities (map #(vector (:username %) (similarity-fn target-user %)) all-users)
        most-similar (apply max-key second similarities)]
    most-similar))


(defn cosine-similarity [user1 user2]
  (let [favs1 (extract-favs user1)
        favs2 (extract-favs user2)]
    (if (or (empty? favs1) (empty? favs2))
      0.0
      (let [all-recipes (set/union favs1 favs2)
            vector1 (map #(if (contains? favs1 %) 1 0) all-recipes)
            vector2 (map #(if (contains? favs2 %) 1 0) all-recipes)]
        (let [dot-product (reduce + (map * vector1 vector2))
              norm1 (Math/sqrt (reduce + (map #(* % %) vector1)))
              norm2 (Math/sqrt (reduce + (map #(* % %) vector2)))]
          (if (and (zero? norm1) (zero? norm2))
            1.0
            (Float/parseFloat (format "%.3f" (/ dot-product (* norm1 norm2))))))))))


(defn get-user-favs [username]
  {:username username
   :favs (u/get-favs-by-username username @c/registered-users)})

(defn most-similar-users [target-user]
  (let [all-users (remove #(= (:username %) (:username target-user)) @c/registered-users)

        most-similar-jaccard  (most-similar-user all-users target-user jaccard-similarity)

        remaining-users (remove #(= (:username %) (first most-similar-jaccard)) all-users)
        most-similar-cosine (if (empty? remaining-users)
                              nil
                              (most-similar-user remaining-users target-user cosine-similarity))]

    (if most-similar-cosine
      [most-similar-jaccard most-similar-cosine]
      [most-similar-jaccard])))


(defn print-recs [username]
  (do
    (println "The following recipes were chosen by users with similar taste in recipes as" username)
    (doseq [s (map first (most-similar-users (c/get-user-by-username username)))]
      (doseq [rec (get-user-favs s)]
        (println rec)))))