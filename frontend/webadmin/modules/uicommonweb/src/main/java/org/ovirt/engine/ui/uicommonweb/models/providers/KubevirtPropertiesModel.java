package org.ovirt.engine.ui.uicommonweb.models.providers;

import org.ovirt.engine.core.common.businessentities.KubevirtProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;

public class KubevirtPropertiesModel extends Model {

    private EntityModel<String> certificateAuthority = new EntityModel<>();

    private EntityModel<String> prometheusUrl = new EntityModel<>();

    private EntityModel<String> prometheusCertificateAuthority = new EntityModel<>();

    public EntityModel<String> getCertificateAuthority() {
        return certificateAuthority;
    }

    public EntityModel<String> getPrometheusUrl() {
        return prometheusUrl;
    }

    public EntityModel<String> getPrometheusCertificateAuthority() {
        return prometheusCertificateAuthority;
    }

    public void setCertificateAuthority(EntityModel<String> certificateAuthority) {
        this.certificateAuthority = certificateAuthority;
    }

    public void setPrometheusUrl(EntityModel<String> prometheusUrl) {
        this.prometheusUrl = prometheusUrl;
    }

    public void setPrometheusCertificateAuthority(EntityModel<String> prometheusCertificateAuthority) {
        this.prometheusCertificateAuthority = prometheusCertificateAuthority;
    }

    public KubevirtProviderProperties getKubevirtProviderProperties() {
        KubevirtProviderProperties properties = new KubevirtProviderProperties();
        properties.setCertificateAuthority(getCertificateAuthority().getEntity());
        properties.setPrometheusUrl(getPrometheusUrl().getEntity());
        properties.setPrometheusCertificateAuthority(getPrometheusCertificateAuthority().getEntity());
        return properties;
    }

    public void init(Provider<KubevirtProviderProperties> provider) {
        getCertificateAuthority().setEntity(provider.getAdditionalProperties().getCertificateAuthority());
        getPrometheusUrl().setEntity(provider.getAdditionalProperties().getPrometheusUrl());
        getPrometheusCertificateAuthority().setEntity(
            provider.getAdditionalProperties().getPrometheusCertificateAuthority()
        );
    }
}
