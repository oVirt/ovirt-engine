package org.ovirt.engine.core.common.queries;

import java.io.Serializable;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.Erratum;

public class ErrataFilter implements Serializable {

    private Set<Erratum.ErrataType> errataTypes;
    private Integer pageNumber;
    private Integer pageSize;

    public ErrataFilter(){
    }

    public Set<Erratum.ErrataType> getErrataTypes() {
        return errataTypes;
    }

    public void setErrataTypes(Set<Erratum.ErrataType> errataTypes) {
        this.errataTypes = errataTypes;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}
