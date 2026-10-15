package org.ovirt.engine.core.bll.exportimport;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.ConvertOvaParameters;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorageDisk;
import org.ovirt.engine.core.compat.Guid;

@NonTransactiveCommandAttribute
public class MbsConvertOvaCommand<T extends ConvertOvaParameters> extends ConvertOvaCommand<T> {

    public MbsConvertOvaCommand(Guid commandId) {
        super(commandId);
    }

    public MbsConvertOvaCommand(T parameters, CommandContext context) {
        super(parameters, context);
    }

    @Override
    protected void executeVmCommand() {
        applyPreAttachedManagedBlockDevicesToChildCommandDisks();
        super.executeVmCommand();
    }

    private void applyPreAttachedManagedBlockDevicesToChildCommandDisks() {
        Map<Guid, Map<String, Object>> fromParent = getParameters().getPreAttachedManagedBlockDevicesByDiskId();
        if (MapUtils.isEmpty(fromParent) || CollectionUtils.isEmpty(getParameters().getDisks())) {
            return;
        }
        for (DiskImage d : getParameters().getDisks()) {
            if (d instanceof ManagedBlockStorageDisk) {
                Map<String, Object> dev = fromParent.get(d.getId());
                if (dev != null) {
                    ((ManagedBlockStorageDisk) d).setDevice(new HashMap<>(dev));
                }
            }
        }
    }
}
