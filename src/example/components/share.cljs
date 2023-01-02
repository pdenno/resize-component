(ns example.components.share
  (:require
   ["@mui/material/Stack$default" :as Stack]
   ["@mui/material/Divider$default" :as Divider]
   ["@mui/material/Box$default" :as MuiBox]
   [applied-science.js-interop :as j]
   [helix.core :refer [defnc $]]
   [helix.hooks :as hooks]))

(defnc ShareUpDown [{:keys [up down]}]
  {:helix/features {:check-invalid-hooks-usage true}}
  (let [[up-height set-up-height] (hooks/use-state {:size "50%"})
        [dn-height set-dn-height] (hooks/use-state {:size "50%"})
        u-ref (hooks/use-ref nil)
        d-ref (hooks/use-ref nil)
        mouse-down? (atom false)] ; ToDo: Why is this necessary? (It is necessary.)
    (letfn [(do-drag [e]
              (when @mouse-down?
                (when-let [ubox (j/get u-ref :current)]
                  (when-let [dbox (j/get d-ref :current)]
                    (let [ubound (j/get (.getBoundingClientRect ubox) :top)
                          dbound (j/get (.getBoundingClientRect dbox) :bottom)
                          mouse-y (j/get e .-clientY)]
                      (when (<  ubound mouse-y dbound)
                        (let [upct (int (* 100 (- 1.0 (/ (- dbound mouse-y) (- dbound ubound)))))]
                          (set-up-height {:size (str upct "%")})
                          (set-dn-height {:size (str (- 100 upct) "%")}))))))))
            (start-drag [_e]
              (reset! mouse-down? true)
              (js/document.addEventListener "mouseup"   stop-drag)
              (js/document.addEventListener "mousemove" do-drag))
            (stop-drag []
              (reset! mouse-down? false)
              (js/document.removeEventListener "mouseup"   stop-drag)
              (js/document.removeEventListener "mousemove" do-drag))]
      ($ Stack
         {:direction "column"
          :display   "flex"
          :height    "100%"
          :width     "100%"
          :spacing   0
          :divider ($ Divider {:variant "active-horiz"
                               :height 5
                               :onMouseDown start-drag
                               :onMouseMove do-drag
                               :onMouseUp   stop-drag
                               :color "black"})}
         ($ MuiBox {:ref u-ref :height (:size up-height)} up)
         ($ MuiBox {:ref d-ref :height (:size dn-height)} down)))))

(defnc ShareLeftRight [{:keys [left right height] :or {height 300}}]
  {:helix/features {:check-invalid-hooks-usage true}}
  (let [[lwidth set-lwidth] (hooks/use-state {:size "50%"})
        [rwidth set-rwidth] (hooks/use-state {:size "50%"})
        l-ref (hooks/use-ref nil)
        r-ref (hooks/use-ref nil)
        mouse-down? (atom false)]
    (letfn [(do-drag [e]
              (when @mouse-down?
                (when-let [rbox (j/get r-ref :current)]
                  (when-let [lbox (j/get l-ref :current)]
                    (let [lbound (j/get (.getBoundingClientRect lbox) :left)
                          rbound (j/get (.getBoundingClientRect rbox) :right)
                          mouse-x (j/get e .-clientX)]
                      (when (<  lbound mouse-x rbound)
                        (let [lpct (int (* 100 (/ (- mouse-x lbound) (- rbound lbound))))]
                          (set-lwidth {:size (str lpct "%")})
                          (set-rwidth {:size (str (- 100 lpct) "%")}))))))))
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
          :height    height
          :width     "100%"
          :spacing   0
          :divider ($ Divider {:variant "active-vert"
                               :width 5
                               :onMouseDown start-drag
                               :onMouseMove do-drag
                               :onMouseUp   stop-drag
                               :color "black"})}
         ($ MuiBox {:ref l-ref :width (:size lwidth)} left)
         ($ MuiBox {:ref r-ref :width (:size rwidth)} right)))))
