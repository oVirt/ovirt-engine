package org.ovirt.engine.core.bll;

import java.lang.reflect.Constructor;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.ReflectionUtils;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public final class CommandsFactory {
    private static final String CLASS_NAME_FORMAT = "%1$s.%2$s%3$s";
    private static final String CommandSuffix = "Command";
    private static final String QueryPrefix = "Query";
    private static final String CTOR_MISMATCH =
            "could not find matching constructor for Command class {0}";
    private static final String CTOR_NOT_FOUND_FOR_PARAMETERS =
            "Can't find constructor for type {0} with parameter types: {1}";

    private static final String[] COMMAND_PACKAGES = new String[] { "org.ovirt.engine.core.bll",
            "org.ovirt.engine.core.bll.storage", "org.ovirt.engine.core.bll.lsm", "org.ovirt.engine.core.bll.gluster",
            "org.ovirt.engine.core.bll.network",
            "org.ovirt.engine.core.bll.network.dc",
            "org.ovirt.engine.core.bll.network.cluster",
            "org.ovirt.engine.core.bll.network.host",
            "org.ovirt.engine.core.bll.network.vm",
            "org.ovirt.engine.core.bll.network.template",
            "org.ovirt.engine.core.bll.provider",
            "org.ovirt.engine.core.bll.provider.network",
            "org.ovirt.engine.core.bll.qos"};

    private static ConcurrentMap<String, Class<CommandBase<? extends VdcActionParametersBase>>> commandsCache =
            new ConcurrentHashMap<String, Class<CommandBase<? extends VdcActionParametersBase>>>(VdcActionType.values().length);

    @SuppressWarnings("unchecked")
    public static <P extends VdcActionParametersBase> CommandBase<P> CreateCommand(VdcActionType action, P parameters) {
        try {
            Constructor<CommandBase<? extends VdcActionParametersBase>> constructor =
                    findCommandConstructor(getCommandClass(action.name(), CommandSuffix), parameters.getClass());

            return (CommandBase<P>) constructor.newInstance(new Object[] { parameters });
        }

        catch (Exception e) {
            log.error(
                    "CommandsFactory [parameter: VdcActionParametersBase]: Failed to get type information using " +
                            "reflection for Action: " + action, e);
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
    public static CommandBase<?> CreateCommand(String className, Guid commandId) {
        Constructor<?> constructor = null;
        Boolean isAcessible = null;
        try {
            constructor = Class.forName(className).getDeclaredConstructor(Guid.class);
            // since this constructor is defined as protected, we must modify accessability and restore it afterwards
            if (!constructor.isAccessible()) {
                isAcessible = constructor.isAccessible();
                constructor.setAccessible(true);
            }
            return (CommandBase<?>) constructor.newInstance(new Object[] { commandId });
        } catch (Exception e) {
            log.error(
                    "CommandsFactory : Failed to get type information using " +
                            "reflection for Class : " + className + ", Command Id:" + commandId, e);
            return null;
        } finally {
            if (isAcessible != null) {
                constructor.setAccessible(isAcessible);
            }
        }
    }

    public static QueriesCommandBase<?> CreateQueryCommand(VdcQueryType query, VdcQueryParametersBase parameters) {
        Class<?> type = null;
        try {
            type = getCommandClass(query.name(), QueryPrefix);
            Constructor<?> info = findCommandConstructor(type, parameters.getClass());
            return (QueriesCommandBase<?>) info.newInstance(parameters);
        } catch (Exception e) {
            log.errorFormat("Command Factory: Failed to create command {0} using reflection\n. {1}", type, e);
            throw new RuntimeException(e);
        }
    }

    public static Class<CommandBase<? extends VdcActionParametersBase>> getCommandClass(String name) {
        return getCommandClass(name, CommandSuffix);
    }

    public static Class<CommandBase<? extends VdcActionParametersBase>> getQueryClass(String name) {
        return getCommandClass(name, QueryPrefix);
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
            Class<CommandBase<?>> type = loadClass(className);
            if (type != null) {
                Class<CommandBase<?>> cachedType = commandsCache.putIfAbsent(key, type); // update cache
                return cachedType == null ? type : cachedType;
            }
        }

        // nothing found
        log.warn("Unable to find class for action: " + key);
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
            log.errorFormat(CTOR_NOT_FOUND_FOR_PARAMETERS, type.getName(), Arrays.toString(expectedParams));
            throw new RuntimeException(MessageFormat.format(CTOR_MISMATCH, type));
        }

        return constructor;
    }

    private static Log log = LogFactory.getLog(CommandsFactory.class);
}
