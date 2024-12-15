(ns recipe-recommendation-system.core-test
  (:require [clojure.test :refer :all]
            [recipe-recommendation-system.core :refer :all]
            [midje.sweet :refer :all]
            [recipe-recommendation-system.content :as content]
            [recipe-recommendation-system.users :as users]))

(deftest a-test
  (testing "FIXME, I fail."
    (is (= 0 1))))

;;User wants to add recipe to favorites. That way new recipes can be recommended in multiple ways.
(facts "test-adding-to-favs" (choose-fav "ivana") :truthy)

;;User wants to get recipes that are recommended by difficulty. That way user can discover new recipes with the same difficulty as chosen recipe.
(facts "test-recommend-by-difficulty" (content/recommend-by-difficulty (first (filter #(= (:title %) "Easy Mojitos") @initial-dataset)) @initial-dataset) =not=> nil)

;;User wants to get report on recent activity. That way users can track their improvement. 
(defn contains-more [my-map & keys]
  (every? #(contains? my-map %) keys))
(facts "test-report"
       (contains-more (generate-report (first (filter #(= (:username %) "ivana") @registered-users)))
                      :username :num-favs :difficulty-levels :avg-difficulty :report-time) => true)

;;User wants to get recipes that are recommended by other users. 
;;That way users can connect and discover recipes that are recommended by users with similar taste.
(facts "test-recommend-by-favs" (users/users-recommend (first (filter #(= (:title %) "Easy Mojitos") @initial-dataset)) "ivana") =not=> nil)

;;User wants to find out which recipes are the most popular. That way users can discover current trends in recipes. 
(facts "test-recommend-by-popularity" (choose-by-popularity "ivana") =not=> nil)

;;User wants to group recipes that were chosen as favorites. That way user can find them quciker or get a new idea.
;; (facts "test-group-recipes" (group-favs "ivana" {:title "Easy Mojitos",
;;                                                  :total-time "5",
;;                                                  :serving-size "1 cocktail",
;;                                                  :ingr
;;                                                  ["12 leaves mint"
;;                                                   "2 lime slices"
;;                                                   "1 teaspoon white sugar or more to taste"
;;                                                   "� cup ice cubes or as needed"
;;                                                   "1 (1.5 fluid ounce) jigger rum (such as Bacardi�)"
;;                                                   "4 � ounces diet lemon-lime soda (such as Diet Sprite�)"],
;;                                                  :instructions
;;                                                  "Place mint leaves, lime slice, and sugar in bottom of a glass and muddle with a spoon until mint is crushed. Fill glass with ice cubes. Pour rum and soda over the ice stir.",
;;                                                  :difficulty "easy",
;;                                                  :fav 1} "g1") =not=> nil)

;;User wants to find another user with similar taste in recipes. That way users can connect and find recipes liked by similar users.
(facts "test-jaccard-similarity"
       (most-similar-user @registered-users (get-user-by-username "ivana") jaccard-similarity) => ["ivana2" 0.333])


(facts "test-cosine-similarity"
       (most-similar-user @registered-users (get-user-by-username "ivana") cosine-similarity) => ["ivana2" 0.5])

;;User wants to find another users with similar taste in recipes. This time all similarities are included.
(facts "test-most-similar-users"

       (most-similar-users (get-user-by-username "ivana")) => [["ivana10" 0.333] ["ivana13" 0.0]])

(facts "test-memory-measure-function"
       (clj-memory-meter.core/measure initial-dataset) =not=> nil)