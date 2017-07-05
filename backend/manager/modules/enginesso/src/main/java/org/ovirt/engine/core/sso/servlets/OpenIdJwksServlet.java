package org.ovirt.engine.core.sso.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ovirt.engine.core.sso.utils.SsoConstants;
import org.ovirt.engine.core.sso.utils.SsoUtils;

public class OpenIdJwksServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            SsoUtils.sendJsonData(response, OpenIdUtils.getJson(buildResponse()));
        } catch(Exception ex) {
            SsoUtils.sendJsonDataWithMessage(request, response, SsoConstants.ERR_CODE_SERVER_ERROR, ex);
        }
    }

    private Map<String, Object> buildResponse() {
        Map<String, Object> payload = new HashMap<>();
        List<Object> keys = new ArrayList<>();
        keys.add(OpenIdUtils.getJWK().toJSONObject());
        payload.put("keys", keys);
        return payload;
    }
}
