/**
 *
 */
package org.ovirt.engine.core.common.businessentities.gluster;

import java.io.Serializable;
import java.util.List;

import org.ovirt.engine.core.common.utils.ObjectUtils;

public class NfsProfileDetails implements Serializable {

    private static final long serialVersionUID = 1L;

    private String nfsServerIp;
    private List<StatsInfo> statsInfo;

    public NfsProfileDetails() {
        super();
    }

    public String getNfsServerIp() {
        return nfsServerIp;
    }
    public void setNfsServerIp(String nfsServerIp) {
        this.nfsServerIp = nfsServerIp;
    }
    public List<StatsInfo> getStatsInfo() {
        return statsInfo;
    }
    public void setStatsInfo(List<StatsInfo> statsInfo) {
        this.statsInfo = statsInfo;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof NfsProfileDetails)) {
            return false;
        }
        NfsProfileDetails nfsDetails = (NfsProfileDetails) obj;
        if(!ObjectUtils.objectsEqual(getNfsServerIp(), nfsDetails.getNfsServerIp())) {
            return false;
        }
        if(!ObjectUtils.objectsEqual(getStatsInfo(), nfsDetails.getStatsInfo())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getNfsServerIp() == null) ? 0 : getNfsServerIp().hashCode());
        result = prime * result + ((getStatsInfo() == null) ? 0 : getStatsInfo().hashCode());
        return result;
    }
}
