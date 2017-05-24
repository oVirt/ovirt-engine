package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class ErrataData implements Serializable {

    private static final long serialVersionUID = 3438998117712211584L;

    private List<Erratum> errata;
    private ErrataCounts errataCounts;

    public ErrataData(){
    }

    public List<Erratum> getErrata() {
        return errata;
    }

    public void setErrata(List<Erratum> errata) {
        this.errata = errata;
    }

    public ErrataCounts getErrataCounts() {
        return errataCounts;
    }

    public void setErrataCounts(ErrataCounts errataCounts) {
        this.errataCounts = errataCounts;
    }

    public static ErrataData emptyData() {
        ErrataData emptyData = new ErrataData();
        emptyData.setErrata(Collections.emptyList());
        emptyData.setErrataCounts(new ErrataCounts());
        return emptyData;
    }
}
