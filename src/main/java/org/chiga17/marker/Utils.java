package org.chiga17.marker;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.util.Key;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

public class Utils {

    public static Key<List<RangeHighlighter>> MARKER_KEY = Key.create("MARKER_KEY");

    public static ArrayList<Integer> findMatches(CharSequence text, String selectedText) {
        ArrayList<Integer> matches = new ArrayList<>();
        Matcher m = Pattern.compile(Pattern.quote(selectedText)).matcher(text);
        while (m.find()) {
            matches.add(m.start());
        }
        return matches;
    }

    @NotNull
    public static List<RangeHighlighter> getMarkerData(@NotNull Editor editor) {
        List<RangeHighlighter> rangeHighlighters = editor.getUserData(MARKER_KEY);
        if (rangeHighlighters == null) {
            rangeHighlighters = new ArrayList<>();
            editor.putUserData(MARKER_KEY, rangeHighlighters);
        }
        return rangeHighlighters;
    }

}
