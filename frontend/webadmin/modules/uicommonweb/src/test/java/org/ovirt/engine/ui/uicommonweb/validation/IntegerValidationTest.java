package org.ovirt.engine.ui.uicommonweb.validation;

import org.junit.Test;

public class IntegerValidationTest extends NumberRangeValidationTestUtils<IntegerValidation> {
    private final static Integer MIN = new Integer(1);
    private final static Integer MAX = new Integer(100);
    private final static Integer AVG = (MIN + MAX) / (2);

    public IntegerValidationTest() {
        super(new IntegerValidation(MIN, MAX));
        assert MAX.compareTo(new Integer(0)) != 0;
        assert MIN.compareTo(new Integer(0)) != 0;
        assert new Integer(MIN + AVG).compareTo(MAX) < 0;
    }

    @Test
    @Override
    public void checkAboveMax() {
        setValue(Math.abs(MAX + MAX));
        super.checkAboveMax();
    }

    @Test
    @Override
    public void checkBellowMinimum() {
        setValue(Math.abs(MIN - MIN));
        super.checkBellowMinimum();
    }

    @Test
    @Override
    public void checkNumberRangeOrType() {
        setValue("a"); //$NON-NLS-1$
        super.checkNumberRangeOrType();
    }

    @Test
    @Override
    public void checkNullValue() {
        setValue(null);
        super.checkNullValue();
    }

    @Test
    @Override
    public void checkValidValue() {
        setValue(MIN);
        super.checkValidValue();
        setValue(MAX);
        super.checkValidValue();
        setValue(MIN.toString());
        super.checkValidValue();
        setValue(MAX.toString());
        super.checkValidValue();
        setValue(AVG);
        super.checkValidValue();
        setValue(AVG.toString());
        super.checkValidValue();
    }

}
