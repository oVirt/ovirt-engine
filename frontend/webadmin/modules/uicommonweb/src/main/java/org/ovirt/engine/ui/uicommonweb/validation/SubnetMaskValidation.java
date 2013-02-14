package org.ovirt.engine.ui.uicommonweb.validation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.ui.uicompat.ConstantsManager;

@SuppressWarnings("unused")
public class SubnetMaskValidation implements IValidation
{
    private final static IpAddressValidation IP_VALIDATOR = new IpAddressValidation();
    private final static Set<Integer> CORRECT_RANGE = new HashSet<Integer>();
    private final static List<String> reasons = new ArrayList<String>();

    static {
        CORRECT_RANGE.add(128);
        CORRECT_RANGE.add(192);
        CORRECT_RANGE.add(224);
        CORRECT_RANGE.add(240);
        CORRECT_RANGE.add(248);
        CORRECT_RANGE.add(252);
        CORRECT_RANGE.add(254);
        CORRECT_RANGE.add(255);
        CORRECT_RANGE.add(0);

        reasons.add(ConstantsManager.getInstance().getConstants().subnetMaskIsNotValid());
    }

    @Override
    public ValidationResult validate(Object value) {
        ValidationResult ipValidation = IP_VALIDATOR.validate(value);
        if (!ipValidation.getSuccess()) {
            return ipValidation;
        }

        ValidationResult result = new ValidationResult();
        if (value != null && value instanceof String) {
            result.setSuccess(validateNetMask((String) value));
            result.setReasons(reasons);
        }

        return result;
    }

    private boolean validateNetMask(String mask) {
        // values[0] can be 128, 192, 224, 240, 248, 252, 254, 255
        // values[1] can be 128, 192, 224, 240, 248, 252, 254, 255 if values[0] is 255, else values[1] must be 0
        // values[2] can be 128, 192, 224, 240, 248, 252, 254, 255 if values[1] is 255, else values[2] must be 0
        // values[3] can be 128, 192, 224, 240, 248, 252, 254, 255 if values[2] is 255, else values[3] must be 0

        String[] split = mask.split("\\."); //$NON-NLS-1$
        assert split.length == 4;
        int[] values = new int[split.length];

        for (int i = 0; i < split.length; i++) {
            int value;
            try {
                value = Integer.valueOf(split[i]);
            } catch (NumberFormatException e) {
                return false;
            }

            if (!(CORRECT_RANGE.contains(value))) {
                return false;
            }
            values[i] = value;
        }

        if ((values[0] == 0) || (values[0] != 255 && values[1] != 0) || (values[1] != 255 && values[2] != 0)
                || (values[2] != 255 && values[3] != 0)) {
            return false;
        }
        return true;
    }
}
