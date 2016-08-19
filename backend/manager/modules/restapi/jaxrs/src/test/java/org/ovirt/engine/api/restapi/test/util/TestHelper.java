package org.ovirt.engine.api.restapi.test.util;

import static org.mockito.Matchers.argThat;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.hamcrest.Description;
import org.mockito.ArgumentMatcher;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

public class TestHelper {

    public static VdcActionParametersBase eqActionParams(
            Class<? extends VdcActionParametersBase> clz, String[] names, Object[] values) {
        return argThat(new ActionParametersEquals(clz, names, values));
    }

    protected static Method getMethod(Class<?> clz, String name) throws NoSuchMethodException {
        return clz.getMethod("get" + name);
    }

    protected static Method getMethod(Class<?> clz, String name, Object value) throws NoSuchMethodException {
        Method method = null;
        try {
            method = getMethod(clz, name);
        } catch (NoSuchMethodException nsme) {
            if (Boolean.class.equals(value.getClass())) {
                method = clz.getMethod("is" + name);
            } else {
                throw nsme;
            }
        }
        return method;
    }

    protected static boolean matches(Object lhs, Object rhs) {
        boolean matches = true;
        if (lhs instanceof List && rhs instanceof Iterable) {
            List<?> llhs = (List<?>)lhs;
            Iterator<?> irhs = ((Iterable<?>)rhs).iterator();
            int count = 0;
            while (irhs.hasNext()) {
                count++;
                matches = llhs.contains(irhs.next());
                if (!matches) {
                    break;
                }
            }
            if (matches) {
                matches = count == llhs.size();
            }
        }
        else if (lhs instanceof byte[] && rhs instanceof byte[]) {
            byte[] lhsBytes = (byte[]) lhs;
            byte[] rhsBytes = (byte[]) rhs;
            matches = lhsBytes.length == rhsBytes.length;
            if (matches) {
                matches = matches && Arrays.equals(lhsBytes, rhsBytes);
            }
        }
        else {
            matches = Objects.equals(lhs, rhs);
        }
        return matches;
    }

    protected static boolean matches(Class<?> clz, Object instance, String name, Object value) {
        try {
            if (!topLevel(name)) {
                instance = getMethod(clz, superField(name)).invoke(instance);
                clz = instance.getClass();
                name = subField(name);
            }
            return matches(value, getMethod(clz, name, value).invoke(instance));
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean topLevel(String name) {
        return !name.contains(".");
    }

    private static String superField(String name) {
        return name.substring(0, name.indexOf("."));
    }

    private static String subField(String name) {
        return name.substring(name.indexOf(".") + 1);
    }

    /**
     * This generic matcher for ActionParameters is required because this types
     * don't override Object.equals() with a deep comparison
     */
    protected static class ActionParametersEquals extends ArgumentMatcher<VdcActionParametersBase> {
        Class<? extends VdcActionParametersBase> clz;
        String[] names;
        Object[] values;

        public ActionParametersEquals(Class<? extends VdcActionParametersBase> clz, String[] names,
                Object[] values) {
            this.clz = clz;
            this.names = names;
            this.values = values;
        }

        @Override
        public boolean matches(Object actual) {
            if (clz.isInstance(actual)) {
                for (int i = 0; i < names.length; i++) {
                    if (!TestHelper.matches(clz, actual, names[i], values[i])) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("eqActionParams(");
            for (int i = 0; i < names.length; i++) {
                if (i > 0) {
                    description.appendText(", ");
                }
                description.appendText(names[i]);
                description.appendText("=");
                description.appendText(String.valueOf(values[i]));
            }
            description.appendText(")");
        }
    }

    public static SearchParameters eqSearchParams(SearchParameters expected) {
        return argThat(new SearchParametersEquals(expected));
    }

    /**
     * This specialized matcher for SearchParameters is required because this
     * type doesn't override Object.equals() with a deep comparison
     */
    protected static class SearchParametersEquals extends ArgumentMatcher<SearchParameters> {
        private SearchParameters expected;

        public SearchParametersEquals(SearchParameters expected) {
            this.expected = expected;
        }

        @Override
        public boolean matches(Object actual) {
            boolean ret = false;
            if (actual instanceof SearchParameters) {
                SearchParameters other = (SearchParameters) actual;
                ret = expected.getSearchPattern().equals(other.getSearchPattern())
                        && expected.getSearchTypeValue().equals(other.getSearchTypeValue());
            }
            return ret;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("eqSearchParams(");
            description.appendText(expected.getSearchPattern());
            description.appendText(" on type ");
            description.appendText(expected.getSearchTypeValue() != null ? expected.getSearchTypeValue()
                    .toString() : "none");
        }
    }

    public static VdcQueryParametersBase eqQueryParams(Class<? extends VdcQueryParametersBase> clz,
            String[] names, Object[] values) {
        return argThat(new QueryParametersEquals(clz, names, values));
    }

    /**
     * This generic matcher for QueryParameters is required because this types
     * don't override Object.equals() with a deep comparison
     */
    protected static class QueryParametersEquals extends ArgumentMatcher<VdcQueryParametersBase> {
        Class<? extends VdcQueryParametersBase> clz;
        String[] names;
        Object[] values;

        public QueryParametersEquals(Class<? extends VdcQueryParametersBase> clz, String[] names,
                Object[] values) {
            this.clz = clz;
            this.names = names;
            this.values = values;
        }

        @Override
        public boolean matches(Object actual) {
            if (clz.isInstance(actual)) {
                for (int i = 0; i < names.length; i++) {
                    if (!TestHelper.matches(clz, actual, names[i], values[i])) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("eqQueryParams(");
            for (int i = 0; i < names.length; i++) {
                if (i > 0) {
                    description.appendText(", ");
                }
                description.appendText(names[i]);
                description.appendText("=");
                description.appendText(String.valueOf(values[i]));
            }
            description.appendText(")");
        }
    }
}
