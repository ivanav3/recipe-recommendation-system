(ns recipe-recommendation-system.core-test
  (:require [clojure.test :refer :all]
            [recipe-recommendation-system.core :refer :all]
            [midje.sweet :refer :all]
            [recipe-recommendation-system.core :as c]
            [recipe-recommendation-system.content :as content]
            [recipe-recommendation-system.users :as users]
            [clojure.test :as t]))

;;User wants to add recipe to favorites. That way new recipes can be recommended in multiple ways.
(facts "test-adding-to-favs" (choose-fav "ivana") :truthy)

;;User wants to get recipes that are recommended by difficulty. That way user can discover new recipes with the same difficulty as chosen recipe.
(facts "test-recommend-by-difficulty" (content/recommend-by-difficulty (first (filter #(= (:title %) "Easy Mojitos") @initial-dataset)) @initial-dataset) =not=> nil)

;;User wants to get report on recent activity. That way users can track their improvement. 
(defn contains-more [my-map & keys]
  (every? #(contains? my-map %) keys))
(facts "test-report"
       (contains-more (generate-report (first (filter #(= (:username %) "ivana") @registered-users)))
                      :username :num-favs :difficulty-levels :avg-difficulty :report-time) => true)

;;User wants to get recipes that are recommended by other users. 
;;That way users can connect and discover recipes that are recommended by users with similar taste.
(facts "test-recommend-by-favs" (users/users-recommend (first (filter #(= (:title %) "Easy Mojitos") @initial-dataset)) "ivana") =not=> nil)

;;User wants to find out which recipes are the most popular. That way users can discover current trends in recipes. 
(facts "test-recommend-by-popularity" (choose-by-popularity "ivana") =not=> nil)

;;User wants to find another user with similar taste in recipes. That way users can connect and find recipes liked by similar users.
(facts "test-jaccard-similarity"
       (users/most-similar-user @registered-users (get-user-by-username "ivana") users/jaccard-similarity) => ["ivana2" 0.333])


(facts "test-cosine-similarity"
       (users/most-similar-user @registered-users (get-user-by-username "ivana") users/cosine-similarity) => ["ivana2" 0.5])

;;User wants to find another users with similar taste in recipes. This time all similarities are included.
(facts "test-most-similar-users"

       (users/most-similar-users (get-user-by-username "ivana")) => [["ivana10" 0.333] ["ivana13" 0.0]])

(facts "test-memory-measure-function"
       (clj-memory-meter.core/measure initial-dataset) =not=> nil)


;;Functions that are needed for authentication.
(facts "hash-password-test"
       (hash-password "ivana") =not=> nil)

;;Functions that are used for cleaning datasets.
(facts "clean-datasets-test"
       (let [ds {"name" "Ivana" "age" 23}]
         (c/clean-from-db ds)) => {:name "Ivana" :age 23}
       "removing-namespace-test"
       (let [ds (ref [{:user/id 10, :user/username "ivana10"}])]
         (remove-ns-from-ref ds)
         @ds) => '({:id 10, :username "ivana10"})
       "attach-favs-test"
       (c/attach-favorites-to-user (c/get-user-by-username "ivana")) =not=> nil
       "join-favs-test"
       (c/join-favs @c/initial-dataset) =not=> nil
       "remove-user-id-from-favorites-test"
       (c/remove-user-id-from-favorites (c/get-user-by-username "ivana")) :truthy
       "clean-up-favs-test"
       (c/clean-up-favs @c/registered-users) =not=> empty)
