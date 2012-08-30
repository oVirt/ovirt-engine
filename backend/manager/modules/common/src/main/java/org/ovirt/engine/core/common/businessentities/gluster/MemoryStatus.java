package org.ovirt.engine.core.common.businessentities.gluster;

import java.io.Serializable;
import java.util.List;


/**
 * The gluster volume Memory status info.
 *
 */
public class MemoryStatus implements Serializable {

    private static final long serialVersionUID = -5571133379635552503L;

    private MallInfo mallInfo;

    private List<Mempool> memPools;

    public MallInfo getMallInfo() {
        return mallInfo;
    }

    public void setMallInfo(MallInfo mallInfo) {
        this.mallInfo = mallInfo;
    }

    public List<Mempool> getMemPools() {
        return memPools;
    }

    public void setMemPools(List<Mempool> memPools) {
        this.memPools = memPools;
    }
}
