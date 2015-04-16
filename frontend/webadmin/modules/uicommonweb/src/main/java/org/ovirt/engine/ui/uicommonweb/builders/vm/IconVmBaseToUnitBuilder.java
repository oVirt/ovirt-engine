package org.ovirt.engine.ui.uicommonweb.builders.vm;

import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.models.vms.IconCache;
import org.ovirt.engine.ui.uicommonweb.builders.Builder;
import org.ovirt.engine.ui.uicommonweb.builders.BuilderList;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.vms.IconWithOsDefault;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

import java.util.Arrays;
import java.util.Map;

public class IconVmBaseToUnitBuilder implements Builder<VmBase, UnitVmModel> {

    @Override
    public void build(final VmBase source, final UnitVmModel destination, final BuilderList<VmBase, UnitVmModel> rest) {
        final Guid defaultIconId = AsyncDataProvider.getInstance().getOsDefaultIconId(source.getOsId(), false);
        final Guid iconId = source.getLargeIconId() != null
                ? source.getLargeIconId()
                : defaultIconId;
        IconCache.getInstance().getOrFetchIcons(Arrays.asList(iconId, defaultIconId), new IconCache.IconsCallback() {
            @Override
            public void onSuccess(Map<Guid, String> idToIconMap) {
                destination.getIcon().setEntity(new IconWithOsDefault(
                        idToIconMap.get(iconId),
                        idToIconMap.get(defaultIconId),
                        source.getSmallIconId()
                ));
                rest.head().build(source, destination, rest.tail());
            }
        });
    }
}
