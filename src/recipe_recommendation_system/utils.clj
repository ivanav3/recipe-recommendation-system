(ns recipe-recommendation-system.utils
  (:require [clojure.string :as str]
            [recipe-recommendation-system.data :as d]))


(defn get-user-by-username [username]
  (first (filter #(= (:username %) username) @d/registered-users)))

(defn find-by-title [title dataset]
  (let [lower-title (str/lower-case title)]
    (filter (fn [item]
              (let [title (:title item)]
                (str/includes? (str/lower-case title) lower-title))) dataset)))

(defn contains-more [my-map & keys]
  (every? #(contains? my-map %) keys))

(defn get-favs-by-username [username dataset]
  (some #(if (= (:username %) username) (:favs %)) dataset))