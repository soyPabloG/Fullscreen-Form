(ns core
  (:require
   [clojure.string :as str]
   ["react-dom/client" :refer [createRoot]]
   [goog.dom :as gdom]
   [reagent.core :as r]
   ["react-transition-group" :refer [CSSTransition SwitchTransition]]
   ["react" :as react]))


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


(defn deref-or-value
  "Takes a value or an atom
  If it's a value, returns it
  If it's an object that supports IDeref, returns the value inside it by derefing
  "
  [val-or-atom]
  (if (satisfies? IDeref val-or-atom)
    @val-or-atom
    val-or-atom))


(defn input
  [{:keys [field-name type placeholder value on-change error]}]
  ;; (println "input re-render")
  [:input (merge
           {:key         field-name
            :type        type
            :value       (deref-or-value value)
            :on-change   on-change
            :placeholder placeholder
            :auto-focus  true}
           (when (deref-or-value error)
             {:error :true}))])


(defn input-field
  [{:keys [field-name type label placeholder value on-change data-info error] :as props}]
  (let [label-ref (react/useRef)
        input-ref (react/useRef)]
    [:<>
     [:> SwitchTransition
      [:> CSSTransition
       {:key              field-name
        :class-names      :fs-anim-upper
        :node-ref         label-ref
        :add-end-listener (fn [done]
                            ;; (println "label-end-listenter" done)
                            (-> label-ref .-current (.addEventListener "transitionend" done false)))}
       [:label {:for       field-name
                :data-info data-info
                :ref       (fn [el]
                             ;; (println "label-ref" el)
                             (set! (.-current label-ref) el))}
        label]]]
     [:> SwitchTransition
      [:> CSSTransition
       {:key              field-name
        :class-names      :fs-anim-lower
        :node-ref         input-ref
        :add-end-listener (fn [done]
                            ;; (println "input-end-listenter" done)
                            (-> input-ref .-current (.addEventListener "transitionend" done false)))}
       [:div {:class "fs-input" ;; TODO: Remove this div
              :ref   (fn [el]
                       ;; (println "input-ref" el)
                       (set! (.-current input-ref) el))}
        [input (select-keys props [:field-name :type :placeholder :value :on-change :error])]]]]]))


(defn next-field
  [form-fields current-field]
  (->> form-fields
       (drop-while #(not= % current-field))
       rest
       first))


(defn progress
  [form-fields current-field]
  [(count form-fields)
   (count (take-while #(not= % current-field) form-fields))])


(defn move-forward!
  [state]
  (let [current-field (get-in @state [:ui :current-field])
        current-value (get-in @state [:data current-field :value])]
    (if (empty? (str current-value))
      (swap! state assoc-in [:data current-field :error] true)
      (swap! state assoc-in [:ui :current-field] (next-field form-fields current-field)))))


(defn reset-form!
  [state]
  (reset! state {:ui {:current-field :name}}))


(defn keydown-handler-fn
  [callback-fn]
  (fn [e]
    (let [key-code (or (.-keyCode e) (.-which e))]
      (when (= key-code 13)
        (.preventDefault e)
        (callback-fn)))))


(defn progress-bar
  [form-fields state current-field]
  (let [total-fields  (count form-fields)
        filled-fields (-> @state
                          :data
                          (select-keys (remove #(= % current-field) form-fields))
                          count)]
    [:div {:class "fs-progress"
           :style {:width (str (/ (* 100 filled-fields) total-fields) "%")}}]))


(defn form
  []
  (r/with-let [state            (r/atom {:ui {:current-field :name}})
               current-field    (r/cursor state [:ui :current-field])
               keydown-callback (keydown-handler-fn #(move-forward! state))
               _                (.addEventListener js/document "keydown" keydown-callback)]
    ;; (println "form re-render")
    (let [input-data (assoc (get form-data @current-field)
                            :field-name @current-field
                            :value (r/cursor state [:data @current-field :value])
                            :on-change (fn [e]
                                         (swap! state assoc-in [:data @current-field] {:value (-> e .-target .-value)})
                                         (r/flush))
                            :error (r/cursor state [:data @current-field :error]))
          next-field (next-field form-fields @current-field)]
      [:<>
       [progress-bar form-fields state @current-field]
       [:form {:class "fs-form"}
        [:div {:class "fs-fields"}
         [:f> input-field input-data]]
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
