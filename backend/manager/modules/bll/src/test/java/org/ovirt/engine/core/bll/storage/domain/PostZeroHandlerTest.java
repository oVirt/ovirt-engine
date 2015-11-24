package org.ovirt.engine.core.bll.storage.domain;

import static org.junit.Assert.assertEquals;

import java.text.MessageFormat;

import org.junit.Test;
import org.ovirt.engine.core.common.vdscommands.PostZero;
import org.ovirt.engine.core.common.vdscommands.StorageDomainIdParametersBase;

public class PostZeroHandlerTest {

    @Test
    public void parametersWithSecureDeletionAreFixedOnFileDomainWhenPostZeroIsTrue() {
        ParametersWithPostZero parameters = PostZeroHandler.fixParametersWithPostZero(
                new ParametersWithPostZero(true), true);
        assertPostZeroValue(parameters, false);
    }

    @Test
    public void parametersWithSecureDeletionAreNotFixedOnBlockDomainWhenPostZeroIsTrue() {
        ParametersWithPostZero parameters = PostZeroHandler.fixParametersWithPostZero(
                new ParametersWithPostZero(true), false);
        assertPostZeroValue(parameters, true);
    }

    @Test
    public void parametersWithSecureDeletionAreNotFixedOnFileDomainWhenPostZeroIsFalse() {
        ParametersWithPostZero parameters = PostZeroHandler.fixParametersWithPostZero(
                new ParametersWithPostZero(false), true);
        assertPostZeroValue(parameters, false);
    }

    @Test
    public void parametersWithSecureDeletionAreNotFixedOnBlockDomainWhenPostZeroIsFalse() {
        ParametersWithPostZero parameters = PostZeroHandler.fixParametersWithPostZero(
                new ParametersWithPostZero(false), false);
        assertPostZeroValue(parameters, false);
    }

    private void assertPostZeroValue(ParametersWithPostZero parameters, boolean postZeroExpectedValue) {
        assertEquals(MessageFormat.format(
                "Wrong VDS command parameters: 'postZero' should be {0}.", postZeroExpectedValue),
                parameters.getPostZero(), postZeroExpectedValue);
    }

    private static class ParametersWithPostZero extends StorageDomainIdParametersBase implements PostZero {

        private boolean postZero;

        public ParametersWithPostZero(boolean postZero) {
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
