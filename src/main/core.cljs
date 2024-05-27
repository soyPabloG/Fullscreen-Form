(ns core
  (:require
   ["react-dom/client" :refer [createRoot]]
   [goog.dom :as gdom]
   [reagent.core :as r]))


(defn input
  [{:keys [key type label placeholder data-info error]}]
  (fn [{:keys [key type label placeholder data-info error]}]
    [:<>
     [:label {:for key
              :data-info data-info}
      label]
     [:input (merge
              {:key         key
               :type        type
               :placeholder placeholder}
              (when error
                {:error :true}))]]))

(defn form
  []
  (fn []
    [:form {:class "fs-form"}
     [:div {:class "fs-fields"}
      [input {:key         :name
              :type        :text
              :label       "What's your name?"
              :placeholder "Dean Moriarty"
              :data-info   "Relax, you can trust us..."}]]
     [:div {:class "fs-controls"}
      [:button {:type :reset}
       "Cancel"]
      [:button {:type     :button
                :disabled true}
       "Continue"]]]))


(defn app []
  [form])

(defonce root (createRoot (gdom/getElement "app")))

(defn init []
  (.render root (r/as-element [app])))

(defn ^:dev/after-load re-render
  []
  ;; The `:dev/after-load` metadata causes this function to be called
  ;; after shadow-cljs hot-reloads code.
  ;; This function is called implicitly by its annotation.
  (init))
