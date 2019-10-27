package org.ovirt.engine.core.common.businessentities;

public class KubevirtProviderProperties implements Provider.AdditionalProperties {

    private String certificateAuthority;
    private String consoleUrl;
    private String prometheusUrl;
    private String prometheusCertificateAuthority;

    public KubevirtProviderProperties() {
    }

    public KubevirtProviderProperties(String certificateAuthority) {
        this(certificateAuthority, null);
    }

    public KubevirtProviderProperties(String certificateAuthority, String prometheusUrl) {
        this.setCertificateAuthority(certificateAuthority);
        this.setPrometheusUrl(prometheusUrl);
    }

    public String getCertificateAuthority() {
        return certificateAuthority;
    }

    public void setCertificateAuthority(String certificateAuthority) {
        this.certificateAuthority = certificateAuthority;
    }

    public void setConsoleUrl(String consoleUrl) {
        this.consoleUrl = consoleUrl;
    }

    public String getConsoleUrl() {
        return consoleUrl;
    }

    public void setPrometheusUrl(String prometheusUrl) {
        this.prometheusUrl = prometheusUrl;
    }

    public String getPrometheusUrl() {
        return prometheusUrl;
    }

    public void setPrometheusCertificateAuthority(String prometheusCertificateAuthority) {
        this.prometheusCertificateAuthority = prometheusCertificateAuthority;
    }

    public String getPrometheusCertificateAuthority() {
        return prometheusCertificateAuthority;
    }
}
