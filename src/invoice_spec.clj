(ns invoice-spec
  (:require
   [clojure.spec.alpha :as s]
   [clojure.test :refer :all]
   [invoice-item :as invoice]))

(defn not-blank? [value] (-> value clojure.string/blank? not))
(defn non-empty-string? [x] (and (string? x) (not-blank? x)))

(s/def :customer/name non-empty-string?)
(s/def :customer/email non-empty-string?)
(s/def :invoice/customer (s/keys :req [:customer/name
                                       :customer/email]))

(s/def :tax/rate double?)
(s/def :tax/category #{:iva})
(s/def ::tax (s/keys :req [:tax/category
                           :tax/rate]))
(s/def :invoice-item/taxes (s/coll-of ::tax :kind vector? :min-count 1))

(s/def :invoice-item/price double?)
(s/def :invoice-item/quantity double?)
(s/def :invoice-item/sku non-empty-string?)

(s/def ::invoice-item
  (s/keys :req [:invoice-item/price
                :invoice-item/quantity
                :invoice-item/sku
                :invoice-item/taxes]))

(s/def :invoice/issue-date inst?)
(s/def :invoice/items (s/coll-of ::invoice-item :kind vector? :min-count 1))

(s/def ::invoice
  (s/keys :req [:invoice/issue-date
                :invoice/customer
                :invoice/items]))




;(deftest validateOutput
;  (let [output (invoice/output)]))



;((deftest validateOutput
;  (let [output (invoice/output)]
;    (is (s/valid? ::invoice output)))))


;; Tests for discount-factor
(deftest test-discount-factor-zero-rate
  (is (= (invoice/discount-factor {}) 1.0)))

(deftest test-discount-factor-positive-rate
  (is (= (invoice/discount-factor {:discount-rate 10}) 1.0)))


(deftest test-discount-factor-negative-rate
  (is (= (invoice/discount-factor {:discount-rate -20}) 1.2)))

(deftest test-discount-factor-no-discount-rate
  (is (= (invoice/discount-factor {:some-other-key 5}) 1.0)))

(deftest test-discount-factor-max-rate
  (is (= (invoice/discount-factor {:discount-rate Double/MAX_VALUE}) 0.0)))

(deftest test-discount-factor-min-rate
  (is (= (invoice/discount-factor {:discount-rate Double/MIN_VALUE}) Double/POSITIVE_INFINITY)))

(deftest test-discount-factor-large-rate
  (is (= (invoice/discount-factor {:discount-rate 10000}) 0.0)))

(deftest test-discount-factor-small-rate
  (is (= (invoice/discount-factor {:discount-rate -10000}) 101.0)))

(deftest test-discount-factor-fractional-rate
  (is (= (invoice/discount-factor {:discount-rate 2.5}) 0.975)))



(deftest test-subtotal-no-discount
  (let [invoice-item {:precise-quantity 2.0
                      :precise-price 100.0}]
    (is (= (invoice/subtotal invoice-item) 200.0))))

(deftest test-subtotal-with-discount
  (let [invoice-item {:precise-quantity 3.0
                      :precise-price 50.0
                      :discount-rate 10}]
    (is (= (invoice/subtotal invoice-item) 135.0))))

(deftest test-subtotal-zero-quantity
  (let [invoice-item {:precise-quantity 0.0
                      :precise-price 50.0}]
    (is (= (invoice/subtotal invoice-item) 0.0))))

(deftest test-subtotal-zero-price
  (let [invoice-item {:precise-quantity 5.0
                      :precise-price 0.0}]
    (is (= (invoice/subtotal invoice-item) 0.0))))

(deftest test-subtotal-negative-quantity
  (let [invoice-item {:precise-quantity -2.0
                      :precise-price 100.0}]
    (is (= (invoice/subtotal invoice-item) -200.0))))









