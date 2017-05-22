package org.ovirt.engine.core.bll.storage.disk;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.VmHandler;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.VmDao;

public class GetNextAvailableDiskAliasNameByVMIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private VmDao vmDao;


    @Inject
    private VmHandler vmHandler;

    public GetNextAvailableDiskAliasNameByVMIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        String suggestedDiskName = null;
        if (getParameters().getId() == null) {
            getQueryReturnValue().setReturnValue(suggestedDiskName);
        } else {
            VM vm = vmDao.get(getParameters().getId(), getUserID(), getParameters().isFiltered());
            if (vm != null) {
                updateDisksFromDb(vm);
                suggestedDiskName = getSuggestedDiskName(vm);
            }
            getQueryReturnValue().setReturnValue(suggestedDiskName);
        }
    }

    protected void updateDisksFromDb(VM vm) {
        vmHandler.updateDisksFromDb(vm);
    }

    private String getSuggestedDiskName(VM vm) {
        Set<String> aliases = createDiskAliasesList(vm);
        String suggestedDiskName;
        int i = 0;
        do {
            i++;
            suggestedDiskName = ImagesHandler.getDefaultDiskAlias(vm.getName(), Integer.toString(i));
        } while (aliases.contains(suggestedDiskName));
        return suggestedDiskName;
    }

    private Set<String> createDiskAliasesList(VM vm) {
        Set<String> diskAliases = new HashSet<>(vm.getDiskMap().size());
        for (Disk disk : vm.getDiskMap().values()) {
            diskAliases.add(disk.getDiskAlias());
        }
        return diskAliases;
    }
}
