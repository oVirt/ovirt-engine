package org.ovirt.engine.core.sso.servlets;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ovirt.engine.core.sso.api.SsoConstants;
import org.ovirt.engine.core.sso.service.SsoService;

public class StatusServlet extends HttpServlet {
    private static final long serialVersionUID = -5178735022948234147L;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "active");
        data.put("status_description", "SSO Webapp Deployed");
        data.put("version", SsoConstants.OVIRT_SSO_VERSION);
        SsoService.sendJsonData(response, data);
    }
}
