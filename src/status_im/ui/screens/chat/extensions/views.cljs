(ns status-im.ui.screens.chat.extensions.views
  (:require-macros [status-im.utils.views :as views])
  (:require [status-im.ui.components.react :as react]
            [re-frame.core :as re-frame]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.ui.components.animation :as anim]
            [quo.core :as quo]
            [status-im.i18n :as i18n]))

(defn button [showing?]
  [quo/button
   {:on-press            (fn [_]
                           (re-frame/dispatch [:chat.ui/set-chat-ui-props {:input-bottom-sheet (when-not showing? :extensions)}])
                           (js/setTimeout #(react/dismiss-keyboard!) 100))
    :accessibility-label :show-extensions-icon
    :type                :icon
    :theme               (if showing? :main :disabled)}
   :main-icons/commands])

(defn show-panel-anim
  [bottom-anim-value alpha-value]
  (anim/start
   (anim/parallel
    [(anim/spring bottom-anim-value {:toValue         0
                                     :useNativeDriver true})
     (anim/timing alpha-value {:toValue         1
                               :duration        500
                               :useNativeDriver true})])))

(views/defview extensions-view []
  (views/letsubs [panel-height [:chats/chat-panel-height]

                  bottom-anim-value (anim/create-value @panel-height)
                  alpha-value       (anim/create-value 0)]
    {:component-did-mount #(show-panel-anim bottom-anim-value alpha-value)}
    [react/animated-view {:style {:background-color colors/white
                                  :height    panel-height
                                  :transform [{:translateY bottom-anim-value}]
                                  :opacity   alpha-value}}
     [react/view {:style {:flex-direction :row}}
      [react/touchable-highlight
       {:on-press #(re-frame/dispatch [:wallet/prepare-transaction-from-chat])}
       [react/view {:width 128 :height 128 :justify-content :space-between
                    :padding-horizontal 10 :padding-vertical 12
                    :background-color (colors/alpha colors/purple 0.2) :border-radius 16 :margin-left 8}
        [react/view {:background-color colors/purple :width 40 :height 40 :border-radius 20 :align-items :center
                     :justify-content :center}
         [icons/icon :main-icons/send {:color colors/white}]]
        [react/text {:typography :medium} (i18n/label :t/send-transaction)]]]
      [react/touchable-highlight
       {:on-press #(re-frame/dispatch [:wallet/prepare-request-transaction-from-chat])}
       [react/view {:width 128 :height 128 :justify-content :space-between
                    :padding-horizontal 10 :padding-vertical 12
                    :background-color (colors/alpha colors/orange 0.2) :border-radius 16 :margin-left 8}
        [react/view {:background-color colors/orange :width 40 :height 40 :border-radius 20 :align-items :center
                     :justify-content :center}
         [icons/icon :main-icons/receive {:color colors/white}]]
        [react/text {:typography :medium} (i18n/label :t/request-transaction)]]]]]))
