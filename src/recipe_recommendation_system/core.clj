(ns recipe-recommendation-system.core
  (:gen-class)
  (:require [clojure.string :as str]
            [next.jdbc :as jdbc]
            [clj-memory-meter.core :as mm]
            [clj-java-decompiler.core :refer [decompile]]
            [clojure.set :as set]
            [recipe-recommendation-system.content :as content]
            [recipe-recommendation-system.users :as users])
  (:import (java.security MessageDigest)))


(def db-spec {:dbtype "mysql"
              :dbname "recipe-rs"
              :host "localhost"
              :port 3306
              :user "root"
              :password ""})

(def initial-dataset
  (ref (or (seq (jdbc/execute! db-spec ["SELECT * FROM recipe"])) [])
       :validator
       (comp not nil?)))

(defn hash-password [password]
  (let [md (MessageDigest/getInstance "SHA-256")
        hashed-bytes (.digest md (.getBytes password))]
    (apply str (map #(format "%02x" %) hashed-bytes))))


(defn clean-from-db [data]
  (into {} (map (fn [[k v]] [(keyword (name k)) v]) data)))

(defn remove-ns-from-ref [r]
  (dosync
   (alter r
          (fn [re]
            (map clean-from-db re)))))

(def favorites-base (ref (jdbc/execute! db-spec ["SELECT r.*, f.user_id FROM recipe r
JOIN favorites f ON r.id=f.recipe_id "])))

(remove-ns-from-ref favorites-base)

(def registered-users
  (ref (or (seq (jdbc/execute! db-spec ["SELECT * FROM user"])) [])))

(defn attach-favorites-to-user [user]
  (let [user-id (:id user)]
    (assoc user :favs
           (filter #(= user-id (:user_id %)) @favorites-base))))

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


(remove-ns-from-ref initial-dataset)
(remove-ns-from-ref registered-users)
(join-favs registered-users)
(clean-up-favs registered-users)

(defn register []
  (println "Username:")
  (let [username (read-line)]
    (if (some #(= username (:username %)) @registered-users)
      (do
        (println "This username is taken, try again.")
        (register))
      (dosync
       (println "Password:")
       (let [password (read-line)
             hashed-password (hash-password password)
             _ (jdbc/execute! db-spec
                              ["INSERT INTO user (username, password) VALUES (?, ?)"
                               username hashed-password])]


         (alter registered-users
                (fn [users]
                  (conj users {:id (:user/id  (first (jdbc/execute! db-spec
                                                                    ["SELECT id FROM user WHERE username = ?"
                                                                     username])))
                               :username username
                               :password hashed-password
                               :favs []})))

         (println "Registered!" username))))))

(def logged-in-users (atom []))

(defn is-user-logged-in? [username]
  (some #(= username (:username %)) @logged-in-users))

(defn remove-user [users username]
  (filter #(not= username (:username %)) users))

(defn logout []
  (println "Enter username to logout:")
  (let [username (read-line)]
    (if (is-user-logged-in? username)
      (do
        (swap! logged-in-users remove-user username)
        (println username "has been logged out."))
      (println "No such user is logged in."))))


(defn find-by-title [title dataset]
  (let [lower-title (str/lower-case title)]
    (filter (fn [item]
              (let [title (:title item)]
                (str/includes? (str/lower-case title) lower-title))) dataset)))

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
        results (find-by-title title @initial-dataset)]
    (if (seq results)
      (do
        (println "Found the following recipes:")
        (doseq [result results]
          (println (:title result)))

        (println "Please enter the full title of the recipe you're interested in:")
        (let [chosen-title (str/lower-case (read-line))
              chosen-recipe (some #(if (= (str/lower-case (:title %)) chosen-title) %) results)]
          (if chosen-recipe
            (let [user (some #(if (= (:username %) username) %) @registered-users)
                  user-id (:id user)
                  recipe-id (:id chosen-recipe)]

              (if (and user-id recipe-id)
                (do
                  (dosync
                   (jdbc/execute! db-spec
                                  ["INSERT INTO favorites (`user_id`, `recipe_id`) VALUES (?, ?)" user-id recipe-id])
                   (jdbc/execute! db-spec
                                  ["UPDATE recipe SET fav = fav + 1 WHERE id = ?" recipe-id])
                   (println "Recipe added to favorites!")
                   (alter registered-users update-favs username chosen-recipe)
                   (alter initial-dataset update-rec chosen-recipe)))
                (println "Error: Could not find user or recipe ID.")))

            (println "Error. Recipe not found or invalid input."))))
      (println "No recipes found."))))

(defn choose-by-popularity [username]
  (println (take 3
                 (sort-by :fav > @initial-dataset)))
  (choose-fav username))


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


(defn get-user-by-username [username]
  (first (filter #(= (:username %) username) @registered-users)))

(defn extract-favs [user]
  (set (map :title (:favs user))))

(defn jaccard-similarity [user1 user2]
  (let [favs1 (extract-favs user1)
        favs2 (extract-favs user2)]
    (if (or (empty? favs1) (empty? favs2))
      0.0
      (let
       [intersection (count (clojure.set/intersection favs1 favs2))
        union (count (clojure.set/union favs1 favs2))]
        (if (zero? union)
          0.0
          (let [similarity (float (/ intersection union))]
            (/ (Math/round (* similarity 1000)) 1000.0)))))))

(defn most-similar-user [users-to-compare target-user similarity-fn]
  (let [all-users (remove #(= (:username %) (:username target-user)) users-to-compare)
        similarities (map #(vector (:username %) (similarity-fn target-user %)) all-users)
        most-similar (apply max-key second similarities)]
    most-similar))


(defn cosine-similarity [user1 user2]
  (let [favs1 (extract-favs user1)
        favs2 (extract-favs user2)]
    (if (or (empty? favs1) (empty? favs2))
      0.0
      (let [all-recipes (clojure.set/union favs1 favs2)
            vector1 (map #(if (contains? favs1 %) 1 0) all-recipes)
            vector2 (map #(if (contains? favs2 %) 1 0) all-recipes)]
        (let [dot-product (reduce + (map * vector1 vector2))
              norm1 (Math/sqrt (reduce + (map #(* % %) vector1)))
              norm2 (Math/sqrt (reduce + (map #(* % %) vector2)))]
          (if (and (zero? norm1) (zero? norm2))
            1.0
            (Float/parseFloat (format "%.3f" (/ dot-product (* norm1 norm2))))))))))


(defn get-favs-by-username [username]
  (some #(if (= (:username %) username) (:favs %)) @registered-users))

(defn get-user-favs [username]
  {:username username
   :favs (get-favs-by-username username)})

(defn most-similar-users [target-user]
  (let [all-users (remove #(= (:username %) (:username target-user)) @registered-users)

        most-similar-jaccard  (most-similar-user all-users target-user jaccard-similarity)

        remaining-users (remove #(= (:username %) (first most-similar-jaccard)) all-users)
        most-similar-cosine (if (empty? remaining-users)
                              nil
                              (most-similar-user remaining-users target-user cosine-similarity))]

    (if most-similar-cosine
      [most-similar-jaccard most-similar-cosine]
      [most-similar-jaccard])))

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
        results (find-by-title title (get-favs-by-username username))]
    (if (seq results)
      (do
        (println "Found the following recipes:")
        (doseq [result results]
          (println (:title result)))

        (println "Please enter the full title of the recipe you're interested in:")
        (let [chosen-title (str/lower-case (read-line))
              chosen-recipe (some #(if (= (str/lower-case (:title %)) chosen-title) %) results)]
          (if chosen-recipe
            (let [user (some #(if (= (:username %) username) %) @registered-users)
                  user-id (:id user)
                  recipe-id (:id chosen-recipe)]

              (if (and user-id recipe-id)
                (do
                  (dosync
                   (jdbc/execute! db-spec
                                  ["DELETE FROM favorites WHERE `user_id` = ? AND `recipe_id` = ?" user-id recipe-id])
                   (jdbc/execute! db-spec
                                  ["UPDATE recipe SET fav = fav - 1 WHERE id = ?" recipe-id])
                   (println "Recipe deleted from favorites!")
                   (alter registered-users update-favs-when-removed username chosen-recipe)
                   (alter initial-dataset dec-recipe chosen-recipe)))
                (println "Error: Could not find user or recipe ID.")))

            (println "Error. Recipe not found or invalid input."))))
      (println "No recipes found."))))


(defn print-recs [username]
  (doseq [s (map first (most-similar-users (get-user-by-username username)))]
    (println (get-user-favs s))))


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
  (println "Please select an option:")

  (let [option (read-line)]
    (cond
      (= option "0")
      (do
        (println @initial-dataset)
        (main-menu username))
      (= option "1")
      (do
        (choose-fav username)
        (main-menu username))
      (= option "2")
      (do
        (choose-by-popularity username)
        (main-menu username))
      (= option "3")
      (do
        (println (get-favs-by-username username))
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
        (generate-report (get-user-by-username username))
        (main-menu username))
      (= option "7")
      (do
        (users/by-users-recipe username)
        (main-menu username))
      (= option "8")
      (do
        (print-recs username)
        (main-menu username))
      (= option "9")
      (do
        (content/by-content username)
        (main-menu username))
      (= option "10") (logout)
      :else (do
              (println "Invalid option. Please try again.")
              (main-menu username)))))

(defn login []
  (println "Username:")
  (let [username (read-line)]
    (println "Password:")
    (let [password (read-line)]
      (if (some #(= username (:username %)) @logged-in-users)
        (println "User is already logged in. Please logout first.")
        (let [u (some #(when (and (= username (:username %))
                                  (= (hash-password password) (:password %))) %)
                      @registered-users)]
          (if u
            (do
              (println "Welcome, " username)
              (swap! logged-in-users conj {:username username})
              (main-menu username))
            (println "Error. Try again.")))))))