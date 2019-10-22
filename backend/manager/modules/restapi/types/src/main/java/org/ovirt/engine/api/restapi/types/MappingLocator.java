/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.types;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.ws.rs.WebApplicationException;

import org.ovirt.engine.api.common.util.PackageExplorer;
import org.ovirt.engine.api.restapi.utils.MalformedIdException;
import org.ovirt.engine.api.restapi.utils.MappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovers and manages type mappers.
 */
public class MappingLocator {
    /**
     * The logger used by this class.
     */
    private static final Logger log = LoggerFactory.getLogger(MappingLocator.class);

    private String discoverPackageName;
    private Map<ClassPairKey, Mapper<?, ?>> mappers;

    /**
     * Normal constructor used when injected
     */
    public MappingLocator() {
        mappers = new HashMap<>();
    }

    /**
     * Constructor intended only for testing.
     *
     * @param discoverPackageName
     *            package to look under
     */
    MappingLocator(String discoverPackageName) {
        this.discoverPackageName = discoverPackageName;
        mappers = new HashMap<>();
    }

    /**
     * Discover mappers and populate internal registry. The classloading
     * environment is scanned for classes contained under the
     * org.ovirt.engine.api.restapi.types package and exposing methods decorated
     * with the @Mapping annotation.
     */
    public void populate() {
        String packageName = discoverPackageName != null? discoverPackageName: this.getClass().getPackage().getName();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        List<String> classNames = PackageExplorer.discoverClasses(packageName);
        for (String className : classNames) {
            try {
                Class<?> mapperClass = classLoader.loadClass(className);
                for (Method method : mapperClass.getMethods()) {
                    Mapping mapping = method.getAnnotation(Mapping.class);
                    if (mapping != null) {
                        mappers.put(new ClassPairKey(mapping.from(), mapping.to()),
                            new MethodInvokerMapper(method, mapping.to()));
                    }
                }
            } catch (ClassNotFoundException exception) {
                log.error(
                    "Error while trying to load mapper class \"{}\".",
                    className,
                    exception
                );
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
        private Class<?> from;
        private Class<?> to;

        private ClassPairKey(Class<?> from, Class<?> to) {
            this.from = from;
            this.to = to;
        }

        public int hashCode() {
            return Objects.hash(
                    to,
                    from
            );
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ClassPairKey)) {
                return false;
            }
            ClassPairKey other = (ClassPairKey) obj;
            return Objects.equals(to, other.to)
                    && Objects.equals(from, other.from);
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
            try {
                // REVISIT support non-static mapping methods also
                return to.cast(method.invoke(null, from, template));
            } catch (InvocationTargetException ite) {
              if (ite.getTargetException() instanceof MalformedIdException) {
                   throw (MalformedIdException) ite.getTargetException();
              } else if (ite.getTargetException() instanceof WebApplicationException) {
                  throw (WebApplicationException) ite.getTargetException();
              } else {
                  throw new MappingException(ite);
              }
            } catch (IllegalAccessException e) {
                throw new MappingException(e);
            }
        }

        public String toString() {
            return "map to: " + to + " via " + method;
        }
    }
}
