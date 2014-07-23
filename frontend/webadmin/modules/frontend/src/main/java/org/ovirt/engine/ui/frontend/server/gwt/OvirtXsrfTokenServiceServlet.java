package org.ovirt.engine.ui.frontend.server.gwt;

import java.nio.charset.Charset;

import com.google.gwt.user.client.rpc.XsrfToken;
import com.google.gwt.user.server.rpc.XsrfTokenServiceServlet;
import com.google.gwt.util.tools.shared.Md5Utils;
import com.google.gwt.util.tools.shared.StringUtils;

public class OvirtXsrfTokenServiceServlet extends XsrfTokenServiceServlet {

    /**
     * serial version UID.
     */
    private static final long serialVersionUID = 1854606938563216502L;

    @Override
    public XsrfToken getNewXsrfToken() {
        return new XsrfToken(generateTokenValueResponse());
    }

    private String generateTokenValueResponse() {
        byte[] tokenBytes =  getThreadLocalRequest().getSession().getId().getBytes(
                Charset.forName("UTF-8")); //$NON-NLS-1$
        return StringUtils.toHexString(Md5Utils.getMd5Digest(tokenBytes));
    }
}
