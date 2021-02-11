package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VmTemplateParameters;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmTemplateDao;

public abstract class VmTemplateCommand<T extends VmTemplateParameters> extends CommandBase<T> {

    @Inject
    private VmDeviceUtils vmDeviceUtils;
    @Inject
    protected VmHandler vmHandler;
    @Inject
    protected VmTemplateHandler vmTemplateHandler;
    @Inject
    private VmTemplateDao vmTemplateDao;

    /**
     * Constructor for command creation when compensation is applied on startup
     */
    protected VmTemplateCommand(Guid commandId) {
        super(commandId);
    }

    protected VmTemplateCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        setVmTemplateId(parameters.getVmTemplateId());
    }

    public boolean isVmTemplateWithSameNameExist(String name, Guid datacenterId) {
        return vmTemplateDao.getByName(name, datacenterId, null, false) != null;
    }

    public boolean isInstanceWithSameNameExists(String name) {
        return vmTemplateDao.getInstanceTypeByName(name, null, false) != null;
    }

    protected ValidationResult templateExists() {
        return getVmTemplate() == null ? new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_TEMPLATE_DOES_NOT_EXIST)
                : ValidationResult.VALID;
    }

    @Override
    protected String getDescription() {
        return getVmTemplateName();
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = new ArrayList<>();
        permissionList.add(new PermissionSubject(getVmTemplateId(),
                VdcObjectType.VmTemplate,
                getActionType().getActionGroup()));
        return permissionList;
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = new HashMap<>();
            jobProperties.put(VdcObjectType.StoragePool.name().toLowerCase(), getStoragePoolName());
            jobProperties.put(VdcObjectType.VmTemplate.name().toLowerCase(), getVmTemplateName());
        }
        return jobProperties;
    }

    protected boolean isBlankTemplate() {
        if (getVmTemplate() != null) {
            return VmTemplateHandler.BLANK_VM_TEMPLATE_ID.equals(getVmTemplate().getId());
        }

        return false;
    }

    protected VmDeviceUtils getVmDeviceUtils() {
        return vmDeviceUtils;
    }

    protected VmHandler getVmHandler() {
        return vmHandler;
    }
}
