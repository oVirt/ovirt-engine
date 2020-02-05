package org.ovirt.engine.ui.common.widget.parser.generic;

import java.text.ParseException;

import com.google.gwt.text.shared.Parser;

public class ToShortEntityModelParser implements com.google.gwt.text.shared.Parser<Short> {

    private ToShortEntityModelParser() {
    }

    @Override
    public Short parse(CharSequence text) throws ParseException {
        if (text == null || "".equals(text.toString())) {
            return null;
        }

        Short ret = null;
        try {
            ret = Short.parseShort(text.toString());
        } catch (NumberFormatException e) {
            throw new ParseException("Unable to parse String to Short", 0); //$NON-NLS-1$
        }

        return ret;
    }

    public static Parser<Short> newTrimmingParser() {
        return TrimmingParser.wrap(new ToShortEntityModelParser());
    }
}
