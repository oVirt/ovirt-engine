package org.ovirt.engine.api.restapi.types;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.utils.RandomUtils;

public class MappingTestHelper {

    private static final String SET_ROOT = "set";
    private static final String GET_ROOT = "get";

    /**
     * Populate a JAXB model type by recursively walking element tree and
     * setting leaf nodes to randomized values.
     *
     * @param clz
     *            the model type
     * @return a populated instance
     */
    public static Object populate(Class<?> clz) throws Exception {
        List<Class<?>> seen = getSetMethodTypes(clz);
        return populate(instantiate(clz), clz, seen, 1);
    }

    private static List<Class<?>> getSetMethodTypes(Class<?> clz) {
        List<Class<?>> types = new ArrayList<>();
        for (Method method : clz.getMethods()) {
            if (isSetter(method)) {
                Class<?> type = method.getParameterTypes()[0];
                if (BaseResource.class.isAssignableFrom(type) && !types.contains(type)) {
                    types.add(type);
                }
            }
        }
        return types;
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
    public static Object populate(Object model, Class<?> clz, List<Class<?>> seen, int level) throws Exception {
        for (Method method : clz.getMethods()) {
            if (isSetter(method)) {
                if (takesPrimitive(method)) {
                    random(method, model);
                } else if (takesEnum(method)) {
                    //do nothing
                } else if(takesBigDecimal(method)) {
                    populateBigDecimal(method, model);
                } else if (takesXmlGregorianCalendar(method)) {
                    populateXmlGregorianCalendar(method, model);
                } else {
                    descend(method, model, scope(seen), level);
                }
            } else if (isGetter(method) && returnsList(method)) {
                fill(method, model, seen, level);
            }
        }
        return model;
    }

    private static void populateBigDecimal(Method method, Object model) throws Exception {
        method.invoke(model, new BigDecimal(rand(100)));
    }

    private static void populateXmlGregorianCalendar(Method method, Object model) throws Exception {
        method.invoke(model, DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar(1111, 10, 29)));
    }

    private static Object instantiate(Class<?> clz) throws Exception {
        Object model = null;
        model = clz.newInstance();
        return model;
    }

    private static boolean takesPrimitive(Method m) {
        return m.getParameterTypes().length == 1 && (
            takesString(m) ||
            takesBoolean(m) ||
            takesShort(m) ||
            takesInteger(m) ||
            takesLong(m) ||
            takesDouble(m)
        );
    }

    private static void random(Method m, Object model) throws Exception {
        Object value = null;
        if (takesString(m)) {
            value = garble(m);
        } else if (takesShort(m)) {
            value = (short) rand(100);
        } else if (takesInteger(m)) {
            value = rand(100);
        } else if (takesLong(m)) {
            value = (long) rand(1000000000);
        } else if (takesBoolean(m)) {
            value = RandomUtils.instance().nextBoolean();
        } else if (takesDouble(m)) {
            value = RandomUtils.instance().nextDouble();
        }
        if (value != null) {
            m.invoke(model, value);
        }
    }

    private static <E extends Enum<E>> EnumSet<E> complementOf(Class<E> enumType, E[] excludeValues) {
        final EnumSet<E> result = EnumSet.allOf(enumType);
        result.removeAll(Arrays.asList(excludeValues));
        return result;
    }

    private static void descend(Method method, Object model, List<Class<?>> seen, int level) throws Exception {
        Object child = method.getParameterTypes()[0].newInstance();
        method.invoke(model, child);
        if (level == 1 || (unseen(method, seen) && (level <= 3))) {
            populate(child, child.getClass(), seen, ++level);
        }
    }

    @SuppressWarnings("unchecked")
    private static void fill(Method method, Object model, List<Class<?>> seen, int level) throws Exception {
        ParameterizedType returnType = (ParameterizedType) method.getGenericReturnType();
        Class<?> childType = (Class<?>) returnType.getActualTypeArguments()[0];
        if (level == 1 || unseen(childType, seen)) {
            List<Object> list = (List<Object>) method.invoke(model);
            Object child = null;
            if (childType.isEnum()) {
                Object[] labels = childType.getEnumConstants();
                child = labels[rand(labels.length)];
            } else {
                child = childType.newInstance();
            }
            list.add(child);
            populate(child, child.getClass(), seen, ++level);
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

    private static boolean takesDouble(Method m) {
        return Double.TYPE.equals(m.getParameterTypes()[0])
            || Double.class.equals(m.getParameterTypes()[0]);
    }

    private static boolean takesBigDecimal(Method m) {
        return BigDecimal.class.equals(m.getParameterTypes()[0]);
    }

    private static boolean takesXmlGregorianCalendar(Method m) {
        return XMLGregorianCalendar.class.equals(m.getParameterTypes()[0]);
    }

    private static boolean takesEnum(Method m) {
        return m.getParameterTypes()[0].isEnum();
    }

    private static boolean returnsList(Method m) {
        return List.class.equals(m.getReturnType());
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
        return new ArrayList<>(seen);
    }

    public static int rand(int ceiling) {
        return RandomUtils.instance().nextInt(ceiling);
    }

    private static Object garble(Method m) {
        return m.getName().endsWith("Id") ? GuidUtils.asGuid(UUID.randomUUID().toString()).toString()
                : new String(new byte[] { (byte) (65 + rand(26)), (byte) (65 + rand(26)),
                        (byte) (65 + rand(26)) });
    }
}
