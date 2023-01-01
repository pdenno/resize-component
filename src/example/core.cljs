(ns example.core
  (:require
   ["@mui/material/Typography$default" :as Typography]
   ["@mui/material/TextField$default" :as TextField]
   ["@mui/material/Stack$default" :as Stack]
   ["@mui/material/Divider$default" :as Divider]
   ["@mui/material/Box$default" :as MuiBox]
   ["@mui/material/styles" :as styles]
   ;["@mui/material/CssBaseline" :as CssBaseline]
   ["@mui/material/colors" :as colors]
   [applied-science.js-interop :as j]
   [helix.core :refer [defnc $ <>]]
   [helix.hooks :as hooks]
   [helix.dom :as d]
   ["react-dom/client" :as react-dom]))

(def custom-theme
  (styles/createTheme
   (j/lit {#_#_:palette {:primary   colors/yellow
                         :secondary colors/green}
           :components {:MuiDivider
                        {:variants [{:props {:variant "active" }
                                     :style {:cursor "ew-resize"}}]}}})))

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
          :spacing   0
          :divider ($ Divider {:variant "active"
                               :width 5
                               :onMouseDown start-drag
                               :onMouseMove do-drag
                               :onMouseUp   stop-drag
                               :color "black"})}
         ($ MuiBox {:width (:size lwidth) :backgroundColor "purple"} left)
         ($ MuiBox {:width (:size rwidth) :backgroundColor "blue"} right)))))

(defnc form []
  ($ Stack {:direction "column"
            :spacing   0}
     ($ Typography {:variant         "h3"
                    :color           "white"
                    :backgroundColor "primary.main"
                    :padding         "2px 0 2px 30px"
                    :noWrap  false}
        "RADmapper")
     ($ LeftRightShare
        {:left  ($ TextField {:multiline true :placeholder "true left"})
         :right ($ TextField {:multiline true :placeholder "true right"})})))

(defnc app []
  {:helix/features {:check-invalid-hooks-usage true}}
  (<> ; https://reactjs.org/docs/react-api.html#reactfragment
   ;(CssBaseline) ; ToDo: Investigate purpose of CssBaseline.
   ($ styles/ThemeProvider
      {:theme custom-theme}
      ($ form))))

(defonce root (react-dom/createRoot (js/document.getElementById "app")))

(defn ^{:after-load true, :dev/after-load true} mount []
  (.render root ($ app)))

(defn ^:export init []
  (mount))
