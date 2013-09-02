package org.ovirt.engine.api.restapi.types;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class MappingTestHelper {

    private static final String SET_ROOT = "set";
    private static final String GET_ROOT = "get";

    private static final Log logger = LogFactory.getLog(MappingTestHelper.class);

    /**
     * Populate a JAXB model type by recursively walking element tree and
     * setting leaf nodes to randomized values.
     *
     * @param clz
     *            the model type
     * @return a populated instance
     */
    public static Object populate(Class<?> clz) {
        return populate(instantiate(clz), clz, new ArrayList<Class<?>>());
    }

    /**
     * Populate a JAXB model type by recursively walking element tree and
     * setting leaf nodes to randomized values.
     *
     * @param model
     *            the model instance
     * @param clz
     *            the model type
     * @param seen
     *            model types seen so far
     * @return a populated instance
     */
    public static Object populate(Object model, Class<?> clz, List<Class<?>> seen) {

        for (Method method : clz.getMethods()) {
            if (isSetter(method)) {
                if (takesPrimitive(method)) {
                    random(method, model);
                } else if (takesEnum(method)) {
                    shuffle(method, model);
                }
                else if(takesBigDecimal(method)) {
                    populateBigDecimal(method,model);
                }
                else {
                    descend(method, model, scope(seen));
                }
            } else if (isGetter(method) && returnsList(method)) {
                fill(method, model, seen);
            }
        }
        return model;
    }

    private static void populateBigDecimal(Method method, Object model) {
        try {
            method.invoke(model, new BigDecimal(rand(100)));
        } catch (Exception e) {
           logger.error("Failed to populate big decimal in " + method.getDeclaringClass() + "." + method.getName(),e);
        }
    }

    private static Object instantiate(Class<?> clz) {
        Object model = null;
        try {
            model = clz.newInstance();
        } catch (Exception e) {
            // should never occur, trivial instantiation
            logger.error("Failed to instantiate class " + clz.getSimpleName(),e);
        }
        return model;
    }

    private static boolean takesPrimitive(Method m) {
        return m.getParameterTypes().length == 1
                && (takesString(m) || takesBoolean(m) || takesShort(m) || takesInteger(m) || takesLong(m));
    }

    private static void random(Method m, Object model) {
        try {
            m.invoke(
                     model,
                     takesString(m)
                     ? garble(m)
                     : takesShort(m)
                       ? Short.valueOf((short) rand(100))
                       : takesInteger(m)
                         ? Integer.valueOf(rand(100))
                         : takesLong(m)
                            ? Long.valueOf(rand(1000000000))
                            : takesBoolean(m)
                              ? Boolean.valueOf(Math.random() < 0.5D)
                              : null);
        } catch (Exception e) {
            // simple setter, exception should not be thrown
        }
    }

    public static <E extends Enum> E shuffle(Class<E> enumType) {
        E[] values = enumType.getEnumConstants();
        return values[rand(values.length)];
    }

    private static void shuffle(Method method, Object model) {
        Class<? extends Enum> enumType = (Class<? extends Enum>)method.getParameterTypes()[0];
        try {
            method.invoke(model, shuffle(enumType));
        } catch (Exception e) {
            // simple setter, exception should not be thrown
        }
    }

    private static void descend(Method method, Object model, List<Class<?>> seen) {
        try {
            Object child = method.getParameterTypes()[0].newInstance();
            method.invoke(model, child);
            if (unseen(method, seen)) {
                populate(child, child.getClass(), seen);
            }
        } catch (Exception e) {
            // simple setter, exception should not be thrown
        }
    }

    @SuppressWarnings("unchecked")
    private static void fill(Method method, Object model, List<Class<?>> seen) {
        try {
            // List<T> type parameter removed by erasure, hence we attempt to
            // infer from method name
            String elementType = method.getName().substring(GET_ROOT.length());
            Class<?> childType = coPackaged(model, elementType);
            if (unseen(childType, seen)) {
                List<Object> list = (List<Object>) method.invoke(model);
                Object child = null;
                if (childType.isEnum()) {
                    Object[] labels = childType.getEnumConstants();
                    child = labels[rand(labels.length)];
                } else {
                    child = childType.newInstance();
                }
                list.add(child);
                populate(child, child.getClass(), seen);
            }
        } catch (Exception e) {
            // simple getter, exception should not be thrown
        }
    }

    private static boolean isGetter(Method m) {
        return m.getName().startsWith(GET_ROOT);
    }

    private static boolean isSetter(Method m) {
        return m.getName().startsWith(SET_ROOT);
    }

    private static boolean takesString(Method m) {
        return String.class.equals(m.getParameterTypes()[0]);
    }

    private static boolean takesShort(Method m) {
        return Short.TYPE.equals(m.getParameterTypes()[0])
                || Short.class.equals(m.getParameterTypes()[0]);
    }

    private static boolean takesInteger(Method m) {
        return Integer.TYPE.equals(m.getParameterTypes()[0])
                || Integer.class.equals(m.getParameterTypes()[0]);
    }

    private static boolean takesLong(Method m) {
        return Long.TYPE.equals(m.getParameterTypes()[0])
                || Long.class.equals(m.getParameterTypes()[0]);
    }

    private static boolean takesBoolean(Method m) {
        return Boolean.TYPE.equals(m.getParameterTypes()[0])
                || Boolean.class.equals(m.getParameterTypes()[0]);
    }

    private static boolean takesBigDecimal(Method m) {
        return BigDecimal.class.equals(m.getParameterTypes()[0]);
    }

    private static boolean takesEnum(Method m) {
        return m.getParameterTypes()[0].isEnum();
    }

    private static boolean returnsList(Method m) {
        return List.class.equals(m.getReturnType());
    }

    private static Class<?> coPackaged(Object model, String elementType) throws ClassNotFoundException {
        String packageRoot = model.getClass().getPackage().getName() + ".";
        try {
            return Class.forName(packageRoot + singular(elementType));
        } catch (ClassNotFoundException cnf) {
            try {
                return Class.forName(packageRoot + elementType);
            } catch (ClassNotFoundException cnfe) {
                // try inner class
                return Class.forName(model.getClass().getName() + "$" + elementType);
            }
        }
    }

    private static String singular(String s) {
        return s.endsWith("s") ? s.substring(0, s.length() - 1) : s;
    }

    private static boolean unseen(Class<?> type, List<Class<?>> seen) {
        boolean ret = !seen.contains(type);
        if (ret) {
            seen.add(type);
        }
        return ret;
    }

    private static boolean unseen(Method m, List<Class<?>> seen) {
        return unseen(m.getParameterTypes()[0], seen);
    }

    private static List<Class<?>> scope(List<Class<?>> seen) {
        return new ArrayList<Class<?>>(seen);
    }

    public static int rand(int ceiling) {
        return (int) Math.floor(Math.random() * 0.9999 * ceiling);
    }

    private static Object garble(Method m) {
        return m.getName().endsWith("Id") ? GuidUtils.asGuid(UUID.randomUUID().toString()).toString()
                : new String(new byte[] { (byte) (65 + rand(26)), (byte) (65 + rand(26)),
                        (byte) (65 + rand(26)) });
    }
}
