package org.ovirt.engine.ui.common.widget.parser.generic;

import static org.assertj.core.api.Assertions.assertThat;

import java.text.ParseException;

import org.junit.jupiter.api.Test;

import com.google.gwt.text.shared.Parser;

class TrimmingParserTest {

    private Parser<String> dummyParser = new Parser<String>() {
        @Override
        public String parse(CharSequence charSequence) throws ParseException {
            return (String) charSequence;
        }
    };
    private Parser<String> underTest = TrimmingParser.wrap(dummyParser);

    @Test
    void removesWhiteCharsFromBothEnds() throws ParseException {
        assertThat(underTest.parse(" \t qwe rty \t ")).isEqualTo("qwe rty"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    void emptyString() throws ParseException {
        assertThat(underTest.parse("")).isEmpty(); //$NON-NLS-1$
    }

    @Test
    void whiteCharsOnlyString() throws ParseException {
        assertThat(underTest.parse("  \t \t   \n\r ")).isEmpty();//$NON-NLS-1$
    }

    @Test
    void nullPassThrough() throws ParseException {
        assertThat(underTest.parse(null)).isNull();
    }
}
