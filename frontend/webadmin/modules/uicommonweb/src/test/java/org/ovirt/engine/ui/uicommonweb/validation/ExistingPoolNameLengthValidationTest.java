package org.ovirt.engine.ui.uicommonweb.validation;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.ovirt.engine.core.common.osinfo.OsRepository;

public class ExistingPoolNameLengthValidationTest {

    @Test
    public void getPoolName_noVmsAllowed() {
        assertGeneratesCorrect(5, 4, 0);
    }

    @Test
    public void getPoolName_9VmsAllowed() {
        assertGeneratesCorrect(5, 3, 9);
    }

    @Test
    public void getPoolName_99VmsAllowed() {
        assertGeneratesCorrect(5, 2, 99);
    }

    private void assertGeneratesCorrect(int maxNameLengt, int poolNameLength, int expectedMaxNumOfVms) {
        ExistingPoolNameLengthValidation validation = new ExistingPoolNameLengthValidation("", 0, OsRepository.DEFAULT_OS); //$NON-NLS-1$
        int res = validation.doGenerateMaxLength(maxNameLengt, poolNameLength);
        assertThat(res, is(equalTo(expectedMaxNumOfVms)));
    }
}
