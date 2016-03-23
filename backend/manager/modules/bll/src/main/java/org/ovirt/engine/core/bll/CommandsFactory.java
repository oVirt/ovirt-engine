package org.ovirt.engine.core.bll;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CommandsFactory {
    private static final Logger log = LoggerFactory.getLogger(CommandsFactory.class);
    private static final String CLASS_NAME_FORMAT = "%1$s.%2$s%3$s";
    private static final String COMMAND_SUFFIX = "Command";
    private static final String QUERY_SUFFIX = "Query";
    private static final String CTOR_MISMATCH =
            "could not find matching constructor for Command class {0}";
    private static final String CTOR_NOT_FOUND_FOR_PARAMETERS =
            "Can't find constructor for type {} with parameter types: {}";

    private static final String[] COMMAND_PACKAGES = new String[] {
            "org.ovirt.engine.core.bll",
            "org.ovirt.engine.core.bll.aaa",
            "org.ovirt.engine.core.bll.exportimport",
            "org.ovirt.engine.core.bll.gluster",
            "org.ovirt.engine.core.bll.hostdeploy",
            "org.ovirt.engine.core.bll.hostdev",
            "org.ovirt.engine.core.bll.network",
            "org.ovirt.engine.core.bll.network.cluster",
            "org.ovirt.engine.core.bll.network.dc",
            "org.ovirt.engine.core.bll.network.host",
            "org.ovirt.engine.core.bll.network.template",
            "org.ovirt.engine.core.bll.network.vm",
            "org.ovirt.engine.core.bll.numa.host",
            "org.ovirt.engine.core.bll.numa.vm",
            "org.ovirt.engine.core.bll.pm",
            "org.ovirt.engine.core.bll.profiles",
            "org.ovirt.engine.core.bll.provider",
            "org.ovirt.engine.core.bll.provider.network",
            "org.ovirt.engine.core.bll.provider.storage",
            "org.ovirt.engine.core.bll.qos",
            "org.ovirt.engine.core.bll.scheduling.commands",
            "org.ovirt.engine.core.bll.scheduling.queries",
            "org.ovirt.engine.core.bll.snapshots",
            "org.ovirt.engine.core.bll.storage",
            "org.ovirt.engine.core.bll.storage.connection",
            "org.ovirt.engine.core.bll.storage.connection.iscsibond",
            "org.ovirt.engine.core.bll.storage.disk",
            "org.ovirt.engine.core.bll.storage.disk.cinder",
            "org.ovirt.engine.core.bll.storage.disk.image",
            "org.ovirt.engine.core.bll.storage.disk.lun",
            "org.ovirt.engine.core.bll.storage.domain",
            "org.ovirt.engine.core.bll.storage.export",
            "org.ovirt.engine.core.bll.storage.lsm",
            "org.ovirt.engine.core.bll.storage.ovfstore",
            "org.ovirt.engine.core.bll.storage.pool",
            "org.ovirt.engine.core.bll.storage.repoimage"
    };

    protected String[] getCommandPackages() {
        return COMMAND_PACKAGES;
    }

    private static ConcurrentMap<String, Class<CommandBase<? extends VdcActionParametersBase>>> commandsCache =
            new ConcurrentHashMap<>(VdcActionType.values().length);

    public static <P extends VdcActionParametersBase> CommandBase<P> createCommand(VdcActionType action, P parameters) {
        return createCommand(action, parameters, null);
    }

    public static <P extends VdcActionParametersBase> CommandBase<P> createCommand(VdcActionType action, P parameters,
            CommandContext commandContext) {
        try {
            Constructor<CommandBase<? extends VdcActionParametersBase>> commandConstructor =
                    findCommandConstructor(getCommandClass(action.name()), parameters.getClass(), CommandContext.class);

            if (commandContext == null) {
                commandContext = CommandContext.createContext(parameters.getSessionId());
            } else if (commandContext.getEngineContext().getSessionId() == null) {
                // Needed for SEAT mechanism - session ID is available only on parameters
                // upon command re-instantiation (when moving between task handlers).
                commandContext.getEngineContext().withSessionId(parameters.getSessionId());
            }

            @SuppressWarnings("unchecked")
            CommandBase<P> command = (CommandBase<P>) commandConstructor.newInstance(parameters, commandContext);
            return Injector.injectMembers(command);
        }
        catch (InvocationTargetException ex) {
            log.error("Error in invocating CTOR of command '{}': {}", action.name(), ex.getMessage());
            log.debug("Exception", ex);
            return null;
        }
        catch (Exception ex) {
            log.error("An exception has occured while trying to create a command object for command '{}': {}",
                    action.name(),
                    ex.getMessage());
            log.debug("Exception", ex);
            return null;
        }
    }

    /**
     * Creates an instance of the given command class and passed the command id to it's constructor
     *
     * @param className
     *            command class name to be created
     * @param commandId
     *            the command id used by the compensation.
     * @return command instance or null if exception occurred.
     */
    public static CommandBase<?> createCommand(String className, Guid commandId) {
        try {
            Constructor<?> constructor = Class.forName(className).getDeclaredConstructor(Guid.class);
            CommandBase<?> cmd = (CommandBase<?>) constructor.newInstance(commandId);
            return Injector.injectMembers(cmd);
        } catch (Exception e) {
            log.error("CommandsFactory : Failed to get type information using reflection for Class  '{}', Command Id '{}': {}",
                    className,
                    commandId,
                    e.getMessage());
            log.error("Exception", e);
            return null;
        }
    }

    public static QueriesCommandBase<?> createQueryCommand(VdcQueryType query, VdcQueryParametersBase parameters, EngineContext engineContext) {
        Class<?> type = null;
        try {
            type = getQueryClass(query.name());
            QueriesCommandBase<?> result;
            if (engineContext == null) {
                result =
                        (QueriesCommandBase<?>) findCommandConstructor(type, parameters.getClass()).newInstance(parameters);
            } else {
                result =
                        (QueriesCommandBase<?>) findCommandConstructor(type, parameters.getClass(), EngineContext.class).newInstance(parameters,
                                engineContext);

            }
            return Injector.injectMembers(result);
        } catch (Exception e) {
            log.error("Command Factory: Failed to create command '{}' using reflection: {}", type, e.getMessage());
            log.error("Exception", e);
            throw new RuntimeException(e);
        }
    }

    public static Class<CommandBase<? extends VdcActionParametersBase>> getCommandClass(String name) {
        return getCommandClass(name, COMMAND_SUFFIX);
    }

    public static Class<CommandBase<? extends VdcActionParametersBase>> getQueryClass(String name) {
        return getCommandClass(name, QUERY_SUFFIX);
    }

    private static Class<CommandBase<? extends VdcActionParametersBase>> getCommandClass(String name, String suffix) {
        // try the cache first
        String key = name + suffix;
        Class<CommandBase<? extends VdcActionParametersBase>> clazz = commandsCache.get(key);
        if (clazz != null) {
            return clazz;
        }

        for (String commandPackage : COMMAND_PACKAGES) {
            String className = String.format(CLASS_NAME_FORMAT, commandPackage, name, suffix);
            Class<CommandBase<? extends VdcActionParametersBase>> type = loadClass(className);
            if (type != null) {
                Class<CommandBase<? extends VdcActionParametersBase>> cachedType = commandsCache.putIfAbsent(key, type); // update cache
                return cachedType == null ? type : cachedType;
            }
        }

        // nothing found
        log.warn("Unable to find class for action '{}'", key);
        return null;
    }

    @SuppressWarnings("unchecked")
    private static Class<CommandBase<? extends VdcActionParametersBase>> loadClass(String className) {
        try {
            return (Class<CommandBase<? extends VdcActionParametersBase>>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * Return the constructor for the command.
     *
     * @param <T>
     *            The command type to look for.
     * @param type
     *            A class representing the command type to look for.
     * @param expectedParams
     *            The parameters which the constructor is expected to have (can
     *            be empty).
     *
     * @return The first matching constructor for the command.
     * @throws RuntimeException
     *             If a matching constructor can't be found.
     *
     * @see ReflectionUtils#findConstructor(Class, Class...)
     */
    private static <T> Constructor<T> findCommandConstructor(Class<T> type, Class<?>... expectedParams) {
        Constructor<T> constructor = ReflectionUtils.findConstructor(type, expectedParams);

        if (constructor == null) {
            log.error(CTOR_NOT_FOUND_FOR_PARAMETERS, type.getName(), Arrays.toString(expectedParams));
            throw new RuntimeException(MessageFormat.format(CTOR_MISMATCH, type));
        }

        return constructor;
    }
}
