(ns recipe-recommendation-system.core-test
  (:require [clojure.test :refer :all]
            [recipe-recommendation-system.core :refer :all]
            [midje.sweet :refer :all]
            [recipe-recommendation-system.core :as c]
            [recipe-recommendation-system.content :as content]
            [recipe-recommendation-system.users :as users]
            [clojure.test :as t]
            [criterium.core :as crit]))

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

;;User wants to find another user with similar taste in recipes. That way users can connect and find recipes liked by similar users.
(facts "test-jaccard-similarity"
       (users/most-similar-user @registered-users (get-user-by-username "ivana") users/jaccard-similarity) => ["ivana2" 0.333])


(facts "test-cosine-similarity"
       (users/most-similar-user @registered-users (get-user-by-username "ivana") users/cosine-similarity) => ["ivana2" 0.5])

;;User wants to find another users with similar taste in recipes. This time all similarities are included.
(facts "test-most-similar-users"
       (users/most-similar-users (get-user-by-username "ivana")) =not=> nil)


;;Functions that are needed for authentication.
(facts "hash-password-test"
       (hash-password "ivana") =not=> nil
       "register-test"
       (register) =not=> empty)

(facts "is-logged-in-test"
       (c/is-user-logged-in? "ivana" '({:username "ivana" :password "ivana"})) => true
       "remove-user-test"
       (c/remove-user
        '({:username "ivana" :password "ivana"}, {:username "ivana2" :password "ivana2"}) "ivana") => '({:username "ivana2" :password "ivana2"}))

;;Functions that are used for cleaning datasets.
(facts "clean-datasets-test"
       (let [ds {"name" "Ivana" "age" 23}]
         (c/clean-from-db ds)) => {:name "Ivana" :age 23}
       "removing-namespace-test"
       (let [ds (ref [{:user/id 10, :user/username "ivana10"}])]
         (remove-ns-from-ref ds)
         @ds) => '({:id 10, :username "ivana10"})
       "attach-favs-test"
       (c/attach-favorites-to-user (c/get-user-by-username "ivana")) =not=> nil
       "join-favs-test"
       (c/join-favs @c/initial-dataset) =not=> nil
       "remove-user-id-from-favorites-test"
       (c/remove-user-id-from-favorites (c/get-user-by-username "ivana")) :truthy
       "clean-up-favs-test"
       (c/clean-up-favs @c/registered-users) =not=> empty
       "get-user-by-username-test"
       (c/get-user-by-username "ivana") =not=> nil)

;;Functions related to adding to favorites.
(facts "find-by-title-test"
       (c/find-by-title "title1" '({:title "title1" :time 22}, {:title "title2" :time 32})) => '({:title "title1" :time 22})
       "update-and-add-to-favs-test"
       (let [users '({:username "ivana" :favs ()})]
         (c/update-favs users "ivana" {:id 4,
                                       :title "Easy Mojitos",
                                       :total-time "5",
                                       :serving-size "1 cocktail",
                                       :ingr
                                       "12 leaves mint, 2 lime slices, 1 teaspoon white sugar or more to taste, ¼ cup ice cubes or as needed, 1 (1.5 fluid ounce) jigger rum (such as Bacardi®), 4 ½ ounces diet lemon-lime soda (such as Diet Sprite®)\r\n",
                                       :instructions
                                       "Place mint leaves, lime slice, and sugar in bottom of a glass and muddle with a spoon until mint is crushed. Fill glass with ice cubes. Pour rum and soda over the ice stir.\r\n",
                                       :difficulty "easy",
                                       :fav 2}))
       => '({:username "ivana",
             :favs
             ({:id 4,
               :title "Easy Mojitos",
               :total-time "5",
               :serving-size "1 cocktail",
               :ingr
               "12 leaves mint, 2 lime slices, 1 teaspoon white sugar or more to taste, ¼ cup ice cubes or as needed, 1 (1.5 fluid ounce) jigger rum (such as Bacardi®), 4 ½ ounces diet lemon-lime soda (such as Diet Sprite®)\r\n",
               :instructions
               "Place mint leaves, lime slice, and sugar in bottom of a glass and muddle with a spoon until mint is crushed. Fill glass with ice cubes. Pour rum and soda over the ice stir.\r\n",
               :difficulty "easy",
               :fav 2})})
       "update-rec-test"
       (c/update-rec  '({:id 4,
                         :title "Easy Mojitos",
                         :total-time "5",
                         :serving-size "1 cocktail",
                         :ingr
                         "12 leaves mint, 2 lime slices, 1 teaspoon white sugar or more to taste, ¼ cup ice cubes or as needed, 1 (1.5 fluid ounce) jigger rum (such as Bacardi®), 4 ½ ounces diet lemon-lime soda (such as Diet Sprite®)\r\n",
                         :instructions
                         "Place mint leaves, lime slice, and sugar in bottom of a glass and muddle with a spoon until mint is crushed. Fill glass with ice cubes. Pour rum and soda over the ice stir.\r\n",
                         :difficulty "easy",
                         :fav 2}) {:id 4,
                                   :title "Easy Mojitos",
                                   :total-time "5",
                                   :serving-size "1 cocktail",
                                   :ingr
                                   "12 leaves mint, 2 lime slices, 1 teaspoon white sugar or more to taste, ¼ cup ice cubes or as needed, 1 (1.5 fluid ounce) jigger rum (such as Bacardi®), 4 ½ ounces diet lemon-lime soda (such as Diet Sprite®)\r\n",
                                   :instructions
                                   "Place mint leaves, lime slice, and sugar in bottom of a glass and muddle with a spoon until mint is crushed. Fill glass with ice cubes. Pour rum and soda over the ice stir.\r\n",
                                   :difficulty "easy",
                                   :fav 2}) => '({:id 4,
                                                  :title "Easy Mojitos",
                                                  :total-time "5",
                                                  :serving-size "1 cocktail",
                                                  :ingr
                                                  "12 leaves mint, 2 lime slices, 1 teaspoon white sugar or more to taste, ¼ cup ice cubes or as needed, 1 (1.5 fluid ounce) jigger rum (such as Bacardi®), 4 ½ ounces diet lemon-lime soda (such as Diet Sprite®)\r\n",
                                                  :instructions
                                                  "Place mint leaves, lime slice, and sugar in bottom of a glass and muddle with a spoon until mint is crushed. Fill glass with ice cubes. Pour rum and soda over the ice stir.\r\n",
                                                  :difficulty "easy",
                                                  :fav 3})
       "choose-by-popularity-test"
       (with-in-str "Easy Mojitos\nEasy Mojitos"
         (c/choose-by-popularity "ivana13")) =not=> nil)


