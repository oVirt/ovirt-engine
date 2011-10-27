package org.ovirt.engine.ui.genericapi;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.ui.genericapi.parameters.UIQueryParametersBase;
import org.ovirt.engine.ui.genericapi.uiqueries.UIQueryBase;
import org.ovirt.engine.ui.genericapi.uiqueries.UIQueryType;

public class UICommandsFactory {

    private static final String QUERY_SUFFIX = "UIQuery";
    private static final String UI_QUERY_PACKAGE = "org.ovirt.engine.ui.genericapi.uiqueries";

    private static Map<String, Class<? extends UIQueryBase>> queryCache = new ConcurrentHashMap<String, Class<? extends UIQueryBase>>(
            VdcActionType.values().length);

    public static UIQueryBase CreateQueryCommand(UIQueryType query, UIQueryParametersBase parameters) {
        Class<? extends UIQueryBase> type = null;
        try {
            type = getQueryClass(query.name(), QUERY_SUFFIX);
            @SuppressWarnings("rawtypes")
            Class[] types = new Class[1];
            types[0] = parameters.getClass();

            Constructor<? extends UIQueryBase> info = findQueryConstructor(type);
            Object[] UIQueryParametersBase = new Object[1];
            UIQueryParametersBase[0] = parameters;
            Object tempVar = info.newInstance(parameters);
            return (UIQueryBase) ((tempVar instanceof UIQueryBase) ? tempVar : null);
        } catch (Exception e) {
            //TODO add log msg
            //log.errorFormat("ADD ERROR MSG", type, e);
            throw new RuntimeException(e);
        }
    }


    private static Class<? extends UIQueryBase> getQueryClass(String name, String suffix) {
        // try the cache first
        if (queryCache.get(name + suffix) != null)
            return queryCache.get(name + suffix);

        String className = String.format("%1$s.%2$s%3$s", UI_QUERY_PACKAGE, name, suffix);
        @SuppressWarnings("unchecked")
        Class<? extends UIQueryBase> type = loadClass(className);
        if (type != null) {
            queryCache.put(name + suffix, type); // update cache
            return type;
        }

        //TODO add logger for generic api
        // nothing found
        //log.warn("Unable to find class for action: " + name + suffix);
        return null;
    }


    @SuppressWarnings("unchecked")
    //assuming only one constructor.
    //If need to support more this code should change
    private static Constructor<? extends UIQueryBase> findQueryConstructor(Class<? extends UIQueryBase> type) {
        Constructor<?>[] constructors = type.getDeclaredConstructors();
        return (Constructor<? extends UIQueryBase>)constructors[0];
    }



    @SuppressWarnings("rawtypes")
    private static Class loadClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
