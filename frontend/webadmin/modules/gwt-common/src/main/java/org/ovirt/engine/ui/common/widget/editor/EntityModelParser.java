package org.ovirt.engine.ui.common.widget.editor;

import java.text.ParseException;

import com.google.gwt.text.shared.Parser;

public class EntityModelParser implements Parser<Object> {

    @Override
    public Object parse(CharSequence text) throws ParseException {
        return text;
    }

}
