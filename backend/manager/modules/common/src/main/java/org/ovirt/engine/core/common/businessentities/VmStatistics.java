package org.ovirt.engine.core.common.businessentities;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;

public class VmStatistics implements BusinessEntity<Guid>, Comparable<VmStatistics> {
    private static final long serialVersionUID = -7480866662740734452L;

    private Double cpu_sysField = 0.0;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((cpu_sysField == null) ? 0 : cpu_sysField.hashCode());
        result = prime * result
                + ((cpu_userField == null) ? 0 : cpu_userField.hashCode());
        result = prime
                * result
                + ((elapsed_timeField == null) ? 0 : elapsed_timeField
                        .hashCode());
        result = prime
                * result
                + ((interfaceStatisticsField == null) ? 0
                        : interfaceStatisticsField.hashCode());
        result = prime
                * result
                + ((roundedElapsedTimeField == null) ? 0
                        : roundedElapsedTimeField.hashCode());
        result = prime
                * result
                + ((usage_cpu_percentField == null) ? 0
                        : usage_cpu_percentField.hashCode());
        result = prime
                * result
                + ((usage_mem_percentField == null) ? 0
                        : usage_mem_percentField.hashCode());
        result = prime
                * result
                + ((usage_network_percentField == null) ? 0
                        : usage_network_percentField.hashCode());
        result = prime
                * result
                + ((disksUsage == null) ? 0
                        : disksUsage.hashCode());
        result = prime * result
                + ((vm_guidField == null) ? 0 : vm_guidField.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        VmStatistics other = (VmStatistics) obj;
        if (cpu_sysField == null) {
            if (other.cpu_sysField != null)
                return false;
        } else if (!cpu_sysField.equals(other.cpu_sysField))
            return false;
        if (cpu_userField == null) {
            if (other.cpu_userField != null)
                return false;
        } else if (!cpu_userField.equals(other.cpu_userField))
            return false;
        if (elapsed_timeField == null) {
            if (other.elapsed_timeField != null)
                return false;
        } else if (!elapsed_timeField.equals(other.elapsed_timeField))
            return false;
        if (interfaceStatisticsField == null) {
            if (other.interfaceStatisticsField != null)
                return false;
        } else if (!interfaceStatisticsField
                .equals(other.interfaceStatisticsField))
            return false;
        if (roundedElapsedTimeField == null) {
            if (other.roundedElapsedTimeField != null)
                return false;
        } else if (!roundedElapsedTimeField
                .equals(other.roundedElapsedTimeField))
            return false;
        if (usage_cpu_percentField == null) {
            if (other.usage_cpu_percentField != null)
                return false;
        } else if (!usage_cpu_percentField.equals(other.usage_cpu_percentField))
            return false;
        if (usage_mem_percentField == null) {
            if (other.usage_mem_percentField != null)
                return false;
        } else if (!usage_mem_percentField.equals(other.usage_mem_percentField))
            return false;
        if (usage_network_percentField == null) {
            if (other.usage_network_percentField != null)
                return false;
        if (disksUsage == null) {
                if (other.disksUsage != null)
                    return false;
            } else if (!disksUsage.equals(other.disksUsage))
                return false;
        } else if (!usage_network_percentField
                .equals(other.usage_network_percentField))
            return false;
        if (vm_guidField == null) {
            if (other.vm_guidField != null)
                return false;
        } else if (!vm_guidField.equals(other.vm_guidField))
            return false;
        return true;
    }

    public Double getcpu_sys() {
        return this.cpu_sysField;
    }

    public void setcpu_sys(Double value) {
        this.cpu_sysField = value;
    }

    private Double cpu_userField = 0.0;

    public Double getcpu_user() {
        return this.cpu_userField;
    }

    public void setcpu_user(Double value) {
        this.cpu_userField = value;
    }

    private Double elapsed_timeField = 0.0;

    public Double getelapsed_time() {
        return this.elapsed_timeField;
    }

    public void setelapsed_time(Double value) {
        this.elapsed_timeField = value;
        setRoundedElapsedTime(value);
    }

    private Double roundedElapsedTimeField = 0.0;

    public Double getRoundedElapsedTime() {
        return this.roundedElapsedTimeField;
    }

    public void setRoundedElapsedTime(Double value) {
        final int SEC_IN_MIN = 60;
        final int SEC_IN_HOUR = SEC_IN_MIN * 60;
        final int SEC_IN_DAY = SEC_IN_HOUR * 24;
        this.roundedElapsedTimeField = value;
        if (value != null) {
            if (getRoundedElapsedTime() == null) {
                this.setRoundedElapsedTime(value);
            } else {
                // Notify each Min until 1 Hour,each Hour until 1 Day and from then on every day.
                int val = value.intValue();
                int lastVal = getRoundedElapsedTime().intValue();
                if ((val > 0 && val < SEC_IN_MIN && val / SEC_IN_MIN > lastVal / SEC_IN_MIN) ||
                        (val >= SEC_IN_HOUR && val < SEC_IN_DAY && val / SEC_IN_HOUR > lastVal / SEC_IN_HOUR) ||
                        (val / SEC_IN_DAY > lastVal / SEC_IN_DAY)) {
                    this.setRoundedElapsedTime(value);
                }
            }
        }

    }

    private Integer usage_cpu_percentField;

    public Integer getusage_cpu_percent() {
        return this.usage_cpu_percentField;
    }

    public void setusage_cpu_percent(Integer value) {
        this.usage_cpu_percentField = value;
    }

    private Integer usage_mem_percentField;

    public Integer getusage_mem_percent() {
        return this.usage_mem_percentField;
    }

    public void setusage_mem_percent(Integer value) {
        this.usage_mem_percentField = value;
    }

    private String disksUsage;

    /**
     * Field for history db, not intended to be accessible to clients.
     */
    public String getDisksUsage() {
        return disksUsage;
    }

    public void setDisksUsage(String value) {
        disksUsage = value;
    }

    private Integer usage_network_percentField ;

    public Integer getusage_network_percent() {
        return this.usage_network_percentField;
    }

    public void setusage_network_percent(Integer value) {
        this.usage_network_percentField = value;
    }

    private Guid vm_guidField = new Guid();

    private ArrayList<VmNetworkInterface> interfaceStatisticsField;

    public java.util.ArrayList<VmNetworkInterface> getInterfaceStatistics() {
        return this.interfaceStatisticsField;
    }

    public void setInterfaceStatistics(java.util.ArrayList<VmNetworkInterface> value) {
        this.interfaceStatisticsField = value;
    }

    @Override
    public Guid getId() {
        return vm_guidField;
    }

    @Override
    public void setId(Guid id) {
        this.vm_guidField = id;
    }

    @Override
    public int compareTo(VmStatistics o) {
        return BusinessEntityGuidComparator.<VmStatistics>newInstance().compare(this,o);
    }
}
