(ns recipe-recommendation-system.data
  (:require [next.jdbc :as jdbc]))


(def db-spec {:dbtype "mysql"
              :dbname "recipe-rs"
              :host "localhost"
              :port 3306
              :user "root"
              :password ""})

;; (def initial-dataset
;;   (ref (or (seq (jdbc/execute! db-spec ["SELECT * FROM recipe"])) [])
;;        :validator
;;        (comp not nil?)))

;; (def favorites-base (ref (jdbc/execute! db-spec ["SELECT r.*, f.user_id FROM recipe r
;; JOIN favorites f ON r.id=f.recipe_id "])))

;; (def registered-users
;;   (ref (or (seq (jdbc/execute! db-spec ["SELECT * FROM user"])) [])))

