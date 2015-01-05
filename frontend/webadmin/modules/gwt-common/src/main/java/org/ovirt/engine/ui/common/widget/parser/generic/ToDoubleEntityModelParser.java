package org.ovirt.engine.ui.common.widget.parser.generic;

import java.text.ParseException;

public class ToDoubleEntityModelParser implements com.google.gwt.text.shared.Parser<Double> {
    @Override
    public Double parse(CharSequence text) throws ParseException {
        if (text == null || "".equals(text.toString())) {
            return null;
        }

        Double ret = null;
        try {
            ret = Double.parseDouble(text.toString());
        } catch (NumberFormatException e) {
            throw new ParseException("Unable to parse String to Double", 0); //$NON-NLS-1$
        }

        return ret;
    }
}
