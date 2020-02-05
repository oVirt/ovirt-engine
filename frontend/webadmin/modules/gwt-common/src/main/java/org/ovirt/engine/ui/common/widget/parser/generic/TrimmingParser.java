package org.ovirt.engine.ui.common.widget.parser.generic;

import java.text.ParseException;

import com.google.gwt.text.shared.Parser;

public class TrimmingParser<T> implements Parser<T> {

    private Parser<T> delegate;

    public TrimmingParser(Parser<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public T parse(CharSequence input) throws ParseException {
        if (input == null) {
            return delegate.parse(null);
        }
        return delegate.parse(input.toString().trim());
    }

    public static <T> TrimmingParser<T> wrap(Parser<T> parser) {
        return new TrimmingParser(parser);
    }
}
