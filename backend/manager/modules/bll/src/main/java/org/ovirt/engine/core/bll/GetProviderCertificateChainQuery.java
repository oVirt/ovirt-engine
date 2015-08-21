package org.ovirt.engine.core.bll;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.collections.CollectionUtils;
import org.ovirt.engine.core.bll.provider.ProviderProxy;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.common.businessentities.CertificateInfo;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.queries.ProviderQueryParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetProviderCertificateChainQuery<P extends ProviderQueryParameters> extends QueriesCommandBase<P> {

    private static Logger log = LoggerFactory.getLogger(GetProviderCertificateChainQuery.class);

    public GetProviderCertificateChainQuery(P parameters) {
        super(parameters);
    }

    private Provider<?> getProvider() {
        return getParameters().getProvider();
    }

    @Override
    protected void executeQueryCommand() {
        Provider<?> provider = getProvider();
        try {
            ProviderProxy proxy = ProviderProxyFactory.getInstance().create(provider);
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
        MessageDigest sha1 = MessageDigest.getInstance("SHA1");
        sha1.update(cert.getEncoded());

        boolean selfSigned = false;
        try {
            cert.verify(cert.getPublicKey());
            selfSigned = true;
        } catch (GeneralSecurityException e) {
            // ignore
        }

        return new CertificateInfo(new Base64(0).encodeToString(cert.getEncoded()),
                cert.getSubjectX500Principal().toString(), cert.getIssuerX500Principal().toString(),
                selfSigned, Hex.encodeHexString(sha1.digest()));
    }

}
