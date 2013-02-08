package org.ovirt.engine.core.common.queries;

public class GetStoragePoolsByClusterServiceParameters extends GetEntitiesWithPermittedActionParameters {

    private static final long serialVersionUID = -4118891133830586523L;

    boolean supportsVirtService;
    boolean supportsGlusterService;

    public GetStoragePoolsByClusterServiceParameters() {
    }

    public boolean isSupportsVirtService() {
        return supportsVirtService;
    }

    public void setSupportsVirtService(boolean supportsVirtService) {
        this.supportsVirtService = supportsVirtService;
    }

    public boolean isSupportsGlusterService() {
        return supportsGlusterService;
    }

    public void setSupportsGlusterService(boolean supportsGlusterService) {
        this.supportsGlusterService = supportsGlusterService;
    }
}
