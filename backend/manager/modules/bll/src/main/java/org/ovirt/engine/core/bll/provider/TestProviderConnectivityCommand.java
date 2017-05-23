package org.ovirt.engine.core.bll.provider;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ProviderParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.compat.Guid;

/**
 * Allows to test that the provider definition allows connecting to the provider and accessing it's API.<br>
 * In case of connection failure, an exception will be thrown.
 *
 * @param <P>
 *            Parameter type.
 */
@NonTransactiveCommandAttribute
public class TestProviderConnectivityCommand<P extends ProviderParameters> extends CommandBase<P> {
    @Inject
    private ProviderProxyFactory providerProxyFactory;

    public TestProviderConnectivityCommand(Guid commandId) {
        super(commandId);
    }

    public TestProviderConnectivityCommand(P parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        ProviderProxy proxy = providerProxyFactory.create(getParameters().getProvider());

        proxy.testConnection();
        setSucceeded(true);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(Guid.SYSTEM,
                VdcObjectType.System,
                ActionGroup.CREATE_STORAGE_POOL));
    }

}
