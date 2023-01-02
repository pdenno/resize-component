(ns example.core
  (:require
   ["@mui/material/Typography$default" :as Typography]
   ["@mui/material/TextField$default" :as TextField]
   ["@mui/material/Stack$default" :as Stack]
   ["@mui/material/styles" :as styles]
   ;["@mui/material/CssBaseline" :as CssBaseline]
   ["@mui/material/colors" :as colors]
   [applied-science.js-interop :as j]
   [example.components.share :refer [ShareUpDown ShareLeftRight]]
   [helix.core :refer [defnc $ <>]]
   [helix.dom :as d]
   ["react-dom/client" :as react-dom]))

(def custom-theme
  (styles/createTheme
   (j/lit {#_#_:palette {:primary   colors/yellow
                         :secondary colors/green}
           :components {:MuiDivider
                        {:variants [{:props {:variant "active-vert" } ; vertical divider of horizontal layout
                                     :style {:cursor "ew-resize"
                                             :width 5}}
                                    {:props {:variant "active-horiz" } ; vertical divider of horizontal layout
                                     :style {:cursor "ns-resize"
                                             :height 5}}]}}})))

(defnc form []
  ($ Stack {:direction "column"
            :spacing   0}
     ($ Typography {:variant         "h3"
                    :color           "white"
                    :backgroundColor "primary.main"
                    :padding         "2px 0 2px 30px"
                    :noWrap  false}
        "RADmapper")
     ($ ShareLeftRight
        {:left  ($ TextField {:multiline true :placeholder "share left/right left"})
         :right ($ ShareUpDown
                   {:up   ($ TextField {:multiline true :placeholder "(right) share up/down up"})
                    :down ($ TextField {:multiline true :placeholder "(right) share up/down down"})})})))

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
