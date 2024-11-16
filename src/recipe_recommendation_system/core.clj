(ns recipe-recommendation-system.core
  (:gen-class)
  (:require [clojure.string :as str])
  (:import (java.security MessageDigest)))

(def filename "first-cleaned5.csv")

(slurp filename)

(defn parse
  "Convert a CSV string into rows of columns, removing unwanted characters."
  [string]

  (let [cleaned-string (str/replace string #"\r" "")] ;; Removing \r  because it isn't necessary even though dataset contains it
    (map (fn [row]
           (let [trimmed-row (str/trim row)] ;; Removing all unnecessary space characters
             (when (not (str/blank? trimmed-row))
               (str/split trimmed-row #";"))))
         (str/split cleaned-string #"\n"))))
(rest (parse (slurp filename)))

(def initial-dataset (agent (rest (parse (slurp filename)))))

(def keys (agent [:title :total-time :serving-size :ingr :instructions]))

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
                (fn [korisnici]
                  (conj korisnici {:username username :password (hash-password password)})))
          (await registered-users)
          (println "Registered!" username))))))


(register)


(println "Registered users:")
(doseq [u @registered-users]
  (println "Username:" (:username u) ", Password:" (:password u)))


(defn login []
  (println "Username:")
  (let [username (read-line)]
    (println "Password:")
    (let [password (read-line)]
      (let [korisnik (some #(when (and (= username (:username %))
                                       (= (hash-password password) (:password %))) %)
                           @registered-users)]
        (if korisnik
          (println "Welcome, " username)
          (println "Error. Try again."))))))

(login)