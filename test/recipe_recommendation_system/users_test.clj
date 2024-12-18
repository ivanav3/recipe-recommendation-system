(ns recipe-recommendation-system.users-test
  (:require
   [recipe-recommendation-system.core :as c]
   [recipe-recommendation-system.users :as u]
   [midje.sweet :refer :all]
   [criterium.core :as crit]))

(facts
 "extract-favs-test"
 (u/extract-favs (c/get-user-by-username "ivana")) =>
 (set (map #(:title %) (:favs (c/get-user-by-username "ivana"))))
 "users-recommend-test"
 (u/users-recommend (first (filter #(= (:title %) "Easy Mojitos") @c/initial-dataset)) "ivana") =not=> nil
 "by-users-recipe-test"
 (u/by-users-recipe "ivana") =not=> empty)

(facts
 "similarities-tests"
 (let [u1 {:id 39,
           :username "ivana",
           :password "feee3235626c079da26ff0ccbacba430f809cdf5a8dc7358aee52f05ae2e6ed0",
           :favs
           '({:fav 2,
              :difficulty "easy",
              :instructions
              "Place mint leaves, lime slice, and sugar in bottom of a glass and muddle with a spoon until mint is crushed. Fill glass with ice cubes. Pour rum and soda over the ice stir.\r\n",
              :title "Easy Mojitos",
              :ingr
              "12 leaves mint, 2 lime slices, 1 teaspoon white sugar or more to taste, ¼ cup ice cubes or as needed, 1 (1.5 fluid ounce) jigger rum (such as Bacardi®), 4 ½ ounces diet lemon-lime soda (such as Diet Sprite®)\r\n",
              :id 4,
              :total-time "5",
              :serving-size "1 cocktail"}
             {:fav 1,
              :difficulty "easy",
              :instructions
              "Pulse the garlic and onion in a blender until very finely chopped.  Pour in orange juice, lime juice season with cumin, oregano, lemon-pepper, black pepper, salt, cilantro, and hot pepper sauce.  Blend until thoroughly incorporated.  Pour in the olive oil, and blend until smooth.\r\n",
              :title "Mojo Grilling Marinade",
              :ingr
              "6 cloves garlic coarsely chopped, ½ cup minced yellow onion, 1 cup freshly squeezed orange juice, ½ cup freshly squeezed lime juice, ½ teaspoon ground cumin, 1 teaspoon dried oregano flakes, ½ teaspoon lemon-pepper seasoning, ½ teaspoon freshly ground black pepper, 1 teaspoon kosher salt, ¼ cup chopped cilantro, 1 teaspoon hot pepper sauce (e.g. Tabasco™), 1 cup olive oil\r\n",
              :id 5,
              :total-time "15",
              :serving-size "3 cups"})}
       u2 {:id 36,
           :username "ivana10",
           :password "40f03b3eb82aea157048232f75d4745e2f5846f2d0185ce1fc4b523ef892952b",
           :favs
           '({:fav 1,
              :difficulty "easy",
              :instructions
              "In a large bowl, stir together the flour, baking powder and salt. Rub in the butter until it is in pieces no larger than peas. Mix in water 1 tablespoon at a time just until the mixture is wet enough to form into a ball. The dough should be a firm consistency. Knead briefly. Heat the oil in a large heavy skillet over medium heat until hot. Break off pieces of the dough and shape into a patty - kind of like a flat biscuit. Place just enough of the dumplings in the pan so they are not crowded. Fry on each side until golden brown, about 3 minutes per side. Remove from the pan and drain on paper towels before serving.\r\n",
              :title "Jamaican Fried Dumplings",
              :ingr
              "4 cups all-purpose flour, 2 teaspoons baking powder, 1 ½ teaspoons salt, ½ cup butter, ½ cup cold water, 1 cup vegetable oil for frying",
              :id 1,
              :total-time "20",
              :serving-size "6 servings"}
             {:fav 2,
              :difficulty "easy",
              :instructions
              "Place mint leaves, lime slice, and sugar in bottom of a glass and muddle with a spoon until mint is crushed. Fill glass with ice cubes. Pour rum and soda over the ice stir.\r\n",
              :title "Easy Mojitos",
              :ingr
              "12 leaves mint, 2 lime slices, 1 teaspoon white sugar or more to taste, ¼ cup ice cubes or as needed, 1 (1.5 fluid ounce) jigger rum (such as Bacardi®), 4 ½ ounces diet lemon-lime soda (such as Diet Sprite®)\r\n",
              :id 4,
              :total-time "5",
              :serving-size "1 cocktail"})}]
   (do
     (u/jaccard-similarity u1 u2) => 0.333)
   (u/cosine-similarity u1 u2) => 0.5))

(facts "most-similar-users-tests"
       (u/most-similar-user @c/registered-users (c/get-user-by-username "ivana") u/jaccard-similarity) =not=> nil
       (u/most-similar-users (c/get-user-by-username "ivana")) =not=> nil
       (u/print-recs "ivana") =not=> empty)

;;Performance of functions.

(crit/with-progress-reporting
  (crit/quick-bench (u/extract-favs (c/get-user-by-username "ivana"))))

(crit/with-progress-reporting
  (crit/quick-bench (u/users-recommend (filter #(= (:title %) "Easy Mojitos") @c/initial-dataset) "ivana")))


(crit/with-progress-reporting
  (crit/quick-bench (let [u1 {:id 39,
                              :username "ivana",
                              :password "feee3235626c079da26ff0ccbacba430f809cdf5a8dc7358aee52f05ae2e6ed0",
                              :favs
                              '({:fav 2,
                                 :difficulty "easy",
                                 :instructions
                                 "Place mint leaves, lime slice, and sugar in bottom of a glass and muddle with a spoon until mint is crushed. Fill glass with ice cubes. Pour rum and soda over the ice stir.\r\n",
                                 :title "Easy Mojitos",
                                 :ingr
                                 "12 leaves mint, 2 lime slices, 1 teaspoon white sugar or more to taste, ¼ cup ice cubes or as needed, 1 (1.5 fluid ounce) jigger rum (such as Bacardi®), 4 ½ ounces diet lemon-lime soda (such as Diet Sprite®)\r\n",
                                 :id 4,
                                 :total-time "5",
                                 :serving-size "1 cocktail"}
                                {:fav 1,
                                 :difficulty "easy",
                                 :instructions
                                 "Pulse the garlic and onion in a blender until very finely chopped.  Pour in orange juice, lime juice season with cumin, oregano, lemon-pepper, black pepper, salt, cilantro, and hot pepper sauce.  Blend until thoroughly incorporated.  Pour in the olive oil, and blend until smooth.\r\n",
                                 :title "Mojo Grilling Marinade",
                                 :ingr
                                 "6 cloves garlic coarsely chopped, ½ cup minced yellow onion, 1 cup freshly squeezed orange juice, ½ cup freshly squeezed lime juice, ½ teaspoon ground cumin, 1 teaspoon dried oregano flakes, ½ teaspoon lemon-pepper seasoning, ½ teaspoon freshly ground black pepper, 1 teaspoon kosher salt, ¼ cup chopped cilantro, 1 teaspoon hot pepper sauce (e.g. Tabasco™), 1 cup olive oil\r\n",
                                 :id 5,
                                 :total-time "15",
                                 :serving-size "3 cups"})}
                          u2 {:id 36,
                              :username "ivana10",
                              :password "40f03b3eb82aea157048232f75d4745e2f5846f2d0185ce1fc4b523ef892952b",
                              :favs
                              '({:fav 1,
                                 :difficulty "easy",
                                 :instructions
                                 "In a large bowl, stir together the flour, baking powder and salt. Rub in the butter until it is in pieces no larger than peas. Mix in water 1 tablespoon at a time just until the mixture is wet enough to form into a ball. The dough should be a firm consistency. Knead briefly. Heat the oil in a large heavy skillet over medium heat until hot. Break off pieces of the dough and shape into a patty - kind of like a flat biscuit. Place just enough of the dumplings in the pan so they are not crowded. Fry on each side until golden brown, about 3 minutes per side. Remove from the pan and drain on paper towels before serving.\r\n",
                                 :title "Jamaican Fried Dumplings",
                                 :ingr
                                 "4 cups all-purpose flour, 2 teaspoons baking powder, 1 ½ teaspoons salt, ½ cup butter, ½ cup cold water, 1 cup vegetable oil for frying",
                                 :id 1,
                                 :total-time "20",
                                 :serving-size "6 servings"}
                                {:fav 2,
                                 :difficulty "easy",
                                 :instructions
                                 "Place mint leaves, lime slice, and sugar in bottom of a glass and muddle with a spoon until mint is crushed. Fill glass with ice cubes. Pour rum and soda over the ice stir.\r\n",
                                 :title "Easy Mojitos",
                                 :ingr
                                 "12 leaves mint, 2 lime slices, 1 teaspoon white sugar or more to taste, ¼ cup ice cubes or as needed, 1 (1.5 fluid ounce) jigger rum (such as Bacardi®), 4 ½ ounces diet lemon-lime soda (such as Diet Sprite®)\r\n",
                                 :id 4,
                                 :total-time "5",
                                 :serving-size "1 cocktail"})}]

                      (u/jaccard-similarity u1 u2))))


(crit/with-progress-reporting
  (crit/quick-bench (let [u1 {:id 39,
                              :username "ivana",
                              :password "feee3235626c079da26ff0ccbacba430f809cdf5a8dc7358aee52f05ae2e6ed0",
                              :favs
                              '({:fav 2,
                                 :difficulty "easy",
                                 :instructions
                                 "Place mint leaves, lime slice, and sugar in bottom of a glass and muddle with a spoon until mint is crushed. Fill glass with ice cubes. Pour rum and soda over the ice stir.\r\n",
                                 :title "Easy Mojitos",
                                 :ingr
                                 "12 leaves mint, 2 lime slices, 1 teaspoon white sugar or more to taste, ¼ cup ice cubes or as needed, 1 (1.5 fluid ounce) jigger rum (such as Bacardi®), 4 ½ ounces diet lemon-lime soda (such as Diet Sprite®)\r\n",
                                 :id 4,
                                 :total-time "5",
                                 :serving-size "1 cocktail"}
                                {:fav 1,
                                 :difficulty "easy",
                                 :instructions
                                 "Pulse the garlic and onion in a blender until very finely chopped.  Pour in orange juice, lime juice season with cumin, oregano, lemon-pepper, black pepper, salt, cilantro, and hot pepper sauce.  Blend until thoroughly incorporated.  Pour in the olive oil, and blend until smooth.\r\n",
                                 :title "Mojo Grilling Marinade",
                                 :ingr
                                 "6 cloves garlic coarsely chopped, ½ cup minced yellow onion, 1 cup freshly squeezed orange juice, ½ cup freshly squeezed lime juice, ½ teaspoon ground cumin, 1 teaspoon dried oregano flakes, ½ teaspoon lemon-pepper seasoning, ½ teaspoon freshly ground black pepper, 1 teaspoon kosher salt, ¼ cup chopped cilantro, 1 teaspoon hot pepper sauce (e.g. Tabasco™), 1 cup olive oil\r\n",
                                 :id 5,
                                 :total-time "15",
                                 :serving-size "3 cups"})}
                          u2 {:id 36,
                              :username "ivana10",
                              :password "40f03b3eb82aea157048232f75d4745e2f5846f2d0185ce1fc4b523ef892952b",
                              :favs
                              '({:fav 1,
                                 :difficulty "easy",
                                 :instructions
                                 "In a large bowl, stir together the flour, baking powder and salt. Rub in the butter until it is in pieces no larger than peas. Mix in water 1 tablespoon at a time just until the mixture is wet enough to form into a ball. The dough should be a firm consistency. Knead briefly. Heat the oil in a large heavy skillet over medium heat until hot. Break off pieces of the dough and shape into a patty - kind of like a flat biscuit. Place just enough of the dumplings in the pan so they are not crowded. Fry on each side until golden brown, about 3 minutes per side. Remove from the pan and drain on paper towels before serving.\r\n",
                                 :title "Jamaican Fried Dumplings",
                                 :ingr
                                 "4 cups all-purpose flour, 2 teaspoons baking powder, 1 ½ teaspoons salt, ½ cup butter, ½ cup cold water, 1 cup vegetable oil for frying",
                                 :id 1,
                                 :total-time "20",
                                 :serving-size "6 servings"}
                                {:fav 2,
                                 :difficulty "easy",
                                 :instructions
                                 "Place mint leaves, lime slice, and sugar in bottom of a glass and muddle with a spoon until mint is crushed. Fill glass with ice cubes. Pour rum and soda over the ice stir.\r\n",
                                 :title "Easy Mojitos",
                                 :ingr
                                 "12 leaves mint, 2 lime slices, 1 teaspoon white sugar or more to taste, ¼ cup ice cubes or as needed, 1 (1.5 fluid ounce) jigger rum (such as Bacardi®), 4 ½ ounces diet lemon-lime soda (such as Diet Sprite®)\r\n",
                                 :id 4,
                                 :total-time "5",
                                 :serving-size "1 cocktail"})}]

                      (u/cosine-similarity u1 u2))))

(crit/with-progress-reporting
  (crit/quick-bench (u/most-similar-user @c/registered-users (c/get-user-by-username "ivana") u/jaccard-similarity)))

(crit/with-progress-reporting
  (crit/quick-bench (u/most-similar-users (c/get-user-by-username "ivana"))))