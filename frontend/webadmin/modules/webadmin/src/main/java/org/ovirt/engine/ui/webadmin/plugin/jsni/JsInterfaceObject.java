package org.ovirt.engine.ui.webadmin.plugin.jsni;

/**
 * Base class for JS objects implementing a contract (interface) by declaring corresponding native functions.
 * <p>
 * Unlike the traditional concept of interface abstract type in object-oriented languages, an <em>interface object</em>
 * does not necessarily have to declare all functions of the given interface in order to "implement" such interface. In
 * fact, an empty object can be used as a valid interface object. Missing functions will be treated as empty (no-op)
 * functions with default return values, as defined by the contract of such functions.
 * <p>
 * An interface object can "implement" multiple interfaces by declaring functions of those interfaces.
 */
public abstract class JsInterfaceObject extends JsObjectWithProperties {

    protected JsInterfaceObject() {
    }

    /**
     * Returns the given function contained in this interface object, or an empty (no-op) function.
     */
    protected final JsFunction getFunction(String functionName) {
        return JsFunction.get(this, functionName);
    }

}
