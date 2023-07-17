(ns invoice-item
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]))

;primer punto
(def invoicetmp (read-string (slurp "invoice.edn")))
(defn validateInvoiceItems
  "function that receives an invoice as an argument and returns all items that at least have one item that has :iva 19% and at least one item has retention :retfuente 1%"
  [{:keys [:invoice/items]}]
  (->> items
       (group-by (fn [item]
                   (if (and (:taxable/taxes item) (:retentionable/retentions item)) :withBoth
                       (if (:taxable/taxes item)
                         :withTaxes
                         :withRetentions))))))
(def result (validateInvoiceItems invoicetmp))
(println result)


;Segundo punto
(defn read-invoice-data [file-path]
  (with-open [reader (clojure.java.io/reader file-path)]
    (json/read reader)))

(defn transform-invoice-data [invoice-data]
  (let [invoice (:invoice invoice-data)
        customer (:customer invoice)
        items (:items invoice)]
    {:invoice/issue-date (get-in invoice-data ["invoice" "issue_date"])
     :invoice/customer {:customer/name (get-in invoice-data ["invoice" "customer" "company_name"])
                        :customer/email (get-in invoice-data ["invoice" "customer" "email"])}
     :invoice/items (mapv (fn [item]
                            {:invoice-item/price (get item "price")
                             :invoice-item/quantity (get item "quantity")
                             :invoice-item/sku (get item "sku")
                             :taxable/taxes (mapv (fn [tax]
                                                    {:tax/category :iva
                                                     :tax/rate (get tax "tax_rate")})
                                                  (get item "taxes"))})
                          (get-in invoice-data ["invoice" "items"]))}))

(def json-file-path "invoice.json")
json-file-path
(def invoice-data (read-invoice-data json-file-path))
invoice-data
(def transformed-data (transform-invoice-data invoice-data))
transformed-data

;Tercer punto 
(defn discount-factor [{:invoice-item/keys [discount-rate]
                        :or                {discount-rate 0}}]
  (- 1 (/ discount-rate 100.0)))


(defn subtotal
  [{:invoice-item/keys [precise-quantity precise-price discount-rate]
    :as                item
    :or                {discount-rate 0}}]
  (* precise-price precise-quantity (discount-factor item)))
