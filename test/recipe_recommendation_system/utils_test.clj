(ns recipe-recommendation-system.utils-test
  (:require [midje.sweet :refer :all]
            [recipe-recommendation-system.core :as c]
            [recipe-recommendation-system.utils :as u]
            [clojure.test :as t]
            [criterium.core :as crit]))

(facts "get-favs-by-username-test"
       (u/get-favs-by-username "ivana" @c/registered-users)
       => (:favs (c/get-user-by-username "ivana"))

       "find-by-title-test"
       (u/find-by-title "Easy Mojitos" @c/initial-dataset)
       => '({:id 4,
             :title "Easy Mojitos",
             :total-time "5",
             :serving-size "1 cocktail",
             :ingr
             "12 leaves mint, 2 lime slices, 1 teaspoon white sugar or more to taste, ¼ cup ice cubes or as needed, 1 (1.5 fluid ounce) jigger rum (such as Bacardi®), 4 ½ ounces diet lemon-lime soda (such as Diet Sprite®)\r\n",
             :instructions
             "Place mint leaves, lime slice, and sugar in bottom of a glass and muddle with a spoon until mint is crushed. Fill glass with ice cubes. Pour rum and soda over the ice stir.\r\n",
             :difficulty "easy",
             :fav 2}))


(crit/with-progress-reporting
  (crit/quick-bench  (u/get-favs-by-username "ivana" @c/registered-users)))

(crit/with-progress-reporting
  (crit/quick-bench  (u/find-by-title "Easy Mojitos" @c/initial-dataset)))
