package org.ovirt.engine.core.bll;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CommandsFactory {
    private static final Logger log = LoggerFactory.getLogger(CommandsFactory.class);
    private static final String CLASS_NAME_FORMAT = "%1$s.%2$s";
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
            "org.ovirt.engine.core.bll.quota",
            "org.ovirt.engine.core.bll.scheduling.commands",
            "org.ovirt.engine.core.bll.scheduling.queries",
            "org.ovirt.engine.core.bll.snapshots",
            "org.ovirt.engine.core.bll.storage",
            "org.ovirt.engine.core.bll.storage.backup",
            "org.ovirt.engine.core.bll.storage.connection",
            "org.ovirt.engine.core.bll.storage.connection.iscsibond",
            "org.ovirt.engine.core.bll.storage.disk",
            "org.ovirt.engine.core.bll.storage.disk.cinder",
            "org.ovirt.engine.core.bll.storage.disk.image",
            "org.ovirt.engine.core.bll.storage.disk.lun",
            "org.ovirt.engine.core.bll.storage.disk.managedblock",
            "org.ovirt.engine.core.bll.storage.domain",
            "org.ovirt.engine.core.bll.storage.dr",
            "org.ovirt.engine.core.bll.storage.export",
            "org.ovirt.engine.core.bll.storage.lease",
            "org.ovirt.engine.core.bll.storage.lsm",
            "org.ovirt.engine.core.bll.storage.ovfstore",
            "org.ovirt.engine.core.bll.storage.pool",
            "org.ovirt.engine.core.bll.storage.repoimage"
    };

    protected static String[] getCommandPackages() {
        return COMMAND_PACKAGES;
    }

    private static ConcurrentMap<String, Class<?>> commandsCache = new ConcurrentHashMap<>();
    private static ConcurrentMap<Pair<Class<?>, Class<?>[]>, Constructor<?>> constructorCache =
            new ConcurrentHashMap<>();

    public static <P extends ActionParametersBase> CommandBase<P> createCommand(ActionType action, P parameters) {
        return createCommand(action, parameters, null);
    }

    public static <P extends ActionParametersBase> CommandBase<P> createCommand(ActionType action,
            P parameters,
            CommandContext commandContext) {
        try {
            Constructor<?> commandConstructor =
                    getCommandConstructor(getCommandClass(action.name()), parameters.getClass(), CommandContext.class);

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
        } catch (InvocationTargetException ex) {
            logException(ex,
                    "Error in invocating CTOR of command '{}' with parameters '{}': {}",
                    action.name(),
                    parameters,
                    ex.getMessage());
            return null;
        } catch (Exception ex) {
            logException(ex,
                    "An exception has occurred while trying to create a command object for command '{}' with parameters '{}': {}",
                    action.name(),
                    parameters,
                    ex.getMessage());
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
            logException(e,
                    "CommandsFactory : Failed to get type information using reflection for Class  '{}', Command Id '{}': {}",
                    className,
                    commandId,
                    e.getMessage());
            return null;
        }
    }

    public static QueriesCommandBase<?> createQueryCommand(QueryType query,
            QueryParametersBase parameters,
            EngineContext engineContext) {
        Class<?> type = null;
        try {
            type = getQueryClass(query.name());
            QueriesCommandBase<?> result =
                    (QueriesCommandBase<?>) getCommandConstructor(type, parameters.getClass(), EngineContext.class)
                            .newInstance(parameters, engineContext);
            return Injector.injectMembers(result);
        } catch (Exception e) {
            logException(e,
                    "Command Factory: Failed to create command '{}' using reflection: {}",
                    type,
                    e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static Class<?> getCommandClass(String name) {
        return getCommandClass(name, COMMAND_SUFFIX);
    }

    public static Class<?> getQueryClass(String name) {
        return getCommandClass(name, QUERY_SUFFIX);
    }

    private static Class<?> getCommandClass(String name, String suffix) {
        String key = name + suffix;
        return commandsCache.computeIfAbsent(key, CommandsFactory::findClass);
    }

    private static Class<?> findClass(String simpleName) {
        for (String commandPackage : COMMAND_PACKAGES) {
            String className = String.format(CLASS_NAME_FORMAT, commandPackage, simpleName);
            Class<?> type = loadClass(className);
            if (type != null) {
                return type;
            }
        }

        // nothing found
        log.warn("Unable to find class for action '{}'", simpleName);
        return null;
    }

    private static Class<?> loadClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    static <T> Constructor<T> getCommandConstructor(Class<T> type, Class<?>... expectedParams) {
        return (Constructor<T>) constructorCache.computeIfAbsent(new Pair<>(type, expectedParams),
                k -> findCommandConstructor(type, expectedParams));
    }

    /**
     * Return the constructor for the command.
     *
     * @param <T>
     *            The command type to look for.
     * @param type
     *            A class representing the command type to look for.
     * @param expectedParams
     *            The parameters which the constructor is expected to have (can be empty).
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

    private static void logException(Throwable ex, String message, Object... arguments) {
        log.error(message, arguments);
        Throwable rootCause = ExceptionUtils.getRootCause(ex);
        if (rootCause != null) {
            log.error("Exception", rootCause);
            log.debug("Exception", ex);
        } else {
            log.error("Exception", ex);
        }
    }

    static long getConstructorCacheSize() {
        return constructorCache.size();
    }
}
