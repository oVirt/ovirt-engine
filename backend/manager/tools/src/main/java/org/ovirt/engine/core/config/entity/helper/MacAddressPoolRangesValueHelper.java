package org.ovirt.engine.core.config.entity.helper;

import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
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

        String[] ranges = value.split(",");
        for (String range : ranges) {
            String[] rangeParts = range.split("-");
            if (rangeParts.length == 2) {
                String rangeStart = rangeParts[0].toLowerCase();
                String rangeEnd = rangeParts[1].toLowerCase();
                if (!validateRangePart(rangeStart)) {
                    return new ValidationResult(false, "The range start is an invalid MAC address. " + rangeStart
                            + " should be in a format of AA:AA:AA:AA:AA:AA");
                }

                if (!validateRangePart(rangeEnd)) {
                    return new ValidationResult(false, "The range end is an invalid MAC address. " + rangeStart
                            + " should be in a format of AA:AA:AA:AA:AA:AA");
                }

                if (rangeStart.compareTo(rangeEnd) > 0) {
                    return new ValidationResult(false,
                            String.format("The entered range is invalid. %s should be ordered from lower to higer"
                                    + " MAC address. Did you mean %s-%s ?",
                                    range,
                                    rangeEnd,
                                    rangeStart));
                }

                if (!MacAddressRangeUtils.isRangeValid(rangeStart, rangeEnd)) {
                    return new ValidationResult(false,
                            String.format("The entered range is invalid. %s contains no valid MAC addresses.", range));
                }

            } else {
                return new ValidationResult(false, "The entered value is in imporper format. " + value
                        + " should be in a format of AA:AA:AA:AA:AA:AA-BB:BB:BB:BB:BB:BB,...");
            }
        }

        return new ValidationResult(true);
    }

    private boolean validateRangePart(String rangePart) {
        return MAC_ADDRESS_PATTERN.matcher(rangePart).matches();
    }
}
