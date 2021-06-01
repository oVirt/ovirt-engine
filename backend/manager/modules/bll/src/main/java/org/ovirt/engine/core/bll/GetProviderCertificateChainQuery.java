package org.ovirt.engine.core.bll;

import static org.ovirt.engine.core.common.businessentities.CertificateInfo.SHA256_ALGO;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.collections.CollectionUtils;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.provider.ProviderProxy;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.common.businessentities.CertificateInfo;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.queries.ProviderQueryParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetProviderCertificateChainQuery<P extends ProviderQueryParameters> extends QueriesCommandBase<P> {

    private static Logger log = LoggerFactory.getLogger(GetProviderCertificateChainQuery.class);

    public GetProviderCertificateChainQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    private Provider<?> getProvider() {
        return getParameters().getProvider();
    }

    @Inject
    private ProviderProxyFactory providerProxyFactory;

    @Override
    protected void executeQueryCommand() {
        Provider<?> provider = getProvider();
        try {
            ProviderProxy proxy = providerProxyFactory.create(provider);
            List<? extends Certificate> chain = proxy.getCertificateChain();
            List<CertificateInfo> results = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(chain)) {
                for (Certificate cert : chain) {
                    if (cert instanceof X509Certificate) {
                        results.add(createCertificateInfo((X509Certificate) cert));
                    }
                }
            }
            getQueryReturnValue().setReturnValue(results);
        } catch (Exception e) {
            log.error("Error in encoding certificate: {}", e.getMessage());
            log.debug("Exception", e);
        }
    }

    private CertificateInfo createCertificateInfo(X509Certificate cert) throws GeneralSecurityException {
        MessageDigest sha256 = MessageDigest.getInstance(SHA256_ALGO);
        sha256.update(cert.getEncoded());

        boolean selfSigned = false;
        try {
            cert.verify(cert.getPublicKey());
            selfSigned = true;
        } catch (GeneralSecurityException e) {
            // ignore
        }

        return new CertificateInfo(new Base64(0).encodeToString(cert.getEncoded()),
                cert.getSubjectX500Principal().toString(), cert.getIssuerX500Principal().toString(),
                selfSigned, Hex.encodeHexString(sha256.digest()));
    }

}
