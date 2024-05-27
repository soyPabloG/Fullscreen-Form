(ns core
  (:require
   ["react-dom/client" :refer [createRoot]]
   [goog.dom :as gdom]
   [reagent.core :as r]))

(defn app []
  [:p "Hello World!"])

(defonce root (createRoot (gdom/getElement "app")))

(defn init []
  (.render root (r/as-element [app])))

(defn ^:dev/after-load re-render
  []
  ;; The `:dev/after-load` metadata causes this function to be called
  ;; after shadow-cljs hot-reloads code.
  ;; This function is called implicitly by its annotation.
  (init))
