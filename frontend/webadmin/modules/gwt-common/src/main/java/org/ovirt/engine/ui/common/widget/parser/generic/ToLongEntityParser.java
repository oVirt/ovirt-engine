package org.ovirt.engine.ui.common.widget.parser.generic;

import java.text.ParseException;

import com.google.gwt.text.shared.Parser;

public class ToLongEntityParser implements com.google.gwt.text.shared.Parser<Long> {

    private ToLongEntityParser() {
    }

    @Override
    public Long parse(CharSequence text) throws ParseException {
        if (text == null || "".equals(text.toString())) {
            return null;
        }

        Long ret = null;
        try {
            ret = Long.parseLong(text.toString());
        } catch (NumberFormatException e) {
            throw new ParseException("Unable to parse String to Long", 0); //$NON-NLS-1$
        }

        return ret;
    }

    public static Parser<Long> newTrimmingParser() {
        return TrimmingParser.wrap(new ToLongEntityParser());
    }
}
