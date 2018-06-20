/*
Copyright (c) 2018 Red Hat, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.ovirt.engine.core.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Path;

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
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.ansible.AnsibleCommandBuilder;
import org.ovirt.engine.core.common.utils.ansible.AnsibleExecutor;
import org.ovirt.engine.core.common.utils.ansible.AnsibleReturnCode;
import org.ovirt.engine.core.common.utils.ansible.AnsibleReturnValue;
import org.ovirt.engine.core.utils.EngineLocalConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

            Path variablesFile = null;
            try {
                EngineLocalConfig config = EngineLocalConfig.getInstance();
                variablesFile = createVariablesFile(request);
                AnsibleCommandBuilder command = new AnsibleCommandBuilder()
                    .variables(
                        new Pair<>(
                            "engine_url",
                            String.format("https://%s:%s/ovirt-engine/api", config.getHost(), config.getHttpsPort())
                        ),
                        new Pair<>("engine_token", token),
                        new Pair<>("engine_insecure", "true") // TODO: use CA
                    )
                    .variableFilePath(variablesFile.toString())
                    .playbook(request.getParameter("playbook") + ".yml");

                // Return from servlet:
                asyncContext.complete();

                // Execute the ansible-playbook command:
                AnsibleReturnValue ansibleReturnValue = ansibleExecutor.runCommand(command);

                // Wait until ansible-playbook command finish:
                if (ansibleReturnValue.getAnsibleReturnCode() != AnsibleReturnCode.OK) {
                    log.error("Error while executing ansible-playbook command.");
                }
            } catch (IOException | InterruptedException e) {
                log.error("Error while reading variables.", e);
                response.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
                asyncContext.complete();
            } finally {
                if (variablesFile != null) {
                    try {
                        Files.delete(variablesFile);
                    } catch (IOException ex) {
                        log.debug("Failed to delete temporary file '{}': {}", variablesFile, ex.getMessage());
                    }
                }
            }
        });
    }

    private Path createVariablesFile(HttpServletRequest request) throws IOException {
        BufferedReader body = request.getReader();
        StringBuilder buffer = new StringBuilder();

        int r;
        while ((r = body.read()) != -1) {
            buffer.append((char) r);
        }

        Path variablesFile = Files.createTempFile("ansible-variables", "");
        Files.write(variablesFile, buffer.toString().getBytes());

        return variablesFile;
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
