package org.ovirt.engine.core.bll.hostdeploy;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VdsCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.vdsbroker.ResourceManager;

@NonTransactiveCommandAttribute
public class HostEnrollCertificateInternalCommand extends VdsCommand<VdsActionParameters> {

    @Inject
    private ResourceManager resourceManager;

    public HostEnrollCertificateInternalCommand(VdsActionParameters parameters, CommandContext context) {
        super(parameters, context);
    }

    @Override
    protected void executeCommand() {
        setVdsStatus(VDSStatus.Installing);
        try (final VdsDeploy vdsDeploy = new VdsDeploy("ovirt-host-mgmt", getVds(), false)) {
            vdsDeploy.addUnit(
                new VdsDeployPKIUnit(),
                new VdsDeployVmconsoleUnit(true)
            );
            vdsDeploy.setCorrelationId(getCorrelationId());
            vdsDeploy.useDefaultKeyPair();
            vdsDeploy.execute();

            resourceManager.reestablishConnection(getVdsId());
            setVdsStatus(VDSStatus.Maintenance);
            setSucceeded(true);
        } catch (final Exception e) {
            log.error("Failed to enroll certificate for host '{}': {}", getVds().getName(), e.getMessage());
            log.error("Exception", e);
            setVdsStatus(VDSStatus.InstallFailed);
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.HOST_CERTIFICATION_ENROLLMENT_FINISHED
                : AuditLogType.HOST_CERTIFICATION_ENROLLMENT_FAILED;
    }
}
