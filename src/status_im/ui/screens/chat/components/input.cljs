(ns status-im.ui.screens.chat.components.input
  (:require [quo.design-system.colors :as colors]
            [status-im.ui.components.icons.vector-icons :as icons]
            [quo.react-native :as rn]
            [quo.animated :as animated]))

(defn text-input  []
  [rn/view {:style {:height           52
                    :border-top-width 1
                    :border-top-color (:ui-01 @colors/theme)
                    :background-color (:ui-background @colors/theme)
                    :align-items      :center
                    :flex-direction   :row}}
   [animated/view {:flex-direction :row
                   :padding-left   4}
    [rn/view {:padding-horizontal 10}
     [icons/icon :main-icons/commands {:color (:icon-02 @colors/theme)}]]
    [rn/view {:padding-horizontal 10}
     [icons/icon :main-icons/photo {:color (:icon-02 @colors/theme)}]]]
   [animated/view {:style {:background-color           (:ui-01 @colors/theme)
                           :height                     34
                           :flex                       1
                           :border-top-left-radius     16
                           :border-top-right-radius    16
                           :border-bottom-right-radius 4
                           :border-bottom-left-radius  16
                           :flex-direction             :row
                           :align-items                :center
                           :margin-horizontal          8}}
    [rn/text-input {:flex               1
                    :line-height        22
                    :font-size          15
                    :padding-vertical   6
                    :padding-horizontal 12
                    :placeholder        "Message"}]
    [rn/view {:padding-horizontal 6}
     [icons/icon :main-icons/sticker {:color (:icon-02 @colors/theme)}]]
    [rn/view {:padding-horizontal 6}
     [icons/icon :main-icons/commands {:color (:icon-02 @colors/theme)}]]]])

(defn chat-input [_]
  [text-input])
