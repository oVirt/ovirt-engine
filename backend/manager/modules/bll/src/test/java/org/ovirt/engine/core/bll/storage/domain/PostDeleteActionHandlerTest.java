package org.ovirt.engine.core.bll.storage.domain;

import static org.junit.Assert.assertEquals;

import java.text.MessageFormat;

import org.junit.Test;
import org.ovirt.engine.core.common.vdscommands.PostDeleteAction;
import org.ovirt.engine.core.common.vdscommands.StorageDomainIdParametersBase;

public class PostDeleteActionHandlerTest {

    @Test
    public void parametersWithSecureDeletionAreFixedOnFileDomainWhenPostZeroIsTrue() {
        ParametersWithPostDeleteAction parameters = PostDeleteActionHandler.fixParameters(
                new ParametersWithPostDeleteAction(true), true);
        assertPostZeroValue(parameters, false);
    }

    @Test
    public void parametersWithSecureDeletionAreNotFixedOnBlockDomainWhenPostZeroIsTrue() {
        ParametersWithPostDeleteAction parameters = PostDeleteActionHandler.fixParameters(
                new ParametersWithPostDeleteAction(true), false);
        assertPostZeroValue(parameters, true);
    }

    @Test
    public void parametersWithSecureDeletionAreNotFixedOnFileDomainWhenPostZeroIsFalse() {
        ParametersWithPostDeleteAction parameters = PostDeleteActionHandler.fixParameters(
                new ParametersWithPostDeleteAction(false), true);
        assertPostZeroValue(parameters, false);
    }

    @Test
    public void parametersWithSecureDeletionAreNotFixedOnBlockDomainWhenPostZeroIsFalse() {
        ParametersWithPostDeleteAction parameters = PostDeleteActionHandler.fixParameters(
                new ParametersWithPostDeleteAction(false), false);
        assertPostZeroValue(parameters, false);
    }

    private void assertPostZeroValue(ParametersWithPostDeleteAction parameters, boolean postZeroExpectedValue) {
        assertEquals(MessageFormat.format(
                "Wrong VDS command parameters: 'postZero' should be {0}.", postZeroExpectedValue),
                parameters.getPostZero(), postZeroExpectedValue);
    }

    private static class ParametersWithPostDeleteAction
            extends StorageDomainIdParametersBase implements PostDeleteAction {

        private boolean postZero;

        public ParametersWithPostDeleteAction(boolean postZero) {
            setPostZero(postZero);
        }

        @Override
        public boolean getPostZero() {
            return postZero;
        }

        @Override
        public void setPostZero(boolean postZero) {
            this.postZero = postZero;
        }
    }

}
