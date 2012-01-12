package org.ovirt.engine.core.common.businessentities;

public interface QuotaVdsGroupProperties {

    /**
     * @return the virtualCpuUsage
     */
    public Integer getVirtualCpuUsage();

    /**
     * @param virtualCpuUsage
     *            the virtualCpuUsage to set
     */
    public void setVirtualCpuUsage(Integer virtualCpuUsage);

    /**
     * @return the virtualCpu
     */
    public Integer getVirtualCpu();

    /**
     * @param virtualCpu
     *            the virtualCpu to set
     */
    public void setVirtualCpu(Integer virtualCpu);

    /**
     * @return the memSizeMB
     */
    public Long getMemSizeMB();

    /**
     * @param memSizeMB
     *            the memSizeMB to set
     */
    public void setMemSizeMB(Long memSizeMB);

    /**
     * @return the memSizeMB
     */
    public Long getMemSizeMBUsage();

    /**
     * @param memSizeMBUsage
     *            the memSizeMBUsage to set
     */
    public void setMemSizeMBUsage(Long setMemSizeMBUsage);
}
