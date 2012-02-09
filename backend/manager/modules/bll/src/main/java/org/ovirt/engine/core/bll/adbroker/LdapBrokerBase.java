package org.ovirt.engine.core.bll.adbroker;

import org.ovirt.engine.core.utils.log.LogCompat;
import org.ovirt.engine.core.utils.log.LogFactoryCompat;
import org.ovirt.engine.core.utils.ReflectionUtils;

public abstract class LdapBrokerBase implements LdapBroker {
    private static final String CommandsContainerAssemblyName = LdapBrokerBase.class.getPackage().getName();
    private static final String CommandPrefix = "Command";

    private static LogCompat log = LogFactoryCompat.getLog(LdapBrokerBase.class);

    protected abstract String getBrokerType();

    public LdapReturnValueBase RunAdAction(AdActionType actionType, LdapBrokerBaseParameters parameters) {
        log.debug("RunAdAction Entry, actionType=" + actionType.toString());
        BrokerCommandBase command = CreateCommand(actionType, parameters);
        return command.Execute();
    }

    private BrokerCommandBase CreateCommand(AdActionType action, LdapBrokerBaseParameters parameters) {
        try {
            java.lang.Class type = java.lang.Class.forName(GetCommandTypeName(action));
            java.lang.reflect.Constructor info = ReflectionUtils.findConstructor(type, parameters.getClass());
            Object tempVar = info.newInstance(parameters);
            return (BrokerCommandBase) ((tempVar instanceof BrokerCommandBase) ? tempVar : null);
        }

        catch (java.lang.Exception e) {
            log.errorFormat("LdapBrokerCommandBase: Failed to get type information using reflection for Action: {0}",
                    action);
            return null;
        }
    }

    private String GetCommandTypeName(AdActionType action) {
        return String
                .format("%1$s.%2$s%3$s%4$s", CommandsContainerAssemblyName, getBrokerType(), action, CommandPrefix);
    }
}
