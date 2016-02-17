package org.ovirt.engine.ui.uicommonweb.validation;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.mockito.Mockito;

public class PoolNameLengthValidationTest {

    @Test
    public void validate_nameItselfTooLong() {
        assertValidationWorks(200, 1, false);
    }

    @Test
    public void validate_farNotOk() {
        assertValidationWorks(63, 500, false);
    }

    @Test
    public void validate_farOk() {
        assertValidationWorks(5, 2, true);
    }

    @Test
    public void validate_okCorner() {
        assertValidationWorks(12, 10, true);
    }

    @Test
    public void validate_notOkCorner() {
        assertValidationWorks(62, 10, false);
    }

    private void assertValidationWorks(int nameLength, int numOfVms, boolean expected) {

        // enough to test for windows, the logic is the same
        PoolNameLengthValidation object = new PoolNameLengthValidation(nameOfLength(nameLength), numOfVms, 3);
        PoolNameLengthValidation spy =
                Mockito.spy(object);

        assertThat(spy.validate(null).getSuccess(), is(equalTo(expected)));
    }

    private String nameOfLength(int nameLength) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nameLength; i++) {
            sb.append("x"); //$NON-NLS-1$
        }

        return sb.toString();
    }

}
