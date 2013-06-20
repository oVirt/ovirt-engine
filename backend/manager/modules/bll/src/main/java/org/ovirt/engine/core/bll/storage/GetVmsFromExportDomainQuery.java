package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.bll.OvfHelper;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParameters;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.ovf.OvfReaderException;

import java.util.ArrayList;
import java.util.List;

public class GetVmsFromExportDomainQuery<P extends GetAllFromExportDomainQueryParameters>
        extends GetAllFromExportDomainQuery<List<VM>, P> {

    public GetVmsFromExportDomainQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected List<VM> buildFromOVFs(List<String> ovfList) {
        OvfHelper ovfHelper = new OvfHelper();
        List<VM> vms = new ArrayList<VM>();

        for (String ovf : ovfList) {
            try {
                if (!ovfHelper.isOvfTemplate(ovf)) {
                    vms.add(ovfHelper.readVmFromOvf(ovf));
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
