package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.VirtIoRngValidator;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.RngDeviceParameters;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.VmTemplateDao;

/**
 * Base class for crud for random number generator devices
 */
public abstract class AbstractRngDeviceCommand<T extends RngDeviceParameters> extends CommandBase<T>  {

    @Inject
    private VmStaticDao vmStaticDao;
    @Inject
    private VmTemplateDao vmTemplateDao;
    @Inject
    private VmDeviceDao vmDeviceDao;
    private VmBase cachedEntity = null;
    private VmEntityType templateType = null;
    private List<VmDevice> cachedRngDevices = null;
    private boolean blankTemplate = false;

    protected AbstractRngDeviceCommand(T parameters, CommandContext context) {
        super(parameters, context);
    }

    @Override
    protected void init() {
        super.init();
        if (getParameters().getRngDevice() == null || getParameters().getRngDevice().getVmId() == null) {
            return;
        }

        Guid vmId = getParameters().getRngDevice().getVmId();
        setVmId(vmId);

        if (getParameters().isVm()) {
            cachedEntity = vmStaticDao.get(vmId);
        } else {
            blankTemplate = VmTemplateHandler.BLANK_VM_TEMPLATE_ID.equals(vmId);

            VmTemplate template = vmTemplateDao.get(vmId);
            templateType = template.getTemplateType();
            cachedEntity = template;
        }

        if (cachedEntity != null) {
            setClusterId(cachedEntity.getClusterId());
        }

        cachedRngDevices = new ArrayList<>();
        List<VmDevice> rngDevs = vmDeviceDao.getVmDeviceByVmIdAndType(vmId, VmDeviceGeneralType.RNG);
        if (rngDevs != null) {
            cachedRngDevices.addAll(rngDevs);
        }
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = new ArrayList<>();
        permissionList.add(new PermissionSubject(getParameters().getRngDevice().getVmId(),
                getParameters().isVm() ? VdcObjectType.VM : VdcObjectType.VmTemplate,
                getActionType().getActionGroup()));
        return permissionList;
    }

    @Override
    protected boolean validate() {
        if (getParameters().getRngDevice().getVmId() == null || cachedEntity == null) {
            return failValidation(getParameters().isVm() ? EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND
                    : EngineMessage.ACTION_TYPE_FAILED_TEMPLATE_DOES_NOT_EXIST);
        }

        if (getParameters().isVm() && getVm() != null && getVm().isRunningOrPaused()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_IS_RUNNING);
        }

        return true;
    }

    /**
     * Provides the new instance of VirtIoRngValidator
     * This method is here only to make it possible to mock it in tests
     */
    protected VirtIoRngValidator getVirtioRngValidator() {
        return new VirtIoRngValidator();
    }

    protected List<VmDevice> getRngDevices() {
        return cachedRngDevices;
    }

    protected VmEntityType getTemplateType() {
        return templateType;
    }

    public boolean isBlankTemplate() {
        return blankTemplate;
    }

    public VmBase getCachedEntity() {
        return cachedEntity;
    }
}
