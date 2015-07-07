/**
 *
 */
package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 *
 */
public class GSSAPICallbackHandler implements CallbackHandler {

    /**
     * @param password
     * @param userDn
     *
     */

    private String userName;
    private String password;

    public GSSAPICallbackHandler(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * javax.security.auth.callback.CallbackHandler#handle(javax.security.auth
     * .callback.Callback[])
     */
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (int i = 0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof NameCallback) {
                NameCallback cb = (NameCallback) callbacks[i];
                cb.setName(userName);

            } else if (callbacks[i] instanceof PasswordCallback) {
                PasswordCallback cb = (PasswordCallback) callbacks[i];

                char[] passwd = new char[password.length()];
                password.getChars(0, passwd.length, passwd, 0);

                cb.setPassword(passwd);
            } else {
                throw new UnsupportedCallbackException(callbacks[i]);
            }
        }
    }

}
