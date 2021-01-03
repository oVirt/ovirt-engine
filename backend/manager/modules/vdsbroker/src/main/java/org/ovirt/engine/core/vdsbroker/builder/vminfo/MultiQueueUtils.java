package org.ovirt.engine.core.vdsbroker.builder.vminfo;

import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;

@Singleton
public class MultiQueueUtils {

    private static final int OPTIMAL_NUM_OF_QUEUES_PER_VNIC = 4;
    private static final int DEFAULT_NUM_OF_SCSI_QUEUES = 4;

    public boolean isInterfaceQueuable(VmDevice vmDevice, VmNic vmNic) {
        return VmDeviceCommonUtils.isBridge(vmDevice) && vmNic.getType() != null
                && VmInterfaceType.forValue(vmNic.getType()) == VmInterfaceType.pv;
    }

    public int getOptimalNumOfQueuesPerVnic(int numOfCpus) {
        return Math.min(numOfCpus, OPTIMAL_NUM_OF_QUEUES_PER_VNIC);
    }

    public int getNumOfScsiQueues(int numOfDisks, int numOfCpus) {
        return Math.min(Math.min(numOfDisks, DEFAULT_NUM_OF_SCSI_QUEUES), numOfCpus);
    }
}
