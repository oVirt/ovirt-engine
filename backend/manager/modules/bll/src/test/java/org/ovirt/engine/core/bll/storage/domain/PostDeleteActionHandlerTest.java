package org.ovirt.engine.core.bll.storage.domain;

import static org.junit.Assert.assertEquals;

import java.text.MessageFormat;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.vdscommands.PostDeleteAction;
import org.ovirt.engine.core.common.vdscommands.StorageDomainIdParametersBase;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.di.InjectorRule;

@RunWith(MockitoJUnitRunner.class)
public class PostDeleteActionHandlerTest {

    @ClassRule
    public static InjectorRule injectorRule = new InjectorRule();

    @InjectMocks
    private PostDeleteActionHandler postDeleteActionHandler;

    @Mock
    private AuditLogableBase auditLogableBase;

    @Mock
    private AuditLogDirector auditLogDirector;

    @Test
    public void parametersWithSecureDeletionAreFixedOnFileDomainWhenPostZeroIsTrue() {
        ParametersWithPostDeleteAction parameters = postDeleteActionHandler.fixPostZeroField(
                new ParametersWithPostDeleteAction(true, false), true);
        assertPostZeroValue(parameters, false);
    }

    @Test
    public void parametersWithSecureDeletionAreNotFixedOnBlockDomainWhenPostZeroIsTrue() {
        ParametersWithPostDeleteAction parameters = postDeleteActionHandler.fixPostZeroField(
                new ParametersWithPostDeleteAction(true, false), false);
        assertPostZeroValue(parameters, true);
    }

    @Test
    public void parametersWithSecureDeletionAreNotFixedOnFileDomainWhenPostZeroIsFalse() {
        ParametersWithPostDeleteAction parameters = postDeleteActionHandler.fixPostZeroField(
                new ParametersWithPostDeleteAction(false, false), true);
        assertPostZeroValue(parameters, false);
    }

    @Test
    public void parametersWithSecureDeletionAreNotFixedOnBlockDomainWhenPostZeroIsFalse() {
        ParametersWithPostDeleteAction parameters = postDeleteActionHandler.fixPostZeroField(
                new ParametersWithPostDeleteAction(false, false), false);
        assertPostZeroValue(parameters, false);
    }

    @Test
    public void discardIsTrueWhenDiscardAfterDeleteIsTrueAndDomainSupportsDiscard() {
        assertDiscardValue(true, true, true);
    }

    @Test
    public void discardIsFalseWhenDiscardAfterDeleteIsTrueAndDomainDoesNotSupportDiscard() {
        assertDiscardValue(true, false, false);
        assertDiscardValue(true, null, false);
    }

    private void assertPostZeroValue(ParametersWithPostDeleteAction parameters, boolean postZeroExpectedValue) {
        assertEquals(MessageFormat.format(
                "Wrong VDS command parameters: 'postZero' should be {0}.", postZeroExpectedValue),
                parameters.getPostZero(), postZeroExpectedValue);
    }

    private void assertDiscardValue(boolean discardAfterDelete, Boolean supportsDiscard,
            boolean expectedFixedDiscardParameter) {
        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setDiscardAfterDelete(discardAfterDelete);
        storageDomain.setSupportsDiscard(supportsDiscard);

        ParametersWithPostDeleteAction params =
                new ParametersWithPostDeleteAction(false, storageDomain.isDiscardAfterDelete());

        injectorRule.bind(AuditLogableBase.class, auditLogableBase);

        assertEquals(expectedFixedDiscardParameter,
                postDeleteActionHandler.fixDiscardField(params, storageDomain).isDiscard());
    }

    private static class ParametersWithPostDeleteAction
            extends StorageDomainIdParametersBase implements PostDeleteAction {

        private boolean postZero;

        private boolean discard;

        public ParametersWithPostDeleteAction(boolean postZero, boolean discard) {
            setPostZero(postZero);
            setDiscard(discard);
        }

        @Override
        public boolean getPostZero() {
            return postZero;
        }

        @Override
        public void setPostZero(boolean postZero) {
            this.postZero = postZero;
        }

        @Override
        public boolean isDiscard() {
            return discard;
        }

        @Override
        public void setDiscard(boolean discard) {
            this.discard = discard;
        }
    }

}
