package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;


public class GetImageByIdParameters extends QueryParametersBase {

    private static final long serialVersionUID = -5607755161097383666L;

    private String repoImageId;

    private Guid storageDomainId;

    public GetImageByIdParameters() {
    }

    public GetImageByIdParameters(Guid storageDomainId, String repoImageId) {
        setStorageDomainId(storageDomainId);
        setRepoImageId(repoImageId);
    }

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public void setStorageDomainId(Guid storageDomainId) {
        this.storageDomainId = storageDomainId;
    }

    public String getRepoImageId() {
        return repoImageId;
    }

    public void setRepoImageId(String value) {
        repoImageId = value;
    }

}
