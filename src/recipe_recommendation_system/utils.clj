(ns recipe-recommendation-system.utils
  (:require [clojure.string :as str]))


(defn find-by-title [title dataset]
  (let [lower-title (str/lower-case title)]
    (filter (fn [item]
              (let [title (:title item)]
                (str/includes? (str/lower-case title) lower-title))) dataset)))


(defn get-favs-by-username [username dataset]
  (some #(if (= (:username %) username) (:favs %)) dataset))
