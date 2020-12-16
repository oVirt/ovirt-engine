package org.ovirt.engine.core.sso.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ovirt.engine.core.sso.api.SsoConstants;
import org.ovirt.engine.core.sso.service.OpenIdService;
import org.ovirt.engine.core.sso.service.SsoService;

public class OpenIdJwksServlet extends HttpServlet {

    @Inject
    private Instance<OpenIdService> openIdService;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            SsoService.sendJsonData(response, openIdService.get().getJson(buildResponse()));
        } catch (Exception ex) {
            SsoService.sendJsonDataWithMessage(request, response, SsoConstants.ERR_CODE_SERVER_ERROR, ex);
        }
    }

    private Map<String, Object> buildResponse() {
        Map<String, Object> payload = new HashMap<>();
        List<Object> keys = new ArrayList<>();
        keys.add(openIdService.get().getJWK());
        payload.put("keys", keys);
        return payload;
    }
}
