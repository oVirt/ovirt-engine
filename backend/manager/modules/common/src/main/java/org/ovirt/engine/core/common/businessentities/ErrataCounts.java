package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.Erratum.ErrataSeverity;
import org.ovirt.engine.core.common.businessentities.Erratum.ErrataType;

public class ErrataCounts implements Serializable {

    private static final long serialVersionUID = -1790668006895698912L;
    private final List<Erratum> errata = new ArrayList<>();

    /**
     * Adds the erratum to the counts by its type and severity
     *
     * @param erratum
     *            the errata to add
     */
    public void addToCounts(Erratum erratum) {
        errata.add(erratum);
    }

    /**
     * @return the overall count of the errata
     */
    public int getTotal() {
        return errata.size();
    }

    public int getCountByType(ErrataType type) {
        int count = 0;
        for (Erratum e : errata) {
            if (e.getType() == type) {
                count++;
            }
        }
        return count;
    }

    public int getCountBySeverity(ErrataSeverity severity) {
        int count = 0;
        for (Erratum e : errata) {
            if (e.getSeverity() == severity) {
                count++;
            }
        }
        return count;
    }

    public int getCountByTypeAndSeverity(ErrataType type, ErrataSeverity severity) {
        int count = 0;
        for (Erratum e : errata) {
            if (e.getType() == type && e.getSeverity() == severity) {
                count++;
            }
        }
        return count;
    }

}
