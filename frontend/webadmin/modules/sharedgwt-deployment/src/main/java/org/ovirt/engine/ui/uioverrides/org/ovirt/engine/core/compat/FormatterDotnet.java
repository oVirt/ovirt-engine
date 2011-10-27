package org.ovirt.engine.core.compat;

import java.io.IOException;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

/**
 * A String Formatter for DotNet string.Format() patterns
 */
public class FormatterDotnet {

    private static final String STR_ARG_FORMAT = "\\{(\\d)\\}";

    // Creating a regex for '{0} {1} ... ' pattern.
    private static final RegExp REGEXP = RegExp.compile(STR_ARG_FORMAT, "g");

    private Appendable a;

    public FormatterDotnet() {
        init(new StringBuilder());
    }

    public FormatterDotnet(Appendable a) {
        if (a == null) {
            a = new StringBuilder();
        }
        init(a);
    }

    public FormatterDotnet format(String format, Object... args) {
        try {
            a.append(getFormattedString(format, args));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    };

    public Appendable out() {
        return a;
    }

    @Override
    public String toString() {
        return a.toString();
    }

    private String getFormattedString(String pattern, Object... args) {

        // Replacing specified arguments according to the indexes in the pattern.
        MatchResult mr = REGEXP.exec(pattern);

        String formattedStr = pattern;

        while (mr != null) {
            String holder = mr.getGroup(0);
            String indexStr = mr.getGroup(1);
            int argIndex = Integer.parseInt(indexStr);

            // Replacing the current match with the corresponding argument.
            Object arg = args[argIndex];
            formattedStr = formattedStr.replace(holder, String.valueOf(arg));

            // Look up for another match.
            mr = REGEXP.exec(pattern);
        }

        return formattedStr;
    }

    // Initialize internal data.
    private void init(Appendable a) {
        this.a = a;
    }
}
