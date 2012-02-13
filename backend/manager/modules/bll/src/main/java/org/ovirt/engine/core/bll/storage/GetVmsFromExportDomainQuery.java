package org.ovirt.engine.core.bll.storage;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.ImportVmCommand;
import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage_domain_static;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParamenters;
import org.ovirt.engine.core.common.vdscommands.GetVmsInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.ovf.OvfManager;
import org.ovirt.engine.core.utils.ovf.OvfReaderException;

public class GetVmsFromExportDomainQuery<P extends GetAllFromExportDomainQueryParamenters>
        extends QueriesCommandBase<P> {
    public GetVmsFromExportDomainQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        storage_domain_static storage = DbFacade.getInstance().getStorageDomainStaticDAO().get(
                getParameters().getStorageDomainId());
        if (storage.getstorage_domain_type() == StorageDomainType.ImportExport) {
            VDSReturnValue retVal = ExecuteVerb();
            BuildOvfReturnValue(retVal.getReturnValue());
        } else {
            getQueryReturnValue().setReturnValue(new java.util.ArrayList<VM>());
        }
    }

    protected VDSReturnValue ExecuteVerb() {
        GetVmsInfoVDSCommandParameters tempVar = new GetVmsInfoVDSCommandParameters(
                getParameters().getStoragePoolId());
        tempVar.setStorageDomainId(getParameters().getStorageDomainId());
        tempVar.setVmIdList(getParameters().getIds());
        VDSReturnValue retVal = Backend.getInstance().getResourceManager()
                .RunVdsCommand(VDSCommandType.GetVmsInfo, tempVar);
        return retVal;
    }

    protected boolean IsValidExportDomain() {
        storage_domains domain = DbFacade.getInstance().getStorageDomainDAO().getForStoragePool(
                getParameters().getStorageDomainId(),
                getParameters().getStoragePoolId());
        if (domain != null && domain.getstorage_domain_type() == StorageDomainType.ImportExport) {
            return true;
        }
        return false;
    }

    protected void BuildOvfReturnValue(Object obj) {
        boolean shouldAdd = true;
        java.util.ArrayList<String> ovfList = (java.util.ArrayList<String>) obj;
        OvfManager ovfManager = new OvfManager();
        java.util.ArrayList<VM> vms = new java.util.ArrayList<VM>();
        List<VM> existsVms = DbFacade.getInstance().getVmDAO().getAll();
        java.util.HashMap<Guid, VM> existsVmDictionary = new java.util.HashMap<Guid, VM>();
        for (VM vm : existsVms) {
            existsVmDictionary.put(vm.getId(), vm);
        }

        if (IsValidExportDomain()) {
            VM vm = null;
            for (String ovf : ovfList) {
                try {
                    if (!ovfManager.IsOvfTemplate(ovf)) {
                        java.util.ArrayList<DiskImage> diskImages = null;
                        RefObject<VM> tempRefObject = new RefObject<VM>(vm);
                        RefObject<java.util.ArrayList<DiskImage>> tempRefObject2 =
                                new RefObject<java.util.ArrayList<DiskImage>>(
                                        diskImages);
                        ovfManager.ImportVm(ovf, tempRefObject, tempRefObject2);

                        vm = tempRefObject.argvalue;
                        diskImages = tempRefObject2.argvalue;
                        shouldAdd = getParameters().getGetAll() ? shouldAdd : !existsVmDictionary
                                .containsKey(vm.getId());

                        if (shouldAdd) {
                            // add images
                            vm.setImages(diskImages);

                            // add disk map
                            Map<String, List<DiskImage>> images = ImportVmCommand
                                    .GetImagesLeaf(diskImages);
                            for (String drive : images.keySet()) {
                                List<DiskImage> list = images.get(drive);
                                vm.getDiskMap().put(drive, list.get(list.size() - 1));
                            }
                            vms.add(vm);
                        }
                    }
                } catch (OvfReaderException ex) {
                    AuditLogableBase logable = new AuditLogableBase();
                    logable.AddCustomValue("ImportedVmName", ex.getName());
                    AuditLogDirector.log(logable, AuditLogType.IMPORTEXPORT_FAILED_TO_IMPORT_VM);
                } catch (RuntimeException ex) {
                    AuditLogableBase logable = new AuditLogableBase();
                    logable.AddCustomValue("ImportedVmName", "[Unknown name]");
                    AuditLogDirector.log(logable, AuditLogType.IMPORTEXPORT_FAILED_TO_IMPORT_VM);
                }
            }
        }

        getQueryReturnValue().setReturnValue(vms);
    }
}
