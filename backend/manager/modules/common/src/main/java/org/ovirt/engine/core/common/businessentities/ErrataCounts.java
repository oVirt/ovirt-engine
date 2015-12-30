package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.Erratum.ErrataSeverity;
import org.ovirt.engine.core.common.businessentities.Erratum.ErrataType;

/**
 * A simple counter for counting errata by type and severity. Primarily used by the 'dashboard'
 * views in the 'Hosts > General > Errata' and 'VMs > Errata' subtabs.
 */
public class ErrataCounts implements Serializable {

    private static final long serialVersionUID = -1790668006895698912L;

    /**
     * The total amount of errata per content host
     */
    private int totalErrata;

    /**
     * The sub-total amount of errata per content host, according to the search query used to filter the errata
     */
    private int subTotalErrata;

    /**
     * A map of errata-type to the total amount of errata of that type
     */
    private Map<ErrataType, ErrataCount> errataCountByType;

    public ErrataCounts() {
        errataCountByType = new HashMap<>();
    }

    public int getCountByType(ErrataType type) {
        if (errataCountByType.containsKey(type)) {
            return errataCountByType.get(type).getTotalCount();
        }

        return 0;
    }

    public int getCountByTypeAndSeverity(ErrataType type, ErrataSeverity severity) {
        if (errataCountByType.containsKey(type)
                && errataCountByType.get(type).getCountBySeverity().containsKey(severity)) {
            return errataCountByType.get(type).getCountBySeverity().get(severity);
        }

        return 0;
    }

    public int getTotalErrata() {
        return totalErrata;
    }

    public void setTotalErrata(int totalErrata) {
        this.totalErrata = totalErrata;
    }

    public int getSubTotalErrata() {
        return subTotalErrata;
    }

    public void setSubTotalErrata(int subTotalErrata) {
        this.subTotalErrata = subTotalErrata;
    }

    public Map<ErrataType, ErrataCount> getErrataCountByType() {
        return errataCountByType;
    }
}
