(ns recipe-recommendation-system.core-test
  (:require [clojure.test :refer :all]
            [recipe-recommendation-system.core :as c]
            [midje.sweet :refer :all]
            [recipe-recommendation-system.utils :as utils]
            [recipe-recommendation-system.data :as d]
            [recipe-recommendation-system.content :as content]
            [recipe-recommendation-system.users :as users]
            [clojure.test :as t]
            [criterium.core :as crit]
            [next.jdbc :as jdbc]))

;;User wants to add recipe to favorites. That way new recipes can be recommended in multiple ways.
(facts "test-adding-to-favs"
       (let [users (ref (or (seq (jdbc/execute! d/db-spec ["SELECT * FROM user"])) []))
             recipes (ref (or (seq (jdbc/execute! d/db-spec ["SELECT * FROM recipe"])) [])
                          :validator
                          (comp not nil?))
             favorites-base (ref (jdbc/execute! d/db-spec ["SELECT r.*, f.user_id FROM recipe r
                                  JOIN favorites f ON r.id=f.recipe_id "]))]
         (c/remove-ns-from-ref users)
         (c/remove-ns-from-ref recipes)
         (c/remove-ns-from-ref favorites-base)
         (c/join-favs users favorites-base)
         (c/clean-up-favs users)
         (c/clean-strings recipes)
         (try
           (c/choose-fav "ivana" users recipes)
           (catch Exception e))) :truthy)

;;User wants to get recipes that are recommended by difficulty. That way user can discover new recipes with the same difficulty as chosen recipe.
(facts "test-recommend-by-difficulty"
       (let [recipes  (ref (or (seq (jdbc/execute! d/db-spec ["SELECT * FROM recipe"])) [])
                           :validator
                           (comp not nil?))]
         (c/remove-ns-from-ref recipes)
         (c/clean-strings recipes)
         (content/recommend-by-difficulty (first (filter #(= (:title %) "Easy Mojitos") @recipes)) @recipes)) =not=> nil)

;;User wants to get report on recent activity. That way users can track their improvement. 
(facts "test-report"
       (let [users (ref (or (seq (jdbc/execute! d/db-spec ["SELECT * FROM user"])) []))
             recipes (ref (or (seq (jdbc/execute! d/db-spec ["SELECT * FROM recipe"])) [])
                          :validator
                          (comp not nil?))
             favorites-base (ref (jdbc/execute! d/db-spec ["SELECT r.*, f.user_id FROM recipe r
                                         JOIN favorites f ON r.id=f.recipe_id "]))]
         (c/remove-ns-from-ref users)
         (c/remove-ns-from-ref recipes)
         (c/remove-ns-from-ref favorites-base)
         (c/join-favs users favorites-base)
         (c/clean-up-favs users)
         (c/clean-strings recipes)
         (utils/contains-more (c/generate-report (first (filter #(= (:username %) "ivana") @users)))
                              :username :num-favs :difficulty-levels :avg-difficulty :report-time)) => true)

;;User wants to get recipes that are recommended by other users. 
;;That way users can connect and discover recipes that are recommended by users with similar taste.
(facts "test-recommend-by-favs"
       (let [users (ref (or (seq (jdbc/execute! d/db-spec ["SELECT * FROM user"])) []))
             recipes (ref (or (seq (jdbc/execute! d/db-spec ["SELECT * FROM recipe"])) [])
                          :validator
                          (comp not nil?))
             favorites-base (ref (jdbc/execute! d/db-spec ["SELECT r.*, f.user_id FROM recipe r
                                         JOIN favorites f ON r.id=f.recipe_id "]))]
         (c/remove-ns-from-ref users)
         (c/remove-ns-from-ref recipes)
         (c/remove-ns-from-ref favorites-base)
         (c/join-favs users favorites-base)
         (c/clean-up-favs users)
         (c/clean-strings recipes)
         (users/users-recommend (first (filter #(= (:title %) "Easy Mojitos") @recipes)) "ivana" users)) =not=> nil)

;;User wants to find out which recipes are the most popular. That way users can discover current trends in recipes. 
(facts "test-recommend-by-popularity"
       (let [recipes (ref (or (seq (jdbc/execute! d/db-spec ["SELECT * FROM recipe"])) [])
                          :validator
                          (comp not nil?))]
         (c/remove-ns-from-ref recipes)
         (c/clean-strings recipes)
         (c/choose-by-popularity recipes)) =not=> empty)

;;User wants to find another user with similar taste in recipes. That way users can connect and find recipes liked by similar users.
(facts "test-jaccard-similarity"
       (let [users (ref (or (seq (jdbc/execute! d/db-spec ["SELECT * FROM user"])) []))
             recipes (ref (or (seq (jdbc/execute! d/db-spec ["SELECT * FROM recipe"])) [])
                          :validator
                          (comp not nil?))
             favorites-base (ref (jdbc/execute! d/db-spec ["SELECT r.*, f.user_id FROM recipe r
                                                JOIN favorites f ON r.id=f.recipe_id "]))]
         (c/remove-ns-from-ref users)
         (c/remove-ns-from-ref recipes)
         (c/remove-ns-from-ref favorites-base)
         (c/join-favs users favorites-base)
         (c/clean-up-favs users)
         (c/clean-strings recipes)
         (users/most-similar-user @users (utils/get-user-by-username "ivana" users) users/jaccard-similarity)) => ["ivana10" 0.2])


(facts "test-cosine-similarity"
       (let [users (ref (or (seq (jdbc/execute! d/db-spec ["SELECT * FROM user"])) []))
             recipes (ref (or (seq (jdbc/execute! d/db-spec ["SELECT * FROM recipe"])) [])
                          :validator
                          (comp not nil?))
             favorites-base (ref (jdbc/execute! d/db-spec ["SELECT r.*, f.user_id FROM recipe r
                                                JOIN favorites f ON r.id=f.recipe_id "]))]
         (c/remove-ns-from-ref users)
         (c/remove-ns-from-ref recipes)
         (c/remove-ns-from-ref favorites-base)
         (c/join-favs users favorites-base)
         (c/clean-up-favs users)
         (c/clean-strings recipes)
         (users/most-similar-user @users (utils/get-user-by-username "ivana" users) users/cosine-similarity)) => ["ivana10" 0.333])

;;User wants to find another users with similar taste in recipes. This time all similarities are included.
(facts "test-most-similar-users"
       (let [users (ref (or (seq (jdbc/execute! d/db-spec ["SELECT * FROM user"])) []))
             recipes (ref (or (seq (jdbc/execute! d/db-spec ["SELECT * FROM recipe"])) [])
                          :validator
                          (comp not nil?))
             favorites-base (ref (jdbc/execute! d/db-spec ["SELECT r.*, f.user_id FROM recipe r
                                         JOIN favorites f ON r.id=f.recipe_id "]))]
         (c/remove-ns-from-ref users)
         (c/remove-ns-from-ref recipes)
         (c/remove-ns-from-ref favorites-base)
         (c/join-favs users favorites-base)
         (c/clean-up-favs users)
         (c/clean-strings recipes)
         (users/most-similar-users (utils/get-user-by-username "ivana" users) users) =not=> nil))


;;Functions that are needed for authentication.
(facts "hash-password-test"
       (c/hash-password "ivana") =not=> nil)

(facts "register-test"
       (let [users (ref (or (seq (jdbc/execute! d/db-spec ["SELECT * FROM user"])) []))
             recipes (ref (or (seq (jdbc/execute! d/db-spec ["SELECT * FROM recipe"])) [])
                          :validator
                          (comp not nil?))
             favorites-base (ref (jdbc/execute! d/db-spec ["SELECT r.*, f.user_id FROM recipe r
                                                      JOIN favorites f ON r.id=f.recipe_id "]))]
         (c/remove-ns-from-ref users)
         (c/remove-ns-from-ref recipes)
         (c/remove-ns-from-ref favorites-base)
         (c/join-favs users favorites-base)
         (c/clean-up-favs users)
         (c/clean-strings recipes)
         (c/register users)) =not=> empty)

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
         (c/remove-ns-from-ref ds)
         @ds) => '({:id 10, :username "ivana10"}))

(facts
 "cleaning-data-test"
 (let [users (ref (or (seq (jdbc/execute! d/db-spec ["SELECT * FROM user"])) []))
       recipes (ref (or (seq (jdbc/execute! d/db-spec ["SELECT * FROM recipe"])) [])
                    :validator
                    (comp not nil?))
       favorites-base (ref (jdbc/execute! d/db-spec ["SELECT r.*, f.user_id FROM recipe r
                                          JOIN favorites f ON r.id=f.recipe_id "]))]
   (c/remove-ns-from-ref users)
   (c/remove-ns-from-ref recipes)
   (c/remove-ns-from-ref favorites-base)
   (c/join-favs users favorites-base)
   (c/clean-up-favs users)
   (c/clean-strings recipes)) =not=> nil)

;;Functions related to adding to favorites.
(facts "find-by-title-test"
       (utils/find-by-title "title1" '({:title "title1" :time 22}, {:title "title2" :time 32})) => '({:title "title1" :time 22})
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
       (let [users (ref (or (seq (jdbc/execute! d/db-spec ["SELECT * FROM user"])) []))
             recipes (ref (or (seq (jdbc/execute! d/db-spec ["SELECT * FROM recipe"])) [])
                          :validator
                          (comp not nil?))
             favorites-base (ref (jdbc/execute! d/db-spec ["SELECT r.*, f.user_id FROM recipe r
                                                JOIN favorites f ON r.id=f.recipe_id "]))]
         (c/remove-ns-from-ref users)
         (c/remove-ns-from-ref recipes)
         (c/remove-ns-from-ref favorites-base)
         (c/join-favs users favorites-base)
         (c/clean-up-favs users)
         (c/clean-strings recipes)
         (with-in-str "Easy Mojitos\nEasy Mojitos"
           (c/choose-fav "ivana13" users recipes))) =not=> nil)


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
       (let [users (ref (or (seq (jdbc/execute! d/db-spec ["SELECT * FROM user"])) []))
             recipes (ref (or (seq (jdbc/execute! d/db-spec ["SELECT * FROM recipe"])) [])
                          :validator
                          (comp not nil?))
             favorites-base (ref (jdbc/execute! d/db-spec ["SELECT r.*, f.user_id FROM recipe r
                                                JOIN favorites f ON r.id=f.recipe_id "]))]
         (c/remove-ns-from-ref users)
         (c/remove-ns-from-ref recipes)
         (c/remove-ns-from-ref favorites-base)
         (c/join-favs users favorites-base)
         (c/clean-up-favs users)
         (c/clean-strings recipes)
         (with-in-str "Easy Mojitos\nEasy Mojitos"
           (c/remove-fav "ivana13" users recipes))) =not=> nil)

;;Menus tested.

(facts "main-menu-test"
       (let [users (ref (or (seq (jdbc/execute! d/db-spec ["SELECT * FROM user"])) []))
             recipes (ref (or (seq (jdbc/execute! d/db-spec ["SELECT * FROM recipe"])) [])
                          :validator
                          (comp not nil?))
             favorites-base (ref (jdbc/execute! d/db-spec ["SELECT r.*, f.user_id FROM recipe r
                                                JOIN favorites f ON r.id=f.recipe_id "]))
             logged-in-users (atom [])]
         (c/remove-ns-from-ref users)
         (c/remove-ns-from-ref recipes)
         (c/remove-ns-from-ref favorites-base)
         (c/join-favs users favorites-base)
         (c/clean-up-favs users)
         (c/clean-strings recipes)

         (with-in-str "10\nivana"
           (c/main-menu "ivana" users logged-in-users))) =not=> empty)
(facts
 "main-test"
 (with-in-str "2\n"
   (c/-main)) =not=> empty)

(facts "login-twice-test"
       (let [users (ref (or (seq (jdbc/execute! d/db-spec ["SELECT * FROM user"])) []))
             recipes (ref (or (seq (jdbc/execute! d/db-spec ["SELECT * FROM recipe"])) [])
                          :validator
                          (comp not nil?))
             favorites-base (ref (jdbc/execute! d/db-spec ["SELECT r.*, f.user_id FROM recipe r
                                                JOIN favorites f ON r.id=f.recipe_id "]))]
         (c/remove-ns-from-ref users)
         (c/remove-ns-from-ref recipes)
         (c/remove-ns-from-ref favorites-base)
         (c/join-favs users favorites-base)
         (c/clean-up-favs users)
         (c/clean-strings recipes)
         (try
           (do
             (with-in-str "ivana\nivana\n11"
               (c/login users))
             (with-in-str "ivana\nivana"
               (c/login users)))

           (catch Exception e
             (is (= "User is already logged in. Please logout first." (ex-message e)))
             (is (= {:username "ivana"} (ex-data e))))) => true))

(facts "logout-test"
       (let [logged-in-users (atom [])]
         (try
           (with-in-str "ivanalogout\n"
             (c/logout logged-in-users))

           (catch Exception e
             (is (= "No such user is logged in.") (ex-message e))
             (is (= {:username "ivanalogout"} (ex-data e)))))) => true)

(facts "register-test"
       (let [users (ref (or (seq (jdbc/execute! d/db-spec ["SELECT * FROM user"])) []))
             recipes (ref (or (seq (jdbc/execute! d/db-spec ["SELECT * FROM recipe"])) [])
                          :validator
                          (comp not nil?))
             favorites-base (ref (jdbc/execute! d/db-spec ["SELECT r.*, f.user_id FROM recipe r
                                                JOIN favorites f ON r.id=f.recipe_id "]))]
         (c/remove-ns-from-ref users)
         (c/remove-ns-from-ref recipes)
         (c/remove-ns-from-ref favorites-base)
         (c/join-favs users favorites-base)
         (c/clean-up-favs users)
         (c/clean-strings recipes)
         (try
           (with-in-str "ivana101\nivana101"
             (c/register users))
           (catch Exception e
             (is (= "This username is taken, try again.") (ex-message e))
             (is (= {:username "ivana101"}) (ex-data e)))) => true))

(facts "add-and-remove-from-favs"
       (let [users (ref (or (seq (jdbc/execute! d/db-spec ["SELECT * FROM user"])) []))
             recipes (ref (or (seq (jdbc/execute! d/db-spec ["SELECT * FROM recipe"])) [])
                          :validator
                          (comp not nil?))
             favorites-base (ref (jdbc/execute! d/db-spec ["SELECT r.*, f.user_id FROM recipe r
                                                JOIN favorites f ON r.id=f.recipe_id "]))]
         (c/remove-ns-from-ref users)
         (c/remove-ns-from-ref recipes)
         (c/remove-ns-from-ref favorites-base)
         (c/join-favs users favorites-base)
         (c/clean-up-favs users)
         (c/clean-strings recipes)
         (do (with-in-str "Easy Mojitos\nEasy Mojitos"
               (c/choose-fav "ivana101" users recipes))
             (with-in-str "Easy Mojitos\nEasy Mojitos"
               (c/remove-fav "ivana101" users recipes))) =not=> nil))


;;Performances.

;; Slowest - 38.13 µs
(crit/with-progress-reporting
  (crit/quick-bench (c/hash-password "password")))

;; 4.23 µs
(let [recipes (ref (or (seq (jdbc/execute! d/db-spec ["SELECT * FROM recipe"])) [])
                   :validator
                   (comp not nil?))]

  (c/remove-ns-from-ref recipes)
  (c/clean-strings recipes)
  (crit/with-progress-reporting
    (crit/quick-bench
     (c/remove-ns-from-ref recipes))))

;; 3.65 µs
(let [users (ref (or (seq (jdbc/execute! d/db-spec ["SELECT * FROM user"])) []))
      recipes (ref (or (seq (jdbc/execute! d/db-spec ["SELECT * FROM recipe"])) [])
                   :validator
                   (comp not nil?))
      favorites-base (ref (jdbc/execute! d/db-spec ["SELECT r.*, f.user_id FROM recipe r
                                         JOIN favorites f ON r.id=f.recipe_id "]))]
  (c/remove-ns-from-ref users)
  (c/remove-ns-from-ref recipes)
  (c/remove-ns-from-ref favorites-base)
  (c/join-favs users favorites-base)
  (c/clean-up-favs users)
  (c/clean-strings recipes)
  (crit/with-progress-reporting
    (crit/quick-bench
     (c/remove-ns-from-ref users))))

;; 5.62 µs
(let [favorites-base (ref (jdbc/execute! d/db-spec ["SELECT r.*, f.user_id FROM recipe r
                                         JOIN favorites f ON r.id=f.recipe_id "]))]

  (c/remove-ns-from-ref favorites-base)
  (crit/with-progress-reporting
    (crit/quick-bench
     (c/remove-ns-from-ref favorites-base))))

;; 3.66 µs
(let [users (ref (or (seq (jdbc/execute! d/db-spec ["SELECT * FROM user"])) []))
      recipes (ref (or (seq (jdbc/execute! d/db-spec ["SELECT * FROM recipe"])) [])
                   :validator
                   (comp not nil?))
      favorites-base (ref (jdbc/execute! d/db-spec ["SELECT r.*, f.user_id FROM recipe r
                                         JOIN favorites f ON r.id=f.recipe_id "]))]
  (c/remove-ns-from-ref users)
  (c/remove-ns-from-ref recipes)
  (c/remove-ns-from-ref favorites-base)
  (c/join-favs users favorites-base)
  (c/clean-up-favs users)
  (c/clean-strings recipes)
  (crit/with-progress-reporting
    (crit/quick-bench
     (c/join-favs users favorites-base))))

;; 3.51 µs
(let [users (ref (or (seq (jdbc/execute! d/db-spec ["SELECT * FROM user"])) []))
      recipes (ref (or (seq (jdbc/execute! d/db-spec ["SELECT * FROM recipe"])) [])
                   :validator
                   (comp not nil?))
      favorites-base (ref (jdbc/execute! d/db-spec ["SELECT r.*, f.user_id FROM recipe r
                                         JOIN favorites f ON r.id=f.recipe_id "]))]
  (c/remove-ns-from-ref users)
  (c/remove-ns-from-ref recipes)
  (c/remove-ns-from-ref favorites-base)
  (c/join-favs users favorites-base)
  (c/clean-up-favs users)
  (c/clean-strings recipes)
  (crit/with-progress-reporting
    (crit/quick-bench
     (c/clean-up-favs users))))

;; 87.09 ns

(crit/with-progress-reporting
  (crit/quick-bench
   (c/is-user-logged-in? "ivana" (atom []))))

;; 22.15 ns
(crit/with-progress-reporting
  (crit/quick-bench
   (c/remove-user (atom [{:username "ivana"}]) "ivana")))

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

;;  1.29 µs
(crit/with-progress-reporting
  (crit/quick-bench (c/generate-report "ivana")))