package org.ovirt.engine.core.searchbackend;

import org.junit.Assert;
import org.junit.Test;

public class BaseConditionFieldAutoCompleterTest {
    @Test
    public void validTimeSpan() {
        Assert.assertFalse(BaseConditionFieldAutoCompleter.validTimeSpan.isValid(null, ""));
        Assert.assertFalse(BaseConditionFieldAutoCompleter.validTimeSpan.isValid(null, "not valid"));
        Assert.assertTrue(BaseConditionFieldAutoCompleter.validTimeSpan.isValid(null, "-1.02:03:04.05"));
    }

    @Test
    public void validInteger() {
        Assert.assertFalse(BaseConditionFieldAutoCompleter.validInteger.isValid(null, ""));
        Assert.assertFalse(BaseConditionFieldAutoCompleter.validInteger.isValid(null, "not valid integer"));
        Assert.assertFalse(BaseConditionFieldAutoCompleter.validInteger.isValid(null, "$%@^%"));
        Assert.assertFalse(BaseConditionFieldAutoCompleter.validInteger.isValid(null, "1$"));
        Assert.assertFalse(BaseConditionFieldAutoCompleter.validInteger.isValid(null, "1.1"));
        Assert.assertTrue(BaseConditionFieldAutoCompleter.validInteger.isValid(null, "1"));
        Assert.assertTrue(BaseConditionFieldAutoCompleter.validInteger.isValid(null, "-1"));
    }

    @Test
    public void validDecimal() {
        Assert.assertFalse(BaseConditionFieldAutoCompleter.validDecimal.isValid(null, ""));
        Assert.assertFalse(BaseConditionFieldAutoCompleter.validDecimal.isValid(null, "not valid integer"));
        Assert.assertFalse(BaseConditionFieldAutoCompleter.validDecimal.isValid(null, "$%@^%"));
        Assert.assertFalse(BaseConditionFieldAutoCompleter.validDecimal.isValid(null, "1$"));
        Assert.assertTrue(BaseConditionFieldAutoCompleter.validDecimal.isValid(null, "1.1"));
        Assert.assertTrue(BaseConditionFieldAutoCompleter.validDecimal.isValid(null, "1"));
        Assert.assertTrue(BaseConditionFieldAutoCompleter.validDecimal.isValid(null, "-1"));
        Assert.assertTrue(BaseConditionFieldAutoCompleter.validDecimal.isValid(null, "-1.1"));
    }

}
