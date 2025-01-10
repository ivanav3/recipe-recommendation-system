(ns recipe-recommendation-system.content-test
  (:require
   [recipe-recommendation-system.core :as c]
   [recipe-recommendation-system.data :as d]
   [recipe-recommendation-system.content :as content]
   [recipe-recommendation-system.utils :as utils]
   [midje.sweet :refer :all]
   [criterium.core :as crit]
   [next.jdbc :as jdbc]))



(facts
 "recommend-by-keywords-test"
 (content/extract-keywords "some words that are extracted") => #{"are" "words" "that" "some" "extracted"}
 "content-similarity-test"
 (content/content-similarity '({:instructions
                                "Testing if this works."})
                             {:instructions
                              "If this works, the result should be 2."}) => 2)
(facts

 "recommend-by-difficulty-test"
 (let [recipes (ref (or (seq (jdbc/execute! d/db-spec ["SELECT * FROM recipe"])) [])
                    :validator
                    (comp not nil?))]
   (c/remove-ns-from-ref recipes)
   (c/clean-strings recipes)
   (content/recommend-by-difficulty (first (filter #(= (:title %) "Easy Mojitos") @recipes)) @recipes) =not=> nil))

(facts
 "recommend-by-content"
 (let [recipes (ref (or (seq (jdbc/execute! d/db-spec ["SELECT * FROM recipe"])) [])
                    :validator
                    (comp not nil?))]
   (c/remove-ns-from-ref recipes)
   (c/clean-strings recipes)
   (content/recommend-by-content @recipes
                                 (utils/find-by-title @recipes "Easy Mojitos")))
 =not=> nil)

(facts
 "by-dif-test"
 (let [users (ref (or (seq (jdbc/execute! d/db-spec ["SELECT * FROM user"])) []))
       recipes (ref (or (seq (jdbc/execute! d/db-spec ["SELECT * FROM recipe"])) [])
                    :validator
                    (comp not nil?))
       favorites-base (ref (jdbc/execute! d/db-spec ["SELECT r.*, f.user_id FROM recipe r
                          JOIN favorites f ON r.id=f.recipe_id "]))]
   (c/remove-ns-from-ref users)
   (c/remove-ns-from-ref recipes)
   (c/remove-ns-from-ref favorites-base)
   (c/join-favs users favorites-base)
   (c/clean-up-favs users)
   (c/clean-strings recipes)
   (content/by-dif "ivana" users recipes)) =not=> empty)

(facts
 "recommend-by-content-test"
 (let [users (ref (or (seq (jdbc/execute! d/db-spec ["SELECT * FROM user"])) []))
       recipes (ref (or (seq (jdbc/execute! d/db-spec ["SELECT * FROM recipe"])) [])
                    :validator
                    (comp not nil?))
       favorites-base (ref (jdbc/execute! d/db-spec ["SELECT r.*, f.user_id FROM recipe r
                           JOIN favorites f ON r.id=f.recipe_id "]))]
   (c/remove-ns-from-ref users)
   (c/remove-ns-from-ref recipes)
   (c/remove-ns-from-ref favorites-base)
   (c/join-favs users favorites-base)
   (c/clean-up-favs users)
   (c/clean-strings recipes)
   (content/by-content "ivana" users recipes)) =not=> empty)

;;Performance of functions.

;;Fastest - 1.36 µs
(crit/with-progress-reporting
  (crit/quick-bench (content/extract-keywords "some words that are extracted")))

;; 5.17 µs
(crit/with-progress-reporting
  (crit/quick-bench (content/content-similarity {:instructions
                                                 "Testing if this works."}
                                                {:instructions
                                                 "If this works, the result should be 2."})))


;; 12.78 µs
(let  [recipes (ref (or (seq (jdbc/execute! d/db-spec ["SELECT * FROM recipe"])) [])
                    :validator
                    (comp not nil?))]
  (c/remove-ns-from-ref recipes)
  (c/clean-strings recipes)
  (crit/with-progress-reporting
    (crit/quick-bench
     (let [diff
           (:difficulty (first (utils/find-by-title "Easy Mojitos" @recipes)))]
       (content/recommend-by-difficulty (first (filter #(= (:title %) "Easy Mojitos") @recipes)) @recipes)))))

;; Slowest - 3.63 ms
(let  [recipes (ref (or (seq (jdbc/execute! d/db-spec ["SELECT * FROM recipe"])) [])
                    :validator
                    (comp not nil?))]
  (c/remove-ns-from-ref recipes)
  (c/clean-strings recipes)
  (crit/with-progress-reporting
    (crit/quick-bench
     (content/recommend-by-content @recipes (utils/find-by-title "Easy Mojitos" @recipes)))))