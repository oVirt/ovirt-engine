package org.ovirt.engine.core.utils;

public class CommandParametersInitializer implements Cloneable {
    public java.util.HashMap<java.lang.Class, java.util.LinkedList<String>> mParameters =
            new java.util.HashMap<java.lang.Class, java.util.LinkedList<String>>();

    public final void AddParameter(java.lang.Class type, String parameterName) {
        java.util.LinkedList<String> values = null;
        if (!((values = mParameters.get(type)) != null)) {
            values = new java.util.LinkedList<String>();
            mParameters.put(type, values);
        }
        if (!values.contains(parameterName)) {
            values.offer(parameterName);
        }
    }

    public final void AddParameters(java.lang.Class type, Iterable<String> parameterNames) {
        java.util.LinkedList<String> values = null;
        if (!((values = mParameters.get(type)) != null)) {
            values = new java.util.LinkedList<String>();
            mParameters.put(type, values);
        }
        for (String param : parameterNames) {
            values.offer(param);
        }
    }

    public final void InitializeParameter(Object obj, Object value) {
        java.lang.Class type = obj.getClass();
        java.util.LinkedList<String> values = null;
        if ((values = mParameters.get(value.getClass())) != null && values.size() != 0) {
            try {
                String paramName = values.poll();
                java.lang.reflect.Field field = type.getField(paramName);
                if (field != null) {
                    try {
                        field.set(obj, value);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            } catch (Exception e) {
                throw new VdcException(e);
            }
        }
    }

    public final Object clone() {
        CommandParametersInitializer newInstance = new CommandParametersInitializer();
        for (java.lang.Class type : mParameters.keySet()) {
            java.util.LinkedList<String> values = mParameters.get(type);
            newInstance.AddParameters(type, values);
        }
        return newInstance;
    }
}
