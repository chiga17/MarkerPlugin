package org.chiga17.marker;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import java.awt.Color;
import java.awt.datatransfer.StringSelection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public class ActionHandler {

    private static final ActionHandler actionHandler = new ActionHandler();
    ExecutorService executorService = Executors.newSingleThreadExecutor();

    public static ActionHandler getInstance() {
        return actionHandler;
    }

    public void update(AnActionEvent e) {
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        final Presentation presentation = e.getPresentation();
        presentation.setEnabled(e.getProject() != null && editor != null);
    }

    public void markActionPerformed(AnActionEvent e, Color color) {
        final Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);

        String selectedText = editor.getSelectionModel().getSelectedText();
        if (StringUtil.isEmpty(selectedText)) {
            removeMarkersGroup(editor, color);
            return;
        }
        addMarkers(editor, selectedText, color);
    }

    public void clearMarksActionPerformed(AnActionEvent e) {
        final Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
        ApplicationManager.getApplication().invokeLater(()->removeMarkers(editor));
    }

    public void copyActionPerformed(AnActionEvent e) {
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor == null) {
            return;
        }

        StringBuilder marked = new StringBuilder();
        List<RangeHighlighter> rangeHighlighters = Utils.getMarkerData(editor);
        List<Integer> lineNumbers = rangeHighlighters.stream()
            .map(x -> editor.getDocument().getLineNumber(x.getStartOffset())).distinct().sorted().collect(Collectors.toList());
        int length = editor.getDocument().getTextLength();
        int docLines = editor.getDocument().getLineCount();
        for (Integer lineNumber: lineNumbers) {
            int startOffset = editor.getDocument().getLineStartOffset(lineNumber);
            int endOffset = (lineNumber+1 < docLines)? editor.getDocument().getLineStartOffset(lineNumber+1): length;
            String line = editor.getDocument().getText(new TextRange(startOffset, Math.min(endOffset, length)));
            marked.append(line);
        }

        StringSelection selection = new StringSelection(marked.toString());
        CopyPasteManager.getInstance().setContents(selection);
    }





    private void addMarkers(@NotNull Editor editor, String text, Color color) {
        List<Integer> matches = Utils.findMatches(editor.getDocument().getCharsSequence(), text);
        TextAttributes textAttributes = new TextAttributes();
        textAttributes.setBackgroundColor(color);
        for (Integer match : matches) {
            RangeHighlighter highlighter = editor.getMarkupModel().addRangeHighlighter(
                match, match + text.length(), HighlighterLayer.SELECTION - 1, textAttributes, HighlighterTargetArea.EXACT_RANGE);
            highlighter.setErrorStripeMarkColor(color.darker());
            Utils.getMarkerData(editor).add(highlighter);
        }
    }

    private void removeMarkers(Editor editor) {
        List<RangeHighlighter> highlighters = Utils.getMarkerData(editor);
        for (RangeHighlighter highlighter : highlighters) {
            editor.getMarkupModel().removeHighlighter(highlighter);
        }
        highlighters.clear();
    }

    private void removeMarkersGroup(Editor editor, Color color) {
        List<RangeHighlighter> highlighters = Utils.getMarkerData(editor);
        for (RangeHighlighter highlighter : highlighters) {
            TextAttributes ta = highlighter.getTextAttributes();
            if (ta == null) {
                continue;
            }
            if (ta.getBackgroundColor().equals(color)) {
                editor.getMarkupModel().removeHighlighter(highlighter);
            }
        }
    }

}
