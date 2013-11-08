package org.ovirt.engine.ui.common.widget.parser.generic;

import com.google.gwt.text.shared.Parser;
import java.text.ParseException;

public class ToIntEntityModelParser implements Parser<Integer> {

    @Override
    public Integer parse(CharSequence text) throws ParseException {
        if (text == null || "".equals(text.toString())) {
            return null;
        }

        Integer ret = null;
        try {
            ret = Integer.parseInt(text.toString());
        } catch (NumberFormatException e) {}

        return ret;
    }

}
