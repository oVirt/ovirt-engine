package org.ovirt.engine.core.bll;

import java.lang.reflect.Constructor;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.utils.ReflectionUtils;

public final class CommandsFactory {
    private static final String CommandSuffix = "Command";
    private static final String QueryPrefix = "Query";
    private static final String CanDoCommandPrefix = "Can";
    private static final String CTOR_MISMATCH =
            "could not find matching constructor for Command class {0}";
    private static final String CTOR_NOT_FOUND_FOR_PARAMETERS =
            "Can't find constructor for type {0} with parameter types: {1}";

    private static final String[] COMMAND_PACKAGES = new String[] { "org.ovirt.engine.core.bll",
            "org.ovirt.engine.core.bll.storage" };

    private static Map<String, Class<CommandBase<? extends VdcActionParametersBase>>> commandsCache =
            new ConcurrentHashMap<String, Class<CommandBase<? extends VdcActionParametersBase>>>(VdcActionType.values().length);

    public static boolean canDoActionWithParameters(VdcActionType action,
                                                    Object id,
                                                    RefObject<java.util.ArrayList<String>> reasons,
                                                    Object... additionalParameters) {
        boolean returnValue = true;
        reasons.argvalue = null;
        try {

            Class<?> actionType = getCommandClass(action.name(), CommandSuffix);

            /**
             * if action type not exist - operation valid
             */
            if (actionType != null) {
                String canDoActionName = String.format("%1$s%2$s", CanDoCommandPrefix, action);
                reasons.argvalue = new java.util.ArrayList<String>();
                Object[] args;
                if (additionalParameters != null) {
                    args = new Object[2 + additionalParameters.length];
                    for (int i = 0; i < additionalParameters.length; i++) {
                        args[2 + i] = additionalParameters[i];
                    }
                } else {
                    args = new Object[2];
                }
                args[0] = id;
                args[1] = reasons.argvalue;
                /**
                 * Each command must implement static public function in order
                 * to possibility to check operation validity
                 */
                java.lang.reflect.Method method = actionType.getMethod(canDoActionName);
                /**
                 * By default all operations valid
                 */
                if (method != null) {
                    returnValue = (Boolean) method.invoke(null, args);
                }
            }
        } catch (Exception e) {
            log.error("Failed to check Action ", e);
        }
        return returnValue;
    }

    @SuppressWarnings("unchecked")
    public static <P extends VdcActionParametersBase> CommandBase<P> CreateCommand(VdcActionType action, P parameters) {
        try {
            Constructor<CommandBase<? extends VdcActionParametersBase>> constructor =
                    findCommandConstructor(getCommandClass(action.name(), CommandSuffix), parameters.getClass());

            return (CommandBase<P>) constructor.newInstance(new Object[] { parameters });
        }

        catch (java.lang.Exception e) {
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
    @SuppressWarnings("unchecked")
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
        } catch (java.lang.Exception e) {
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

    public static QueriesCommandBase CreateQueryCommand(VdcQueryType query, VdcQueryParametersBase parameters) {
        java.lang.Class<?> type = null;
        try {
            type = getCommandClass(query.name(), QueryPrefix);
            java.lang.Class<?>[] types = new java.lang.Class[1];
            types[0] = parameters.getClass();

            java.lang.reflect.Constructor<?> info = findCommandConstructor(type, types);
            Object[] vdcParameters = new Object[1];
            vdcParameters[0] = parameters;
            Object tempVar = info.newInstance(vdcParameters);
            return (QueriesCommandBase) ((tempVar instanceof QueriesCommandBase) ? tempVar : null);
        } catch (Exception e) {
            log.errorFormat("Command Factory: Failed to create command {0} using reflection\n. {1}", type, e);
            throw new RuntimeException(e);
        }
    }

    private static Class<CommandBase<? extends VdcActionParametersBase>> getCommandClass(String name, String suffix) {
        // try the cache first
        if (commandsCache.get(name + suffix) != null)
            return commandsCache.get(name + suffix);

        for (String commandPackage : COMMAND_PACKAGES) {
            String className = String.format("%1$s.%2$s%3$s", commandPackage, name, suffix);
            Class<CommandBase<?>> type = loadClass(className);
            if (type != null) {
                commandsCache.put(name + suffix, type); // update cache
                return type;
            }
        }

        // nothing found
        log.warn("Unable to find class for action: " + name + suffix);
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

    private static LogCompat log = LogFactoryCompat.getLog(CommandsFactory.class);
}
