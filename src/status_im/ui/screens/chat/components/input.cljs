(ns status-im.ui.screens.chat.components.input
  (:require [reagent.core :as reagent]
            [status-im.ui.components.icons.vector-icons :as icons]
            [quo.react-native :as rn]
            [status-im.ui.screens.chat.components.style :as styles]
            [status-im.ui.screens.chat.components.reply :as reply]
            [quo.animated :as animated]
            [status-im.utils.config :as config]
            [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [clojure.string :as string]))

(def panel->icons {:stickers   :main-icons/stickers
                   :extensions :main-icons/commands
                   :images     :main-icons/photo})

(defn touchable-icon [{:keys [panel active set-active]}]
  [rn/touchable-highlight {:style    (styles/touchable-icon active)
                           :type     :icon
                           :on-press #(set-active panel)}
   [icons/icon
    (panel->icons panel)
    (styles/icon (= active panel))]])

(defn send-button [{:keys [on-send-press]}]
  [rn/touchable-highlight {:on-press on-send-press
                           :style    (styles/send-message-button)}
   [icons/icon :main-icons/arrow-up
    {:container-style     (styles/send-message-container)
     :accessibility-label :send-message-button
     :color               (styles/send-icon-color)}]])

(defn text-input [{:keys [cooldown-enabled? set-active-panel]}]
  [rn/text-input {:style               (styles/text-input)
                  :accessibility-label :chat-message-input
                  :multiline           true
                  ;; :default-value       (or @text-value "")
                  :editable            (not cooldown-enabled?)
                  :blur-on-submit      false
                  :on-focus            #(set-active-panel nil)
                  ;; :on-change           #(reset! text-value (.-text ^js (.-nativeEvent ^js %)))
                  :placeholder         (if cooldown-enabled?
                                         (i18n/label :cooldown/text-input-disabled)
                                         (i18n/label :t/type-a-message))
                  :auto-capitalize     :sentences}])

(defn chat-input
  [{:keys [set-active-panel active-panel on-send-press reply
           show-send show-image show-stickers show-extensions]
    :as props}]
  [rn/view {:style (styles/toolbar)}
   [animated/view {:flex-direction :row
                   :padding-left   4}
    (when show-extensions
      [touchable-icon {:panel      :extensions
                       :active     active-panel
                       :set-active set-active-panel}])
    (when show-image
      [touchable-icon {:panel      :images
                       :active     active-panel
                       :set-active set-active-panel}])]
   [animated/view {:style (styles/input-container)}
    (when reply
      [reply/reply-message reply])
    [rn/view {:style {:flex-direction :row
                      :align-items    :flex-end}}
     [text-input props]
     (when show-send
       [send-button {:on-send-press on-send-press}])
     (when show-stickers
       [touchable-icon {:panel      :stickers
                        :active     active-panel
                        :set-active set-active-panel}])]]])

(defn chat-toolbar []
  (fn [{:keys [active-panel set-active-panel]}]
    (let [disconnected?        @(re-frame/subscribe [:disconnected?])
          {:keys [processing]} @(re-frame/subscribe [:multiaccounts/login])
          mainnet?             @(re-frame/subscribe [:mainnet?])
          input-text           @(re-frame/subscribe [:chats/current-chat-input-text])
          cooldown-enabled?    @(re-frame/subscribe [:chats/cooldown-enabled?])
          one-to-one-chat?     @(re-frame/subscribe [:current-chat/one-to-one-chat?])
          public?              @(re-frame/subscribe [:current-chat/public?])
          reply                @(re-frame/subscribe [:chats/reply-message])
          sending-image        @(re-frame/subscribe [:chats/sending-image])
          empty-text           (string/blank? (string/trim (or input-text "")))]
      [chat-input {:set-active-panel  set-active-panel
                   :active-panel      active-panel
                   :reply             reply
                   :on-send-press     identity
                   :cooldown-enabled? cooldown-enabled?
                   :show-send         (and (not empty-text)
                                           (not sending-image)
                                           (not (or processing disconnected?)))
                   :show-stickers     (and empty-text mainnet? (not reply))
                   :show-image        (and empty-text
                                           (not sending-image)
                                           (not reply)
                                           (not public?))
                   :show-extensions   (and empty-text
                                           one-to-one-chat?
                                           (or config/commands-enabled? mainnet?)
                                           (not reply))}])))
