package org.ovirt.engine.core.bll;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.utils.Injector;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CommandsFactory {
    private static final Logger log = LoggerFactory.getLogger(CommandsFactory.class);
    private static final Injector injector = new Injector();
    private static final String CLASS_NAME_FORMAT = "%1$s.%2$s%3$s";
    private static final String CommandSuffix = "Command";
    private static final String QueryPrefix = "Query";
    private static final String CTOR_MISMATCH =
            "could not find matching constructor for Command class {0}";
    private static final String CTOR_NOT_FOUND_FOR_PARAMETERS =
            "Can't find constructor for type {} with parameter types: {}";

    private static final String[] COMMAND_PACKAGES = new String[] {
        "org.ovirt.engine.core.bll",
        "org.ovirt.engine.core.bll.aaa",
        "org.ovirt.engine.core.bll.storage",
        "org.ovirt.engine.core.bll.lsm",
        "org.ovirt.engine.core.bll.gluster",
        "org.ovirt.engine.core.bll.network",
        "org.ovirt.engine.core.bll.network.dc",
        "org.ovirt.engine.core.bll.network.cluster",
        "org.ovirt.engine.core.bll.network.host",
        "org.ovirt.engine.core.bll.network.vm",
        "org.ovirt.engine.core.bll.network.template",
        "org.ovirt.engine.core.bll.numa.host",
        "org.ovirt.engine.core.bll.numa.vm",
        "org.ovirt.engine.core.bll.provider",
        "org.ovirt.engine.core.bll.provider.network",
        "org.ovirt.engine.core.bll.qos",
        "org.ovirt.engine.core.bll.scheduling.commands",
        "org.ovirt.engine.core.bll.scheduling.queries",
        "org.ovirt.engine.core.bll.profiles"
        };

    private static ConcurrentMap<String, Class<CommandBase<? extends VdcActionParametersBase>>> commandsCache =
            new ConcurrentHashMap<String, Class<CommandBase<? extends VdcActionParametersBase>>>(VdcActionType.values().length);

    @SuppressWarnings("unchecked")
    public static <P extends VdcActionParametersBase> CommandBase<P> createCommand(VdcActionType action, P parameters) {
        return createCommand(action, parameters, null);
    }

    public static <P extends VdcActionParametersBase> CommandBase<P> createCommand(VdcActionType action, P parameters, CommandContext commandContext) {
        try {
            return Injector.injectMembers(instantiateCommand(action, parameters, commandContext));
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

    @SuppressWarnings("unchecked")
    private static <P extends VdcActionParametersBase> CommandBase<P> instantiateCommand(VdcActionType action, P parameters, CommandContext commandContext)
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        return commandContext == null ?
                (CommandBase<P>)findCommandConstructor(getCommandClass(action.name(), CommandSuffix), parameters.getClass()).newInstance(parameters)
                : (CommandBase<P>) findCommandConstructor(getCommandClass(action.name(), CommandSuffix),
                        parameters.getClass(),
                        commandContext.getClass()).newInstance(parameters, commandContext);
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
        Constructor<?> constructor = null;
        Boolean isAcessible = null;
        try {
            constructor = Class.forName(className).getDeclaredConstructor(Guid.class);
            // since this constructor is defined as protected, we must modify accessability and restore it afterwards
            if (!constructor.isAccessible()) {
                isAcessible = constructor.isAccessible();
                constructor.setAccessible(true);
            }
            CommandBase<?> cmd = (CommandBase<?>) constructor.newInstance(new Object[]{commandId});
            return Injector.injectMembers(cmd);
        } catch (Exception e) {
            log.error("CommandsFactory : Failed to get type information using reflection for Class  '{}', Command Id '{}': {}",
                    className,
                    commandId,
                    e.getMessage());
            log.error("Exception", e);
            return null;
        } finally {
            if (isAcessible != null) {
                constructor.setAccessible(isAcessible);
            }
        }
    }

    public static QueriesCommandBase<?> createQueryCommand(VdcQueryType query, VdcQueryParametersBase parameters, EngineContext engineContext) {
        Class<?> type = null;
        try {
            type = getCommandClass(query.name(), QueryPrefix);
            QueriesCommandBase<?> result = null;
            if (engineContext == null) {
                result =
                        (QueriesCommandBase<?>) findCommandConstructor(type, parameters.getClass()).newInstance(parameters);
            } else {
                result =
                        (QueriesCommandBase<?>) findCommandConstructor(type, parameters.getClass(), EngineContext.class).newInstance(parameters,
                                engineContext);

            }
            return result;
        } catch (Exception e) {
            log.error("Command Factory: Failed to create command '{}' using reflection: {}", type, e.getMessage());
            log.error("Exception", e);
            throw new RuntimeException(e);
        }
    }

    public static Class<CommandBase<? extends VdcActionParametersBase>> getCommandClass(String name) {
        return getCommandClass(name, CommandSuffix);
    }

    public static Class<CommandBase<? extends VdcActionParametersBase>> getQueryClass(String name) {
        return getCommandClass(name, QueryPrefix);
    }

    public static <P extends VdcActionParametersBase> boolean hasConstructor(VdcActionType action, P parameters) {
        return ReflectionUtils.findConstructor(getCommandClass(action.name(), CommandSuffix), parameters.getClass()) != null;
    }

    public static <P extends VdcActionParametersBase> boolean hasConstructor(VdcActionType action, P parameters, CommandContext cmdContext) {
        return ReflectionUtils.findConstructor(getCommandClass(action.name(), CommandSuffix), parameters.getClass(), cmdContext.getClass()) != null;
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
