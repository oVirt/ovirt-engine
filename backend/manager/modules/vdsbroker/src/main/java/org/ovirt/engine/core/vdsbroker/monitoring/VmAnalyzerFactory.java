package org.ovirt.engine.core.vdsbroker.monitoring;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.VdsManager;
import org.ovirt.engine.core.vdsbroker.vdsbroker.entities.VmInternalData;

public class VmAnalyzerFactory {

    private VdsManager vdsManager;
    private boolean updateStatistics;

    @Inject
    private AuditLogDirector auditLogDirector;
    @Inject
    private DbFacade dbFacade;
    @Inject
    private ResourceManager resourceManager;

    public VmAnalyzerFactory(VdsManager vdsManager, boolean updateStatistics) {
        this.vdsManager = vdsManager;
        this.updateStatistics = updateStatistics;
    }

    protected VmAnalyzer getVmAnalyzer(Pair<VM, VmInternalData> monitoredVm) {
        VmAnalyzer vmAnalyzer = new VmAnalyzer(monitoredVm.getFirst(), monitoredVm.getSecond(), updateStatistics);
        vmAnalyzer.setDbFacade(dbFacade);
        vmAnalyzer.setResourceManager(resourceManager);
        vmAnalyzer.setAuditLogDirector(auditLogDirector);
        vmAnalyzer.setVdsManager(vdsManager);
        return vmAnalyzer;
    }
}
