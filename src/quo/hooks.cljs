(ns quo.hooks
  (:require [quo.react :as react]
            [quo.react-native :refer [use-window-dimensions] :as rn]))

(def ^:private keyboard-change-event "keyboardWillChangeFrame")

(defn use-keyboard-dimension []
  (let [{:keys [height]} (use-window-dimensions)
        keyboard-height  (react/state 0)
        keyboard-end-pos (react/state height)]
    (react/effect!
     (fn []
       (letfn [(dimensions-change [evt]
                 (reset! keyboard-end-pos (-> ^js evt .-window .-height)))
               (keyboard-dimensions [evt]
                 (let [duration   (.-duration ^js evt)
                       easing     (.-easing ^js evt)
                       screen-y   (-> ^js evt .-endCoordinates .-screenY)
                       new-height (- height screen-y)]
                   (when-not (= new-height @keyboard-height)
                     (when (and duration easing)
                       (let [delay    (* (max duration 10) 0.2)
                             duration (* delay 4)]
                         (rn/configure-next
                          #js {:duration duration
                               :delay delay
                               :update   {:duration duration
                                          :delay    delay
                                          :type     (-> ^js rn/layout-animation .-Types (aget easing))}}))))
                   (reset! keyboard-end-pos screen-y)
                   (reset! keyboard-height new-height)))]
         (.addEventListener rn/dimensions "change" dimensions-change)
         (.addListener rn/keyboard keyboard-change-event keyboard-dimensions)
         (fn []
           (.removeEventListener rn/dimensions "change" dimensions-change)
           (.removeAllListeners rn/keyboard keyboard-change-event)))))
    {:keyboard-height       @keyboard-height
     :keyboard-end-position @keyboard-end-pos}))
