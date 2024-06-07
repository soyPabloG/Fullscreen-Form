(ns field
  (:require
   [reagent.core :as r]
   ["react-transition-group" :refer [CSSTransition SwitchTransition]]
   ["react" :as react]))


(defn- native-input
  [{:keys [name type placeholder cursor value on-change error]}]
  (let [cursor-props (when cursor
                       (merge
                        {:value     (:value @cursor)
                         :on-change (fn [e]
                                      (reset! cursor {:value (-> e .-target .-value)})
                                      (r/flush))}
                        (when (:error @cursor)
                          {:error :true})))]
    [:input (merge
             {:key         name
              :type        type
              :value       value
              :on-change   on-change
              :placeholder placeholder
              :auto-focus  true}
             (when error
               {:error :true})
             cursor-props)]))


(defn- native-textarea
  [{:keys [name placeholder cursor value on-change error]}]
  (let [cursor-props (when cursor
                       (merge
                        {:value     (:value @cursor)
                         :on-change (fn [e]
                                      (reset! cursor {:value (-> e .-target .-value)})
                                      (r/flush))}
                        (when (:error @cursor)
                          {:error :true})))]
    [:textarea (merge
                {:key         name
                 :value       value
                 :on-change   on-change
                 :placeholder placeholder
                 :auto-focus  true}
                (when error
                  {:error :true})
                cursor-props)]))


(defn- icon-radio-button
  [{:keys [id name label value image-url checked? on-change]}]
  [:span {:class "fs-icon-radio"}
   [:input {:id        id
            :name      name
            :type      :radio
            :value     value
            :checked   checked?
            :on-change on-change}]
   [:label {:for   id
            :style {:background-image (str "url(" image-url ")")}}
    label]])


(defn- icon-radio-group
  [{name           :name
    cursor         :cursor
    selected-value :value
    options        :options
    on-change      :on-change}]
  [:div {:class "fs-icon-radio-group"}
   (doall
    (for [{:keys [label value image-url]} options]
      (let [id           (str name "-" value)
            cursor-props (when cursor
                           {:checked?  (= value (:value @cursor))
                            :on-change (fn [e]
                                         (reset! cursor {:value (keyword (-> e .-target .-value))}))})]
        ^{:key id}
        [icon-radio-button (merge
                            {:id        id
                             :name      name
                             :label     label
                             :value     value
                             :image-url image-url
                             :checked?  (= value selected-value)
                             :on-change on-change}
                            cursor-props)])))])


(defn- input
  [{:keys [name type _cursor _placeholder _value _on-change _error] :as props}]
  (let [ref             (react/useRef)
        input-component (case type
                          :textarea   native-textarea
                          :icon-radio icon-radio-group
                          native-input)]
    [:> SwitchTransition
     [:> CSSTransition
      {:key              name
       :class-names      :fs-anim-lower
       :node-ref         ref
       :add-end-listener (fn [done]
                           (-> ref .-current (.addEventListener "transitionend" done false)))}
      [:div {:class "fs-input" ;; TODO: Remove this div
             :ref   (fn [el]
                      (set! (.-current ref) el))}
       [input-component props]]]]))


(defn- label
  [{:keys [name label data-info]}]
  (let [ref (react/useRef)]
    [:> SwitchTransition
     [:> CSSTransition
      {:key              name
       :class-names      :fs-anim-upper
       :node-ref         ref
       :add-end-listener (fn [done]
                           (-> ref .-current (.addEventListener "transitionend" done false)))}
      [:label {:for       name
               :data-info data-info
               :ref       (fn [el]
                            (set! (.-current ref) el))}
       label]]]))


(defn field
  [{:keys [_name _type _cursor _label _placeholder _value _on-change _data-info _error] :as props}]
  [:<>
   [:f> label (select-keys props [:name :label :data-info])]
   [:f> input (select-keys props [:name :type :cursor :placeholder :value :options :on-change :error])]])
