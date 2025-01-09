# Recipe recommender

The database encompasses a sample of 50 recipes.

Each instance contains the following attributes:

- title
- total-time
- serving-size
- ingr
- instructions
- difficulty
- fav

By adding and removing recipes from favorites, users can get recipes recommended. Multiple options are available in the main menu.

```Welcome, user
Main Menu:
0. View all recipes
1. Choose a recipe
2. View popular recipes
3. View favorites
4. Remove from favorites
5. Recommend by difficulty
6. Recommend by currently most common difficulty
7. Generate a report
8. Recommendations by users that chose the same recipe
9. Recommendations by similar users
10. Content recommendation
11. Logout
12. Exit (without logout)
Please select an option:
```

There are two main aspects: content-based filtering and collaborative filtering.

- Content-based filtering:

In this context, recipes can be recommended by their similarity in keywords that appear in the instructions of each recipe. This way users can discover more recipes that are similar and likely to be added to favorites.

For example,

```
Found the following recipes:
Coconut-Lime Cheesecake with Mango Coulis
Please enter the full title of the recipe you're interested in:
Coconut-Lime Cheesecake with Mango Coulis
-----------------------
Recipe: Budin (Puerto Rican Bread Pudding)

Time: about 300 minutes

Ingredients:
...
-----------------------
```

Users can also get recommendations based on the difficulty of the recipes they chose. That way they can discover new recipes with the similar level of difficultiy as the recipes they have already chosen.

For example,

```
Chosen difficulty of the recipe Coconut-Lime Cheesecake with Mango Coulis is hard . The following recipes have the same level of difficulty:
-----------------------
Recipe:  Coconut Ice...
```

Also, there is an option which enables users to get recipes based on the most common difficulty of their favorites.

For example,

```
Currently most common difficulty is: easy
-----------------------
Recipe: Corn and Rice

Time: about 70 minutes...
```

- Collaborative filtering:

Users can also receive recommendations based on the preferences of other users. User can choose a recipe and by doing that user can get recipes that were chosen by the other users who also chose that specific recipe.

For example,

```
The following recipes were recommended by other users that chose Easy Mojitos as well
-----------------------
Recipe: Limber de Coco (Coconut Ice)

Time: about 485 minutes...
------------------------
Recipe: Jamaican Fried Dumplings

Time: about 20 minutes...
```

In addition, users in general can get recipes that were recommended by other users without the need of chosing a specific recipe. For example, if Ivana chose multiple recipes that were chosen by Mary as well, Ivana can discover other recipes that were chosen by Mary. This way users can connect.

```

The following recipes were chosen by users with similar taste in recipes as ivana
************************
User: mary
Favorite recipes:
------------------------
Recipe: Easy Mojitos

...

************************
User: ashley
Favorite recipes:
------------------------
Recipe: Jamaican Fried Dumplings...


```

Moreover, users can get a report on their activity. This way they can stay updated on their progress.

```
Username: ivana
Number of chosen recipes: 3
Difficulties in numbers: Easy - 2, Medium - 0, Hard -1
Average difficulty: 1.667 (max 3)
Current time: 2025-01-09T20:25:33.028498200
-------------------------------------------
```

## Usage

FIXME

## License

Copyright © 2024 FIXME

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
