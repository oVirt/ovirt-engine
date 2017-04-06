package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.GraphicsParameters;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.VmTemplateDao;

public class AddGraphicsAndVideoDevicesCommand extends AddGraphicsDeviceCommand {

    @Inject
    private VmDeviceDao vmDeviceDao;
    @Inject
    private VmStaticDao vmStaticDao;
    @Inject
    private VmTemplateDao vmTemplateDao;

    public AddGraphicsAndVideoDevicesCommand(GraphicsParameters parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        VmDevice graphicsDev = getParameters().getDev();
        if (graphicsDev.getDeviceId() == null) {
            graphicsDev.setDeviceId(Guid.newGuid());
        }
        vmDeviceDao.save(graphicsDev);

        if (getPrevDevices().isEmpty()) {
            if (getParameters().isVm()) {
                setVmToNonHeadlessMode();
            } else {
                setTemplateToNonHeadlessMode();
            }
        }
        setSucceeded(true);
        setActionReturnValue(graphicsDev.getId().getDeviceId());
    }

    private void setVmToNonHeadlessMode() {
        VmStatic vmStatic = vmStaticDao.get(getVmBaseId());
        if (vmStatic == null) {
            return;
        }
        if (vmStatic.getDefaultDisplayType() == DisplayType.none) {
            vmStatic.setDefaultDisplayType(DisplayType.qxl);
            resourceManager.getVmManager(getVmBaseId()).update(vmStatic);
        }

        vmDeviceUtils.addVideoDevicesOnlyIfNoVideoDeviceExists(vmStatic);
    }

    private void setTemplateToNonHeadlessMode() {
        VmTemplate vmTemplate = vmTemplateDao.get(getVmBaseId());
        if (vmTemplate == null) {
            return;
        }
        if (vmTemplate.getDefaultDisplayType() == DisplayType.none) {
            vmTemplate.setDefaultDisplayType(DisplayType.qxl);
            vmTemplateDao.update(vmTemplate);
        }

        vmDeviceUtils.addVideoDevicesOnlyIfNoVideoDeviceExists(vmTemplate);
    }
}
