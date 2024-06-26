(ns core
  (:require
   [field :refer [field]]
   [goog.dom :as gdom]
   [reagent.core :as r]
   [clojure.string :as str]
   ["react-dom/client" :refer [createRoot]]))


(def form-data
  {:name                 {:type        :text
                          :label       "What's your name?"
                          :placeholder "Jack Skellington"}
   :email                {:type        :email
                          :label       "What's your email address?"
                          :placeholder "spookyname@example.com"
                          :data-info   "To send you spooky updates and party details..."}
   :address              {:type        :text
                          :label       "What's your home address?"
                          :placeholder "1313 Mockingbird Lane, Spooksville, HA 1234"
                          :data-info   "Relax, is just for sending out trick-or-treat goodies 🍬"}
   :horror-story         {:type        :textarea
                          :label       "What is the eeriest thing that has ever happened to you on Halloween night?"
                          :placeholder "Saw a ghost, mysterious knock on the door, black cat followed me home, or something really scary..."}
   :arriving             {:type    :icon-radio
                          :label   "How are you arriving?"
                          :options [{:label     "Regular Human Vehicle"
                                     :image-url "../images/car.png"
                                     :value     :vehicle}
                                    {:label     "Rolling down the street"
                                     :image-url "../images/robot.png"
                                     :value     :walking}
                                    {:label     "Witches' Coven Shuttle"
                                     :image-url "../images/truck.png"
                                     :value     :bus}
                                    {:label     "Invisible Rocket"
                                     :image-url "../images/rocket.png"
                                     :value     :other}]}
   :dietary-restrictions {:type    :radio
                          :label   "Do you have any dietary restrictions?"
                          :options [{:label "None" :value :none}
                                    {:label "Vegetarian" :value :vegetarian}
                                    {:label "Vegan" :value :vegan}
                                    {:label "Gluten-free" :value :gluten-free}
                                    {:label "Nut allergy" :value :nut-allergy}]}
   :people               {:type        :number
                          :max         5
                          :label       "How many any ghoulish friends or family members are you bringing?"
                          :placeholder "Max. 5"}
   :emergency-contact    {:type        :tel
                          :label       "What is the phone number of your emergency contact?"
                          :placeholder "(123) 987-6543"
                          :data-info   "Just in case you get too scared!"}})

(def form-fields
  [:name :email :address :horror-story :arriving :dietary-restrictions :people :emergency-contact])


(defn next-field
  [form-fields current-field]
  (->> form-fields
       (drop-while #(not= % current-field))
       rest
       first))


(defn move-forward!
  [state]
  (let [current-field (get-in @state [:ui :current-field])
        current-value (get-in @state [:data current-field :value])]
    (if (empty? (str current-value))
      (swap! state assoc-in [:data current-field :error] true)
      (let [new-state-fn (fn [current-state]
                           (-> current-state
                               (update-in [:ui :filled-fields] inc)
                               (assoc-in [:ui :current-field] (next-field form-fields current-field))))]
        (swap! state new-state-fn)))))


(defn reset-form!
  [state]
  (reset! state {:ui {:current-field :name
                      :filled-fields 0}}))


(defn keydown-handler-fn
  [callback-fn]
  (fn [e]
    (when-not (-> e .-target .-tagName str/lower-case (= "textarea"))
      (let [key-code (or (.-keyCode e) (.-which e))]
        (when (= key-code 13)
          (.preventDefault e)
          (callback-fn))))))


(defn progress-bar
  [total-fields filled-fields]
  [:div {:class "fs-progress"
         :style {:width (str (/ (* 100 filled-fields) total-fields) "%")}}])


(defn form
  []
  (r/with-let [state            (r/atom {:ui {:current-field :name
                                              :filled-fields 0}})
               total-fields     (count form-fields)
               current-field    (r/cursor state [:ui :current-field])
               filled-fields    (r/cursor state [:ui :filled-fields])
               keydown-callback (keydown-handler-fn #(move-forward! state))
               _                (.addEventListener js/document "keydown" keydown-callback)]
    (let [input-data (assoc
                      (get form-data @current-field)
                      :name @current-field
                      :cursor (r/cursor state [:data @current-field]))
          next-field (next-field form-fields @current-field)]
      [:<>
       [progress-bar total-fields @filled-fields]
       [:form {:class "fs-form"}
        [:div {:class "fs-fields"}
         [field input-data]]
        [:div {:class "fs-controls"}
         [:button {:type     :reset
                   :on-click (fn [e]
                               (.preventDefault e)
                               (reset-form! state))}
          "Cancel"]
         [:button {:type     :button
                   :disabled (nil? next-field)
                   :on-click (fn [_]
                               (move-forward! state))}
          "Continue"]]]])
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
