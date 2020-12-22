package org.ovirt.engine.core.bll;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.GraphicsParameters;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.VmTemplateDao;

public class RemoveGraphicsAndVideoDevicesCommand extends RemoveGraphicsDeviceCommand {

    @Inject
    private VmStaticDao vmStaticDao;
    @Inject
    private VmDeviceDao vmDeviceDao;
    @Inject
    private VmTemplateDao vmTemplateDao;

    public RemoveGraphicsAndVideoDevicesCommand(GraphicsParameters parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        VmDevice graphicsDev = getParameters().getDev();
        vmDeviceDao.remove(graphicsDev.getId());

        if (noGraphicsDevicesLeft()) {
            vmDeviceUtils.removeVideoDevices(getVmBaseId());
            //Since getParameters().isVm() isn't set by REST api, try to set VM and if failed then try to set Template
            if(!setVmToHeadlessMode()) {
                setTemplateToHeadlessMode();
            }
        }
        setSucceeded(true);
    }

    private boolean noGraphicsDevicesLeft() {
        List<GraphicsDevice> devices =
                runInternalQuery(QueryType.GetGraphicsDevices, new IdQueryParameters(getVmBaseId())).getReturnValue();
        return devices.isEmpty();
    }

    private boolean setVmToHeadlessMode() {
        VmStatic vmStatic = vmStaticDao.get(getVmBaseId());
        if (vmStatic != null && vmStatic.getDefaultDisplayType() != DisplayType.none) {
            vmStatic.setDefaultDisplayType(DisplayType.none);
            resourceManager.getVmManager(getVmBaseId()).update(vmStatic);
            return true;
        }
        return false;
    }

    private void setTemplateToHeadlessMode() {
        VmTemplate vmTemplate = vmTemplateDao.get(getVmBaseId());
        if (vmTemplate != null && vmTemplate.getDefaultDisplayType() != DisplayType.none) {
            vmTemplate.setDefaultDisplayType(DisplayType.none);
            vmTemplateDao.update(vmTemplate);
        }
    }
}
