(ns recipe-recommendation-system.core
  (:gen-class)
  (:require [clojure.string :as str]
            [next.jdbc :as jdbc]
            [clj-memory-meter.core :as mm]
            [clj-java-decompiler.core :refer [decompile]]
            [recipe-recommendation-system.content :as content]
            [recipe-recommendation-system.users :as users]
            [recipe-recommendation-system.utils :as u]
            [recipe-recommendation-system.core :as c]
            [recipe-recommendation-system.data :as d])
  (:import (java.security MessageDigest)))


(defn hash-password [password]
  (let [md (MessageDigest/getInstance "SHA-256")
        hashed-bytes (.digest md (.getBytes password))]
    (apply str (map #(format "%02x" %) hashed-bytes))))


(defn clean-from-db [data]
  (into {} (map (fn [[k v]] [(keyword (name k)) v]) data)))

(defn remove-ns-from-ref [r]
  (dosync
   (alter r #(map clean-from-db %))))

(defn attach-favorites-to-user [user]
  (let [user-id (:id user)]
    (assoc user :favs
           (filter #(= user-id (:user_id %)) @d/favorites-base))))

(defn join-favs [r]
  (dosync
   (alter r
          (fn [users] (map #(attach-favorites-to-user %) users)))))

(defn remove-user-id-from-favorites [user]
  (let [favorites (map #(dissoc % :user_id) (:favs user))]
    (assoc user :favs favorites)))

(defn clean-up-favs [r]
  (dosync
   (alter r
          (fn [users] (map remove-user-id-from-favorites users)))))

(defn clean-strings [dataset]
  (dosync
   (alter dataset
          (fn [s]
            (map (fn [item]
                   (let [updated-title (str/replace (:title item) #"\n|\r" "")
                         updated-ingredients (str/replace (:ingr item) "\n|\r" "")
                         updated-serving-size (str/replace (:serving-size item) #"\n|\r" "")
                         updated-instructions (str/replace (:instructions item) #"\n|\r" "")]

                     (assoc item :title updated-title :serving-size updated-serving-size
                            :ingr updated-ingredients
                            :instructions updated-instructions)))
                 s)))))

(remove-ns-from-ref d/initial-dataset)
(remove-ns-from-ref d/registered-users)
(remove-ns-from-ref d/favorites-base)
(join-favs d/registered-users)
(clean-up-favs d/registered-users)
(clean-strings d/initial-dataset)

(defn register []
  (println "Username:")
  (let [username (read-line)]
    (if (some #(= username (:username %)) @d/registered-users)
      (do
        (throw (ex-info (println "This username is taken, try again.") {:username username})))
      (dosync
       (println "Password:")
       (let [password (read-line)
             hashed-password (hash-password password)
             _ (jdbc/execute! d/db-spec
                              ["INSERT INTO user (username, password) VALUES (?, ?)"
                               username hashed-password])]


         (alter d/registered-users
                (fn [users]
                  (conj users {:id (:user/id  (first (jdbc/execute! d/db-spec
                                                                    ["SELECT id FROM user WHERE username = ?"
                                                                     username])))
                               :username username
                               :password hashed-password
                               :favs []})))

         (println "Registered!" username))))))

(def logged-in-users (atom []))

(defn is-user-logged-in? [username dataset]
  (some #(= username (:username %)) dataset))

(defn remove-user [users username]
  (filter #(not= username (:username %)) users))

(defn logout []
  (println "Enter username to logout:")
  (let [username (read-line)]
    (if (is-user-logged-in? username @logged-in-users)
      (do
        (swap! logged-in-users remove-user username)
        (println username "has been logged out."))
      (throw (ex-info "No such user is logged in." {:username username})))))

(defn add-to-favs [favs chosen-recipe]
  (if (not-any? #(= (str/lower-case (:title %)) (str/lower-case (:title chosen-recipe))) favs)
    (conj favs chosen-recipe)
    (do (println "Already marked as fav.") favs)))


(defn update-favs [users username chosen-recipe]
  (map #(if (= username (:username %))
          (update % :favs add-to-favs chosen-recipe)
          %)
       users))

(defn update-rec [recipes chosen]
  (map #(if (= (str/lower-case (:title chosen)) (str/lower-case (:title %)))
          (update % :fav inc)
          %)
       recipes))

(defn choose-fav [username]
  (println "Enter recipe title or part of title:")
  (let [title (read-line)
        results (u/find-by-title title @d/initial-dataset)]
    (if (seq results)
      (do
        (println "Found the following recipes:")
        (doseq [result results]
          (println (:title result)))

        (println "Please enter the full title of the recipe you're interested in:")
        (let [chosen-title (str/lower-case (read-line))
              chosen-recipe (some #(if (= (str/lower-case (:title %)) chosen-title) %) results)]
          (if chosen-recipe
            (let [user (some #(if (= (:username %) username) %) @d/registered-users)
                  user-id (:id user)
                  recipe-id (:id chosen-recipe)]

              (if (and user-id recipe-id)
                (do
                  (dosync
                   (jdbc/execute! d/db-spec
                                  ["INSERT INTO favorites (`user_id`, `recipe_id`) VALUES (?, ?)" user-id recipe-id])
                   (jdbc/execute! d/db-spec
                                  ["UPDATE recipe SET fav = fav + 1 WHERE id = ?" recipe-id])
                   (println "Recipe added to favorites!")
                   (alter d/registered-users update-favs username chosen-recipe)
                   (alter d/initial-dataset update-rec chosen-recipe)))
                (println "Error: Could not find user or recipe ID.")))

            (println "Error. Recipe not found or invalid input."))))
      (println "No recipes found."))))

(defn choose-by-popularity [username]
  (do
    (doseq [rec (take 3
                      (sort-by :fav > @d/initial-dataset))]
      (println rec))
    (choose-fav username)))

(defn generate-report [user]
  (let [favs (count (:favs user))
        difficulty-counts (frequencies (map :difficulty (:favs user)))
        avg-difficulty (if (empty? (:favs user))
                         "No favorite recipes"
                         (let [difficulty-map {"easy" 1, "medium" 2, "hard" 3}
                               avg-diff (/ (apply + (map #(difficulty-map (:difficulty %)) (:favs user)))
                                           favs)]
                           avg-diff))
        report-time (str (java.time.LocalDateTime/now))]
    {:username (:username user)
     :num-favs favs
     :difficulty-levels difficulty-counts
     :avg-difficulty avg-difficulty
     :report-time report-time}))

;; (defn get-user-by-username [username]
;;   (first (filter #(= (:username %) username) @d/registered-users)))

(defn remove-from-favs [favs chosen-recipe]
  (if (some #(= (str/lower-case (:title %)) (str/lower-case (:title chosen-recipe))) favs)
    (remove #(= (str/lower-case (:title %)) (str/lower-case (:title chosen-recipe))) favs)
    (do (println "Recipe not found in favs.") favs)))

(defn update-favs-when-removed [users username chosen-recipe]
  (map #(if (= username (:username %))
          (update % :favs remove-from-favs chosen-recipe)
          %)
       users))

(defn dec-recipe [recipes chosen]
  (map #(if (= (str/lower-case (:title chosen)) (str/lower-case (:title %)))
          (update % :fav dec)
          %)
       recipes))

(defn remove-fav [username]
  (println "Enter recipe title or part of title:")
  (let [title (read-line)
        results (u/find-by-title title (u/get-favs-by-username username @d/registered-users))]
    (if (seq results)
      (do
        (println "Found the following recipes:")
        (doseq [result results]
          (println (:title result)))

        (println "Please enter the full title of the recipe you're interested in:")
        (let [chosen-title (str/lower-case (read-line))
              chosen-recipe (some #(if (= (str/lower-case (:title %)) chosen-title) %) results)]
          (if chosen-recipe
            (let [user (some #(if (= (:username %) username) %) @d/registered-users)
                  user-id (:id user)
                  recipe-id (:id chosen-recipe)]

              (if (and user-id recipe-id)
                (do
                  (dosync
                   (jdbc/execute! d/db-spec
                                  ["DELETE FROM favorites WHERE `user_id` = ? AND `recipe_id` = ?" user-id recipe-id])
                   (jdbc/execute! d/db-spec
                                  ["UPDATE recipe SET fav = fav - 1 WHERE id = ?" recipe-id])
                   (println "Recipe deleted from favorites!")
                   (alter d/registered-users update-favs-when-removed username chosen-recipe)
                   (alter d/initial-dataset dec-recipe chosen-recipe)))
                (println "Error: Could not find user or recipe ID.")))

            (println "Error. Recipe not found or invalid input."))))
      (println "No recipes found."))))

(defn main-menu [username]
  (println "--------------------------------------------")
  (println "\nMain Menu:")
  (println "0. View all recipes")
  (println "1. Choose a recipe")
  (println "2. View popular recipes")
  (println "3. View favorites")
  (println "4. Remove from favorites")
  (println "5. Recommend by difficulty")
  (println "6. Generate a report")
  (println "7. Recommendations by users that chose the same recipe")
  (println "8. Recommendations by similar users")
  (println "9. Content recommendation")
  (println "10. Logout")
  (println "11. Exit (without logout)")
  (println "Please select an option:")

  (let [option (read-line)]
    (cond
      (= option "0")
      (do
        (doseq [rec @d/initial-dataset]
          (println rec))
        (main-menu username))
      (= option "1")
      (do
        (try
          (choose-fav username)
          (catch Exception e
            (println "This recipe has already been chosen.")))
        (main-menu username))
      (= option "2")
      (do
        (choose-by-popularity username)
        (main-menu username))
      (= option "3")
      (do
        (doseq [rec (u/get-favs-by-username username @d/registered-users)]
          (println rec))
        (main-menu username))

      (= option "4")
      (do
        (remove-fav username)
        (main-menu username))
      (= option "5")
      (do
        (content/by-dif username)
        (main-menu username))

      (= option "6")
      (do
        (println (generate-report (u/get-user-by-username username)))
        (main-menu username))
      (= option "7")
      (do
        (users/by-users-recipe username)
        (main-menu username))
      (= option "8")
      (do
        (users/print-recs username)
        (main-menu username))
      (= option "9")
      (do
        (content/by-content username)
        (main-menu username))
      (= option "10") (logout)
      (= option "11")
      (do
        (println "Goodbye!"))
      :else (do
              (println "Invalid option. Please try again.")
              (main-menu username)))))

(defn login []
  (println "Username:")
  (let [username (read-line)]
    (println "Password:")
    (let [password (read-line)]
      (if (some #(= username (:username %)) @logged-in-users)
        (throw (ex-info "User is already logged in. Please logout first." {:username username}))
        (let [u (some #(when (and (= username (:username %))
                                  (= (hash-password password) (:password %))) %)
                      @d/registered-users)]
          (if u
            (do
              (println "Welcome, " username)
              (swap! logged-in-users conj {:username username})
              (main-menu username))
            (println "Error. Try again.")))))))

(defn -main []
  (println "--------------------------------------------")
  (println "\nStart:")
  (println "0. Register")
  (println "1. Login")
  (println "2. Exit")
  (println "Please select an option:")

  (let [option (read-line)]
    (cond
      (= option "0")
      (do
        (register))
      (= option "1")
      (do
        (login))
      (= option "2")
      (do
        (println "Goodbye!"))
      :else (do
              (println "Invalid option. Please try again.")
              (-main)))))

(-main)
