package org.ovirt.engine.core.common.businessentities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.comparators.BusinessEntityComparator;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;

public class VmStatistics implements BusinessEntity<Guid>, Comparable<VmStatistics> {
    private static final long serialVersionUID = -7480866662740734452L;

    private Double cpu_sysField;
    private List<VmJob> vmJobs;
    // NOT PERSISTED
    private VmBalloonInfo vmBalloonInfo;
    // NOT PERSISTED
    private List<VmNumaNode> vNumaNodeStatisticsList;

    private List<Integer> memoryUsageHistory;
    private List<Integer> cpuUsageHistory;
    private List<Integer> networkUsageHistory;

    public VmStatistics() {
        cpu_sysField = 0.0;
        cpu_userField = 0.0;
        elapsed_timeField = 0.0;
        roundedElapsedTimeField = 0.0;
        vm_guidField = Guid.Empty;
        vNumaNodeStatisticsList = new ArrayList<>();
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                cpu_sysField,
                cpu_userField,
                elapsed_timeField,
                interfaceStatisticsField,
                roundedElapsedTimeField,
                usage_cpu_percentField,
                usage_mem_percentField,
                usage_network_percentField,
                migrationProgressPercent,
                disksUsage,
                vm_guidField,
                cpuUsageHistory,
                networkUsageHistory,
                memoryUsageHistory,
                vNumaNodeStatisticsList
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof VmStatistics)) {
            return false;
        }
        VmStatistics other = (VmStatistics) obj;
        return Objects.equals(cpu_sysField, other.cpu_sysField)
                && Objects.equals(cpu_userField, other.cpu_userField)
                && Objects.equals(elapsed_timeField, other.elapsed_timeField)
                && Objects.equals(interfaceStatisticsField, other.interfaceStatisticsField)
                && Objects.equals(roundedElapsedTimeField, other.roundedElapsedTimeField)
                && Objects.equals(usage_cpu_percentField, other.usage_cpu_percentField)
                && Objects.equals(usage_mem_percentField, other.usage_mem_percentField)
                && Objects.equals(migrationProgressPercent, other.migrationProgressPercent)
                && Objects.equals(usage_network_percentField, other.usage_network_percentField)
                && Objects.equals(disksUsage, other.disksUsage)
                && Objects.equals(vm_guidField, other.vm_guidField)
                && Objects.equals(cpuUsageHistory, other.cpuUsageHistory)
                && Objects.equals(networkUsageHistory, other.networkUsageHistory)
                && Objects.equals(memoryUsageHistory, other.memoryUsageHistory)
                && Objects.equals(vNumaNodeStatisticsList, other.vNumaNodeStatisticsList);
    }

    public Double getcpu_sys() {
        return this.cpu_sysField;
    }

    public void setcpu_sys(Double value) {
        this.cpu_sysField = value;
    }

    private Double cpu_userField;

    public Double getcpu_user() {
        return this.cpu_userField;
    }

    public void setcpu_user(Double value) {
        this.cpu_userField = value;
    }

    private Double elapsed_timeField;

    public Double getelapsed_time() {
        return this.elapsed_timeField;
    }

    public void setelapsed_time(Double value) {
        this.elapsed_timeField = value;
        setRoundedElapsedTime(value);
    }

    private Double roundedElapsedTimeField;

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

    private Integer migrationProgressPercent;

    public Integer getMigrationProgressPercent() {
        return migrationProgressPercent;
    }

    public void setMigrationProgressPercent(Integer migrationProgressPercent) {
        this.migrationProgressPercent = migrationProgressPercent;
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

    private Integer usage_network_percentField;

    public Integer getusage_network_percent() {
        return this.usage_network_percentField;
    }

    public void setusage_network_percent(Integer value) {
        this.usage_network_percentField = value;
    }

    private Guid vm_guidField;

    private ArrayList<VmNetworkInterface> interfaceStatisticsField;

    public ArrayList<VmNetworkInterface> getInterfaceStatistics() {
        return this.interfaceStatisticsField;
    }

    public void setInterfaceStatistics(ArrayList<VmNetworkInterface> value) {
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
        return BusinessEntityComparator.<VmStatistics, Guid>newInstance().compare(this, o);
    }

    public List<VmJob> getVmJobs() {
        return vmJobs;
    }

    public void setVmJobs(List<VmJob> vmJobs) {
        this.vmJobs = vmJobs;
    }

    public VmBalloonInfo getVmBalloonInfo() {
        return vmBalloonInfo;
    }

    public void setVmBalloonInfo(VmBalloonInfo vmBalloonInfo) {
        this.vmBalloonInfo = vmBalloonInfo;
    }

    public List<Integer> getMemoryUsageHistory() {
        return memoryUsageHistory;
    }

    public void addMemoryUsageHistory(Integer memoryUsageHistory, int limit) {
        this.memoryUsageHistory = addToHistory(this.memoryUsageHistory, memoryUsageHistory, limit);
    }

    public List<Integer> getCpuUsageHistory() {
        return cpuUsageHistory;
    }

    public void addCpuUsageHistory(Integer cpuUsageHistory, int limit) {
        this.cpuUsageHistory = addToHistory(this.cpuUsageHistory, cpuUsageHistory, limit);
    }

    public List<Integer> getNetworkUsageHistory() {
        return networkUsageHistory;
    }

    public void addNetworkUsageHistory(Integer networkUsageHistory, int limit) {
        this.networkUsageHistory = addToHistory(this.networkUsageHistory, networkUsageHistory, limit);
    }

    public void setMemoryUsageHistory(List<Integer> memoryUsageHistory) {
        this.memoryUsageHistory = memoryUsageHistory;
    }

    public void setCpuUsageHistory(List<Integer> cpuUsageHistory) {
        this.cpuUsageHistory = cpuUsageHistory;
    }

    public void setNetworkUsageHistory(List<Integer> networkUsageHistory) {
        this.networkUsageHistory = networkUsageHistory;
    }

    List<Integer> addToHistory(List<Integer> current, Integer newValue, int limit) {
        if (newValue == null) {
            return current;
        }

        if (current == null || current.isEmpty()) {
            return Arrays.asList(newValue);
        }

        if (limit == 0) {
            return Collections.emptyList();
        }

        List<Integer> res = new ArrayList<>(current);
        res.add(newValue);
        if (limit >= res.size()) {
            return res;
        }

        return res.subList(res.size() - limit, res.size());
    }

    public List<VmNumaNode> getvNumaNodeStatisticsList() {
        return vNumaNodeStatisticsList;
    }

    public void setvNumaNodeStatisticsList(List<VmNumaNode> vNumaNodeStatisticsList) {
        this.vNumaNodeStatisticsList = vNumaNodeStatisticsList;
    }
}
