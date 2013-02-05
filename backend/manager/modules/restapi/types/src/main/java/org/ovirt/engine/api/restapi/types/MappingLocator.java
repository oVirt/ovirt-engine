package org.ovirt.engine.api.restapi.types;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.api.common.util.PackageExplorer;
import org.ovirt.engine.api.restapi.utils.MalformedIdException;

/**
 * Discovers and manages type mappers.
 */
public class MappingLocator {

    private String discoverPackageName;
    private Map<ClassPairKey, Mapper<?, ?>> mappers;

    /**
     * Normal constructor used when injected
     */
    public MappingLocator() {
        mappers = new HashMap<ClassPairKey, Mapper<?, ?>>();
    }

    /**
     * Constructor intended only for testing.
     *
     * @param discoverPackageName
     *            package to look under
     */
    MappingLocator(String discoverPackageName) {
        this.discoverPackageName = discoverPackageName;
        mappers = new HashMap<ClassPairKey, Mapper<?, ?>>();
    }

    /**
     * Discover mappers and populate internal registry. The classloading
     * environment is scanned for classes contained under the
     * org.ovirt.engine.api.restapi.types package and exposing methods decorated
     * with the @Mapping annotation.
     */
    public void populate() {
        List<Class<?>> classes = PackageExplorer.discoverClasses(discoverPackageName != null ? discoverPackageName
                : this.getClass().getPackage().getName());
        for (Class<?> clz : classes) {
            for (Method method : clz.getMethods()) {
                Mapping mapping = method.getAnnotation(Mapping.class);
                if (mapping != null) {
                    mappers.put(new ClassPairKey(mapping.from(), mapping.to()),
                            new MethodInvokerMapper(method, mapping.to()));
                }
            }
        }
    }

    /**
     * Get an appropriate mapper mediating between the required types.
     *
     * @param <F>
     *            the from type
     * @param <T>
     *            the to type
     * @param from
     *            the from class
     * @param to
     *            the to class
     * @return a mapped instance of the to type
     */
    @SuppressWarnings("unchecked")
    public <F, T> Mapper<F, T> getMapper(Class<F> from, Class<T> to) {
        return (Mapper<F, T>) mappers.get(new ClassPairKey(from, to));
    }

    private static class ClassPairKey {
        private Class<?> from, to;

        private ClassPairKey(Class<?> from, Class<?> to) {
            this.from = from;
            this.to = to;
        }

        public int hashCode() {
            return to.hashCode() + from.hashCode();
        }

        public boolean equals(Object other) {
            if (other == this) {
                return true;
            } else if (other instanceof ClassPairKey) {
                ClassPairKey key = (ClassPairKey) other;
                return to == key.to && from == key.from;
            }
            return false;
        }

        public String toString() {
            return "map from: " + from + " to: " + to;
        }
    }

    private static class MethodInvokerMapper implements Mapper<Object, Object> {
        private Method method;
        private Class<?> to;

        private MethodInvokerMapper(Method method, Class<?> to) {
            this.method = method;
            this.to = to;
        }

        @Override
        public Object map(Object from, Object template) {
            Object ret = null;
            try {
                // REVISIT support non-static mapping methods also
                ret = method.invoke(null, from, template);
            } catch (InvocationTargetException ite) {
              if (ite.getTargetException() instanceof MalformedIdException) {
                   throw (MalformedIdException) ite.getTargetException();
              }
            } catch (Exception e) {
                // REVISIT logging, fallback null-mapping
                e.printStackTrace();
            }
            return to.cast(ret);
        }

        public String toString() {
            return "map to: " + to + " via " + method;
        }
    }
}
