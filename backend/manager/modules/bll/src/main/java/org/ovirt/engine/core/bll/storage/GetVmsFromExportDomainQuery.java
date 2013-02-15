package org.ovirt.engine.core.bll.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.ImportVmCommand;
import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParameters;
import org.ovirt.engine.core.common.vdscommands.GetVmsInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.ovf.OvfManager;
import org.ovirt.engine.core.utils.ovf.OvfReaderException;

public class GetVmsFromExportDomainQuery<P extends GetAllFromExportDomainQueryParameters>
        extends QueriesCommandBase<P> {
    public GetVmsFromExportDomainQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        StorageDomainStatic storage = DbFacade.getInstance().getStorageDomainStaticDao().get(
                getParameters().getStorageDomainId());
        if (storage.getStorageDomainType() == StorageDomainType.ImportExport) {
            VDSReturnValue retVal = null;
            retVal = executeVerb(storage);
            buildOvfReturnValue(retVal.getReturnValue());
        } else {
            getQueryReturnValue().setReturnValue(new java.util.ArrayList<VM>());
        }
    }

    protected VDSReturnValue executeVerb(StorageDomainStatic storage) {
        try {
            GetVmsInfoVDSCommandParameters tempVar = new GetVmsInfoVDSCommandParameters(
                    getParameters().getStoragePoolId());
            tempVar.setStorageDomainId(getParameters().getStorageDomainId());
            tempVar.setVmIdList(getParameters().getIds());
            VDSReturnValue retVal = Backend.getInstance().getResourceManager()
                    .RunVdsCommand(VDSCommandType.GetVmsInfo, tempVar);
            return retVal;
        } catch (RuntimeException e) {
            AuditLogableBase logable = new AuditLogableBase();
            logable.addCustomValue("StorageDomainName", storage.getStorageName());
            AuditLogDirector.log(logable, AuditLogType.IMPORTEXPORT_GET_VMS_INFO_FAILED);
            throw e;
        }
    }

    protected boolean isValidExportDomain() {
        StorageDomain domain = DbFacade.getInstance().getStorageDomainDao().getForStoragePool(
                getParameters().getStorageDomainId(),
                getParameters().getStoragePoolId());
        if (domain != null && domain.getStorageDomainType() == StorageDomainType.ImportExport) {
            return true;
        }
        return false;
    }

    protected void buildOvfReturnValue(Object obj) {
        ArrayList<String> ovfList = (ArrayList<String>) obj;
        OvfManager ovfManager = new OvfManager();
        ArrayList<VM> vms = new ArrayList<VM>();

        if (isValidExportDomain()) {
            VM vm = null;
            for (String ovf : ovfList) {
                try {
                    if (!ovfManager.IsOvfTemplate(ovf)) {
                        vm = new VM();
                        ArrayList<DiskImage> diskImages = new ArrayList<DiskImage>();
                        ArrayList<VmNetworkInterface> interfaces  = new ArrayList<VmNetworkInterface>();
                        ovfManager.ImportVm(ovf, vm, diskImages, interfaces);

                        // add images
                        vm.setImages(diskImages);
                        // add interfaces
                        vm.setInterfaces(interfaces);

                        // add disk map
                        Map<Guid, List<DiskImage>> images = ImportVmCommand
                                .getImagesLeaf(diskImages);
                        for (Guid id : images.keySet()) {
                            List<DiskImage> list = images.get(id);
                            vm.getDiskMap().put(id, list.get(list.size() - 1));
                        }
                        vms.add(vm);
                    }
                } catch (OvfReaderException ex) {
                    AuditLogableBase logable = new AuditLogableBase();
                    logable.addCustomValue("ImportedVmName", ex.getName());
                    AuditLogDirector.log(logable, AuditLogType.IMPORTEXPORT_FAILED_TO_IMPORT_VM);
                } catch (RuntimeException ex) {
                    AuditLogableBase logable = new AuditLogableBase();
                    logable.addCustomValue("ImportedVmName", "[Unknown name]");
                    AuditLogDirector.log(logable, AuditLogType.IMPORTEXPORT_FAILED_TO_IMPORT_VM);
                }
            }
        }

        getQueryReturnValue().setReturnValue(vms);
    }
}
