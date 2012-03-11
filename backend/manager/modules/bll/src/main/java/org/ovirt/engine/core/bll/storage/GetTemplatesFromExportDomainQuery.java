package org.ovirt.engine.core.bll.storage;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage_domain_static;
import org.ovirt.engine.core.common.queries.DiskImageList;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParamenters;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.ovf.OvfManager;
import org.ovirt.engine.core.utils.ovf.OvfReaderException;

public class GetTemplatesFromExportDomainQuery<P extends GetAllFromExportDomainQueryParamenters>
        extends GetVmsFromExportDomainQuery<P> {
    public GetTemplatesFromExportDomainQuery(P parameters) {
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
            java.util.HashMap<VmTemplate, java.util.ArrayList<DiskImage>> templates =
                    new java.util.HashMap<VmTemplate, java.util.ArrayList<DiskImage>>();
            getQueryReturnValue().setReturnValue(templates);
        }
    }

    @Override
    protected void BuildOvfReturnValue(Object obj) {
        boolean shouldAdd = true;
        java.util.ArrayList<String> ovfList = (java.util.ArrayList<String>) obj;
        OvfManager ovfManager = new OvfManager();
        java.util.HashMap<VmTemplate, DiskImageList> templates = new java.util.HashMap<VmTemplate, DiskImageList>();
        List<VmTemplate> existsTemplates = DbFacade.getInstance().getVmTemplateDAO().getAll();
        java.util.HashMap<Guid, VmTemplate> existsVmDictionary = new java.util.HashMap<Guid, VmTemplate>();
        for (VmTemplate vmTemplate : existsTemplates) {
            existsVmDictionary.put(vmTemplate.getId(), vmTemplate);
        }
        if (IsValidExportDomain()) {
            VmTemplate template = null;
            for (String ovf : ovfList) {
                try {
                    if (ovfManager.IsOvfTemplate(ovf)) {
                        java.util.ArrayList<DiskImage> diskImages = null;
                        java.util.ArrayList<VmNetworkInterface> interfaces = null;
                        RefObject<VmTemplate> tempRefObject = new RefObject<VmTemplate>(template);
                        RefObject<java.util.ArrayList<DiskImage>> tempRefObject2 =
                                new RefObject<java.util.ArrayList<DiskImage>>(
                                        diskImages);
                        RefObject<ArrayList<VmNetworkInterface>> interfacesRefObject =
                            new RefObject<ArrayList<VmNetworkInterface>>(
                                    interfaces);
                        ovfManager.ImportTemplate(ovf, tempRefObject, tempRefObject2, interfacesRefObject);
                        template = tempRefObject.argvalue;
                        diskImages = tempRefObject2.argvalue;
                        interfaces = interfacesRefObject.argvalue;
                        shouldAdd = getParameters().getGetAll() ? shouldAdd :
                                (!existsVmDictionary.containsKey(template.getId()) && diskImages != null);
                        if (shouldAdd) {
                            templates.put(template, new DiskImageList(diskImages));
                            template.setInterfaces(interfaces);
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
