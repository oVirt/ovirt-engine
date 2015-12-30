package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ErrataCount implements Serializable {

    private Map<Erratum.ErrataSeverity, Integer> countBySeverity;
    private int totalCount;

    public ErrataCount(){
        countBySeverity = new HashMap<>();
    }

    public Map<Erratum.ErrataSeverity, Integer> getCountBySeverity() {
        return countBySeverity;
    }

    public void setCountBySeverity(Map<Erratum.ErrataSeverity, Integer> countBySeverity) {
        this.countBySeverity = countBySeverity;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
}
