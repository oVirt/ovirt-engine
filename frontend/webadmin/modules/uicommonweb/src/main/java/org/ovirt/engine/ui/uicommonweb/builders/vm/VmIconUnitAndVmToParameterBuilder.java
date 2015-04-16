package org.ovirt.engine.ui.uicommonweb.builders.vm;

import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.ui.uicommonweb.IconUtils;
import org.ovirt.engine.ui.uicommonweb.builders.BaseSyncBuilder;
import org.ovirt.engine.ui.uicommonweb.models.vms.IconCache;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

/**
 * It sets {@link VmManagementParametersBase#setVmLargeIcon(String)} if necessary i.e. if the icon was changed in
 * editor && new icon is not predefined one.
 */
public class VmIconUnitAndVmToParameterBuilder
        extends BaseSyncBuilder<Pair<UnitVmModel, VM>, VmManagementParametersBase> {
    @Override
    protected void build(Pair<UnitVmModel, VM> source, VmManagementParametersBase destination) {
        final UnitVmModel newModel = source.getFirst();
        final VM oldVm = source.getSecond();
        final String newIcon = newModel.getIcon().getEntity().getIcon();
        final String originalIcon = IconCache.getInstance().getIcon(oldVm.getStaticData().getLargeIconId());
        if (!newIcon.equals(originalIcon) && IconUtils.isCustom(newIcon)) {
            destination.setVmLargeIcon(newIcon);
        }
    }
}
