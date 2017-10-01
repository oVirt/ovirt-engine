package org.ovirt.engine.core.bll.exportimport;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.storage.ovfstore.OvfHelper;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.GetAllFromExportDomainQueryParameters;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.utils.ovf.OvfReaderException;

public class GetVmsFromExportDomainQuery<P extends GetAllFromExportDomainQueryParameters>
        extends GetAllFromExportDomainQuery<List<VM>, P> {

    @Inject
    private OvfHelper ovfHelper;

    public GetVmsFromExportDomainQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected List<VM> buildFromOVFs(List<String> ovfList) {
        List<VM> vms = new ArrayList<>();

        for (String ovf : ovfList) {
            try {
                if (!ovfHelper.isOvfTemplate(ovf)) {
                    vms.add(ovfHelper.readVmFromOvf(ovf).getVm());
                }
            } catch (OvfReaderException ex) {
                auditLogOvfLoadError(ex.getName(), ex.getMessage());
            }
        }

        return vms;
    }

    private void auditLogOvfLoadError(String machineName, String errorMessage) {
        AuditLogable logable = new AuditLogableImpl();
        logable.addCustomValue("ImportedVmName", machineName).addCustomValue("ErrorMessage", errorMessage);
        auditLogDirector.log(logable, AuditLogType.IMPORTEXPORT_FAILED_TO_IMPORT_VM);
    }
}