(facts "choose-fav-test"
       (with-in-str "Easy Mojitos\nEasy Mojitos"
         (c/choose-fav "ivana13")) =not=> nil)


;;Functions related to removing from favs.
(facts "update-and-remove-from-favs-test"
       (let [users '({:username "ivana",
                      :favs
                      ({:id 4,
                        :title "Easy Mojitos",
                        :total-time "5",
                        :serving-size "1 cocktail",
                        :ingr
                        "12 leaves mint, 2 lime slices, 1 teaspoon white sugar or more to taste, ¼ cup ice cubes or as needed, 1 (1.5 fluid ounce) jigger rum (such as Bacardi®), 4 ½ ounces diet lemon-lime soda (such as Diet Sprite®)\r\n",
                        :instructions
                        "Place mint leaves, lime slice, and sugar in bottom of a glass and muddle with a spoon until mint is crushed. Fill glass with ice cubes. Pour rum and soda over the ice stir.\r\n",
                        :difficulty "easy",
                        :fav 2})})]
         (c/update-favs-when-removed users "ivana" {:id 4,
                                                    :title "Easy Mojitos",
                                                    :total-time "5",
                                                    :serving-size "1 cocktail",
                                                    :ingr
                                                    "12 leaves mint, 2 lime slices, 1 teaspoon white sugar or more to taste, ¼ cup ice cubes or as needed, 1 (1.5 fluid ounce) jigger rum (such as Bacardi®), 4 ½ ounces diet lemon-lime soda (such as Diet Sprite®)\r\n",
                                                    :instructions
                                                    "Place mint leaves, lime slice, and sugar in bottom of a glass and muddle with a spoon until mint is crushed. Fill glass with ice cubes. Pour rum and soda over the ice stir.\r\n",
                                                    :difficulty "easy",
                                                    :fav 2}))
       => '({:username "ivana",
             :favs
             ()})
       "dec-recipe-test"
       (c/dec-recipe  '({:id 4,
                         :title "Easy Mojitos",
                         :total-time "5",
                         :serving-size "1 cocktail",
                         :ingr
                         "12 leaves mint, 2 lime slices, 1 teaspoon white sugar or more to taste, ¼ cup ice cubes or as needed, 1 (1.5 fluid ounce) jigger rum (such as Bacardi®), 4 ½ ounces diet lemon-lime soda (such as Diet Sprite®)\r\n",
                         :instructions
                         "Place mint leaves, lime slice, and sugar in bottom of a glass and muddle with a spoon until mint is crushed. Fill glass with ice cubes. Pour rum and soda over the ice stir.\r\n",
                         :difficulty "easy",
                         :fav 2}) {:id 4,
                                   :title "Easy Mojitos",
                                   :total-time "5",
                                   :serving-size "1 cocktail",
                                   :ingr
                                   "12 leaves mint, 2 lime slices, 1 teaspoon white sugar or more to taste, ¼ cup ice cubes or as needed, 1 (1.5 fluid ounce) jigger rum (such as Bacardi®), 4 ½ ounces diet lemon-lime soda (such as Diet Sprite®)\r\n",
                                   :instructions
                                   "Place mint leaves, lime slice, and sugar in bottom of a glass and muddle with a spoon until mint is crushed. Fill glass with ice cubes. Pour rum and soda over the ice stir.\r\n",
                                   :difficulty "easy",
                                   :fav 2}) => '({:id 4,
                                                  :title "Easy Mojitos",
                                                  :total-time "5",
                                                  :serving-size "1 cocktail",
                                                  :ingr
                                                  "12 leaves mint, 2 lime slices, 1 teaspoon white sugar or more to taste, ¼ cup ice cubes or as needed, 1 (1.5 fluid ounce) jigger rum (such as Bacardi®), 4 ½ ounces diet lemon-lime soda (such as Diet Sprite®)\r\n",
                                                  :instructions
                                                  "Place mint leaves, lime slice, and sugar in bottom of a glass and muddle with a spoon until mint is crushed. Fill glass with ice cubes. Pour rum and soda over the ice stir.\r\n",
                                                  :difficulty "easy",
                                                  :fav 1}))


