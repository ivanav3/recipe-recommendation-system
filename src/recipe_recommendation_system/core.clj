(ns recipe-recommendation-system.core
  (:gen-class)
  (:require [clojure.string :as str])
  (:import (java.security MessageDigest)))

(def filename "first-cleaned5.csv")

(defn parse
  "Convert a CSV string into rows of columns, removing unwanted characters."
  [string]

  (let [cleaned-string (str/replace string #"\r" "")] ;; Removing \r  because it isn't necessary even though dataset contains it
    (map (fn [row]
           (let [trimmed-row (str/trim row)] ;; Removing all unnecessary space characters
             (when (not (str/blank? trimmed-row))
               (str/split trimmed-row #";"))))
         (str/split cleaned-string #"\n"))))

(def initial-dataset (agent (rest (parse (slurp filename)))))

(def keys (agent [:title :total-time :serving-size :ingr :instructions :difficulty]))

(defn vectors-to-maps [vectors]
  (map #(zipmap @keys %) vectors))

(send initial-dataset vectors-to-maps)

(defn clean-ingr [recipes]
  (map #(update % :ingr (fn [ingr] (str/split ingr #",\s*"))) recipes))

(send initial-dataset clean-ingr)

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


(register)


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

(defn update-favs [users username chosen-recipe]
  (map #(if (= username (:username %))
          (update % :favs conj chosen-recipe)
          %)
       users))

(defn add-to-favs [favs chosen-recipe]
  (if (not-any? #(= % chosen-recipe) favs)
    (conj favs chosen-recipe)
    (println "Already marked as fav.")))

(defn update-favs [users username chosen-recipe]
  (map #(if (= username (:username %))
          (update % :favs add-to-favs chosen-recipe)
          %)
       users))


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
            (send registered-users update-favs username chosen-recipe)
            (println "Error. Try again."))))
      (println "No recipes found."))))

(defn main-menu [username]
  (println "--------------------------------------------")
  (println "\nMain Menu:")
  (println "0. View all recipes")
  (println "1. Choose a recipe")
  (println "2. Logout")
  (println "Please select an option:")

  (let [option (read-line)]
    (cond
      (= option "0") @initial-dataset
      (= option "1") (choose-fav username)
      (= option "2") (logout)
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


(defn reccomend-by-difficulty [chosen]
  (let [diff (:difficulty chosen)
        same-diff (filter #(= (:difficulty %) diff) @initial-dataset)
        others (remove #(= (:title %) (:title chosen)) same-diff)]
    (take 3 (shuffle others))))

(reccomend-by-difficulty (first (filter #(= (:title %) "Easy Mojitos") @initial-dataset)))

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
        without-selected (remove #(= (:title %) (:title selected-recipe)) all-favs)  ; Ukloni selected-recipe
        shuffled (shuffle without-selected)
        top-3 (take 3 shuffled)]
    top-3))

(users-recommend (first (filter #(= (:title %) "Easy Mojitos") @initial-dataset)))
(login)

(logout)
