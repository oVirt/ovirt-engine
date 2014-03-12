package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import java.lang.reflect.Constructor;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.ReflectionUtils;

public abstract class LdapBrokerBase implements LdapBroker {
    private static final String CommandsContainerAssemblyName = LdapBrokerBase.class.getPackage().getName();
    private static final String CommandPrefix = "Command";

    private static final Log log = LogFactory.getLog(LdapBrokerBase.class);

    protected abstract String getBrokerType();

    public LdapReturnValueBase runAdAction(AdActionType actionType, LdapBrokerBaseParameters parameters) {
        log.debug("runAdAction Entry, actionType=" + actionType.toString());
        BrokerCommandBase command = CreateCommand(actionType, parameters);
        return command.execute();
    }

    private BrokerCommandBase CreateCommand(AdActionType action, LdapBrokerBaseParameters parameters) {
        try {
            Class type = Class.forName(GetCommandTypeName(action));
            Constructor info = ReflectionUtils.findConstructor(type, parameters.getClass());
            Object tempVar = info.newInstance(parameters);
            return (BrokerCommandBase) ((tempVar instanceof BrokerCommandBase) ? tempVar : null);
        }

        catch (Exception e) {
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
