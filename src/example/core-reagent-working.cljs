(ns example.core
  (:require-macros [reagent-mui.util :refer [react-component]])
  (:require [reagent.core :as r]
            [reagent.dom :as reagent-dom]
            [reagent-mui.cljs-time-adapter :refer [cljs-time-adapter]]
            [reagent-mui.colors :as colors]
            [reagent-mui.material.box :refer [box]]
            [reagent-mui.material.button :refer [button]]
            [reagent-mui.material.css-baseline :refer [css-baseline]]
            [reagent-mui.material.divider :refer [divider]]
            [reagent-mui.material.drawer :refer [drawer]]
            [reagent-mui.material.list :refer [list]]
            [reagent-mui.material.list-item :refer [list-item]]
            [reagent-mui.material.list-item-icon :refer [list-item-icon]]
            [reagent-mui.material.list-item-text :refer [list-item-text]]
            [reagent-mui.material.stack :refer [stack]]
            [reagent-mui.material.text-field :refer [text-field]]
            [reagent-mui.material.toolbar :refer [toolbar]]
            [reagent-mui.material.typography :refer [typography]]
            [reagent-mui.icons.add-box :refer [add-box]]
            [reagent-mui.icons.email :refer [email]]
            [reagent-mui.icons.inbox :refer [inbox]]
            [reagent-mui.x.localization-provider :refer [localization-provider]]
            [reagent-mui.styles :as styles])
  ;; Other stuff
  (:import (goog.i18n DateTimeSymbols_en_US)))

(set! *warn-on-infer* true)

(defn event-value
  [e]
  (.. e -target -value))

;; Example
(def custom-theme
  {:palette {:primary   colors/purple
             :secondary colors/green}})

(def classes (let [prefix "rmui-example"]
               {:root       (str prefix "-root")
                :button     (str prefix "-button")
                :text-field (str prefix "-text-field")
                :dragger    (str prefix "-box") ; Was :dragger "-dragger"
                :drawer     (str prefix "-drawer")}))

(defn custom-styles [{:keys [theme]}]
  (let [spacing (:spacing theme)]
    {(str "&." (:root classes))        {:margin-left (spacing 0) ; was 8
                                        :align-items :flex-start}
     (str "& ." (:button classes))     {:margin (spacing 1)}
     (str "& ." (:text-field classes)) {:width        500 ; "80%" ;200
                                        :margin-left  (spacing 1)
                                        :margin-right (spacing 1)}
     (str "& ." (:dragger classes))    {:width "30px",
                                        :height "150px", ; "100%" ; not good
                                        :cursor "ew-resize",
                                        :backgroundColor "black"}
     (str "& ." (:drawer classes))     {:flexShrink 0}}))

;;;============================= dragger ==================================
(def minDrawerWidth 50)
(def maxDrawerWidth 1000)
(def diag (atom nil))

(defonce text-state (r/atom "foobar"))
(defonce select-state (r/atom 1))
(defonce date-picker-state (r/atom nil))
(defonce autocomplete-state (r/atom nil))

(defonce left-editor-width  (r/atom 500))
(defonce right-editor-width (r/atom 500))
(defonce horiz-drag-pos     (r/atom nil))

;;; This declares a new state variable. It is something you'd do with React hooks.
;;; https://reactjs.org/docs/hooks-intro.html
;;; const [drawerWidth, setDrawerWidth] = React.useState(defaultDrawerWidth);

;;;  const handleMouseMove = useCallback(e => {
;;;    const newWidth = e.clientX - document.body.offsetLeft;
;;;    if (newWidth > minDrawerWidth && newWidth < maxDrawerWidth) {
;;;      setDrawerWidth(newWidth); }
(defn handle-mouse-move [e]
  (let [mouse-x (. e -clientX)]
    (reset! horiz-drag-pos mouse-x)
    (js/console.log (str "x = " mouse-x))))

;;;  const handleMouseUp = () => {
;;;    document.removeEventListener("mouseup", handleMouseUp, true);
;;;    document.removeEventListener("mousemove", handleMouseMove, true); }
(defn handle-mouse-up []
  (js/console.log "UP")
  (js/document.removeEventListener "mouseup"   handle-mouse-up)
  (js/document.removeEventListener "mousemove" handle-mouse-move))

;;;  const handleMouseDown = e => {
;;;    document.addEventListener("mouseup", handleMouseUp, true);
;;;    document.addEventListener("mousemove", handleMouseMove, true); }
(defn handle-mouse-down [e]
  (js/console.log "DOWN" e)
  (js/document.addEventListener "mouseup"   handle-mouse-up)
  (js/document.addEventListener "mousemove" handle-mouse-move))

(defn Dragger  []
  [stack {:direction :row
          :display   "flex" ; I'm guessing; there was no :display except here: (on a box) https://mui.com/material-ui/react-divider/
          :spacing   2}     ; This is used on the stack example with a divider too. Without it, divider will touch 2nd.

   [text-field
    {:id          "data-editor"
     :value       @text-state
     :label       "Data"
     :placeholder "Placeholder"
     :class       (:text-field classes)
     :on-change   (fn [e] (reset! text-state (event-value e)))
     :multiline   true
     :rows        10}]

   [box
    {:class         (:dragger classes)
     :on-mouse-down handle-mouse-down
     :on-mouse-up   handle-mouse-up
     :on-mouse-move handle-mouse-move}]

   [text-field
    {:id          "rm-editor"
     :value       @text-state
     :label       "Editor"
     :placeholder "Placeholder"
     :class       (:text-field classes)
     :on-change   (fn [e] (reset! text-state (event-value e)))
     :multiline   true
     :rows        10}]])

;;;=============================================================================
(defn form* [{:keys [class-name]}]
  [stack {:direction "column"
          :spacing   2
          :class     [class-name (:root classes)]}
   [typography
    {:variant :h3
     :color "white"
     :width "100%"
     :padding "5px 0px 5px 40px"
     :backgroundColor "primary.main"
     :noWrap  true}
    "RADmapper"]
   [Dragger]])

(def form (styles/styled form* custom-styles))

(defn main []
  ;; fragment
  [:<>
   [css-baseline]
   ;; localization-provider provides date handling utils to date and time pickers.
   ;; cljs-time-adapter is a date adapter that allows you to use cljs-time / goog.date date objects.
   [localization-provider {:date-adapter   cljs-time-adapter
                           :adapter-locale DateTimeSymbols_en_US}
    [styles/theme-provider (styles/create-theme custom-theme)
     [form]]]])

(defn ^{:after-load true, :dev/after-load true}
  mount []
    (reagent-dom/render [main] (js/document.getElementById "app")))

(defn ^:export init []
  (mount))
