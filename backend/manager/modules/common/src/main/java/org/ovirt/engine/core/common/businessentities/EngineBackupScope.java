package org.ovirt.engine.core.common.businessentities;

public enum EngineBackupScope {

    DB("db"),
    FILES("files"),
    DWH("dwhdb"),
    @Deprecated
    CINDER("cinderlib"),
    KEYCLOAK("keycloak"),
    GRAFANA("grafanadb"),
    MANAGEDBLOCK("managedblock");

    String name;

    EngineBackupScope(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static EngineBackupScope fromString(String name) {
        for (EngineBackupScope scope : EngineBackupScope.values()) {
            if (scope.getName().equalsIgnoreCase(name)) {
                return scope;
            }
        }
        throw new IllegalArgumentException("No enum constant for name " + name);
    }
}
