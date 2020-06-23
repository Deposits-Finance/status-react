(ns quo.hooks
  (:require [quo.react :as react]
            [quo.react-native :refer [use-window-dimensions] :as rn]
            [quo.components.safe-area :refer [use-safe-area]]
            [quo.animated :as animated]))

(def ^:private keyboard-change-event "keyboardWillChangeFrame")

(defn use-keyboard-dimension []
  (let [{:keys [bottom]} (use-safe-area)
        {:keys [height]} (use-window-dimensions)
        keyboard-height  (react/state 0)
        keyboard-end-pos (react/state height)
        bottom-safe-area (react/state 0)]
    (react/effect!
     (fn []
       (letfn [(dimensions-change [evt]
                 (reset! keyboard-end-pos (-> ^js evt .-window .-height)))
               (keyboard-dimensions [evt]
                 (let [duration   (.-duration ^js evt)
                       easing     (.-easing ^js evt)
                       screen-y   (-> ^js evt .-endCoordinates .-screenY)
                       new-height (- height screen-y)]
                   (when-not (= new-height keyboard-height)
                     (when (and duration easing)
                       (rn/configure-next
                        #js {:duration (max duration 10)
                             :update   {:duration (max duration 10)
                                        :type     (-> ^js rn/layout-animation .-Types (aget easing))}})))
                   (reset! keyboard-end-pos screen-y)
                   (reset! keyboard-height new-height)
                   (reset! bottom-safe-area (if (pos? new-height) bottom 0))))]
         (.addEventListener rn/dimensions "change" dimensions-change)
         (.addListener rn/keyboard keyboard-change-event keyboard-dimensions)
         (fn []
           (.removeEventListener rn/dimensions "change" dimensions-change)
           (.removeAllListeners rn/keyboard keyboard-change-event)))))
    {:keyboard-height       @keyboard-height
     :keyboard-end-position @keyboard-end-pos
     :bottom-safe-area      @bottom-safe-area}))

(defn use-pan-responder []
  (let [y (animated/value 0)]
    {:pan-handlers (js->clj
                    (.-panHandlers ^js
                                   (rn/create-pan-responder
                                    {:onPanResponderMove (fn [_ ^js state]
                                                           (animated/set-value y (.-moveY state)))
                                     :onPanResponderEnd  (fn []
                                                           (js/setTimeout
                                                            (fn []
                                                              (animated/set-value y 0))
                                                            10))})))
     :position-y   y}))
