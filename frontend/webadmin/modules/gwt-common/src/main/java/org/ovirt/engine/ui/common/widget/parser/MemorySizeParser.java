package org.ovirt.engine.ui.common.widget.parser;

import java.text.ParseException;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.text.shared.Parser;

public class MemorySizeParser implements Parser<Object> {

    @Override
    public Object parse(CharSequence text) throws ParseException {
        MatchResult match = RegExp.compile("(\\d*)\\s*(\\w*)").exec(text.toString()); //$NON-NLS-1$
        String prefix = match.getGroup(1);
        String suffix = match.getGroup(2);
        Integer size = null;

        try {
            size = Integer.parseInt(prefix);
        } catch (NumberFormatException e) {
            return 0;
        }

        if (suffix.equalsIgnoreCase("GB") || suffix.equalsIgnoreCase("G")) {  //$NON-NLS-1$ $NON-NLS-2$
            size *= 1024;
            return size;
        }

        if (suffix.equalsIgnoreCase("MB") || suffix.equalsIgnoreCase("M")) { //$NON-NLS-1$ $NON-NLS-2$
            return Integer.parseInt(prefix);
        }

        return size;
    }

}
