package org.ovirt.engine.core.common.queries;

import java.io.Serializable;
import java.util.EnumSet;

import org.ovirt.engine.core.common.businessentities.Erratum;

public class ErrataFilter implements Serializable {

    private EnumSet<Erratum.ErrataType> errataTypes;
    private Integer pageNumber;
    private Integer pageSize;

    public ErrataFilter(){
    }

    public EnumSet<Erratum.ErrataType> getErrataTypes() {
        return errataTypes;
    }

    public void setErrataTypes(EnumSet<Erratum.ErrataType> errataTypes) {
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
