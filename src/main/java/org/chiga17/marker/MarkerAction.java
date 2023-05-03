package org.chiga17.marker;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.ui.JBColor;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class MarkerAction extends AnAction {
    public static List<Color> colorList = new ArrayList<>();
    static {
        MyEditorFactoryListener listener = new MyEditorFactoryListener();
        EditorFactory.getInstance().addEditorFactoryListener(listener, listener);
    }

    static {
        colorList.add(new JBColor(new Color(0, 255, 255, 100), new Color(255, 0, 0, 100)));
        colorList.add(new JBColor(new Color(255, 128, 0, 100), new Color(0, 128, 0, 100)));
        colorList.add(new JBColor(new Color(255, 255, 0, 100), new Color(0, 0, 255, 100)));
        colorList.add(new JBColor(new Color(128, 0, 255, 100), new Color(128, 255, 0, 100)));
        colorList.add(new JBColor(new Color(0, 128, 0, 100), new Color(255, 128, 255, 100)));
    }

    private Color color;

    public MarkerAction() {
    }

    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        ActionHandler.getInstance().update(e);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        ActionHandler.getInstance().markActionPerformed(e, color);
    }

    public static class Action1 extends MarkerAction {
        public Action1() { setColor(colorList.get(0)); }
    }
    public static class Action2 extends MarkerAction {
        public Action2() { setColor(colorList.get(1)); }
    }
    public static class Action3 extends MarkerAction {
        public Action3() { setColor(colorList.get(2)); }
    }
    public static class Action4 extends MarkerAction {
        public Action4() { setColor(colorList.get(3)); }
    }
    public static class Action5 extends MarkerAction {
        public Action5() { setColor(colorList.get(4)); }
    }



    public static class ClearAllAction extends AnAction {

        @Override
        public void update(@NotNull AnActionEvent e) {
            ActionHandler.getInstance().update(e);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            ActionHandler.getInstance().clearMarksActionPerformed(e);
        }
    }


    public static class CopyAction extends AnAction {

        @Override
        public void update(@NotNull AnActionEvent e) {
            ActionHandler.getInstance().update(e);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            ActionHandler.getInstance().copyActionPerformed(e);
        }
    }


}
