package org.ovirt.engine.core.common.businessentities;

import java.util.Objects;

import javax.validation.constraints.Min;

import org.ovirt.engine.core.compat.Guid;

/**
 * The <code>QuotaCluster</code> is a business entity that reflects cluster limitation.
 */
public class QuotaCluster implements Queryable {

    public static final Long UNLIMITED_MEM = -1L;
    public static final Integer UNLIMITED_VCPU = -1;

    /**
     * Automatic generated serial version ID.
     */
    private static final long serialVersionUID = 168673911413393235L;

    /**
     * The quota vds group Id.
     */
    private Guid quotaClusterId;

    /**
     * The quota ID.
     */
    private Guid quotaId;

    /**
     * The vds group Id which this limitation is enforced on.
     */
    private Guid clusterId;

    /**
     * Transient field indicates the vds group name.
     */
    private String clusterName;

    /**
     * The virtual CPU limitations for vds group.
     */
    @Min(-1)
    private Integer virtualCpu;

    /**
     * Transient field indicates the virtual CPU usage of the quota vds group.
     */
    private Integer virtualCpuUsage;

    /**
     * The virtual memory limitations for specific quota cluster.
     */
    @Min(-1)
    private Long memSizeMB;

    /**
     * Transient field indicates the virtual memory usage of the quota cluster.
     */
    private Long memSizeMBUsage;

    /**
     * Default constructor of QuotaCluster.
     */
    public QuotaCluster() {
    }

    public QuotaCluster(Guid quotaClusterId,
            Guid quotaId,
            Guid clusterId,
            Integer virtualCpu,
            Integer virtualCpuUsage,
            Long memSizeMB,
            Long memSizeMBUsage) {
        this.quotaClusterId = quotaClusterId;
        this.quotaId = quotaId;
        this.clusterId = clusterId;
        this.virtualCpu = virtualCpu;
        this.virtualCpuUsage = virtualCpuUsage;
        this.memSizeMB = memSizeMB;
        this.memSizeMBUsage = memSizeMBUsage;
    }

    /**
     * @return the quotaId
     */
    public Guid getQuotaId() {
        return quotaId;
    }

    /**
     * @param quotaId
     *            the quotaId to set
     */
    public void setQuotaId(Guid quotaId) {
        this.quotaId = quotaId;
    }

    /**
     * @return the clusterId
     */
    public Guid getClusterId() {
        return clusterId;
    }

    /**
     * @param clusterId
     *            the clusterId to set
     */
    public void setClusterId(Guid clusterId) {
        this.clusterId = clusterId;
    }

    /**
     * @return the memSizeMBUsage
     */
    public Long getMemSizeMBUsage() {
        return memSizeMBUsage;
    }

    /**
     * @param memSizeMB
     *            the memSizeMB to set
     */
    public void setMemSizeMB(Long memSizeMB) {
        this.memSizeMB = memSizeMB;
    }

    /**
     * @return the virtualCpuUsage
     */
    public Integer getVirtualCpuUsage() {
        return virtualCpuUsage;
    }

    /**
     * @param virtualCpuUsage
     *            the virtualCpuUsage to set
     */
    public void setVirtualCpuUsage(Integer virtualCpuUsage) {
        this.virtualCpuUsage = virtualCpuUsage;
    }

    /**
     * @return the virtualCpu
     */
    public Integer getVirtualCpu() {
        return virtualCpu;
    }

    /**
     * @param virtualCpu
     *            the virtualCpu to set
     */
    public void setVirtualCpu(Integer virtualCpu) {
        this.virtualCpu = virtualCpu;
    }

    /**
     * @param memSizeMBUsage
     *            the memSizeMBUsage to set
     */
    public void setMemSizeMBUsage(Long memSizeMBUsage) {
        this.memSizeMBUsage = memSizeMBUsage;
    }

    /**
     * @return the memory size in mega bytes.
     */
    public Long getMemSizeMB() {
        return memSizeMB;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                quotaId,
                quotaClusterId,
                clusterId,
                virtualCpu,
                memSizeMB
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof QuotaCluster)) {
            return false;
        }
        QuotaCluster other = (QuotaCluster) obj;
        return Objects.equals(quotaId, other.quotaId)
                && Objects.equals(quotaClusterId, other.quotaClusterId)
                && Objects.equals(clusterId, other.clusterId)
                && Objects.equals(virtualCpu, other.virtualCpu)
                && Objects.equals(virtualCpuUsage, other.virtualCpuUsage)
                && Objects.equals(memSizeMB, other.memSizeMB)
                && Objects.equals(memSizeMBUsage, other.memSizeMBUsage);
    }

    /**
     * @return the quotaClusterId
     */
    public Guid getQuotaClusterId() {
        return quotaClusterId;
    }

    /**
     * @param quotaClusterId
     *            the quotaClusterId to set
     */
    public void setQuotaClusterId(Guid quotaClusterId) {
        this.quotaClusterId = quotaClusterId;
    }

    /**
     * @return the clusterName
     */
    public String getClusterName() {
        return clusterName;
    }

    /**
     * @param clusterName
     *            the clusterName to set
     */
    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    @Override
    public Object getQueryableId() {
        return getQuotaClusterId();
    }
}
