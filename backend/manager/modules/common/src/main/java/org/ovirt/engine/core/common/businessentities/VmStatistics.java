package org.ovirt.engine.core.common.businessentities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.comparators.BusinessEntityComparator;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;

public class VmStatistics implements BusinessEntity<Guid>, Comparable<VmStatistics> {
    private static final long serialVersionUID = -7480866662740734452L;

    private List<VmJob> vmJobs;
    // NOT PERSISTED
    private VmBalloonInfo vmBalloonInfo;
    // NOT PERSISTED
    private List<VmNumaNode> vNumaNodeStatisticsList;

    private List<Integer> memoryUsageHistory;
    private List<Integer> cpuUsageHistory;
    private List<Integer> networkUsageHistory;

    private Double cpuSys;
    private Double cpuUser;
    private Double elapsedTime;
    private Double roundedElapsedTime;
    private Integer usageMemPercent;
    private Integer migrationProgressPercent;
    private String disksUsage;
    private Integer usageNetworkPercent;
    private Guid vmId;
    private ArrayList<VmNetworkInterface> interfaceStatistics;
    private Integer usageCpuPercent;

    public VmStatistics() {
        cpuSys = 0.0;
        cpuUser = 0.0;
        elapsedTime = 0.0;
        roundedElapsedTime = 0.0;
        vmId = Guid.Empty;
        vNumaNodeStatisticsList = new ArrayList<>();
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                cpuSys,
                cpuUser,
                elapsedTime,
                interfaceStatistics,
                roundedElapsedTime,
                usageCpuPercent,
                usageMemPercent,
                usageNetworkPercent,
                migrationProgressPercent,
                disksUsage,
                vmId,
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
        return Objects.equals(cpuSys, other.cpuSys)
                && Objects.equals(cpuUser, other.cpuUser)
                && Objects.equals(elapsedTime, other.elapsedTime)
                && Objects.equals(interfaceStatistics, other.interfaceStatistics)
                && Objects.equals(roundedElapsedTime, other.roundedElapsedTime)
                && Objects.equals(usageCpuPercent, other.usageCpuPercent)
                && Objects.equals(usageMemPercent, other.usageMemPercent)
                && Objects.equals(migrationProgressPercent, other.migrationProgressPercent)
                && Objects.equals(usageNetworkPercent, other.usageNetworkPercent)
                && Objects.equals(disksUsage, other.disksUsage)
                && Objects.equals(vmId, other.vmId)
                && Objects.equals(cpuUsageHistory, other.cpuUsageHistory)
                && Objects.equals(networkUsageHistory, other.networkUsageHistory)
                && Objects.equals(memoryUsageHistory, other.memoryUsageHistory)
                && Objects.equals(vNumaNodeStatisticsList, other.vNumaNodeStatisticsList);
    }

    public Double getCpuSys() {
        return this.cpuSys;
    }

    public void setCpuSys(Double cpuSys) {
        this.cpuSys = cpuSys;
    }

    public Double getCpuUser() {
        return this.cpuUser;
    }

    public void setCpuUser(Double cpuUser) {
        this.cpuUser = cpuUser;
    }

    public Double getElapsedTime() {
        return this.elapsedTime;
    }

    public void setElapsedTime(Double elapsedTime) {
        this.elapsedTime = elapsedTime;
        setRoundedElapsedTime(elapsedTime);
    }

    public Double getRoundedElapsedTime() {
        return this.roundedElapsedTime;
    }

    public void setRoundedElapsedTime(Double value) {
        final int SEC_IN_MIN = 60;
        final int SEC_IN_HOUR = SEC_IN_MIN * 60;
        final int SEC_IN_DAY = SEC_IN_HOUR * 24;
        this.roundedElapsedTime = value;
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

    public Integer getUsageCpuPercent() {
        return this.usageCpuPercent;
    }

    public void setUsageCpuPercent(Integer usageCpuPercent) {
        this.usageCpuPercent = usageCpuPercent;
    }

    public Integer getUsageMemPercent() {
        return this.usageMemPercent;
    }

    public void setUsageMemPercent(Integer usageMemPercent) {
        this.usageMemPercent = usageMemPercent;
    }

    public Integer getMigrationProgressPercent() {
        return migrationProgressPercent;
    }

    public void setMigrationProgressPercent(Integer migrationProgressPercent) {
        this.migrationProgressPercent = migrationProgressPercent;
    }

    /**
     * Field for history db, not intended to be accessible to clients.
     */
    public String getDisksUsage() {
        return disksUsage;
    }

    public void setDisksUsage(String disksUsage) {
        this.disksUsage = disksUsage;
    }

    public Integer getUsageNetworkPercent() {
        return this.usageNetworkPercent;
    }

    public void setUsageNetworkPercent(Integer usageNetworkPercent) {
        this.usageNetworkPercent = usageNetworkPercent;
    }

    public ArrayList<VmNetworkInterface> getInterfaceStatistics() {
        return this.interfaceStatistics;
    }

    public void setInterfaceStatistics(ArrayList<VmNetworkInterface> interfaceStatistics) {
        this.interfaceStatistics = interfaceStatistics;
    }

    @Override
    public Guid getId() {
        return vmId;
    }

    @Override
    public void setId(Guid id) {
        this.vmId = id;
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

    /**
     * Update data that was received from VDSM
     * @param vmStatistics - the reported statistics from VDSM
     * @param vm - the static part of the VM for which the statistics belong
     */
    public void updateRuntimeData(VmStatistics vmStatistics, VmStatic vm) {
        Integer usageHistoryLimit = Config.getValue(ConfigValues.UsageHistoryLimit);

        setElapsedTime(vmStatistics.getElapsedTime());

        setDisksUsage(vmStatistics.getDisksUsage());

        // -------- cpu --------------
        setCpuSys(vmStatistics.getCpuSys());
        setCpuUser(vmStatistics.getCpuUser());
        if ((getCpuSys() != null) && (getCpuUser() != null)) {
            Double percent = (getCpuSys() + getCpuUser()) / vm.getNumOfCpus();
            setUsageCpuPercent(percent.intValue());
            if (getUsageCpuPercent() != null && getUsageCpuPercent() > 100) {
                setUsageCpuPercent(100);
            }
        }
        addCpuUsageHistory(getUsageCpuPercent(), usageHistoryLimit);

        // -------- memory --------------
        setUsageMemPercent(vmStatistics.getUsageMemPercent());
        addMemoryUsageHistory(getUsageMemPercent(), usageHistoryLimit);

        // -------- migration --------------
        setMigrationProgressPercent(vmStatistics.getMigrationProgressPercent());
    }
}
