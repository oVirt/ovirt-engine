package org.ovirt.engine.core.bll.storage.disk;

import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_ACTIVE;
import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_PLUGGED;
import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_SNAPABLE;

import java.util.List;

import org.ovirt.engine.core.bll.DisableInPrepareMode;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.image.DisksFilter;
import org.ovirt.engine.core.common.action.CreateAllTemplateDisksParameters;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;

@DisableInPrepareMode
@NonTransactiveCommandAttribute
@InternalCommandAttribute
public class CreateAllTemplateDisksFromSnapshotCommand<T extends CreateAllTemplateDisksParameters> extends CreateAllTemplateDisksCommand<T> {

    public CreateAllTemplateDisksFromSnapshotCommand(Guid commandId) {
        super(commandId);
    }

    public CreateAllTemplateDisksFromSnapshotCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected List<DiskImage> getVmDisksFromDb() {
        List<DiskImage> disksFromDb =
                DisksFilter.filterImageDisks(getVm().getDiskMap().values(), ONLY_SNAPABLE, ONLY_ACTIVE);
        disksFromDb.addAll(DisksFilter.filterCinderDisks(getVm().getDiskMap().values(), ONLY_PLUGGED));
        return disksFromDb;
    }

}
