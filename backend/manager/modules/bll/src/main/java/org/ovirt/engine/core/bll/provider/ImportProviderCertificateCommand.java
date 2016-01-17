package org.ovirt.engine.core.bll.provider;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.util.Collections;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ImportProviderCertificateParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;

/*
 * This command class imports a certificate of an external provider into the external trust store.
 * This class is deprecated, eventually {@link ImportProviderCertificateCommand should be used}
 */
public class ImportProviderCertificateCommand<P extends ImportProviderCertificateParameters> extends CommandBase<P> {

    public ImportProviderCertificateCommand(Guid commandId) {
        super(commandId);
    }

    public ImportProviderCertificateCommand(P parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    private Provider<?> getProvider() {
        return getParameters().getProvider();
    }

    public String getProviderName() {
        return getProvider().getName();
    }

    @Override
    protected void executeCommand() {
        try {
            String encoded = getParameters().getCertificate();
            if (encoded == null || encoded.isEmpty()) {
                throw new RuntimeException("Certificate is missing");
            }

            try (ByteArrayInputStream bis = new ByteArrayInputStream(new Base64(0).decode(encoded))) {
                ExternalTrustStoreInitializer.addCertificate(CertificateFactory.getInstance("X.509").generateCertificate(bis));
            }
            setSucceeded(true);
        } catch (Throwable e) {
            handleException(e);
        }
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        // Currently it requires what's required for adding a new Provider
        // Need to revisit that when designing the permission scheme for providers
        return Collections.singletonList(new PermissionSubject(Guid.SYSTEM,
                VdcObjectType.System,
                ActionGroup.CREATE_STORAGE_POOL));
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.PROVIDER_CERTIFICATE_IMPORTED
                : AuditLogType.PROVIDER_CERTIFICATE_IMPORT_FAILED;
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__IMPORT);
        addValidationMessage(EngineMessage.VAR__TYPE__PROVIDER_CERTIFICATE);
    }

    private void handleException(Throwable e) {
        log.error(String.format("Failed to import certificate: %1$s", e.getMessage()));
        log.debug("Exception", e);
        throw new EngineException(EngineError.PROVIDER_IMPORT_CERTIFICATE_ERROR, e.getMessage());
    }
}
