(ns recipe-recommendation-system.core
  (:gen-class)
  (:require [clojure.string :as str])
  (:import (java.security MessageDigest)))

(def filename "first-cleaned.csv")

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

(def initial-dataset (rest (parse (slurp filename))))

(def keys [:title :total-time :serving-size :ingr :instructions])

(defn vectors-to-maps [vectors]
  (map #(zipmap keys %) vectors))

(def map-dataset (vectors-to-maps initial-dataset))
(def initial-dataset (doall map-dataset))

(defn hash-password [password]
  (let [md (MessageDigest/getInstance "SHA-256") ;; Object of class MessageDigest configured for using SHA-256 algorithm
        hashed-bytes (.digest md (.getBytes password))] ;;getBytes as a method implemented in String class is used for creating this into bytes array
        ;;parameters for .digest are algorithm and data
    (apply str (map #(format "%02x" %) hashed-bytes))))
    ;;this is only for converting into hex format

(defn create-user [username password]
  (let [hashed-password (hash-password password)]
    {:username username
     :password hashed-password}))

(let [username "ivana"
      password "sifra12345"
      user (create-user username password)]
  (println "Created user:" user))