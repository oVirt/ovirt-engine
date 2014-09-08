package org.ovirt.engine.api.restapi.utils.aaa;

import java.util.Collection;

public class AuthzUtils {

    public static String getAuthzNameFromEntityName(String entityName, Collection<String> authzProvidersNames) {
        String result = null;
        if (entityName != null && entityName.contains("@")) {
            String lastPart = entityName.substring(entityName.lastIndexOf('@') + 1);
            result = authzProvidersNames.contains(lastPart) ? lastPart : null;
        }
        return result;
    }


    public static String getEntityNameWithoutAuthz(String entityName, String authzProviderName) {
        String result = entityName;
       if (entityName != null && entityName.contains("@") && entityName.lastIndexOf('@') < entityName.length() -1) {
           result = entityName.substring(entityName.lastIndexOf('@')+1).equals(authzProviderName) ? entityName.substring(0,  entityName.lastIndexOf('@')) : entityName;
       }
       return result;
    }
}
