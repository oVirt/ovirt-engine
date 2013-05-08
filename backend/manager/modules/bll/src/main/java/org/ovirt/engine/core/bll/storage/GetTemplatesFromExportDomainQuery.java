package org.ovirt.engine.core.bll.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.queries.DiskImageList;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParameters;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.ovf.OvfManager;
import org.ovirt.engine.core.utils.ovf.OvfReaderException;

public class GetTemplatesFromExportDomainQuery<P extends GetAllFromExportDomainQueryParameters>
        extends GetAllFromExportDomainQuery<Map<VmTemplate, DiskImageList>, P> {

    public GetTemplatesFromExportDomainQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected Map<VmTemplate, DiskImageList> buildFromOVFs(List<String> ovfList) {
        OvfManager ovfManager = new OvfManager();
        Map<VmTemplate, DiskImageList> templates = new HashMap<VmTemplate, DiskImageList>();

        for (String ovf : ovfList) {
            try {
                if (ovfManager.IsOvfTemplate(ovf)) {
                    ArrayList<DiskImage> diskImages = new ArrayList<DiskImage>();
                    ArrayList<VmNetworkInterface> interfaces = new ArrayList<VmNetworkInterface>();
                    VmTemplate template = new VmTemplate();
                    ovfManager.ImportTemplate(ovf, template, diskImages, interfaces);
                    template.setInterfaces(interfaces);
                    // add disk map
                    for (DiskImage disk : diskImages) {
                        template.getDiskMap().put(disk.getId(), disk);
                    }
                    templates.put(template, new DiskImageList(diskImages));
                }
            } catch (OvfReaderException ex) {
                AuditLogableBase logable = new AuditLogableBase();
                logable.addCustomValue("Template", ex.getName());
                AuditLogDirector.log(logable, AuditLogType.IMPORTEXPORT_FAILED_TO_IMPORT_TEMPLATE);
            } catch (RuntimeException ex) {
                AuditLogableBase logable = new AuditLogableBase();
                logable.addCustomValue("Template", "[Unknown name]");
                AuditLogDirector.log(logable, AuditLogType.IMPORTEXPORT_FAILED_TO_IMPORT_TEMPLATE);
            }

        }

        return templates;
    }
}
