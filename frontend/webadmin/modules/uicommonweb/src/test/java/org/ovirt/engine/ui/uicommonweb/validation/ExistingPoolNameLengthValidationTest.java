package org.ovirt.engine.ui.uicommonweb.validation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

import org.junit.jupiter.api.Test;
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

    private void assertGeneratesCorrect(int maxNameLength, int poolNameLength, int expectedMaxNumOfVms) {
        ExistingPoolNameLengthValidation validation = new ExistingPoolNameLengthValidation("", 0, OsRepository.DEFAULT_X86_OS); //$NON-NLS-1$
        int res = validation.doGenerateMaxNumOfVms(maxNameLength, poolNameLength);
        assertThat(res, is(equalTo(expectedMaxNumOfVms)));
    }

    @Test
    public void testDoGenerateMaxNumOfVmsWithQuestionMark_9VmsAllowed() {
        assertGeneratesWithQuestionMarksCorrect(5, 5, 1, 9);
    }

    @Test
    public void testDoGenerateMaxNumOfVmsWithQuestionMark_99VmsAllowed() {
        assertGeneratesWithQuestionMarksCorrect(5, 4, 1, 99);
    }

    @Test
    public void testDoGenerateMaxNumOfVmsWithQuestionMark_999VmsAllowed() {
        assertGeneratesWithQuestionMarksCorrect(5, 4, 2, 999);
    }

    private void assertGeneratesWithQuestionMarksCorrect(
            int maxNameLength, int poolNameLength, int numberOfQuestionMarks, int expectedMaxNumOfVms) {
        ExistingPoolNameLengthValidation validation = new ExistingPoolNameLengthValidation("", 0, OsRepository.DEFAULT_X86_OS); //$NON-NLS-1$
        int res = validation.doGenerateMaxNumOfVmsWithQuestionMark(maxNameLength, poolNameLength, numberOfQuestionMarks);
        assertThat(res, is(equalTo(expectedMaxNumOfVms)));
    }
}
