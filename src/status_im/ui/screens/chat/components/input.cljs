(ns status-im.ui.screens.chat.components.input
  (:require [reagent.core :as reagent]
            [quo.hooks :refer [use-keyboard-dimension]]
            [quo.react-native :as rn]
            [quo.react :as react]
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
          [rn/view {:flex 1}
           [rn/text-input {:placeholder      "Text here"
                           :height           64
                           :background-color :black
                           :width            "100%"}]]]))))))

(defn chat-input [y]
  [accessory-view {:y y}])
