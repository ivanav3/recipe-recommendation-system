(ns recipe-recommendation-system.content
  (:require
   [clojure.string :as str]
   [recipe-recommendation-system.core :as c]
   [recipe-recommendation-system.utils :as u]
   [clojure.set :as set]))

(defn recommend-by-difficulty [chosen dataset]
  (let [diff (:difficulty chosen)
        same-diff (filter #(= (:difficulty %) diff) @c/initial-dataset)
        others (remove #(= (:title %) (:title chosen)) same-diff)]
    (take 3 (shuffle others))))

(defn by-dif [username]
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
            (println (recommend-by-difficulty (first (filter #(= (:title %) (:title chosen-recipe)) @c/initial-dataset)) @c/initial-dataset))
            (println "Error. Recipe not found or invalid input."))))
      (println "No recipes found."))))


(defn extract-keywords [description]
  (set (str/split (str/lower-case description) #"\s+")))

(defn content-similarity [r1 r2]
  (let [keywords1 (extract-keywords (:instructions r1))
        keywords2 (extract-keywords (:instructions r2))]
    (count (set/intersection keywords1 keywords2))))

(defn recommend-by-content [recipes target-index]
  (let [target-product (nth recipes target-index)
        similarities (map #(content-similarity target-product %) recipes)
        indexed-similarities (map-indexed vector similarities)
        sorted-similarities (sort-by second > indexed-similarities)
        top-recommendations (take 3 (filter #(not= (first %) target-index) sorted-similarities))]
    (map #(nth recipes (first %)) top-recommendations)))


(defn find-index-by-title [dataset title]
  (first (keep-indexed (fn [index element]
                         (when (= (str/lower-case (:title element))
                                  (str/lower-case title))
                           index))
                       dataset)))

(defn by-content [username]
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
            (println (recommend-by-content @c/initial-dataset (find-index-by-title @c/initial-dataset (:title chosen-recipe))))
            (println "Error. Recipe not found or invalid input."))))
      (println "No recipes found."))))