(facts "remove-from-fav-test"
       (with-in-str "Easy Mojitos\nEasy Mojitos"
         (c/remove-fav "ivana13")) =not=> nil)

;;Menus tested.

(facts "main-menu-test"
       (with-in-str "10\nivana"
         (c/main-menu "ivana")) =not=> empty
       "main-test"
       (with-in-str "2\n"
         (c/-main)) =not=> empty)

(facts "login-twice-test"
       (try
         (do
           (with-in-str "ivana\nivana\n11"
             (c/login))
           (with-in-str "ivana\nivana"
             (c/login)))

         (catch Exception e
           (is (= "User is already logged in. Please logout first." (ex-message e)))
           (is (= {:username "ivana"} (ex-data e))))) => true)

(facts "logout-test"
       (try
         (with-in-str "ivanalogout\n"
           (c/logout))

         (catch Exception e
           (is (= "No such user is logged in.") (ex-message e))
           (is (= {:username "ivanalogout"} (ex-data e))))) => true)

(facts "register-test"
       (try
         (with-in-str "ivana101\nivana101"
           (c/register))
         (catch Exception e
           (is (= "This username is taken, try again.") (ex-message e))
           (is (= {:username "ivana101"}) (ex-data e)))) => true)

(facts "add-and-remove-from-favs"
       (do (with-in-str "Easy Mojitos\nEasy Mojitos"
             (choose-fav "ivana101"))
           (with-in-str "Easy Mojitos\nEasy Mojitos"
             (remove-fav "ivana101"))) =not=> nil)


;;Performances.
(crit/with-progress-reporting
  (crit/quick-bench (c/hash-password "password")))

(crit/with-progress-reporting
  (crit/quick-bench
   (c/remove-ns-from-ref c/initial-dataset)))

(crit/with-progress-reporting
  (crit/quick-bench
   (c/remove-ns-from-ref c/registered-users)))

(crit/with-progress-reporting
  (crit/quick-bench
   (c/remove-ns-from-ref c/favorites-base)))

(crit/with-progress-reporting
  (crit/quick-bench
   (c/join-favs c/registered-users)))

(crit/with-progress-reporting
  (crit/quick-bench
   (c/clean-up-favs c/registered-users)))

(crit/with-progress-reporting
  (crit/quick-bench
   (c/is-user-logged-in? "ivana" @c/logged-in-users)))

(crit/with-progress-reporting
  (crit/quick-bench
   (c/remove-user @c/logged-in-users "ivana")))

;;Fastest - 18.68 ns
(crit/with-progress-reporting
  (crit/quick-bench
   (let [users '({:username "ivana" :favs ()})]
     (c/update-favs users "ivana" {:id 4,
                                   :title "Easy Mojitos",
                                   :total-time "5",
                                   :serving-size "1 cocktail",
                                   :ingr
                                   "12 leaves mint, 2 lime slices, 1 teaspoon white sugar or more to taste, ¼ cup ice cubes or as needed, 1 (1.5 fluid ounce) jigger rum (such as Bacardi®), 4 ½ ounces diet lemon-lime soda (such as Diet Sprite®)\r\n",
                                   :instructions
                                   "Place mint leaves, lime slice, and sugar in bottom of a glass and muddle with a spoon until mint is crushed. Fill glass with ice cubes. Pour rum and soda over the ice stir.\r\n",
                                   :difficulty "easy",
                                   :fav 2}))))

;;Slowest -  1.29 µs
(crit/with-progress-reporting
  (crit/quick-bench (c/generate-report "ivana")))