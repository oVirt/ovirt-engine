package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.bll.ImagesHandler;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.ovf.OvfManager;
import org.ovirt.engine.core.utils.ovf.OvfReaderException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GetVmsFromExportDomainQuery<P extends GetAllFromExportDomainQueryParameters>
        extends GetAllFromExportDomainQuery<List<VM>, P> {

    public GetVmsFromExportDomainQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected List<VM> buildFromOVFs(List<String> ovfList) {
        OvfManager ovfManager = new OvfManager();
        List<VM> vms = new ArrayList<VM>();

        for (String ovf : ovfList) {
            try {
                if (!ovfManager.IsOvfTemplate(ovf)) {
                    VM vm = new VM();
                    ArrayList<DiskImage> diskImages = new ArrayList<DiskImage>();
                    ArrayList<VmNetworkInterface> interfaces  = new ArrayList<VmNetworkInterface>();
                    ovfManager.ImportVm(ovf, vm, diskImages, interfaces);

                    // add images
                    vm.setImages(diskImages);
                    // add interfaces
                    vm.setInterfaces(interfaces);

                    // add disk map
                    Map<Guid, List<DiskImage>> images = ImagesHandler
                            .getImagesLeaf(diskImages);
                    for (Map.Entry<Guid, List<DiskImage>> entry : images.entrySet()) {
                        List<DiskImage> list = entry.getValue();
                        vm.getDiskMap().put(entry.getKey(), list.get(list.size() - 1));
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

        return vms;
    }
}
