/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.core.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ovirt.engine.core.aaa.filters.FiltersHelper;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.queries.GetEngineSessionIdForSsoTokenQueryParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.utils.ansible.AnsibleCommandConfig;
import org.ovirt.engine.core.common.utils.ansible.AnsibleExecutor;
import org.ovirt.engine.core.common.utils.ansible.AnsibleReturnCode;
import org.ovirt.engine.core.common.utils.ansible.AnsibleReturnValue;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AnsibleServlet extends HttpServlet {

    private static final long serialVersionUID = -8894175367839134323L;
    private static final Logger log = LoggerFactory.getLogger(AnsibleServlet.class);

    private static final String BEARER = "Bearer";
    private static final String HEADER_AUTHORIZATION = "Authorization";

    @Inject
    private AnsibleExecutor ansibleExecutor;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        final AsyncContext asyncContext = request.startAsync();
        asyncContext.start(() -> {

            // Check if the user is administrator:
            String token = getTokenFromHeader(request);
            DbUser dbUser = getUserFromToken(token);
            if (!dbUser.isAdmin()) {
                log.error("Insufficient permissions for user '{}' to execute Ansible playbook.", dbUser.getLoginName());
                response.setStatus(HttpURLConnection.HTTP_FORBIDDEN);
                return;
            }

            // Build the engine_url, taking proxy configs into account
            EngineLocalConfig config = EngineLocalConfig.getInstance();
            String engineUrl = String.format(
                "https://%s:%s/ovirt-engine/api",
                config.getHost(),
                config.isProxyEnabled() ? config.getProxyHttpsPort() : config.getHttpsPort()
            );

            try {
                AnsibleCommandConfig commandConfig = new AnsibleCommandConfig()
                        .playbook(request.getParameter("playbook") + ".yml")
                        .variable("engine_url", engineUrl)
                        .variable("engine_token", token)
                        .variable("engine_insecure", "true"); // TODO: use CA

                JsonNode jsonRequest = getJsonFromRequest(request);
                Iterator<Entry<String, JsonNode>> jsonIterator = jsonRequest.fields();
                while (jsonIterator.hasNext()) {
                    Entry<String, JsonNode> entry = jsonIterator.next();
                    commandConfig.variable(entry.getKey(), entry.getValue());
                }

                // Return from servlet:
                asyncContext.complete();

                // Execute the ansible-playbook command:
                AnsibleReturnValue ansibleReturnValue;
                String timeout = request.getParameter("execution_timeout");
                if (timeout != null) {
                    ansibleReturnValue = ansibleExecutor.runCommand(commandConfig, Integer.parseInt(timeout));
                } else {
                    ansibleReturnValue = ansibleExecutor.runCommand(commandConfig);
                }

                // Wait until ansible-playbook command finish:
                if (ansibleReturnValue.getAnsibleReturnCode() != AnsibleReturnCode.OK) {
                    log.error("Error while executing ansible-playbook command.");
                }
            } catch (IOException e) {
                log.error("Error while reading variables.", e);
                response.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
                asyncContext.complete();
            }
        });
    }

    private JsonNode getJsonFromRequest(HttpServletRequest request) throws IOException {
        BufferedReader body = request.getReader();
        StringBuilder buffer = new StringBuilder();

        int r;
        while ((r = body.read()) != -1) {
            buffer.append((char) r);
        }
        return new ObjectMapper().readValue(buffer.toString(), JsonNode.class);
    }

    private String getTokenFromHeader(HttpServletRequest request) {
        String token = null;
        String headerValue = request.getHeader(HEADER_AUTHORIZATION);
        if (headerValue != null && headerValue.startsWith(BEARER)) {
            token = headerValue.substring(BEARER.length()).trim();
        }

        return token;
    }

    private DbUser getUserFromToken(String token) {
        DbUser dbUser = null;
        try {
            InitialContext ctx = null;
            try {
                ctx = new InitialContext();
                QueryReturnValue returnValue = FiltersHelper.getBackend(ctx).runPublicQuery(
                    QueryType.GetEngineSessionIdForSsoToken,
                    new GetEngineSessionIdForSsoTokenQueryParameters(token)
                );
                QueryParametersBase qpb = new QueryParametersBase();
                qpb.setSessionId(returnValue.getReturnValue());
                QueryReturnValue getUserSession = FiltersHelper.getBackend(ctx).runQuery(
                    QueryType.GetDbUserBySession, qpb
                );
                dbUser = getUserSession.getReturnValue();
            } finally {
                if (ctx != null) {
                    ctx.close();
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        return dbUser;
    }
}
