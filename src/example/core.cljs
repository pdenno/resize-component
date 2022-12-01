(ns example.core
  (:require
   ["@mui/material/Typography$default" :as Typography]
   ["@mui/material/TextField$default" :as TextField]
   ["@mui/material/Stack$default" :as Stack]
   ["@mui/material/Divider$default" :as Divider]
   ["@mui/material/Box$default" :as MuiBox]
   ["@mui/material/styles" :as styles]
   ["@mui/material/CssBaseline" :refer [css-baseline]]
   ["@mui/material/colors" :refer [colors]]
   #_["@mui/material/colors/purple" :refer [purple]]
   ;["@mui/x-date-pickers/LocalizationProvider" :refer [localization-provider]]

   #_["@mui/icons-material/ArrowDownward" :as AEH]

   [helix.core :refer [defnc $]]
   [helix.hooks :as hooks]
   [helix.dom :as d]
   ["react-dom/client" :as react-dom]))

#_(def custom-theme
  {:palette {:primary   colors/purple
             :secondary colors/green}})

(def custom-theme {})

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

;;; https://github.com/lilactown/helix/blob/master/docs/creating-elements.md#-macro
;;; The $ macro takes a component type (string, keyword, or symbol referring to a Component), optionally some props,
;;; and any children, and returns a React Element with that same information, like React.createElement.

;;; This declares a new state variable. It is something you'd do with React hooks.
;;; https://reactjs.org/docs/hooks-intro.html
;;; const [drawerWidth, setDrawerWidth] = React.useState(defaultDrawerWidth);

;;;  const handleMouseMove = useCallback(e => {
;;;    const newWidth = e.clientX - document.body.offsetLeft;
;;;    if (newWidth > minDrawerWidth && newWidth < maxDrawerWidth) {
;;;      setDrawerWidth(newWidth); }
(defonce text-state         (atom "Initial Text"))
(defonce horiz-drag-pos     (atom nil))
(defn event-value [e] (.. e -target -value))

(defnc Dragger []
  (let [[position set-position] (hooks/use-state {:pos 500})]
    (letfn [(handle-mouse-move [e]
              (let [mouse-x (. e -clientX)]
                (reset! horiz-drag-pos mouse-x)
                (js/console.log (str "x = " mouse-x))))
            (handle-mouse-up []
              (js/console.log "UP")
              (js/document.removeEventListener "mouseup"   handle-mouse-up)
              (js/document.removeEventListener "mousemove" handle-mouse-move))
            (handle-mouse-down [e]
              (js/console.log "DOWN" e)
              (js/document.addEventListener "mouseup"   handle-mouse-up)
              (js/document.addEventListener "mousemove" handle-mouse-move))]
    ($ Stack
       {:direction "row"
        :display   "flex" ; I'm guessing; there was no :display except here: (on a box) https://mui.com/material-ui/react-divider/
        :spacing   2}

    ($ TextField
       {:id          "data-editor"
        :width       "fit-content"
        :value       @text-state
        :label       "Data"
        :placeholder "Placeholder"
        :multiline   true
        :rows        8})

     ($ MuiBox
        {:id "a-box"
         :width  5
         :height 250
         :border "1px dashed grey"
         :cursor "ew-resize", ; ew = east-west?
         :onMouseDown handle-mouse-down
         :onMouseUp   handle-mouse-up
         :onMouseMove handle-mouse-move
         :backgroundColor "black",
         :&:hover {:backgroundColor "primary.main", ; This from the MUI doc example, https://mui.com/material-ui/react-box/
                   :opacity [0.9, 0.8, 0.7]}})

     ($ TextField
        {:id          "rm-editor"
         :width       "100%" ;"500px" ; ignored.
         :value       @text-state
         :label       "Editor"
         :placeholder "Placeholder"
         :multiline   true
         :rows        8})))))

(defnc app []
  (let [[state set-state] (hooks/use-state {:name "Helix User"})]
    (d/div
     ($ Typography {:variant "h3"
                    :color "white"
                    :backgroundColor "primary.main"
                    :padding "2px 0 2px 30px"
                    :noWrap  false}
        "RADmapper")
     #_($ styles/ThemeProvider
        (styles/createTheme (clj->js custom-theme))
        ($ Box {:id "box2"
                :width  "100px"
                :height "100px"
                :backgroundcolor "black"
                :backgroundColor "primary.dark"}))
     ;; create elements out of components
     ($ Dragger))))

;; start your app with your favorite React renderer
(defonce root (react-dom/createRoot (js/document.getElementById "app")))

(defn ^{:after-load true, :dev/after-load true} mount []
  (.render root ($ app)))

(defn ^:export init []
  (mount))
