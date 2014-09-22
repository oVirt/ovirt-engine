package org.ovirt.engine.core.services;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.ovirt.engine.core.uutils.servlet.ProxyServletBase;

public class ProxyServlet extends ProxyServletBase {

    private static final long serialVersionUID = 209763132892410985L;

    private static final String VERIFY_HOST_PRM = "verifyHost";
    private static final String VERIFY_CHAIN_PRM = "verifyChain";
    private static final String HTTPS_PROTOCOL_PRM = "httpsProtocol";
    private static final String TRUST_MANAGER_ALGORITHM_PRM = "trustManagerAlgorithm";
    private static final String TRUST_STORE_PRM = "trustStore";
    private static final String TRUST_STORE_TYPE_PRM = "trustStoreType";
    private static final String TRUST_STORE_PASSWORD_PRM = "trustStorePassword";
    private static final String READ_TIMEOUT_PRM = "readTimeout";
    private static final String URL_PRM = "url";
    private static final String HTTP = "http://";
    private static final String HTTPS = "https://";

    private String getConfigString(String name) {
        String r = getServletConfig().getInitParameter(name);
        if (r != null) {
            r = EngineLocalConfig.getInstance().expandString(r.replaceAll("%\\{", "\\${"));
        }
        if (StringUtils.isEmpty(r)) {
            r = null;
        }
        return r;
    }

    private Boolean getConfigBoolean(String name) {
        Boolean r = null;
        String s = getConfigString(name);
        if (s != null) {
            r = Boolean.valueOf(s);
        }
        return r;
    }

    private Integer getConfigInteger(String name) {
        Integer r = null;
        String s = getConfigString(name);
        if (s != null) {
            r = Integer.valueOf(s);
        }
        return r;
    }

    private String getUrl() {
        String url = getConfigString(URL_PRM);
        if (!url.startsWith(HTTP) && !url.startsWith(HTTPS)) {
            url = null;
        }
        return url;
    }

    @Override
    public void init() throws ServletException {
        setVerifyHost(getConfigBoolean(VERIFY_HOST_PRM));
        setVerifyChain(getConfigBoolean(VERIFY_CHAIN_PRM));
        setHttpsProtocol(getConfigString(HTTPS_PROTOCOL_PRM));
        setTrustManagerAlgorithm(getConfigString(TRUST_MANAGER_ALGORITHM_PRM));
        setTrustStore(getConfigString(TRUST_STORE_PRM));
        setTrustStoreType(getConfigString(TRUST_STORE_TYPE_PRM));
        setTrustStorePassword(getConfigString(TRUST_STORE_PASSWORD_PRM));
        setReadTimeout(getConfigInteger(READ_TIMEOUT_PRM));
        setUrl(getUrl());
    }

}
