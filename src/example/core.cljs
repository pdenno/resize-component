(ns example.core
  (:require
   ["@mui/material/Typography$default" :as Typography]
   ["@mui/material/TextField$default" :as TextField]
   ["@mui/material/Stack$default" :as Stack]
   ["@mui/material/Divider$default" :as Divider]
   ["@mui/material/Box$default" :as MuiBox]
   ["@mui/material/styles" :as styles]
   ["@mui/material/CssBaseline" :as CssBaseline]
   ["@mui/material/colors" :as colors]
   [applied-science.js-interop :as j]
   [helix.core :refer [defnc $ <>]]
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
                :partition  (str prefix "-box")
                :drawer     (str prefix "-drawer")}))

(defn custom-styles [{:keys [theme]}]
  (let [spacing (:spacing theme)]
    {(str "&." (:root classes))        {:margin-left (spacing 0) ; was 8
                                        :align-items :flex-start}
     (str "& ." (:button classes))     {:margin (spacing 1)}
     (str "& ." (:text-field classes)) {:width        500 ; "80%" ;200
                                        :margin-left  (spacing 1)
                                        :margin-right (spacing 1)}
     (str "& ." (:partition classes))  {:width "10px",
                                        :cursor "ew-resize",      ; <======================== Probably why cursor worked with older stuff!
                                        :backgroundColor "black"}})) ; (Cursor isn't

(def boxx (styles/styled "MuiBox" {"&:hover" {:cursor "ew-resize"}}))

;;; ToDo:
;;; (1) Get cursor working. (May require makeStyle; see https://github.com/mui/material-ui/issues/19983)
;;; (2) Add left and right args.
(defnc LeftRightShare [{:keys [left right]}]
  {:helix/features {:check-invalid-hooks-usage true}}
  (let [parent-width  (.-innerWidth  js/window)
        parent-height (.-innerHeight js/window)
        [lwidth set-lwidth] (hooks/use-state {:size "50%"})
        [rwidth set-rwidth] (hooks/use-state {:size "50%"})
        mouse-down? (atom false)] ; ToDo: Why is this necessary? (It is necessary.)
    (letfn [(do-drag [e]
              (when @mouse-down?
                (let [mouse-x (j/get e .-clientX)]
                  (set-lwidth {:size mouse-x})
                  (set-rwidth {:size (- parent-width mouse-x)}))))
            (start-drag [_e]
              (reset! mouse-down? true)
              (js/document.addEventListener "mouseup"   stop-drag)
              (js/document.addEventListener "mousemove" do-drag))
            (stop-drag []
              (reset! mouse-down? false)
              (js/document.removeEventListener "mouseup"   stop-drag)
              (js/document.removeEventListener "mousemove" do-drag))]
      ($ Stack
         {:direction "row"
          :display   "flex"
          :height    parent-height ; Use of mouse-down? helped here?!?
          :width     "100%" ; This (e.g. 300) can be used to limit width.
          :divider ($ Divider ; #js {"&:hover:not(.Mui-disabled)" #js {:cursor "ew-resize"}}
                      {:width 50
                       :height 50
                       :cursor "ew-resize"})
          :spacing   0}
         ($ MuiBox {:width (:size lwidth) :backgroundColor "purple"} left)
         ($ MuiBox
            {:width  5
             :cursor "ew-resize", ; Not supported? https://github.com/mui/material-ui/issues/19983
             :onMouseDown start-drag
             :onMouseMove do-drag
             :onMouseUp   stop-drag
             :backgroundColor "black"})
         ($ MuiBox {:width (:size rwidth) :backgroundColor "blue"} right)))))


;(def form (styles/styled form* custom-styles))

(defnc app []
  {:helix/features {:check-invalid-hooks-usage true}}
  (<> ; https://reactjs.org/docs/react-api.html#reactfragment
   #_(CssBaseline)
   (d/div
    ($ Typography {:variant         "h3"
                   :color           "white"
                   :backgroundColor "primary.main"
                   :padding         "2px 0 2px 30px"
                   :noWrap  false}
       "RADmapper")
    ;; ToDo: Theme is supposed to wrap the code!
;    ($ styles/ThemeProvider
;       (j/obj :theme ($ styles/createTheme (clj->js custom-theme)))
;       ($ styles/styled
        ($ LeftRightShare
           {:left  ($ TextField {:multiline true :placeholder "true left"})
            :right ($ TextField {:multiline true :placeholder "true right"})})
        #_(clj->js custom-styles))))

;; start your app with your favorite React renderer
(defonce root (react-dom/createRoot (js/document.getElementById "app")))

(defn ^{:after-load true, :dev/after-load true} mount []
  (.render root ($ app)))

(defn ^:export init []
  (mount))
