package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.storage.StorageJobCommand;
import org.ovirt.engine.core.bll.storage.utils.VdsCommandsHelper;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ColdMergeCommandParameters;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.common.vdscommands.ColdMergeVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;

@NonTransactiveCommandAttribute
@InternalCommandAttribute
public class ColdMergeCommand<T extends ColdMergeCommandParameters> extends StorageJobCommand<T> implements QuotaStorageDependent {

    public ColdMergeCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        VdsCommandsHelper.runVdsCommandWithFailover(VDSCommandType.ColdMerge,
                new ColdMergeVDSCommandParameters(getParameters().getStorageJobId(),
                        getParameters().getSubchainInfo()),
                getParameters().getStoragePoolId(), this);
        setSucceeded(true);
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            DiskImage diskImage = diskImageDao.getSnapshotById(getParameters().getSubchainInfo().getTopImageId());
            DiskImage destDiskImage = diskImageDao.getSnapshotById(getParameters().getSubchainInfo().getBaseImageId());
            jobProperties = super.getJobMessageProperties();
            jobProperties.put(VdcObjectType.Disk.name().toLowerCase(), diskImage.getDiskAlias());
            jobProperties.put("sourcesnapshot",
                    Optional.ofNullable(snapshotDao.get(diskImage.getVmSnapshotId()).getDescription()).orElse(""));
            jobProperties.put("destinationsnapshot",
                    Optional.ofNullable(snapshotDao.get(destDiskImage.getVmSnapshotId()).getDescription()).orElse(""));
        }
        return jobProperties;
    }

    @Override
    public StepEnum getCommandStep() {
        return StepEnum.MERGE_SNAPSHOTS;
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        return null;
    }
}
