(ns status-im.ui.screens.chat.components.accessory
  (:require [quo.animated :as animated]
            [quo.gesture-handler :as gh]
            [status-im.ui.components.react :as r]
            [status-im.ui.components.animation :as animation]
            [reagent.core :as reagent]
            [quo.hooks :refer [use-keyboard-dimension]]
            [quo.react :as react]
            [quo.components.safe-area :refer [use-safe-area]]))

(def tabbar-height 36)

(defn pan-gesture [{:keys [y]} & children]
  (let [list-ref      (react/create-ref)
        gesture-ref   (react/create-ref)
        state         (animated/value (:undetermined gh/states))
        gesture-event (animated/on-gesture {:absoluteY y
                                            :state     state})]
    [:<>
     [animated/code {:exec (animated/on-change state
                                               [(animated/cond* (animated/eq state (:end gh/states))
                                                                (animated/set y 0))])}]
     [gh/pan-gesture-handler (merge {:ref                  gesture-ref
                                     :simultaneousHandlers list-ref}
                                    gesture-event)
      [animated/view {:flex 1}
       (into [gh/native-view-gesture-handler {:simultaneousHandlers gesture-ref
                                              :ref                  list-ref}]
             children)]]]))

(def view
  (reagent/adapt-react-class
   (react/memo
    (fn [props]
      (let [{:keys [keyboard-height
                    keyboard-end-position]}
            (use-keyboard-dimension)
            {:keys [bottom]} (use-safe-area)
            ;; y                (.-y ^js props)
            a-y              (.-aY ^js props)
            on-update-inset  (.-onUpdateInset ^js props)
            children         (.-children ^js props)
            kb-on-screen     (- keyboard-height bottom tabbar-height)
            bottom-position  (max 0 kb-on-screen)
            ;; delta-y          (animated/clamp (animated/sub y keyboard-end-position) 0 bottom-position)
            on-update        (react/callback
                              (fn []
                                (when on-update-inset
                                  (on-update-inset (+ 52 bottom-position))))
                              [keyboard-height bottom])
            trans            (animation/subtract a-y keyboard-end-position)]
        ;; (animation/add-listener trans println)
        (react/effect! on-update)
        (reagent/as-element
         [r/animated-view {:style {:position  :absolute
                                   :left      0
                                   :right     0
                                   :bottom    bottom-position
                                   :transform [{:translateY (animation/interpolate trans
                                                                                   {:inputRange  [0 bottom-position]
                                                                                    :outputRange [0 bottom-position]
                                                                                    :extrapolate "clamp"})}]}}
          (into [r/animated-view {:flex 1}]
                (react/get-children children))]))))))
