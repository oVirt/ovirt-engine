package org.ovirt.engine.api.common.security.auth;

import java.util.List;
import java.util.UUID;

import javax.security.jacc.PolicyContextException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.HttpHeaders;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*
 * This class contains useful session utils
 */
public class SessionUtils {
    public static String ENGINE_SESSION_ID_KEY = "engineSessionId";
    public static String PREFER_HEADER_FIELD = "Prefer";
    public static String PERSIST_FIELD_VALUE = "persistent-auth";
    public static String JSESSIONID_HEADER = "JSESSIONID";
    private static final Log log = LogFactory.getLog(SessionUtils.class);

    /*
     * This method returns the header field "key" from the http headers
     */
    public static List<String> getHeaderField(HttpHeaders headers, String key) {
        List<String> returnValue = null;
        if (headers != null) {
            returnValue = headers.getRequestHeader(key);
        }
        return returnValue;
    }

    /*
     * This method returns the current http servlet request
     */
    public static HttpServletRequest getCurrentHttpServletRequest() {
        try {
            return (HttpServletRequest) javax.security.jacc.PolicyContext.getContext("javax.servlet.http.HttpServletRequest");
        } catch (PolicyContextException e) {
            log.error("Failed in getting current session. ", e);
            return null;
        }
    }

    /*
     * This method returns the current http session
     */
    public static HttpSession getCurrentSession(boolean create) {
        HttpServletRequest request = getCurrentHttpServletRequest();
        HttpSession retVal = null;

        if (request != null) {
            retVal = request.getSession(create);
        }
        return retVal;
    }

    /*
     * This method returns the engine session ID from the http session
     */
    public static String getEngineSessionId(HttpSession session) {
        return (String) session.getAttribute(ENGINE_SESSION_ID_KEY);
    }

    /*
     * This method sets the engine session ID on the http session
     */
    public static void setEngineSessionId(HttpSession session, String sessionId) {
        if (session != null) {
            log.debug("setting engine session ID to " + sessionId);
            session.setAttribute(ENGINE_SESSION_ID_KEY, sessionId);
        }
    }

    /*
     * This method generates a random engine session ID.
     */
    public static String generateEngineSessionId() {
        return UUID.randomUUID().toString();
    }
}
