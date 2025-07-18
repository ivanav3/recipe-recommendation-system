# Recipe recommender

This project was written in Clojure.

Clone the project with the command:

`git clone https://github.com/ivanav3/recipe-recommendation-system`

It is necessary to import the database. If you are using XAMPP, make sure the MySQL module is running.

This project was created using Leiningen, therefore we can run this Clojure application from the command line by entering the following command:

`lein run`

## User stories

User stories describe different functionalities that are valuable for users. The following user stories are listed:

- User wants to find out which recipes are the most popular. That way users can discover current trends in recipes.

- User wants to get recipes that are recommended by difficulty. That way user can discover new recipes with the same difficulty as chosen recipe.

- User wants to get recipes that are recommended based on currently most common difficulty. That way users can discover more recipes with the same difficulty as their previously chosen recipes.

- User wants to get report on recent activity. That way users can track their improvement.

- User wants to get recipes that are recommended by other users. That way users can connect and discover recipes that are recommended by users with similar taste.

- User wants to find another user with similar taste in recipes. That way users can connect and find recipes liked by similar users.

- User wants to get recipes that are recommended based on the similar content. That way user can discover recipes that resemble the chosen one.

## Usage

The database encompasses a sample of 50 recipes.

Each instance contains the following attributes:

- **title**: the name of the recipe.
- **total-time**: the ammount of time needed for preparing the recipe.
- **serving-size**: the measurement of the portion.
- **ingr**: ingredients of the recipe.
- **instructions**: the preparing process.
- **difficulty**: difficulty of the recipe.
- **fav**: how many times has a recipe been chosen as a favorite by users.

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
8. Recommendations by users that have chosen the same recipe
9. Recommendations by similar users
10. Content recommendation
11. Logout
12. Exit (without logout)
Please select an option:
```

There are two main aspects: content-based filtering and collaborative filtering.

- Content-based filtering:

In this type of filtering, the similarities are specifically related to the content (in this example - recipes), not multiple users. This technique is used for discovering the similar content based on previous reactions by a specific user. The user can react differently to content - similar content to what is selected as prefered will be further recommended.

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

The similarities are established by calculating the preferences of the users and their comparison. Based on those characteristics, new recommendations are created. As a result of those predictions, similar content is recommended. In other words, preferences are made based on what similar users like. This type of recommendation technique can be additionally detailed.

Therefore, users can also receive recommendations based on the preferences of other users. User can choose a recipe and by doing that user can get recipes that were chosen by the other users who also chose that specific recipe.

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

The following metrics have been used for calculating the similarities. Sets of favorite recipes are presented as vectors, based on whether the set contains a recipe from the set of total recipes chosen by two users or not.

Jaccard similarity:

![1](https://github.com/ivanav3/recipe-recommendation-system/blob/main/metrics/Jaccard.PNG)

Cosine similarity:

![2](https://github.com/ivanav3/recipe-recommendation-system/blob/main/metrics/Cosine.PNG)

Euclidean distance:

![3](https://github.com/ivanav3/recipe-recommendation-system/blob/main/metrics/Euclidean.PNG)

Manhattan distance:

![4](https://github.com/ivanav3/recipe-recommendation-system/blob/main/metrics/Manhattan.PNG)

Moreover, users can get a report on their activity. This way they can stay updated on their progress.

```
Username: ivana
Number of chosen recipes: 3
Difficulties in numbers: Easy - 2, Medium - 0, Hard -1
Average difficulty: 1.667 (max 3)
Current time: 2025-01-09T20:25:33.028498200
-------------------------------------------
```

All of the functions have been tested. The performance of each function has been compared.

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
