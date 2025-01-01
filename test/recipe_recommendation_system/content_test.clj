(ns recipe-recommendation-system.content-test
  (:require
   [recipe-recommendation-system.data :as d]
   [recipe-recommendation-system.content :as content]
   [recipe-recommendation-system.utils :as utils]
   [midje.sweet :refer :all]
   [criterium.core :as crit]))

(facts
 "recommend-by-keywords-test"
 (content/extract-keywords "some words that are extracted") => #{"are" "words" "that" "some" "extracted"}
 "content-similarity-test"
 (content/content-similarity '({:instructions
                                "Testing if this works."})
                             {:instructions
                              "If this works, the result should be 2."}) => 2)

;;User wants to get recipes that are recommended by difficulty. That way user can discover new recipes with the same difficulty as chosen recipe.
(facts
 "recommend-by-difficulty-test"
 (content/recommend-by-difficulty (first (filter #(= (:title %) "Easy Mojitos") @d/initial-dataset)) @d/initial-dataset) =not=> nil)

(facts
 "recommend-by-content"
 (content/recommend-by-content @d/initial-dataset
                               (utils/find-by-title @d/initial-dataset "Easy Mojitos"))
 =not=> nil)

(facts
 "by-dif-test"
 (content/by-dif "ivana") =not=> empty
 "recommend-by-content-test"
 (content/by-content "ivana") =not=> empty)


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
(crit/with-progress-reporting
  (crit/quick-bench
   (let [diff
         (:difficulty (first (utils/find-by-title "Easy Mojitos" @d/initial-dataset)))]
     (content/recommend-by-difficulty (first (filter #(= (:title %) "Easy Mojitos") @d/initial-dataset)) @d/initial-dataset))))

;; Slowest - 3.63 ms
(crit/with-progress-reporting
  (crit/quick-bench
   (content/recommend-by-content @d/initial-dataset (utils/find-by-title "Easy Mojitos" @d/initial-dataset))))