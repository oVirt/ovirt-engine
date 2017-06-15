package org.ovirt.engine.ui.webadmin.plugin.jsni;

import org.ovirt.engine.ui.webadmin.plugin.jsni.JsFunction.ErrorHandler;

import com.google.gwt.core.client.JsArray;

/**
 * Helper class that stores {@link JsFunction} invocation result information for later use.
 */
public class JsFunctionResultHelper {

    private final JsFunction function;

    // Function invocation result information
    private Object resultValue;
    private String resultType;

    public JsFunctionResultHelper(JsFunction function) {
        this.function = function;
    }

    /**
     * Convenience method combining {@link #invokeAsString} and {@link #getResultAsString}.
     * <p>
     * All method parameters are optional (can be {@code null}).
     */
    public static String invokeAndGetResultAsString(JsFunction function, JsArray<?> args, ErrorHandler errorHandler, String defaultValue) {
        if (function == null) {
            return defaultValue;
        }

        JsFunctionResultHelper helper = new JsFunctionResultHelper(function);
        if (helper.invokeAsString(args, errorHandler)) {
            String result = helper.getResultAsString();
            if (result != null) {
                return result;
            }
        }

        return defaultValue;
    }

    /**
     * Convenience method combining {@link #invokeAsNumber} and {@link #getResultAsNumber}.
     * <p>
     * All method parameters are optional (can be {@code null}).
     */
    public static double invokeAndGetResultAsNumber(JsFunction function, JsArray<?> args, ErrorHandler errorHandler, double defaultValue) {
        if (function == null) {
            return defaultValue;
        }

        JsFunctionResultHelper helper = new JsFunctionResultHelper(function);
        if (helper.invokeAsNumber(args, errorHandler)) {
            Double result = helper.getResultAsNumber();
            if (result != null) {
                return result.doubleValue();
            }
        }

        return defaultValue;
    }

    /**
     * Convenience method combining {@link #invokeAsBoolean} and {@link #getResultAsBoolean}.
     * <p>
     * All method parameters are optional (can be {@code null}).
     */
    public static boolean invokeAndGetResultAsBoolean(JsFunction function, JsArray<?> args, ErrorHandler errorHandler, boolean defaultValue) {
        if (function == null) {
            return defaultValue;
        }

        JsFunctionResultHelper helper = new JsFunctionResultHelper(function);
        if (helper.invokeAsBoolean(args, errorHandler)) {
            Boolean result = helper.getResultAsBoolean();
            if (result != null) {
                return result.booleanValue();
            }
        }

        return defaultValue;
    }

    void clearResultInformation() {
        this.resultValue = null;
        this.resultType = null;
    }

    /**
     * Invokes the native function, expecting String return value.
     *
     * @see JsFunction#invoke(JsArray, String, org.ovirt.engine.ui.webadmin.plugin.jsni.JsFunction.ResultHandler, ErrorHandler)
     * @see #getResultAsString
     */
    public boolean invokeAsString(JsArray<?> args, ErrorHandler errorHandler) {
        clearResultInformation();
        return function.invoke(args, JsFunction.RESULT_TYPE_STRING, (String result) -> {
            JsFunctionResultHelper.this.resultValue = result;
            JsFunctionResultHelper.this.resultType = JsFunction.RESULT_TYPE_STRING;
        }, errorHandler);
    }

    public String getResultAsString() {
        return JsFunction.RESULT_TYPE_STRING.equals(resultType) ? (String) resultValue : null;
    }

    /**
     * Invokes the native function, expecting Double return value.
     *
     * @see JsFunction#invoke(JsArray, String, org.ovirt.engine.ui.webadmin.plugin.jsni.JsFunction.ResultHandler, ErrorHandler)
     * @see #getResultAsNumber
     */
    public boolean invokeAsNumber(JsArray<?> args, ErrorHandler errorHandler) {
        clearResultInformation();
        return function.invoke(args, JsFunction.RESULT_TYPE_NUMBER, (Double result) -> {
            JsFunctionResultHelper.this.resultValue = result;
            JsFunctionResultHelper.this.resultType = JsFunction.RESULT_TYPE_NUMBER;
        }, errorHandler);
    }

    public Double getResultAsNumber() {
        return JsFunction.RESULT_TYPE_NUMBER.equals(resultType) ? (Double) resultValue : null;
    }

    /**
     * Invokes the native function, expecting Boolean return value.
     *
     * @see JsFunction#invoke(JsArray, String, org.ovirt.engine.ui.webadmin.plugin.jsni.JsFunction.ResultHandler, ErrorHandler)
     * @see #getResultAsBoolean
     */
    public boolean invokeAsBoolean(JsArray<?> args, ErrorHandler errorHandler) {
        clearResultInformation();
        return function.invoke(args, JsFunction.RESULT_TYPE_BOOLEAN, (Boolean result) -> {
            JsFunctionResultHelper.this.resultValue = result;
            JsFunctionResultHelper.this.resultType = JsFunction.RESULT_TYPE_BOOLEAN;
        }, errorHandler);
    }

    public Boolean getResultAsBoolean() {
        return JsFunction.RESULT_TYPE_BOOLEAN.equals(resultType) ? (Boolean) resultValue : null;
    }

}
