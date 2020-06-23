(ns status-im.ui.screens.chat.components.input
  (:require [reagent.core :as reagent]
            [quo.hooks :refer [use-keyboard-dimension]]
            [quo.react-native :as rn]
            [quo.react :as react]
            [quo.core :as quo]
            [quo.design-system.colors :as colors]
            [status-im.ui.components.icons.vector-icons :as icons]
            [quo.animated :as animated]))

(def max-int (.-MAX_SAFE_INTEGER ^js js/Number))

(def accessory-view
  (reagent/adapt-react-class
   (react/memo
    (fn [props]
      (let [{:keys [keyboard-height
                    keyboard-end-position
                    bottom-safe-area]}
            (use-keyboard-dimension)
            y           (.-y ^js props)
            children    (.-children ^js props)
            delta-y     (animated/sub y keyboard-end-position)
            translate-y (animated/sub keyboard-height
                                      (animated/clamp delta-y 0 max-int)
                                      bottom-safe-area
                                      36)]
        (reagent/as-element
         [animated/view {:style {:position  :absolute
                                 :left      0
                                 :right     0
                                 :bottom    0
                                 :transform [{:translateY (animated/multiply -1
                                                                             (animated/clamp translate-y 0 max-int))}]}
                         }
          (into [rn/view {:flex 1}]
                (react/get-children children))]))))))


(defn text-input  []
  [rn/view {:style {:height           52
                    :border-top-width 1
                    :border-top-color (:ui-01 @colors/theme)
                    :background-color (:ui-background @colors/theme)
                    :align-items      :center
                    :flex-direction   :row}}
   [animated/view {:flex-direction :row}
    [icons/icon :main-icons/commands {:color :grey}]
    [icons/icon :main-icons/photo {:color :grey}]]
   [animated/view {:style {:background-color           (:ui-01 @colors/theme)
                           :height                     34
                           :flex                       1
                           :border-top-left-radius     16
                           :border-top-right-radius    16
                           :border-bottom-right-radius 4
                           :border-bottom-left-radius  16
                           :flex-direction             :row}}
    [rn/text-input {:flex               1
                    :line-height        22
                    :font-size          15
                    :padding-vertical   6
                    :padding-horizontal 12
                    :placeholder        "Message"}]
    [icons/icon :main-icons/sticker {:color :grey}]
    [icons/icon :main-icons/commands {:color :grey}]
    ]])

(defn chat-input [y]
  [accessory-view {:y y}
   [text-input]])
