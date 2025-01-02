(ns recipe-recommendation-system.content
  (:require
   [clojure.string :as str]
   [recipe-recommendation-system.utils :as u]
   [clojure.set :as set]))

(defn recommend-by-difficulty [chosen dataset]
  (let [diff (apply str (map :difficulty chosen))
        same-diff (filter #(= (:difficulty %) diff) dataset)
        others (remove #(= (:title %) (map :title chosen)) same-diff)]
    (take 3 (shuffle others))))

(defn by-dif [username users recipes]
  (println "Enter recipe title or part of title (from your favs):")
  (let [title (read-line)
        results (u/find-by-title title (u/get-favs-by-username username @users))]
    (if (seq results)
      (do
        (println "Found the following recipes:")
        (doseq [result results]
          (println (:title result)))

        (println "Please enter the full title of the recipe you're interested in:")
        (let [chosen-title (str/lower-case (read-line))
              chosen-recipe (u/find-by-title chosen-title results)]
          (if chosen-recipe
            (do
              (println "Chosen difficulty of the recipe" (apply str (map :title chosen-recipe)) "is" (apply str (map :difficulty chosen-recipe)) ". The following recipes have the same level of difficulty: ")
              (doseq [rec (recommend-by-difficulty (u/find-by-title chosen-title results) @recipes)]
                (println rec)))
            (println "Error. Recipe not found or invalid input."))))
      (println "No recipes found."))))


(defn extract-keywords [description]
  (set (str/split (str/lower-case description) #"\s+")))

(defn content-similarity [r1 r2]
  (let [keywords1 (extract-keywords (apply str (map :instructions r1)))
        keywords2 (extract-keywords (apply str (:instructions r2)))]
    (count (set/intersection keywords1 keywords2))))

(defn recommend-by-content [recipes target]
  (let [similarities (map #(content-similarity target %) recipes)
        indexed-similarities (map-indexed vector similarities)
        sorted-similarities (sort-by second > indexed-similarities)
        top-recommendations (take 3 (rest sorted-similarities))]
    (map #(nth recipes (first %)) top-recommendations)))

(defn by-content [username users recipes]
  (println "Enter recipe title or part of title (from your favs):")
  (let [title (read-line)
        results (u/find-by-title title (u/get-favs-by-username username @users))]
    (if (seq results)
      (do
        (println "Found the following recipes:")
        (doseq [result results]
          (println (:title result)))

        (println "Please enter the full title of the recipe you're interested in:")
        (let [chosen-title (str/lower-case (read-line))
              chosen-recipe (u/find-by-title chosen-title results)]
          (if chosen-recipe
            (doseq [rec  (recommend-by-content @recipes chosen-recipe)]
              (println rec))
            (println "Error. Recipe not found or invalid input."))))
      (println "No recipes found."))))
