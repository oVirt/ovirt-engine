package org.ovirt.engine.ui.common.widget.parser.generic;

import com.google.gwt.text.shared.Parser;
import java.text.ParseException;

public class ToStringEntityModelParser implements Parser<String> {

    @Override
    public String parse(CharSequence text) throws ParseException {
        return text == null ? "" : text.toString();
    }

}
