package org.ovirt.engine.core.common.queries;

public class GetDataCentersWithPermittedActionOnClustersParameters extends GetEntitiesWithPermittedActionParameters {

    private static final long serialVersionUID = -6927484770394754188L;

    boolean supportsVirtService;
    boolean supportsGlusterService;

    public GetDataCentersWithPermittedActionOnClustersParameters() {
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
