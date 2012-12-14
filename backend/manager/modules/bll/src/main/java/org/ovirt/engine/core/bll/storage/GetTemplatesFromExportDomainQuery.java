package org.ovirt.engine.core.bll.storage;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.queries.DiskImageList;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParameters;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.ovf.OvfManager;
import org.ovirt.engine.core.utils.ovf.OvfReaderException;

public class GetTemplatesFromExportDomainQuery<P extends GetAllFromExportDomainQueryParameters>
        extends GetVmsFromExportDomainQuery<P> {
    public GetTemplatesFromExportDomainQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        StorageDomainStatic storage = DbFacade.getInstance().getStorageDomainStaticDao().get(
                getParameters().getStorageDomainId());
        if (storage.getstorage_domain_type() == StorageDomainType.ImportExport) {
            VDSReturnValue retVal = executeVerb();
            buildOvfReturnValue(retVal.getReturnValue());
        } else {
            java.util.HashMap<VmTemplate, java.util.ArrayList<DiskImage>> templates =
                    new java.util.HashMap<VmTemplate, java.util.ArrayList<DiskImage>>();
            getQueryReturnValue().setReturnValue(templates);
        }
    }

    @Override
    protected void buildOvfReturnValue(Object obj) {
        boolean shouldAdd = true;
        ArrayList<String> ovfList = (ArrayList<String>) obj;
        OvfManager ovfManager = new OvfManager();
        java.util.HashMap<VmTemplate, DiskImageList> templates = new java.util.HashMap<VmTemplate, DiskImageList>();
        List<VmTemplate> existsTemplates = DbFacade.getInstance().getVmTemplateDao().getAll();
        java.util.HashMap<Guid, VmTemplate> existsVmDictionary = new java.util.HashMap<Guid, VmTemplate>();
        for (VmTemplate vmTemplate : existsTemplates) {
            existsVmDictionary.put(vmTemplate.getId(), vmTemplate);
        }
        if (isValidExportDomain()) {
            VmTemplate template = null;
            for (String ovf : ovfList) {
                try {
                    if (ovfManager.IsOvfTemplate(ovf)) {
                        ArrayList<DiskImage> diskImages = new ArrayList<DiskImage>();
                        ArrayList<VmNetworkInterface> interfaces = new ArrayList<VmNetworkInterface>();
                        template = new VmTemplate();
                        ovfManager.ImportTemplate(ovf, template, diskImages, interfaces);
                        shouldAdd = getParameters().getGetAll() ? shouldAdd :
                                (!existsVmDictionary.containsKey(template.getId()) && diskImages != null);
                        if (shouldAdd) {
                            template.setInterfaces(interfaces);
                            templates.put(template, new DiskImageList(diskImages));
                        }
                    }
                } catch (OvfReaderException ex) {
                    AuditLogableBase logable = new AuditLogableBase();
                    logable.AddCustomValue("Template", ex.getName());
                    AuditLogDirector.log(logable, AuditLogType.IMPORTEXPORT_FAILED_TO_IMPORT_TEMPLATE);
                } catch (RuntimeException ex) {
                    AuditLogableBase logable = new AuditLogableBase();
                    logable.AddCustomValue("Template", "[Unknown name]");
                    AuditLogDirector.log(logable, AuditLogType.IMPORTEXPORT_FAILED_TO_IMPORT_TEMPLATE);
                }

            }
        }

        getQueryReturnValue().setReturnValue(templates);
    }
}
