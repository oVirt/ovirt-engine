package org.ovirt.engine.ui.webadmin.plugin.jsni;

import java.util.logging.Logger;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

/**
 * Simple wrapper around a native JS function object.
 */
public final class JsFunction extends JavaScriptObject {

    /**
     * Callback interface for handling results from native function invocation.
     */
    public interface ResultHandler<T> {

        void onResult(T result);

    }

    /**
     * Callback interface for handling errors during native function invocation.
     */
    public interface ErrorHandler {

        void onError(String message);

    }

    private static final Logger logger = Logger.getLogger(JsFunction.class.getName());

    private static final ErrorHandler fallbackErrorHandler = message -> {
        logger.severe("Error while invoking function: " + message); //$NON-NLS-1$
    };

    public static final String RESULT_TYPE_STRING = "string"; //$NON-NLS-1$
    public static final String RESULT_TYPE_NUMBER = "number"; //$NON-NLS-1$
    public static final String RESULT_TYPE_BOOLEAN = "boolean"; //$NON-NLS-1$

    protected JsFunction() {
    }

    /**
     * Retrieves a function object from the given {@code owner} object.
     * <p>
     * Returns an empty (no-op) function in following situations:
     * <ul>
     * <li>{@code owner} object is {@code null}
     * <li>requested function object is not present in {@code owner} object
     * <li>requested function object is present in {@code owner} object, but has wrong type (i.e. not a function)
     * </ul>
     * <p>
     * As a result, this method will never return {@code null}.
     */
    public static native JsFunction get(JavaScriptObject owner, String functionName) /*-{
        var targetFunction = (owner != null) ? owner[functionName] : null;

        if (targetFunction != null && typeof targetFunction === 'function') {
            return targetFunction;
        }

        return function() {};
    }-*/;

    /**
     * Invokes the native function, without expecting return value.
     * <p>
     * All method parameters are optional (can be {@code null}).
     * <p>
     * Returns {@code true} if the function completed successfully, or {@code false} if an exception escaped the
     * function call.
     */
    public boolean invoke(JsArray<?> args, ErrorHandler errorHandler) {
        return invoke(args, null, null, errorHandler);
    }

    /**
     * Invokes the native function.
     * <p>
     * All method parameters are optional (can be {@code null}). Following result types are supported:
     * <ul>
     * <li>{@value #RESULT_TYPE_STRING}, maps to {@code ResultHandler<String>}
     * <li>{@value #RESULT_TYPE_NUMBER}, maps to {@code ResultHandler<Double>}
     * <li>{@value #RESULT_TYPE_BOOLEAN}, maps to {@code ResultHandler<Boolean>}
     * </ul>
     * <p>
     * Returns {@code true} if the function completed successfully, or {@code false} if an exception escaped the
     * function call.
     */
    public native <T> boolean invoke(JsArray<?> args, String resultType, ResultHandler<T> resultHandler, ErrorHandler errorHandler) /*-{
        var wrappedFunction = this;

        var invokeWrappedFunction = function() {
            if (typeof wrappedFunction === 'function') {
                return wrappedFunction.apply(this, args);
            } else {
                // If the wrapped object is not a JavaScript function, use null as invocation result
                return null;
            }
        };

        var handleInvocationResult = function(result) {
            if (resultHandler != null && resultType != null && (typeof result === resultType || result === null)) {
                switch (resultType) {
                    case 'string':
                        resultHandler.@org.ovirt.engine.ui.webadmin.plugin.jsni.JsFunction.ResultHandler::onResult(Ljava/lang/Object;)(result);
                        break;
                    case 'number':
                        // All native JavaScript numeric values are implicitly double-precision
                        resultHandler.@org.ovirt.engine.ui.webadmin.plugin.jsni.JsFunction.ResultHandler::onResult(Ljava/lang/Object;)(@java.lang.Double::valueOf(D)(result));
                        break;
                    case 'boolean':
                        resultHandler.@org.ovirt.engine.ui.webadmin.plugin.jsni.JsFunction.ResultHandler::onResult(Ljava/lang/Object;)(@java.lang.Boolean::valueOf(Z)(result));
                        break;
                }
            }
        };

        var handleInvocationError = function(error) {
            var actualErrorHandler = errorHandler || @org.ovirt.engine.ui.webadmin.plugin.jsni.JsFunction::fallbackErrorHandler;
            actualErrorHandler.@org.ovirt.engine.ui.webadmin.plugin.jsni.JsFunction.ErrorHandler::onError(Ljava/lang/String;)(error.toString());
        };

        try {
            handleInvocationResult(invokeWrappedFunction());
            return true;
        } catch (e) {
            handleInvocationError(e);
            return false;
        }
    }-*/;

}
