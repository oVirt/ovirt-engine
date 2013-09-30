package org.ovirt.engine.api.restapi.types;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.restapi.utils.GuidUtils;

public class MappingTestHelper {

    private static final String USAGES = "Usages";
    private static final String SLAVE_SINGLE = "HostNIC";
    private static final String SLAVES_PLURAL = "Slaves";
    private static final String FLOPPY_SINGLE = "Floppy";
    private static final String FLOPPIES_PLURAL = "Floppies";
    private static final String NIC_SINGLE = "NIC";
    private static final String NICS_PLURAL = "Nics";
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
        List<Class<?>> types = new ArrayList<Class<?>>();
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
                    shuffle(method, model);
                }
                else if(takesBigDecimal(method)) {
                    populateBigDecimal(method,model);
                } else if (takesXmlGregorianCalendar(method)) {
                    populateXmlGregorianCalendar(method, model);
                }
                else {
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
        return m.getParameterTypes().length == 1
                && (takesString(m) || takesBoolean(m) || takesShort(m) || takesInteger(m) || takesLong(m));
    }

    private static void random(Method m, Object model) throws Exception {
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
    }

    public static <E extends Enum> E shuffle(Class<E> enumType) {
        E[] values = enumType.getEnumConstants();
        return values[rand(values.length)];
    }

    private static void shuffle(Method method, Object model) throws Exception {
        Class<? extends Enum> enumType = (Class<? extends Enum>)method.getParameterTypes()[0];
        method.invoke(model, shuffle(enumType));
    }

    private static void descend(Method method, Object model, List<Class<?>> seen, int level) throws Exception {
        Object child = method.getParameterTypes()[0].newInstance();
        method.invoke(model, child);
        if (level == 1 || unseen(method, seen)) {
            populate(child, child.getClass(), seen, ++level);
        }
    }

    @SuppressWarnings("unchecked")
    private static void fill(Method method, Object model, List<Class<?>> seen, int level) throws Exception {
        // List<T> type parameter removed by erasure, hence we attempt to
        // infer from method name
        String elementType = method.getName().substring(GET_ROOT.length());
        Class<?> childType = getChildType(model, elementType);
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

    private static Class<?> getChildType(Object model, String elementType) throws ClassNotFoundException {
        if (isSpecialType(elementType)) {
            return handleSpecialType(elementType);
        } else {
            return coPackaged(model, elementType);
        }
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

    private static boolean isSpecialType(String elementType) {
        // Right now there's only one special case ('Usages'), but this
        // was assigned a method for prospective future special cases.
        return elementType.equals(USAGES);
    }

    private static Class<String> handleSpecialType(String elementType) {
        // Consider special case: "Usages" contains a list of Strings, not 'Usage' elements.
        if (elementType.equals(USAGES)) {
            return String.class;
        } // else... other special cases in the future.
        return String.class; // default which should never be reached.
    }

    private static String singular(String s) {
        return isSingularSpecialCase(s) ? handleSingularSpecialCase(s) :
                s.endsWith("s") ? s.substring(0, s.length() - 1) : s;
    }

    private static boolean isSingularSpecialCase(String s) {
        return s.equals(NICS_PLURAL) || s.equals(FLOPPIES_PLURAL) || s.equals(SLAVES_PLURAL);
    }

    private static String handleSingularSpecialCase(String s) {
        // 'Nics' is plural of 'NIC' (uppercase)
        // 'Floppies' is plural of 'Floppy' (not 'Floppie')
        // 'Slaves' is plural of 'HostNIC' (we don't have a 'Slave' entity)
        return s.equals(NICS_PLURAL) ? NIC_SINGLE : s.equals(FLOPPIES_PLURAL) ? FLOPPY_SINGLE
                : s.equals(SLAVES_PLURAL) ? SLAVE_SINGLE : s;
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
