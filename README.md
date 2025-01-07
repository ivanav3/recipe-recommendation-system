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

```; Welcome,  user
; --------------------------------------------
;
; Main Menu:
; 0. View all recipes
; 1. Choose a recipe
; 2. View popular recipes
; 3. View favorites
; 4. Remove from favorites
; 5. Recommend by difficulty
; 6. Generate a report
; 7. Recommendations by users that chose the same recipe
; 8. Recommendations by similar users
; 9. Content recommendation
; 10. Logout
; 11. Exit (without logout)
; Please select an option:
```

There are two main aspects: content-based filtering and collaborative filtering.

- Content-based filtering:

In this context, recipes can be recommended by their similarity in keywords that appear in the instructions of each recipe. This way users can discover more recipes that are similar and likely to be added to favorites.

For example,

```
; Enter recipe title or part of title (from your favs):
; Found the following recipes:
; Coconut-Lime Cheesecake with Mango Coulis
; Please enter the full title of the recipe you're interested in:
; {:id 35, :title Chef John's ...}
; {:id 25, :title Pastelon ...}
```

Users can also get recommendations based on the difficulty of the recipes they chose. That way they can discover new recipes with the similar level of difficultiy as the recipes they have already chosen.

For example,

```
; Chosen difficulty of the recipe Coconut-Lime Cheesecake with Mango Coulis is hard . The following recipes have the same level of difficulty:
; {:id 29, :title Coconut Ice ...}
```

- Collaborative filtering:

Users can also receive recommendations based on the preferences of other users. User can choose a recipe and by doing that user can get recipes that were chosen by the other users who also chose that specific recipe.

For example,

```
; The following recipes were recommended by other users that chose Easy Mojitos as well
; {:id 1, :title Jamaican Fried Dumplings ...}
```

In addition, users in general can get recipes that were recommended by other users without the need of chosing a specific recipe. For example, if Ivana chose multiple recipes that were chosen by Mary as well, Ivana can discover other recipes that were chosen by Mary. This way users can connect.

```
; The following recipes were chosen by users with similar taste in recipes as ivana
; [:username mary]
; [:favs ...]
; [:username ashley]
; [:favs ...]
```

Moreover, users can get a report on their activity. This way they can stay updated on their progress.

```
; {:username ivana, :num-favs 3, :difficulty-levels {hard 1, easy 2}, :avg-difficulty 5/3, :report-time 2024-12-20T21:06:40.911151400}
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
