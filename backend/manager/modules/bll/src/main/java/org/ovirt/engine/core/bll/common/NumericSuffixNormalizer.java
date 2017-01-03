package org.ovirt.engine.core.bll.common;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

public class NumericSuffixNormalizer {

    private static final Pattern NUMERIC_SUFFIX_PATTERN = Pattern.compile("^(.*?)(\\d+)$");

    /**
     * Left pads with zeroes the numeric suffixes, so they will have same length and would be properly comparable as
     * strings. A string with no numeric suffix would remain unchanged.
     */
    public List<String> normalize(String... strings) {
        final List<TextWithNumericSuffix> inputAsTextWithNumericSuffix = Arrays.stream(strings)
                .map(this::decompose)
                .collect(Collectors.toList());

        final int maxNumericSuffixLength = findMaxNumericSuffixLength(inputAsTextWithNumericSuffix);

        return inputAsTextWithNumericSuffix.stream()
                .map(e -> e.compose(maxNumericSuffixLength))
                .collect(Collectors.toList());
    }

    private int findMaxNumericSuffixLength(List<TextWithNumericSuffix> inputAsTextWithNumericSuffix) {
        return inputAsTextWithNumericSuffix
                .stream()
                .map(TextWithNumericSuffix::getSuffix)
                .filter(Objects::nonNull)
                .collect(Collectors.summarizingInt(String::length))
                .getMax();
    }

    private TextWithNumericSuffix decompose(String str) {
        if (str == null) {
            return new TextWithNumericSuffix(null);
        } else {
            final Matcher matcher = NUMERIC_SUFFIX_PATTERN.matcher(str);
            if (matcher.matches()) {
                final String prefix = matcher.group(1);
                final String numericSuffix = matcher.group(2);
                return new TextWithNumericSuffix(prefix, numericSuffix);
            } else {
                return new TextWithNumericSuffix(str);
            }
        }
    }

    private static class TextWithNumericSuffix {
        private final String text;
        private final String suffix;

        TextWithNumericSuffix(String text) {
            this(text, null);
        }

        TextWithNumericSuffix(String text, String numericSuffix) {
            this.text = text;
            this.suffix = numericSuffix;
        }

        private String compose(int maxNumericSuffixLength) {
            if (getSuffix() == null) {
                return getText();
            } else {
                return getText() + StringUtils.leftPad(getSuffix(), maxNumericSuffixLength, '0');
            }
        }

        String getText() {
            return text;
        }

        String getSuffix() {
            return suffix;
        }
    }
}
