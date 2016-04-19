package org.ovirt.engine.core.services;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.aaa.SsoOAuthServiceUtils;
import org.ovirt.engine.core.utils.serialization.json.JsonObjectSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SsoLogoutServlet extends HttpServlet {
    private static final long serialVersionUID = 9210030009170727847L;

    private static final Logger log = LoggerFactory.getLogger(SsoLogoutServlet.class);

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        log.debug("Entered SsoLogoutServlet");
        String token = request.getParameter("token");
        String scope = request.getParameter("scope");

        Map<String, Object> revokeResponse =  SsoOAuthServiceUtils.revoke(token, scope);
        String error = (String) revokeResponse.get("error");
        if (StringUtils.isNotEmpty(error)) {
            log.error("Unable to logout user: {}", error);
        }

        try (OutputStream os = response.getOutputStream()) {
            String jsonPayload = new JsonObjectSerializer().serialize(revokeResponse);
            response.setContentType("application/json");
            byte[] jsonPayloadBytes = jsonPayload.getBytes(StandardCharsets.UTF_8.name());
            response.setContentLength(jsonPayloadBytes.length);
            os.write(jsonPayloadBytes);
            log.trace("Sending json data {}", jsonPayload);
        }

        log.debug("Exiting SsoLogoutServlet");
    }

}
