(ns core
  (:require
   [clojure.string :as str]
   ["react-dom/client" :refer [createRoot]]
   [goog.dom :as gdom]
   [reagent.core :as r]))


(def form-data
  {:name              {:type        :text
                       :label       "What's your name?"
                       :placeholder "Jack Skellington"}
   :email             {:type        :email
                       :label       "What's your email address?"
                       :placeholder "spookyname@example.com"
                       :data-info   "To send you spooky updates and party details..."}
   :address           {:type        :text
                       :label       "What's your home address?"
                       :placeholder "1313 Mockingbird Lane, Spooksville, HA 1234"
                       :data-info   "Relax, is just for sending out trick-or-treat goodies 🍬"}
   :horror-story      {:type        :text
                       :label       "What is the eeriest thing that has ever happened to you on Halloween night?"
                       :placeholder "Saw a ghost, mysterious knock on the door, black cat followed me home, or something really scary..."}
   :people            {:type        :number
                       :max         5
                       :label       "How many any ghoulish friends or family members are you bringing?"
                       :placeholder "Max. 5"}
   :emergency-contact {:type        :tel
                       :label       "What is the phone number of your emergency contact?"
                       :placeholder "(123) 987-6543"
                       :data-info   "Just in case you get too scared!"}})

(def form-fields
  [:name :email :address :horror-story :people :emergency-contact])


(defn input
  [state {:keys [key type label placeholder data-info error]}]
  (fn [state {:keys [key type label placeholder data-info error]}]
    [:<>
     [:label {:for       key
              :data-info data-info}
      label]
     [:input (merge
              {:key         key
               :type        type
               :value       (get @state key)
               :on-change   (fn [e]
                              (swap! state dissoc :error)
                              (swap! state assoc key (-> e .-target .-value)))
               :placeholder placeholder
               :auto-focus  true
               :error       (when (get @state :error) "true")}
              (when error
                {:error :true}))]]));


(defn next-field
  [form-fields current-field]
  (->> form-fields
       (drop-while #(not= % current-field))
       rest
       first))


(defn move-forward!
  [state current-field]
  (let [current-value (get @state @current-field)]
    (if (empty? (str current-value))
      (swap! state assoc :error true)
      (swap! current-field #(next-field form-fields %)))))


(defn reset-form!
  [state current-field]
  (reset! current-field :name)
  (reset! state {}))


(defn keydown-handler-fn
  [callback-fn]
  (fn [e]
    (let [key-code (or (.-keyCode e) (.-which e))]
      (when (= key-code 13)
        (.preventDefault e)
        (callback-fn)))))


(defn form
  []
  (r/with-let [current-field    (r/atom :name)
               state            (r/atom {})
               keydown-callback (keydown-handler-fn #(move-forward! state current-field))
               _                (.addEventListener js/document "keydown" keydown-callback)]
    (let [input-data (assoc (get form-data @current-field) :key @current-field)
          next-field (next-field form-fields @current-field)]
      [:form {:class "fs-form"}
       [:div {:class "fs-fields"}
        [input state input-data]]
       [:div {:class "fs-controls"}
        [:button {:type     :reset
                  :on-click (fn [e]
                              (.preventDefault e)
                              (reset-form! state current-field))}
         "Cancel"]
        [:button {:type     :button
                  :disabled (nil? next-field)
                  :on-click (fn [_]
                              (move-forward! state current-field))}
         "Continue"]]])
    (finally
      (.removeEventListener js/document "keydown" keydown-callback))))


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
