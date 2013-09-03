package org.ovirt.engine.core.bll.provider;

import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ProviderParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;

/*
 * This command class imports a certificate chain of an external provider into the external trust store.
 */
public class ImportProviderCertificateChainCommand<P extends ProviderParameters> extends CommandBase<P> {

    public ImportProviderCertificateChainCommand(Guid commandId) {
        super(commandId);
    }

    public ImportProviderCertificateChainCommand(P parameters) {
        super(parameters);
    }

    private Provider getProvider() {
        return getParameters().getProvider();
    }

    public String getProviderName() {
        return getProvider().getName();
    }

    @Override
    protected void executeCommand() {
        Provider provider = getProvider();
        ProviderProxy proxy = ProviderProxyFactory.getInstance().create(provider);
        List<? extends Certificate> chain = proxy.getCertificateChain();
        saveChainToTrustStore(chain);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        // Currently it requires what's required for adding a new Provider
        // Need to revisit that when designing the permission scheme for providers
        return Collections.singletonList(new PermissionSubject(Guid.SYSTEM,
                VdcObjectType.System,
                ActionGroup.CREATE_STORAGE_POOL));
    }

    private void saveChainToTrustStore(List<? extends Certificate> chain) {
        if (chain != null && chain.size() > 0) {
            KeyStore ks = null;
            try {
                ks = ExternalTrustStoreInitializer.getTrustStore();
            } catch (Throwable e) {
                handleException(e);
            }

            try {
                // In case there is only one certificate, we insert it.
                // Otherwise, we need to insert the entire chain except the end certificate (the end certificate here is the first one)
                int firstCertificateIndex = chain.size() == 1 ? 0 : 1;
                for (int certIndex = firstCertificateIndex; certIndex < chain.size(); ++certIndex) {
                    Certificate certificate = chain.get(certIndex);
                    String alias = Guid.newGuid().toString();
                    ks.setCertificateEntry(alias, certificate);
                }

                ExternalTrustStoreInitializer.setTrustStore(ks);
                setSucceeded(true);
            } catch (Throwable e) {
                handleException(e);
            }
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.PROVIDER_CERTIFICATE_CHAIN_IMPORTED : AuditLogType.PROVIDER_CERTIFICATE_CHAIN_IMPORT_FAILED;
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__IMPORT);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__PROVIDER_CERTIFICATE_CHAIN);
    }

    private void handleException(Throwable e) {
        throw new VdcBLLException(VdcBllErrors.PROVIDER_IMPORT_CERTIFICATE_CHAIN_ERROR, e.getMessage());
    }
}
