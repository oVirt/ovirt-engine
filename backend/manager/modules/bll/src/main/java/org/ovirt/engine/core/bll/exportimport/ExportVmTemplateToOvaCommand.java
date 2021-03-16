package org.ovirt.engine.core.bll.exportimport;

import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_ACTIVE;
import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_NOT_SHAREABLE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.LockMessage;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.SerialChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.SerialChildExecutingCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.storage.disk.image.DisksFilter;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ExportOvaParameters;
import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskVmElementDao;

@NonTransactiveCommandAttribute
public class ExportVmTemplateToOvaCommand<T extends ExportOvaParameters> extends ExportOvaCommand<T> implements SerialChildExecutingCommand {

    @Inject
    @Typed(SerialChildCommandsExecutionCallback.class)
    private Instance<SerialChildCommandsExecutionCallback> callbackProvider;
    @Inject
    private DiskDao diskDao;
    @Inject
    private DiskVmElementDao diskVmElementDao;

    private String cachedTemplateIsBeingExportedMessage;
    private List<DiskImage> cachedDisks;

    public ExportVmTemplateToOvaCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void init() {
        setVmTemplateId(getParameters().getEntityId());
        if (getVmTemplate() != null) {
            setStoragePoolId(getVmTemplate().getStoragePoolId());
        }
        super.init();
    }

    @Override
    protected Nameable getEntity() {
        return getVmTemplate();
    }

    @Override
    protected boolean validate() {
        if (getEntity() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_TEMPLATE_DOES_NOT_EXIST);
        }
        return super.validate();
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        return Collections.singletonMap(getParameters().getEntityId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.TEMPLATE, getTemplateIsBeingExportedMessage()));
    }

    private String getTemplateIsBeingExportedMessage() {
        if (cachedTemplateIsBeingExportedMessage == null) {
            cachedTemplateIsBeingExportedMessage = new LockMessage(EngineMessage.ACTION_TYPE_FAILED_TEMPLATE_IS_BEING_EXPORTED)
                    .withOptional("TemplateName", getVmTemplate() != null ? getVmTemplate().getName() : null)
                    .toString();
        }
        return cachedTemplateIsBeingExportedMessage;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            return getSucceeded() ?
                    AuditLogType.IMPORTEXPORT_STARTING_EXPORT_TEMPLATE_TO_OVA
                    : AuditLogType.IMPORTEXPORT_EXPORT_TEMPLATE_TO_OVA_FAILED;

        case END_SUCCESS:
            return getSucceeded() ?
                    AuditLogType.IMPORTEXPORT_EXPORT_TEMPLATE_TO_OVA
                    : AuditLogType.IMPORTEXPORT_EXPORT_TEMPLATE_TO_OVA_FAILED;

        default:
            return AuditLogType.IMPORTEXPORT_EXPORT_TEMPLATE_TO_OVA_FAILED;
        }
    }

    @Override
    protected List<DiskImage> getDisks() {
        if (cachedDisks == null) {
            List<Disk> allDisks = diskDao.getAllForVm(getParameters().getEntityId());
            cachedDisks = DisksFilter.filterImageDisks(allDisks, ONLY_NOT_SHAREABLE, ONLY_ACTIVE);
            cachedDisks.forEach(disk -> disk.setDiskVmElements(Collections.singleton(
                    diskVmElementDao.get(new VmDeviceId(disk.getId(), getParameters().getEntityId())))));
            for (DiskImage disk : cachedDisks) {
                disk.getImage().setVolumeFormat(VolumeFormat.COW);
            }
        }
        return cachedDisks;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionSubjects = new ArrayList<>();
        permissionSubjects.add(new PermissionSubject(
                getParameters().getEntityId(),
                VdcObjectType.VmTemplate,
                getActionType().getActionGroup()));
        return permissionSubjects;
    }

    @Override
    protected void executeCommand() {
        createOva();
        setSucceeded(true);
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__EXPORT);
        addValidationMessage(EngineMessage.VAR__TYPE__VM_TEMPLATE);
    }

    @Override
    protected CommandContext createOvaCreationStepContext() {
        return ExecutionHandler.createDefaultContextForTasks(getContext());
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }

    @Override
    public boolean performNextOperation(int completedChildCount) {
        return false;
    }
}
