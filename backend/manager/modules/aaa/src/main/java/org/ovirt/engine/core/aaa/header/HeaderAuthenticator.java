package org.ovirt.engine.core.aaa.header;

import static org.apache.commons.lang.StringUtils.isEmpty;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ovirt.engine.api.extensions.Extension.ExtensionProperties;
import org.ovirt.engine.core.aaa.Authenticator;
import org.ovirt.engine.core.aaa.NegotiationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This authenticator assumes that the web server has already performed the authentication and takes the user from a
 * request header. The Apache web server, for example, can be configured to perform authentication and then populate a
 * header as follows:
 *
 * <pre>
 * &lt;Location /webadmin&gt;
 *   AuthType Basic
 *   AuthName "Protected"
 *   AuthBasicProvider file
 *   AuthUserFile /etc/httpd/conf/users
 *   Require valid-user
 *
 *   #
 *   # This is needed in order to enable the rewrite engine later, otherwise
 *   # the web server refuses to enable it because it allows similar mechanism to
 *   # cincumvent directory restrictions:
 *   #
 *   Options +FollowSymLinks
 *
 *   #
 *   # This rewrite rules are intended to copy the value of the REMOTE_USER
 *   # CGI environment variable into a header, as JBoss AS 7 doesn't currently
 *   # have a mechanism to access the remote user name:
 *   #
 *   RewriteEngine On
 *   RewriteCond %{REMOTE_USER} ^(.*)$
 *   RewriteRule ^(.*)$ - [E=REMOTE_USER:%1]
 *   RequestHeader set X-Remote-User %{REMOTE_USER}e
 * &lt;/Location&gt;
 * </pre>
 *
 * Once the web server is configured this authenticator can be included in an authentication profile with a
 * configuraion file similar to this one:
 *
 * <pre>
 * name=myprofile
 * module=org.ovirt.engine.core.aaa
 * authenticator.type=header
 * authenticator.header=X-Remote-User
 * directory.type=nop
 * </pre>
 */
public class HeaderAuthenticator extends Authenticator {
    private static final Logger log = LoggerFactory.getLogger(HeaderAuthenticator.class);

    /**
     * The name of the header that contains the name of the user already authenticated by the web server.
     */
    private String header;

    /**
     * {@inheritDoc}
     */
    @Override
    public NegotiationResult negotiate(HttpServletRequest req, HttpServletResponse rsp) {
        // Get the value of the header, if it isn't available send a warning explaining that the web server may not be
        // correctly configured:
        String value = req.getHeader(header);
        if (isEmpty(value)) {
            log.warn(
                "Can't authenticate the user because the header \"{}\" doesn't contain the name of the user, check" +
                "the configuration of the web server.",
                header
            );
            return new NegotiationResult(false, null);
        }

        // We are good, the user has already been authenticated by the web server:
        return new NegotiationResult(true, value);
    }

    @Override
    public void init() {
        context.put(ExtensionProperties.AUTHOR, "The oVirt Project");
        context.put(ExtensionProperties.EXTENSION_NAME, "Header authentication (Built-in)");
        context.put(ExtensionProperties.LICENSE, "ASL 2.0");
        context.put(ExtensionProperties.HOME, "http://www.ovirt.org");
        context.put(ExtensionProperties.VERSION, "N/A");
        context.put(ExtensionProperties.AAA_AUTHENTICATION_CAPABILITIES, AAA_AUTH_CAP_FLAGS_NEGOTIATING);
    }
}
