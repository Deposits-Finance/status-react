(ns status-im.ui.screens.chat.components.style
  (:require [quo.design-system.colors :as colors]))

(defn toolbar []
  {:min-height       52
   :padding-vertical 9
   :border-top-width 1
   :border-top-color (:ui-01 @colors/theme)
   :background-color (:ui-background @colors/theme)
   :align-items      :flex-end
   :flex-direction   :row})

(defn input-container []
  {:background-color           (:ui-01 @colors/theme)
   :min-height                 34
   :max-height                 144
   :flex                       1
   :border-top-left-radius     16
   :border-top-right-radius    16
   :border-bottom-right-radius 4
   :border-bottom-left-radius  16
   :margin-horizontal          8})

(defn text-input []
  {:flex               1
   :line-height        22
   :font-size          15
   :padding-vertical   6
   :padding-horizontal 12})

(defn touchable-icon [_]
  {:padding-horizontal 6
   :padding-vertical   5
   :justify-content    :center
   :align-items        :center})

(defn icon [active]
  {:color (if active
            (:icon-04 @colors/theme)
            (:icon-02 @colors/theme))})

(defn reply-container []
  {:border-top-left-radius     14
   :border-top-right-radius    14
   :border-bottom-right-radius 4
   :border-bottom-left-radius  14
   :margin                     2
   :flex-direction             :row
   :background-color           (:ui-02 @colors/theme)})

(defn reply-content []
  {:padding-vertical   6
   :padding-horizontal 10
   :flex               1})

(defn close-button []
  {:padding 4})

(defn send-message-button []
  {:margin-vertical   4
   :margin-horizontal 5})

(defn send-message-container []
  {:background-color  (:interactive-01 @colors/theme)
   :width             26
   :height            26
   :border-radius     13
   :justify-content   :center
   :align-items       :center})

(defn send-icon-color []
  colors/white)
