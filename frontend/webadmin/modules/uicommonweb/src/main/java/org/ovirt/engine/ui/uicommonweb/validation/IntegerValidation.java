package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.ui.uicompat.external.StringUtils;

@SuppressWarnings("unused")
public class IntegerValidation extends NumberRangeValidation<Integer> {

    public IntegerValidation(Integer min, Integer max) {
        super(min, max);
    }

    public IntegerValidation() {
        this(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    @Override
    protected String getValueTypeName() {
        return getMessages().naturalNumber();
    }

    @Override
    protected Integer parse(String value) {
        Integer output = null;
        try {
            output = StringUtils.parseInteger(value);
        } catch (NumberFormatException ex) {
            // eat it return null
        }
        return output;
    }

    @Override
    protected boolean belowMinimum(Integer value) {
        return value < getMinimum();
    }

    @Override
    protected boolean aboveMaximum(Integer value) {
        return value > getMaximum();
    }
}
