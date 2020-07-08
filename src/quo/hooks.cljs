(ns quo.hooks
  (:require [quo.react :as react]
            [quo.platform :as platform]
            [quo.components.safe-area :refer [use-safe-area]]
            [quo.react-native :refer [use-window-dimensions] :as rn]))

(def ^:private keyboard-change-event "keyboardWillChangeFrame")

(def default-kb-height (if platform/ios? 258 272))

(defn use-keyboard-dimension []
  (let [{:keys [height]}    (use-window-dimensions)
        {:keys [bottom]}    (use-safe-area)
        keyboard-height     (react/state 0)
        keyboard-max-height (react/state (+ bottom default-kb-height))
        keyboard-end-pos    (react/state height)]
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
                       (rn/configure-next
                        #js {:duration (max 10 duration)
                             :update   {:duration (max 10 duration)
                                        :type     (-> ^js rn/layout-animation .-Types (aget easing))}})))
                   (reset! keyboard-end-pos screen-y)
                   (reset! keyboard-height new-height)
                   (swap! keyboard-max-height max new-height)))]
         (.addEventListener rn/dimensions "change" dimensions-change)
         (.addListener rn/keyboard keyboard-change-event keyboard-dimensions)
         (fn []
           (.removeEventListener rn/dimensions "change" dimensions-change)
           (.removeAllListeners rn/keyboard keyboard-change-event)))))
    {:keyboard-height       @keyboard-height
     :keyboard-max-height   @keyboard-max-height
     :keyboard-end-position @keyboard-end-pos}))
