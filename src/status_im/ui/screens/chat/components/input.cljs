(ns status-im.ui.screens.chat.components.input
  (:require [status-im.ui.components.icons.vector-icons :as icons]
            [quo.react-native :as rn]
            [quo.react :as react]
            [status-im.ui.screens.chat.components.style :as styles]
            [status-im.ui.screens.chat.components.reply :as reply]
            [quo.components.animated.pressable :as pressable]
            [quo.animated :as animated]
            [status-im.utils.config :as config]
            [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [clojure.string :as string]))

(def panel->icons {:extensions :main-icons/commands
                   :images     :main-icons/photo})

(defn touchable-icon [{:keys [panel active set-active accessibility-label]}]
  [pressable/pressable {:type                :scale
                        :accessibility-label accessibility-label
                        :on-press            #(set-active (when-not (= active panel) panel))}
   [rn/view {:style (styles/touchable-icon)}
    [icons/icon
     (panel->icons panel)
     (styles/icon (= active panel))]]])

(defn touchable-stickers-icon [{:keys [panel active set-active accessibility-label input-focus]}]
  [pressable/pressable {:type                :scale
                        :accessibility-label accessibility-label
                        :on-press            #(if (= active panel)
                                                (input-focus)
                                                (set-active panel))}
   [rn/view {:style (styles/in-input-touchable-icon)}
    (if (= active panel)
      [icons/icon :main-icons/keyboard (styles/icon false)]
      [icons/icon :main-icons/stickers (styles/icon false)])]])

(defn send-button [{:keys [on-send-press]}]
  [pressable/pressable {:type     :scale
                        :on-press on-send-press}
   [rn/view {:style (styles/send-message-button)}
    [icons/icon :main-icons/arrow-up
     {:container-style     (styles/send-message-container)
      :accessibility-label :send-message-button
      :color               (styles/send-icon-color)}]]])

(defn text-input [{:keys [cooldown-enabled? text-value on-text-change set-active-panel text-input-ref]}]
  [rn/view {:style (styles/text-input-wrapper)}
   [rn/text-input {:style                 (styles/text-input)
                   :ref                   text-input-ref
                   :maxFontSizeMultiplier 1
                   :accessibility-label   :chat-message-input
                   :text-align-vertical   :top
                   :multiline             true
                   :default-value         text-value
                   :editable              (not cooldown-enabled?)
                   :blur-on-submit        false
                   :on-focus              #(set-active-panel nil)
                   :on-change             #(on-text-change (.-text ^js (.-nativeEvent ^js %)))
                   :placeholder           (if cooldown-enabled?
                                            (i18n/label :cooldown/text-input-disabled)
                                            (i18n/label :t/type-a-message))
                   :underlineColorAndroid :transparent
                   :auto-capitalize       :sentences}]])

(defn chat-input
  [{:keys [set-active-panel active-panel on-send-press reply
           show-send show-image show-stickers show-extensions
           sending-image input-focus]
    :as   props}]
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
    (when sending-image
      [reply/send-image sending-image])
    [rn/view {:style (styles/input-row)}
     [text-input props]
     [rn/view {:style (styles/in-input-buttons)}
      (when show-send
        [send-button {:on-send-press on-send-press}])
      (when show-stickers
        [touchable-stickers-icon {:panel       :stickers
                                  :active      active-panel
                                  :input-focus input-focus
                                  :set-active  set-active-panel}])]]]])

(defn chat-toolbar []
  (let [text-input-ref (react/create-ref)
        input-focus    (fn []
                         (some-> ^js (react/current-ref text-input-ref) .focus))
        had-reply      (atom nil)]
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

        (when-not (= reply @had-reply)
          (reset! had-reply reply)
          (when reply
            (js/setTimeout input-focus 250)))

        [chat-input {:set-active-panel  set-active-panel
                     :active-panel      active-panel
                     :text-input-ref    text-input-ref
                     :input-focus       input-focus
                     :reply             reply
                     :on-send-press     #(re-frame/dispatch [:chat.ui/send-current-message])
                     :text-value        input-text
                     :on-text-change    #(re-frame/dispatch [:chat.ui/set-chat-input-text %])
                     :cooldown-enabled? cooldown-enabled?
                     :show-send         (and (not empty-text)
                                             (not sending-image)
                                             (not (or processing disconnected?)))
                     :show-stickers     (and empty-text mainnet? (not reply))
                     :show-image        (and empty-text
                                             (not sending-image)
                                             (not reply)
                                             (not public?))
                     :sending-image     sending-image
                     :show-extensions   (and empty-text
                                             one-to-one-chat?
                                             (or config/commands-enabled? mainnet?)
                                             (not reply))}]))))
