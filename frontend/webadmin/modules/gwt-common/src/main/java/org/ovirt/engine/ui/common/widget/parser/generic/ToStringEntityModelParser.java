package org.ovirt.engine.ui.common.widget.parser.generic;

import java.text.ParseException;

import com.google.gwt.text.shared.Parser;

public class ToStringEntityModelParser implements Parser<String> {

    @Override
    public String parse(CharSequence text) throws ParseException {
        return text == null ? "" : text.toString();
    }

    public static Parser<String> newTrimmingParser() {
        return TrimmingParser.wrap(new ToStringEntityModelParser());
    }

}
