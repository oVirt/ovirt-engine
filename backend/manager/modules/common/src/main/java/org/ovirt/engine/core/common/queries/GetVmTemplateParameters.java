package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetVmTemplateParameters extends QueryParametersBase {
    private static final long serialVersionUID = 8906662143775124331L;

    private Guid _id;
    private String _name;
    private Guid clusterId;
    private Guid dataCenterId;

    public GetVmTemplateParameters(Guid id) {
        _id = id;
    }

    public GetVmTemplateParameters(String name) {
        this(Guid.Empty);
        _name = name;
    }

    public Guid getId() {
        return _id;
    }

    public String getName() {
        return _name;
    }

    public GetVmTemplateParameters() {
    }

    public Guid getDataCenterId() {
        return dataCenterId;
    }

    public void setDataCenterId(Guid dataCenterId) {
        this.dataCenterId = dataCenterId;
    }

    public Guid getClusterId() {
        return clusterId;
    }

    public void setClusterId(Guid clusterId) {
        this.clusterId = clusterId;
    }

}
