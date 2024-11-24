(ns recipe-recommendation-system.core
  (:gen-class)
  (:require [clojure.string :as str])
  (:import (java.security MessageDigest)))

(defn parse
  "Convert a CSV string into rows of columns, removing unwanted characters."
  [string]

  (let [cleaned-string (str/replace string #"\r" "")] ;; Removing \r  because it isn't necessary even though dataset contains it
    (map (fn [row]
           (let [trimmed-row (str/trim row)] ;; Removing all unnecessary space characters
             (when (not (str/blank? trimmed-row))
               (str/split trimmed-row #";"))))
         (str/split cleaned-string #"\n"))))

(def initial-dataset (agent (rest (parse (slurp "first-cleaned5.csv")))))

(def keys (agent [:title :total-time :serving-size :ingr :instructions :difficulty :fav]))

(defn vectors-to-maps [vectors]
  (map #(zipmap @keys %) vectors))

(send initial-dataset vectors-to-maps)

(defn reset-fav [recipes]
  (map #(assoc % :fav 0) recipes))

(defn clean-ingr [recipes]
  (map #(update % :ingr (fn [ingr] (str/split ingr #",\s*"))) recipes))

(send initial-dataset clean-ingr)
(send initial-dataset reset-fav)

(defn hash-password [password]
  (let [md (MessageDigest/getInstance "SHA-256") ;; Object of class MessageDigest configured for using SHA-256 algorithm
        hashed-bytes (.digest md (.getBytes password))] ;;getBytes as a method implemented in String class is used for changing this into bytes array
        ;;parameters for .digest are algorithm and data
    (apply str (map #(format "%02x" %) hashed-bytes))))
    ;;this is only for converting into hex format


(def registered-users (agent []))
(defn register []
  (println "Username:")
  (let [username (read-line)]
    (if (some #(= username (:username %)) @registered-users)
      (do
        (println "This username is taken, try again.")
        (register))
      (do
        (println "Password:")
        (let [password (read-line)]
          (send registered-users
                (fn [users]
                  (conj users {:username username :password (hash-password password) :favs []})))
          (await registered-users)
          (println "Registered!" username))))))


(println "Registered users:")
(doseq [u @registered-users]
  (println "Username:" (:username u) ", Password:" (:password u)))


(def logged-in-users (agent []))

(defn is-user-logged-in? [username]
  (some #(= username (:username %)) @logged-in-users))

(defn remove-user [users username]
  (filter #(not= username (:username %)) users))

(defn logout []
  (println "Enter username to logout:")
  (let [username (read-line)]
    (if (is-user-logged-in? username)
      (do
        (send logged-in-users remove-user username)
        (println username "has been logged out."))
      (println "No such user is logged in."))))


(defn find-by-title [title]
  (let [lower-title (str/lower-case title)]
    (filter (fn [item]
              (let [title (:title item)]
                (str/includes? (str/lower-case title) lower-title))) @initial-dataset)))

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
        results (find-by-title title)]
    (if (seq results)
      (do
        (println "Found the following recipes:")
        (doseq [result results]
          (println (:title result)))

        (println "Please enter the full title of the recipe you're interested in:")
        (let [chosen-title (str/lower-case (read-line))
              chosen-recipe (some #(if (= (str/lower-case (:title %)) chosen-title) %) results)]
          (if chosen-recipe
            (do
              (send registered-users update-favs username chosen-recipe)
              (send initial-dataset update-rec chosen-recipe))
            (println "Error. Try again."))))
      (println "No recipes found."))))


(defn choose-by-popularity [username]
  (println (take 3
                 (sort-by :fav > @initial-dataset)))
  (choose-fav username))
(first (filter #(= (:username %) "ivana") @registered-users))

(defn main-menu [username]
  (println "--------------------------------------------")
  (println "\nMain Menu:")
  (println "0. View all recipes")
  (println "1. Choose a recipe")
  (println "2. View popular recipes")
  (println "3. Logout")
  (println "Please select an option:")

  (let [option (read-line)]
    (cond
      (= option "0") @initial-dataset
      (= option "1") (choose-fav username)
      (= option "2") (choose-by-popularity username)
      (= option "3") (logout)
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
              (send logged-in-users conj {:username username})
              (main-menu username))
            (println "Error. Try again.")))))))


(defn recommend-by-difficulty [chosen]
  (let [diff (:difficulty chosen)
        same-diff (filter #(= (:difficulty %) diff) @initial-dataset)
        others (remove #(= (:title %) (:title chosen)) same-diff)]
    (take 3 (shuffle others))))

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


(generate-report (first (filter #(= (:username %) "ivana") @registered-users)))

(defn users-recommend [selected-recipe]
  (let [users-with-selected (filter
                             (fn [user]
                               (some #(= (:title %) (:title selected-recipe)) (:favs user)))
                             @registered-users)
        all-favs (mapcat :favs users-with-selected)
        without-selected (remove #(= (:title %) (:title selected-recipe)) all-favs)
        shuffled (shuffle without-selected)
        top-3 (take 3 shuffled)]
    top-3))

(users-recommend (first (filter #(= (:title %) "Easy Mojitos") @initial-dataset)))

(defn group-favs [username recipe group-name]
  (let [user (first (filter #(= (:username %) username) @registered-users))]
    (let [groups (:groups user)
          group (get groups group-name [])]
      (send registered-users #(mapv (fn [u]
                                      (if (= (:username u) username)
                                        (update u :groups assoc group-name (conj group recipe))
                                        u)) %)))))



(register)
(login)
(logout)

@registered-users

(defn get-user-by-username [username]
  (first (filter #(= (:username %) username) @registered-users)))

(defn extract-favs [user]
  (set (map :title (:favs user))))

(defn jaccard-similarity [user1 user2]
  (let [favs1 (extract-favs user1)
        favs2 (extract-favs user2)
        intersection (count (clojure.set/intersection favs1 favs2))
        union (count (clojure.set/union favs1 favs2))]
    (if (zero? union)
      0.0
      (float (/ intersection union)))))

(defn most-similar-user [target-user]
  (let [all-users (remove #(= (:username %) (:username target-user)) @registered-users)  ;; Izuzmi target-user iz liste
        similarities (map #(vector (:username %) (jaccard-similarity target-user %)) all-users)  ;; Izračunaj sličnosti za sve korisnike
        most-similar (apply max-key second similarities)]
    most-similar))

(most-similar-user (get-user-by-username "ivana"))