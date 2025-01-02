(ns recipe-recommendation-system.utils-test
  (:require [midje.sweet :refer :all]
            [recipe-recommendation-system.core :as c]
            [recipe-recommendation-system.data :as d]
            [recipe-recommendation-system.utils :as u]
            [clojure.test :as t]
            [criterium.core :as crit]
            [next.jdbc :as jdbc]))
(let [registered-users
      (ref (or (seq (jdbc/execute! d/db-spec ["SELECT * FROM user"])) []))
      favorites-base (ref (jdbc/execute! d/db-spec ["SELECT r.*, f.user_id FROM recipe r
             JOIN favorites f ON r.id=f.recipe_id "]))
      recipes (ref (or (seq (jdbc/execute! d/db-spec ["SELECT * FROM user"])) []))]
  (c/remove-ns-from-ref recipes)
  (c/remove-ns-from-ref registered-users)
  (c/remove-ns-from-ref favorites-base)
  (c/join-favs registered-users favorites-base)
  (c/clean-up-favs registered-users)
  (c/clean-strings recipes)
  (c/remove-ns-from-ref registered-users)

  (= (u/get-favs-by-username "ivana" registered-users)
     (:favs (u/get-user-by-username "ivana" registered-users))))

(facts "get-favs-by-username-test"
       (let [registered-users
             (ref (or (seq (jdbc/execute! d/db-spec ["SELECT * FROM user"])) []))
             favorites-base (ref (jdbc/execute! d/db-spec ["SELECT r.*, f.user_id FROM recipe r
                    JOIN favorites f ON r.id=f.recipe_id "]))
             recipes (ref (or (seq (jdbc/execute! d/db-spec ["SELECT * FROM user"])) []))]
         (c/remove-ns-from-ref recipes)
         (c/remove-ns-from-ref registered-users)
         (c/remove-ns-from-ref favorites-base)
         (c/join-favs registered-users favorites-base)
         (c/clean-up-favs registered-users)
         (c/clean-strings recipes)
         (c/remove-ns-from-ref registered-users)

         (= (u/get-favs-by-username "ivana" registered-users)
            (:favs (u/get-user-by-username "ivana" registered-users)))) => true)

(facts
 "find-by-title-test"
 (let [recipes (ref (or (seq (jdbc/execute! d/db-spec ["SELECT * FROM recipe"])) [])
                    :validator
                    (comp not nil?))]
   (c/remove-ns-from-ref recipes)
   (c/clean-strings recipes)
   (u/find-by-title "Easy Mojitos" @recipes))
 => '({:id 4,
       :title "Easy Mojitos",
       :total-time "5",
       :serving-size "1 cocktail",
       :ingr
       "12 leaves mint, 2 lime slices, 1 teaspoon white sugar or more to taste, ¼ cup ice cubes or as needed, 1 (1.5 fluid ounce) jigger rum (such as Bacardi®), 4 ½ ounces diet lemon-lime soda (such as Diet Sprite®)",
       :instructions
       "Place mint leaves, lime slice, and sugar in bottom of a glass and muddle with a spoon until mint is crushed. Fill glass with ice cubes. Pour rum and soda over the ice stir.",
       :difficulty "easy",
       :fav 2}))

;;Performance of functions.

;;215,26 ns

(let [registered-users
      (ref (or (seq (jdbc/execute! d/db-spec ["SELECT * FROM user"])) []))
      favorites-base (ref (jdbc/execute! d/db-spec ["SELECT r.*, f.user_id FROM recipe r
                    JOIN favorites f ON r.id=f.recipe_id "]))
      recipes (ref (or (seq (jdbc/execute! d/db-spec ["SELECT * FROM user"])) []))]

  (c/remove-ns-from-ref recipes)
  (c/remove-ns-from-ref registered-users)
  (c/remove-ns-from-ref favorites-base)
  (c/join-favs registered-users favorites-base)
  (c/clean-up-favs registered-users)
  (c/clean-strings recipes)
  (c/remove-ns-from-ref registered-users)

  (crit/with-progress-reporting
    (crit/quick-bench  (u/get-favs-by-username "ivana" registered-users))))

;;Fastest - 68,79s

(let [recipes (ref (or (seq (jdbc/execute! d/db-spec ["SELECT * FROM recipe"])) [])
                   :validator
                   (comp not nil?))]
  (c/remove-ns-from-ref recipes)
  (c/clean-strings recipes)

  (crit/with-progress-reporting
    (crit/quick-bench  (u/find-by-title "Easy Mojitos" @recipes))))

