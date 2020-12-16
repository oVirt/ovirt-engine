package org.ovirt.engine.core.sso.api;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum InteractiveAuth {
    B {
        @Override
        public String getName() {
            return "BasicEnforce";
        }

        @Override
        public String getAuthUrl(HttpServletRequest request, HttpServletResponse response) {
            log.debug("Redirecting to Basic Enforce Servlet");
            return request.getContextPath() + SsoConstants.INTERACTIVE_LOGIN_BASIC_ENFORCE_URI;
        }
    },
    b {
        @Override
        public String getName() {
            return "BasicAccept";
        }

        @Override
        public String getAuthUrl(HttpServletRequest request, HttpServletResponse response) {
            log.debug("Redirecting to Basic Auth Servlet");
            return request.getContextPath() + SsoConstants.INTERACTIVE_LOGIN_BASIC_URI;
        }
    },
    I {
        @Override
        public String getName() {
            return "Internal";
        }

        @Override
        public String getAuthUrl(HttpServletRequest request, HttpServletResponse response) {
            log.debug("Redirecting to Internal Auth Servlet");
            return request.getContextPath() + SsoConstants.INTERACTIVE_LOGIN_URI;
        }
    },
    N {
        @Override
        public String getName() {
            return "Negotiate";
        }

        @Override
        public String getAuthUrl(HttpServletRequest request, HttpServletResponse response) {
            log.debug("Redirecting to External Auth Servlet");
            return request.getContextPath() + SsoConstants.INTERACTIVE_LOGIN_NEGOTIATE_URI;
        }
    };

    private static Logger log = LoggerFactory.getLogger(InteractiveAuth.class);

    public abstract String getName();

    public abstract String getAuthUrl(HttpServletRequest request, HttpServletResponse response);
}
