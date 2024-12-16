(ns recipe-recommendation-system.utils-test
  (:require [midje.sweet :refer :all]
            [recipe-recommendation-system.core :as c]
            [recipe-recommendation-system.utils :as u]))

(facts "find-by-title-test"
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

(facts "get-favs-by-username-test"
       (u/get-favs-by-username "ivana" @c/registered-users)
       => '({:fav 2,
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
             :serving-size "3 cups"}))
