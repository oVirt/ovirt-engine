package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.EntityPollingCommand;
import org.ovirt.engine.core.bll.storage.StorageJobCommand;
import org.ovirt.engine.core.bll.storage.utils.VdsCommandsHelper;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase.EndProcedure;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.CopyDataCommandParameters;
import org.ovirt.engine.core.common.action.ExternalLeaseParameters;
import org.ovirt.engine.core.common.action.FenceVolumeJobCommandParameters;
import org.ovirt.engine.core.common.businessentities.HostJobInfo.HostJobStatus;
import org.ovirt.engine.core.common.businessentities.LocationInfo;
import org.ovirt.engine.core.common.businessentities.ManagedBlockStorageLocationInfo;
import org.ovirt.engine.core.common.businessentities.VdsmImageLocationInfo;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ExternalLease;
import org.ovirt.engine.core.common.businessentities.storage.LeaseJobStatus;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.common.job.StepSubjectEntity;
import org.ovirt.engine.core.common.vdscommands.CopyVolumeDataVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.ExternalLeaseDao;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;
import org.ovirt.engine.core.dao.VdsStaticDao;

@NonTransactiveCommandAttribute
@InternalCommandAttribute
public class CopyDataCommand<T extends CopyDataCommandParameters> extends
        StorageJobCommand<T> implements EntityPollingCommand {

    @Inject
    private VdsmImagePoller vdsmImagePoller;
    @Inject
    private AuditLogDirector auditLogDirector;
    @Inject
    private VdsStaticDao vdsStaticDao;
    @Inject
    private StorageDomainStaticDao storageDomainStaticDao;
    @Inject
    private VdsCommandsHelper vdsCommandsHelper;
    @Inject
    private ExternalLeasePoller externalLeasePoller;
    @Inject
    private ExternalLeaseDao externalLeaseDao;

    public CopyDataCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void init() {
        super.init();
        if (getParameters().getVdsId() != null) {
            setVdsId(getParameters().getVdsId());
        } else {
            setVdsId(vdsCommandsHelper.getHostForExecution(getParameters().getStoragePoolId()));
            getParameters().setVdsId(getVdsId());
            getParameters().setVdsRunningOn(getVdsId());
        }
    }

    @Override
    protected void executeCommand() {
        completeGenerationInfo();
        CopyVolumeDataVDSCommandParameters parameters =
                new CopyVolumeDataVDSCommandParameters(getParameters().getStorageJobId(),
                        getParameters().getSrcInfo(),
                        getParameters().getDstInfo(),
                        getParameters().isCollapse(),
                        getParameters().isCopyBitmaps());
        parameters.setVdsId(getVdsId());

        logExecutionHost();

        vdsCommandsHelper.runVdsCommandWithFailover(VDSCommandType.CopyVolumeData,
                parameters,
                getParameters().getStoragePoolId(),
                this);
        setSucceeded(true);
    }

    private void completeGenerationInfo() {
        if (!isDstVdsmImage()) {
            return;
        }
        VdsmImageLocationInfo info = (VdsmImageLocationInfo) getParameters().getDstInfo();
        DiskImage image = imagesHandler.getVolumeInfoFromVdsm(getParameters().getStoragePoolId(),
                info.getStorageDomainId(), info.getImageGroupId(), info.getImageId());
        info.setGeneration(image.getImage().getGeneration());
        persistCommandIfNeeded();
    }

    @Override
    public StepEnum getCommandStep() {
        return StepEnum.COPY_VOLUME;
    }

    @Override
    public boolean shouldUpdateStepProgress() {
        return true;
    }

    @Override
    public List<StepSubjectEntity> getCommandStepSubjectEntities() {
        if (getParameters().getJobWeight() != null && getParameters().getDstInfo() instanceof VdsmImageLocationInfo) {
            return Collections.singletonList(new StepSubjectEntity(VdcObjectType.Disk,
                    ((VdsmImageLocationInfo) getParameters().getDstInfo()).getImageGroupId(),
                    getParameters().getJobWeight()));
        }

        return super.getCommandStepSubjectEntities();
    }

    @Override
    public void attemptToFenceJob() {
        log.info("Command {} id: '{}': attempting to fence job {}",
                getActionType(),
                getCommandId(),
                getJobId());

        if (isDstVdsmImage()) {
            VdsmImageLocationInfo info = (VdsmImageLocationInfo) getParameters().getDstInfo();
            FenceVolumeJobCommandParameters p = new FenceVolumeJobCommandParameters(info);
            p.setParentCommand(getActionType());
            p.setParentParameters(getParameters());
            p.setStoragePoolId(getParameters().getStoragePoolId());
            p.setEndProcedure(EndProcedure.COMMAND_MANAGED);
            runInternalActionWithTasksContext(ActionType.FenceVolumeJob, p);
        } else if (isDstManagedBlockDisk()) {
            ManagedBlockStorageLocationInfo info = (ManagedBlockStorageLocationInfo) getParameters().getDstInfo();
            Guid leaseId = Guid.createGuidFromString((String) info.getLease().get("lease_id"));
            ExternalLease externalLease = externalLeaseDao.get(leaseId);

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("generation", info.getGeneration());
            metadata.put("job_id", getJobId().toString());
            metadata.put("type", "JOB");
            metadata.put("job_status", LeaseJobStatus.Pending.getValue());

            ExternalLeaseParameters p =
                    new ExternalLeaseParameters(getParameters().getStoragePoolId(),
                            externalLease.getStorageDomainId(),
                            leaseId,
                            metadata);
            p.setJobId(getJobId());
            p.setParentCommand(getActionType());
            p.setParentParameters(getParameters());
            p.setEndProcedure(EndProcedure.COMMAND_MANAGED);
            runInternalAction(ActionType.FenceLeaseJob, p);
        }
    }

    private boolean isDstVdsmImage() {
        return getParameters().getDstInfo() instanceof VdsmImageLocationInfo;
    }

    private boolean isDstManagedBlockDisk() {
        return getParameters().getDstInfo() instanceof ManagedBlockStorageLocationInfo;
    }

    @Override
    public HostJobStatus poll() {
        if (isDstVdsmImage()) {
            VdsmImageLocationInfo info = (VdsmImageLocationInfo) getParameters().getDstInfo();
            return vdsmImagePoller.pollImage(getParameters().getStoragePoolId(), info.getStorageDomainId(),
                    info.getImageGroupId(), info.getImageId(), info.getGeneration(), getCommandId(), getActionType());
        } else if (isDstManagedBlockDisk()) {
            ManagedBlockStorageLocationInfo info = (ManagedBlockStorageLocationInfo) getParameters().getDstInfo();
            Guid leaseId = Guid.createGuidFromString((String) info.getLease().get("lease_id"));
            Guid leastStorageDomainId = Guid.createGuidFromString((String) info.getLease().get("sd_id"));
            return externalLeasePoller.pollLease(getParameters().getStoragePoolId(),
                    leastStorageDomainId,
                    leaseId,
                    this.getCommandId());
        }

        return null;
    }

    private void logExecutionHost() {
        AuditLogable loggable = new AuditLogableImpl();
        LocationInfo destInfo = getParameters().getDstInfo();
        Guid storageDomainId = Guid.Empty;
        Guid diskId = Guid.Empty;
        Guid imageId = Guid.Empty;

        if (destInfo instanceof VdsmImageLocationInfo) {
            storageDomainId = ((VdsmImageLocationInfo) destInfo).getStorageDomainId();
            diskId = ((VdsmImageLocationInfo) destInfo).getImageGroupId();
            imageId = ((VdsmImageLocationInfo) destInfo).getImageId();
        } else if (destInfo instanceof ManagedBlockStorageLocationInfo) {
            storageDomainId = ((ManagedBlockStorageLocationInfo) destInfo).getStorageDomainId();
            // TODO: create a nicer API for this in the future
            diskId = Guid.createGuidFromString((String) ((ManagedBlockStorageLocationInfo) destInfo).getLease().get("lease_id"));
            // MBS disks don't actually have an image id
            imageId = diskId;
        }

        String vdsName = vdsStaticDao.get(getVdsId()).getName();
        String domainName = storageDomainStaticDao.get(storageDomainId).getStorageName();

        loggable.setVdsId(getParameters().getVdsId());
        loggable.setVdsName(vdsName);
        loggable.setStorageDomainId(storageDomainId);
        loggable.setStorageDomainName(domainName);
        loggable.addCustomValue("diskId", diskId.toString());
        loggable.addCustomValue("imageId", imageId.toString());
        auditLogDirector.log(loggable, AuditLogType.COPY_VOLUME_DATA_EXECUTION_HOST);
    }
}
