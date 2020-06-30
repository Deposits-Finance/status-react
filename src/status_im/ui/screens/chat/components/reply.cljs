(ns status-im.ui.screens.chat.components.reply
  (:require [quo.core :as quo]
            [quo.react-native :as rn]
            [status-im.i18n :as i18n]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.ethereum.stateofus :as stateofus]
            [status-im.ui.screens.chat.components.style :as styles]
            [re-frame.core :as re-frame]))

(def ^:private reply-symbol "↪ ")

(defn format-author [contact-name]
  (if (or (= (aget contact-name 0) "@")
          ;; in case of replies
          (= (aget contact-name 1) "@"))
    (or (stateofus/username contact-name)
        (subs contact-name 0 81))
    contact-name))

(defn format-reply-author [from username current-public-key]
  (or (and (= from current-public-key)
           (str reply-symbol (i18n/label :t/You)))
      (format-author (str reply-symbol username))))

(defn reply-message [{:keys [from content]}]
  (let [contact-name       @(re-frame/subscribe [:contacts/contact-name-by-identity from])
        current-public-key @(re-frame/subscribe [:multiaccount/public-key])]
    [rn/view {:style (styles/reply-container)}
     [rn/view {:style (styles/reply-content)}
      [quo/text {:weight          :medium
                 :number-of-lines 1
                 :style           {:line-height 18}
                 :size            :small}
       (format-reply-author from contact-name current-public-key)]
      [quo/text {:size            :small
                 :number-of-lines 1
                 :style           {:line-height 18}}
       (:text content)]]
     [rn/view
      [rn/touchable-highlight {:on-press #(re-frame/dispatch [:chat.ui/cancel-message-reply])
                               :style (styles/close-button)}
       [icons/icon :main-icons/close-circle {}]]]]))
