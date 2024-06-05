(ns field
  (:require
   ["react-transition-group" :refer [CSSTransition SwitchTransition]]
   ["react" :as react]))


(defn deref-or-value
  "Takes a value or an atom
  If it's a value, returns it
  If it's an object that supports IDeref, returns the value inside it by derefing
  "
  [val-or-atom]
  (if (satisfies? IDeref val-or-atom)
    @val-or-atom
    val-or-atom))


(defn- native-input
  [{:keys [name type placeholder value on-change error]}]
  [:input (merge
           {:key         name
            :type        type
            :value       (deref-or-value value)
            :on-change   on-change
            :placeholder placeholder
            :auto-focus  true}
           (when (deref-or-value error)
             {:error :true}))])


(defn- native-textarea
  [{:keys [name placeholder value on-change error]}]
  [:textarea (merge
              {:key         name
               :value       (deref-or-value value)
               :on-change   on-change
               :placeholder placeholder
               :auto-focus  true}
              (when (deref-or-value error)
                {:error :true}))])


(defn- input
  [{:keys [name type _placeholder _value _on-change _error] :as props}]
  (let [ref             (react/useRef)
        input-component (case type
                          :textarea native-textarea
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
  [{:keys [_name _type _label _placeholder _value _on-change _data-info _error] :as props}]
  [:<>
   [:f> label (select-keys props [:name :label :data-info])]
   [:f> input (select-keys props [:name :type :placeholder :value :on-change :error])]])
