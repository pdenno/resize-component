(ns example.core
  (:require
   ["@mui/material/Typography$default" :as Typography]
   ["@mui/material/TextField$default" :as TextField]
   ["@mui/material/Stack$default" :as Stack]
   ["@mui/material/Box$default" :as MuiBox]
   ["@mui/material/styles" :as styles]
   ["@mui/material/CssBaseline" :refer [css-baseline]]
   ["@mui/material/colors" :as colors]
   ;["@mui/x-date-pickers/LocalizationProvider" :refer [localization-provider]]
   [applied-science.js-interop :as j]
   [helix.core :refer [defnc $]]
   [helix.hooks :as hooks]
   [helix.dom :as d]
   ["react-dom/client" :as react-dom]))

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

;;; The $ macro takes a component type (string, keyword, or symbol referring to a Component), optionally some props,
;;; and any children, and returns a React Element with that same information, like React.createElement.
(defonce text-state         (atom "Initial Text"))
(defn event-value [e] (.. e -target -value))
(defonce diag (atom nil))
(def move-event (atom nil))

(defnc Resizable [{:keys [the-ref axis backgroundColor init-width init-height]}]
  (let [[coords set-coords] (hooks/use-state {:x 200 :y 200}) ; js/Infinity
        [dims set-dims]     (hooks/use-state {:width init-width :height init-height})
        [size set-size]     (hooks/use-state {:width init-width :height init-height})
        cursor (case axis :both "nwse-resize" :vertical "ns-resize" :horizonal "ew-resize")
        resize   (fn [e]
                   (when-let [current (.-current the-ref)]
                     (when e
                       (let [width  (- (+ (:width  dims) (j/get e .-clientX)) (:x coords)) ; Change these for REAL movement.
                             height (- (+ (:height dims) (j/get e .-clientY)) (:y coords))]
                         (js/console.log (str "**** I run when 'coords' changes. size=" size ))
                         (set-size {:width width :height height})
                         (set-dims {:width width :height height})))))]
    (hooks/use-effect [coords] (resize @move-event))
    (letfn [(do-drag [e]
              (reset! move-event e)
              (js/console.log (str "mouse move x = " (j/get e .-clientX) " y = " (j/get e .-clientY)))
              (set-coords {:x (j/get e .-clientX) :y (j/get e .-clientY)}))
            (start-drag [e]
              (js/console.log "DOWN" e)
              (js/document.addEventListener "mouseup"   stop-drag)
              (js/document.addEventListener "mousemove" do-drag))
            (stop-drag []
              (js/console.log "UP")
              (js/document.removeEventListener "mouseup"   stop-drag)
              (js/document.removeEventListener "mousemove" do-drag))]
      ($ MuiBox
         {:backgroundColor backgroundColor
          :width       (:width size)
          :height      (:height size)
          :onMouseDown start-drag
          :onMouseMove do-drag
          :onMouseUp   stop-drag}))))

(def drag-event (atom nil))


(defnc Dragger [{:keys [the-ref direction] :or {direction "row"}}]
  {:helix/features {:check-invalid-hooks-usage true}}
  (let [[coords set-coords] (hooks/use-state {:x 250 :y 250})
        [size set-size]     (hooks/use-state {:width 250 :height 250})
        [dims set-dims]     (hooks/use-state {:width 250 :height 250})
        size-atm            (atom 250)
        cursor              (case (keyword direction) :column "ns-resize" :row "ew-resize")
        resize              (fn [e] (when-let [current (.-current the-ref)]
                                      (when e
                                        (js/console.log "***CALLING set-dims ***")
                                        (set-dims {:width 300 :height 300}))))]
    #_(js/console.log (str "I can log here: "
                           (.-width (.getComputedStyle js/window (-> js/document (.getElementById "the-stack"))))))


    (letfn [(do-drag [e]
              (reset! drag-event e)
              (let [mouse-x (j/get e .-clientX)]
                (reset! size-atm mouse-x)
                (set-size {:width @size-atm})))
            (start-drag [e]
              (js/console.log "DOWN" e)
              (js/document.addEventListener "mouseup"   stop-drag)
              (js/document.addEventListener "mousemove" do-drag))
            (stop-drag []
              (js/console.log "UP")
              (js/document.removeEventListener "mouseup"   stop-drag)
              (js/document.removeEventListener "mousemove" do-drag))]
      (hooks/use-effect
         :always
         [size]
         (js/console.log (str "I run when 'size' changes:" size))
         (resize @drag-event))

      ($ Stack
         {:id "the-stack"
          :direction direction ; "row" or "column"
          :display   "flex" ; I'm guessing; there was no :display except here: (on a box) https://mui.com/material-ui/react-divider/
          :spacing   1}

         ($ MuiBox
             {:id          "box-1"
              :height      250
              :width       (:width dims)
              :onMousedown stop-drag
              :onMouseUp   stop-drag
              :backgroundColor "purple"})

         ($ MuiBox
            {:id "dragger"
             :width  5
             :height 250
             :border "1px dashed grey"
             :cursor "ew-resize", ; ew = east-west? Works on reagent version, not here.
             :onMouseDown start-drag
             :onMouseMove do-drag
             :onMouseUp   stop-drag
             :backgroundColor "black"})

         ($ MuiBox
            {:id          "box-2"
             :height      250
             :width       (:width dims)
             :onMousedown stop-drag
             :onMouseUp   stop-drag
             :backgroundColor "blue"})))))

(defnc app []
  {:helix/features {:check-invalid-hooks-usage true}}
  (let [[state set-state] (hooks/use-state {:name "Helix User"})]
    (d/div
     ($ Typography {:variant         "h3"
                    :color           "white"
                    :backgroundColor "primary.main"
                    :padding         "2px 0 2px 30px"
                    :noWrap  false}
        "RADmapper")
     ;; ToDo: Theme is supposed to wrap the code!
     #_(styles/ThemeProvider (styles/createTheme (clj->js custom-theme)))
     ($ Dragger {:the-ref (hooks/use-ref "dragger-ref")})
     #_($ Resizable {:axis :both
                   :the-ref (hooks/use-ref "the-ref") ; :ref and :key are reserved
                   :backgroundColor "blue"
                   :init-width 100
                   :init-height 100}))))

;; start your app with your favorite React renderer
(defonce root (react-dom/createRoot (js/document.getElementById "app")))

(defn ^{:after-load true, :dev/after-load true} mount []
  (.render root ($ app)))

(defn ^:export init []
  (mount))


