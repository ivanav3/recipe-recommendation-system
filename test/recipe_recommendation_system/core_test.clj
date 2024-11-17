(ns recipe-recommendation-system.core-test
  (:require [clojure.test :refer :all]
            [recipe-recommendation-system.core :refer :all]
            [midje.sweet :refer :all]))

(deftest a-test
  (testing "FIXME, I fail."
    (is (= 0 1))))



(facts "Test adding to favs" (choose-fav "ivana") :truthy)

(facts "Test recommend by difficulty" (recommend-by-difficulty (first (filter #(= (:title %) "Easy Mojitos") @initial-dataset))) =not=> nil)

(defn contains-more [my-map & keys]
  (every? #(contains? my-map %) keys))
(facts "Test report"
       (contains-more (generate-report (first (filter #(= (:username %) "ivana") @registered-users)))
                      :username :num-favs :difficulty-levels :avg-difficulty :report-time) => true)

(facts "Test recommend by favs" (users-recommend (first (filter #(= (:title %) "Easy Mojitos") @initial-dataset))) =not=> nil)

(facts "Test recommend by popularity" (choose-by-popularity "ivana") =not=> nil)

(facts "Test group recipes" (group-favs "ivana" {:title "Easy Mojitos",
                                                 :total-time "5",
                                                 :serving-size "1 cocktail",
                                                 :ingr
                                                 ["12 leaves mint"
                                                  "2 lime slices"
                                                  "1 teaspoon white sugar or more to taste"
                                                  "� cup ice cubes or as needed"
                                                  "1 (1.5 fluid ounce) jigger rum (such as Bacardi�)"
                                                  "4 � ounces diet lemon-lime soda (such as Diet Sprite�)"],
                                                 :instructions
                                                 "Place mint leaves, lime slice, and sugar in bottom of a glass and muddle with a spoon until mint is crushed. Fill glass with ice cubes. Pour rum and soda over the ice stir.",
                                                 :difficulty "easy",
                                                 :fav 1} "g1") =not=> nil)