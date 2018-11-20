package org.ovirt.checkstyle.checks;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;

public class NlsCheck extends AbstractCheck {
    private static final Pattern patternString = Pattern.compile("[\"][^\"]*[^\\\\][\"]");

    private boolean run = false;

    @Override
    public int[] getAcceptableTokens() {
        return getDefaultTokens();
    }

    @Override
    public int[] getDefaultTokens() {
        return getRequiredTokens();
    }

    @Override
    public int[] getRequiredTokens() {
        return new int[0];
    }

    @Override
    public void beginTree(DetailAST aRootAST) {
        if (!run){
            return;
        }
        if (!getFileContents().getFileName().matches(".*\\.java")) {
            return;
        }

        int i = 0;
        boolean startAnnotation = false;
        for (String lineText : getLines()) {
            ++i;

            // Ignore strings passed as annotation parameters
            if (lineText.matches("^\\s*@.*")) {
                int parenthesesStart = lineText.indexOf("(");

                while (parenthesesStart != -1 && getFileContents().hasIntersectionWithComment(i,
                        parenthesesStart, i, parenthesesStart)){
                    parenthesesStart = lineText.indexOf("(", parenthesesStart + 1);
                }

                if (parenthesesStart != -1){
                    startAnnotation = true;
                }
            }

            List<QuotedString> stringList = new LinkedList<>();
            Matcher matcher = patternString.matcher(lineText);
            while (matcher.find()) {

                if (getFileContents().hasIntersectionWithComment(i,
                        matcher.start(), i, matcher.end())) {
                    continue;
                }

                stringList.add(new QuotedString(matcher.group(), matcher
                        .start(), matcher.end()));
            }

            if (startAnnotation) {

                int endAnno = hasEndAnno(lineText, i, stringList);
                if (endAnno != -1) {
                    startAnnotation = false;

                    // maybe the rest of the line has quoted string
                    List<QuotedString> tmpStringList = new LinkedList<>();
                    for (QuotedString quotedString : stringList) {
                        if (quotedString.startIndex > endAnno) {
                            tmpStringList.add(quotedString);
                        }
                    }
                    stringList = tmpStringList;
                } else {
                    continue;
                }

            }

            int j = 0;
            for (QuotedString str : stringList) {
                ++j;
                Pattern patternNls = Pattern.compile("\\$NON-NLS-" + j + "\\$");
                Matcher matcherNls = patternNls.matcher(lineText);

                int matchNum = 0;
                while (matcherNls.find()) {
                    ++matchNum;
                }
                if (matchNum != 1) {
                    log(i, "String on line " + i + " (at index " + str.startIndex + ") is non-localized.\n" +
                            "Please localize it via Constants/Messages interface " +
                            "or use //$NON-NLS-" + j + "$" + " comment to indicate that it shouldn''t be localized.");
                }
            }
        }
    }

    int hasEndAnno(String lineText, int lineNum,
            List<QuotedString> quotedStrings) {
        int startIndex = lineText.indexOf(")");
        while (startIndex != -1) {
            boolean isValidEnd = true;
            for (QuotedString quoted : quotedStrings) {
                if (startIndex >= quoted.startIndex
                        && startIndex <= quoted.endIndex) {
                    isValidEnd = false;
                    break;
                }
            }

            if (getFileContents().hasIntersectionWithComment(lineNum,
                    startIndex, lineNum, startIndex)) {
                isValidEnd = false;
            }

            if (isValidEnd) {
                return startIndex;
            }
            startIndex = lineText.indexOf(")", startIndex + 1);
        }
        return -1;
    }

    private static class QuotedString {
        String text;
        int startIndex;
        int endIndex;

        public QuotedString(String text, int startIndex, int endIndex) {
            this.text = text;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }

        @Override
        public String toString() {
            return String.format("%s [%d:%d]", text, startIndex, endIndex);
        }
    }

    public void setRun(boolean run) {
        this.run = run;
    }

}
