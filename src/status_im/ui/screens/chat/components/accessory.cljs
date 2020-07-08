(ns status-im.ui.screens.chat.components.accessory
  (:require [quo.animated :as animated]
            [reagent.core :as reagent]
            [cljs-bean.core :as bean]
            [quo.design-system.colors :as colors]
            [quo.hooks :refer [use-keyboard-dimension]]
            [quo.react :as react]
            [quo.react-native :as rn]
            [quo.components.safe-area :refer [use-safe-area]]))

(def tabbar-height 36)

(defn create-pan-responder [y pan-active]
  (js->clj (.-panHandlers
            ^js (.create
                 ^js rn/pan-responder
                 #js {:onPanResponderGrant   (fn []
                                               (animated/set-value pan-active 1))
                      :onPanResponderMove    (fn [_ ^js state]
                                               (animated/set-value y (.-moveY state)))
                      :onPanResponderRelease (fn []
                                               (println "release")
                                               (animated/set-value pan-active 0))
                      :onPanResponderEnd     (fn []
                                               (println "end")
                                               (js/setTimeout
                                                #(animated/set-value y 0)
                                                10))}))))

(def view
  (reagent/adapt-react-class
   (react/memo
    (fn [props]
      (let [{on-update-inset :onUpdateInset
             y               :y
             pan-state       :panState
             on-close        :onClose
             has-panel       :hasPanel
             children        :children}
            (bean/bean props)

            {:keys [keyboard-height
                    keyboard-max-height
                    keyboard-end-position]} (use-keyboard-dimension)
            {:keys [bottom]}                (use-safe-area)
            {on-layout  :on-layout
             bar-height :height}            (rn/use-layout)

            visible         (or has-panel (pos? keyboard-height))
            kb-on-screen    (* -1 (- keyboard-height bottom tabbar-height))
            panel-on-screen (* -1 (- keyboard-max-height bottom tabbar-height))
            max-delta       (min 0 (if has-panel panel-on-screen kb-on-screen))
            end-position    (- keyboard-end-position (when has-panel keyboard-max-height))
            drag-diff       (animated/sub y end-position)
            delta-y         (animated/clamp (animated/add drag-diff panel-on-screen)
                                            max-delta
                                            0)
            panel-height    (* -1 max-delta)
            on-update       (react/callback
                             (fn []
                               (when on-update-inset
                                 (on-update-inset (+ bar-height panel-height))))
                             [panel-height bar-height])
            ;; timing          (animated/use-timing-transition visible {})
            children        (react/get-children children)]
        (react/effect! on-update)
        (animated/code!
         (fn []
           (when has-panel
             ;; TODO: Check also velocity
             (animated/cond* (animated/and* (animated/greater-or-eq delta-y (* 0.25 max-delta))
                                            (animated/not* pan-state))
                             [(animated/call* [] on-close)])))
         [delta-y pan-state has-panel on-close])
        (rn/use-back-handler
         (fn []
           (when visible
             (on-close))
           visible))
        (reagent/as-element
         [animated/view {:style {:position         :absolute
                                 :left             0
                                 :right            0
                                 :background-color (:ui-background @colors/theme)
                                 :bottom           max-delta
                                 :transform        [{:translateY delta-y}]}}
          [rn/view {:on-layout on-layout}
           (first children)]
          [rn/view {:style {:flex   1
                            :height (when (pos? panel-height) panel-height)}}
           (second children)]]))))))
