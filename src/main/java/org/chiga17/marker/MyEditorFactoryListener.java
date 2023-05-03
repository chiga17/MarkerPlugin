package org.chiga17.marker;

import static com.intellij.openapi.editor.markup.HighlighterLayer.SELECTION;
import static com.intellij.openapi.editor.markup.HighlighterTargetArea.EXACT_RANGE;
import static com.intellij.psi.PlainTextTokenTypes.PLAIN_TEXT_FILE;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.event.EditorFactoryListener;
import com.intellij.openapi.editor.event.SelectionEvent;
import com.intellij.openapi.editor.event.SelectionListener;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.ui.JBColor;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public class MyEditorFactoryListener implements EditorFactoryListener, Disposable {

    static Map<Editor, List<RangeHighlighter>> rangeHighlightersMap = new HashMap<>();

    @Override
    public void dispose() {
    }

    public static class MySelectionListener implements SelectionListener {

        @Override
        public void selectionChanged(@NotNull SelectionEvent e) {
            if (e.getOldRange().equals(e.getNewRange())) {
                return;
            }

            Editor editor = e.getEditor();
            List<RangeHighlighter> rangeHighlighters = rangeHighlightersMap.get(editor);
            if (!rangeHighlighters.isEmpty()) {
                for (RangeHighlighter rangeHighlighter: rangeHighlighters) {
                    editor.getMarkupModel().removeHighlighter(rangeHighlighter);
                }
                rangeHighlighters.clear();
            }

            SelectionModel selectionModel = editor.getSelectionModel();
            String selectedText = selectionModel.getSelectedText();
            if (StringUtil.isEmpty(selectedText)) {
                return;
            }
            boolean wholeWord = isWholeWord(editor.getDocument().getText(), selectionModel.getSelectionStart(), selectionModel.getSelectionEnd());
            if (!wholeWord) {
                return;
            }

            List<Integer> starts = Utils.findMatches(editor.getDocument().getText(), selectedText);
            TextAttributes textAttributes = new TextAttributes();
            textAttributes.setBackgroundColor(new JBColor(new Color(0, 255, 0, 100), new Color(255, 0, 255, 100)));

            int length = selectedText.length();
            rangeHighlighters.clear();
            for (Integer start: starts) {
                rangeHighlighters.add(
                    editor.getMarkupModel().addRangeHighlighter(start, start+length, SELECTION-1, textAttributes, EXACT_RANGE));
            }
        }
    }

    @Override
    public void editorCreated(@NotNull EditorFactoryEvent event) {
        Editor editor = event.getEditor();
        Project project = editor.getProject();
        PsiFile psiFile;
        if (project != null) {
            psiFile = PsiDocumentManager.getInstance(editor.getProject()).getPsiFile(editor.getDocument());
            if ( (psiFile != null) && (!Objects.equals(psiFile.getFileElementType(), PLAIN_TEXT_FILE)) ) {
                System.out.println("Skip adding listener to file: " + psiFile.getName());
                return;
            }
        }
        editor.getSelectionModel().addSelectionListener(new MySelectionListener());
        rangeHighlightersMap.put(editor, new ArrayList<>());
    }

    @Override
    public void editorReleased(@NotNull EditorFactoryEvent event) {
        Editor editor = event.getEditor();
        if (rangeHighlightersMap.get(editor) != null) {
            rangeHighlightersMap.get(editor).clear();
            rangeHighlightersMap.remove(editor);
        }
    }

    private static boolean isWholeWord(CharSequence text, int startOffset, int endOffset) {
        boolean isWordStart;

        if (startOffset != 0) {
            boolean previousCharacterIsIdentifier = Character.isJavaIdentifierPart(text.charAt(startOffset - 1)) &&
                (startOffset <= 1 || text.charAt(startOffset - 2) != '\\');
            boolean previousCharacterIsSameAsNext = text.charAt(startOffset - 1) == text.charAt(startOffset);

            boolean firstCharacterIsIdentifier = Character.isJavaIdentifierPart(text.charAt(startOffset));
            isWordStart = firstCharacterIsIdentifier ? !previousCharacterIsIdentifier : !previousCharacterIsSameAsNext;
        }
        else {
            isWordStart = true;
        }

        boolean isWordEnd;

        if (endOffset != text.length()) {
            boolean nextCharacterIsIdentifier = Character.isJavaIdentifierPart(text.charAt(endOffset));
            boolean nextCharacterIsSameAsPrevious = endOffset > 0 && text.charAt(endOffset) == text.charAt(endOffset - 1);
            boolean lastSearchedCharacterIsIdentifier = endOffset > 0 && Character.isJavaIdentifierPart(text.charAt(endOffset - 1));

            isWordEnd = lastSearchedCharacterIsIdentifier ? !nextCharacterIsIdentifier : !nextCharacterIsSameAsPrevious;
        }
        else {
            isWordEnd = true;
        }

        return isWordStart && isWordEnd;
    }
}
