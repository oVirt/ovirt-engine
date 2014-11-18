package org.ovirt.engine.core.config.entity.helper;

import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.LongRange;
import org.ovirt.engine.core.config.entity.ConfigKey;
import org.ovirt.engine.core.utils.MacAddressRangeUtils;

/**
 * The class verifies the provided MAC address ranges to set the values of MAC addresses pool is defined properly. The
 * expected format is:
 *
 * <pre>
 * AA:AA:AA:AA:AA:AA-BB:BB:BB:BB:BB:BB,CC:CC:CC:CC:CC:CC-DD:DD:DD:DD:DD:DD,...
 * </pre>
 */
public class MacAddressPoolRangesValueHelper extends StringValueHelper {

    private static final String VALID_MAC_ADDRESS_FORMAT = "(\\p{XDigit}{2}:){5}\\p{XDigit}{2}";
    private static final Pattern MAC_ADDRESS_PATTERN = Pattern.compile(VALID_MAC_ADDRESS_FORMAT);

    @Override
    public ValidationResult validate(ConfigKey key, String value) {

        if (StringUtils.isBlank(value)) {
            return new ValidationResult(false, "The MAC address range cannot be empty.");
        }

        try {
            List<String[]> rangesBoundaries = MacAddressRangeUtils.rangeStringToStringBoundaries(value);
            ValidationResult rangeValidation = validateRangesSyntax(rangesBoundaries);
            if (!rangeValidation.isOk()) {
                return rangeValidation;
            }

            if (rangesAreEmpty(rangesBoundaries)) {
                return new ValidationResult(false,
                        String.format("The entered ranges is invalid. %s contains no valid MAC addresses.", value));
            } else {
                return new ValidationResult(true);
            }
        } catch (IllegalArgumentException e) {
            return new ValidationResult(false, "The entered value is in improper format. " + value
                    + " should be in a format of AA:AA:AA:AA:AA:AA-BB:BB:BB:BB:BB:BB,...");
        }
    }

    public static boolean rangesAreEmpty(List<String[]> rangesBoundaries) {
        Collection<LongRange> ranges = MacAddressRangeUtils.parseRangeStringBoundaries(rangesBoundaries);

        if (ranges.isEmpty()) {
            return true;
        }

        for (LongRange range : ranges) {
            if (range.getMaximumLong() - range.getMinimumLong() >= 0) {
                return false;
            }
        }

        return true;
    }

    private ValidationResult validateRangesSyntax(List<String[]> rangesBoundaries) {
        for (String[] rangeBoundaries : rangesBoundaries) {
            for (String rangeBoundary : rangeBoundaries) {
                ValidationResult startPartValidation = validateRangePart(rangeBoundary);
                if (!startPartValidation.isOk()) {
                    return startPartValidation;
                }
            }

            long from = MacAddressRangeUtils.macToLong(rangeBoundaries[0]);
            long to = MacAddressRangeUtils.macToLong(rangeBoundaries[1]);

            if (from > to) {
                return new ValidationResult(false,
                        String.format("The entered range is invalid. "
                                + "Range should be specified as "
                                + "<lowerBoundMAC>"+ MacAddressRangeUtils.BOUNDARIES_DELIMITER +"<upperBoundMAC>."
                                + " Did you mean %s?",
                                MacAddressRangeUtils.boundariesToRangeString(to, from)));

            }
        }
        return new ValidationResult(true);
    }

    private ValidationResult validateRangePart(String rangePart) {
        boolean matches = MAC_ADDRESS_PATTERN.matcher(rangePart).matches();
        if (!matches) {
            return new ValidationResult(false, "The range start/end is an invalid MAC address. " + rangePart
                    + " should be in a format of AA:AA:AA:AA:AA:AA");
        } else {
            return new ValidationResult(true);
        }
    }
}
