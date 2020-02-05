package org.ovirt.engine.ui.common.widget.parser.generic;

import java.text.ParseException;

import com.google.gwt.text.shared.Parser;

public class ToIntEntityModelParser implements Parser<Integer> {

    private ToIntEntityModelParser() {
    }

    @Override
    public Integer parse(CharSequence text) throws ParseException {
        if (text == null || "".equals(text.toString())) {
            return null;
        }

        Integer ret = null;
        try {
            ret = Integer.parseInt(text.toString());
        } catch (NumberFormatException e) {
            throw new ParseException("Unable to parse String to Integer", 0); //$NON-NLS-1$
        }

        return ret;
    }

    public static Parser<Integer> newTrimmingParser() {
        return TrimmingParser.wrap(new ToIntEntityModelParser());
    }

}
