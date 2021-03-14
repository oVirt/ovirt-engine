package org.ovirt.engine.core.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.GeneralSecurityException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.LoginOnBehalfParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.UserSshKey;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.GetEntitiesWithPermittedActionParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.UserProfileDao;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.utils.crypt.EngineEncryptionUtils;
import org.ovirt.engine.core.uutils.crypto.ticket.TicketDecoder;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;


public class VMConsoleProxyServlet extends HttpServlet {

    @Inject
    private BackendInternal backend;
    @Inject
    private ResourceManager resourceManager;
    @Inject
    private VdsStaticDao vdsStaticDao;
    @Inject
    private UserProfileDao userProfileDao;

    private static final String VM_CONSOLE_PROXY_EKU = "1.3.6.1.4.1.2312.13.1.2.1.1";

    private static final Logger log = LoggerFactory.getLogger(VMConsoleProxyServlet.class);

    // TODO: implement key filtering based on input parameters
    private List<Map<String, String>> availablePublicKeys(String keyFingerPrint, String keyType, String keyContent) {
        List<Map<String, String>> jsonUsers = new ArrayList<>();
        for (UserSshKey userSshKey : userProfileDao.getAllPublicSshKeys()) {
            for (String publicKey : StringUtils.split(userSshKey.getContent(), "\n")) {
                if (StringUtils.isNotBlank(publicKey)) {
                    Map<String, String> jsonUser = new HashMap<>();

                    jsonUser.put("entityid", userSshKey.getUserId());
                    jsonUser.put("entity", userSshKey.getLoginName());
                    jsonUser.put("key", publicKey.trim());

                    jsonUsers.add(jsonUser);
                }
            }
        }

        return jsonUsers;
    }

    private List<Map<String, String>> availableConsoles(String userIdAsString) {
        Guid userGuid = null;

        try {
            if (StringUtils.isNotEmpty(userIdAsString)) {
                userGuid = Guid.createGuidFromString(userIdAsString);
            }
        } catch (IllegalArgumentException e) {
            log.debug("Could not read User GUID");
        }

        if (userGuid != null) {
            ActionReturnValue loginResult = backend.runInternalAction(ActionType.LoginOnBehalf,
                    new LoginOnBehalfParameters(userGuid));
            if (!loginResult.getSucceeded()) {
                throw new RuntimeException("Unable to create session using LoginOnBehalf");
            }
            String engineSessionId = loginResult.getActionReturnValue();
            try {
                QueryReturnValue retVms = backend.runInternalQuery(QueryType.GetAllVmsForUserAndActionGroup,
                        new GetEntitiesWithPermittedActionParameters(ActionGroup.CONNECT_TO_SERIAL_CONSOLE),
                        new EngineContext().withSessionId(engineSessionId));
                if (retVms != null) {
                    List<VmDynamic> vms = retVms.getReturnValue();
                    List<Guid> vdsIds = getRunOnVdsList(vms);
                    Map<Guid, String> vdsIdToHostname = getVdsIdToHostname(vdsIds);
                    Function<VmDynamic, SimpleEntry<VmDynamic, String>> vmToVmAndHostname = vm -> {
                        String hostname = vdsIdToHostname.get(vm.getRunOnVds());
                        return hostname != null ? new SimpleEntry<>(vm, hostname) : null;
                    };
                    return vms.stream()
                            .map(vmToVmAndHostname)
                            .filter(Objects::nonNull)
                            .map(this::toJsonVm)
                            .collect(Collectors.toList());
                }
            } finally {
                backend.runInternalAction(ActionType.LogoutSession, new ActionParametersBase(engineSessionId));
            }
        }

        return Collections.emptyList();
    }

    private List<Guid> getRunOnVdsList(List<VmDynamic> vms) {
        return vms.stream()
                .map(VmDynamic::getRunOnVds)
                .distinct()
                .collect(Collectors.toList());
    }

    private Map<Guid, String> getVdsIdToHostname(List<Guid> vdsIds) {
        return vdsStaticDao.getByIds(vdsIds).stream()
                .collect(Collectors.toMap(VdsStatic::getId, VdsStatic::getHostName));
    }

    private Map<String, String> toJsonVm(Map.Entry<VmDynamic, String> vmToHostname) {
        Guid vmId = vmToHostname.getKey().getId();
        Map<String, String> jsonVm = new HashMap<>();
        jsonVm.put("vmid", vmId.toString());
        jsonVm.put("vmname", resourceManager.getVmManager(vmId).getName());
        jsonVm.put("host", vmToHostname.getValue());
        /* there is only one serial console, no need and no way to distinguish them */
        jsonVm.put("console", "default");
        return jsonVm;
    }

    // Caller must ensure to close the #body to avoid resource leaking.
    // Recommended way is to use this helper inside a try-with-resources block.
    private String readBody(BufferedReader body) throws IOException {
        StringBuilder buffer = new StringBuilder();

        int r;
        while ((r = body.read()) != -1) {
            buffer.append((char) r);
        }

        return buffer.toString();
    }

    private String validateTicket(String ticket) throws GeneralSecurityException, IOException {
        TicketDecoder ticketDecoder = new TicketDecoder(EngineEncryptionUtils.getTrustStore(),
                                                        VM_CONSOLE_PROXY_EKU,
                                                        Config.<Integer> getValue(ConfigValues.VMConsoleTicketTolerance));
        return ticketDecoder.decode(ticket);
    }

    private Map<String, Object> buildResult(String content_type, String content_id, Object content) {
        Map<String, Object> result = new HashMap<>();
        result.put("version", "1");
        result.put("content", content_type);
        result.put(content_id, content);
        return result;
    }

    private Map<String, Object> produceContentFromParameters(Map<String, String> parameters) {
        String command = parameters.get("command");
        String version = parameters.get("version");

        Map<String, Object> result = null;

        if ("1".equals(version)) {
            if ("available_consoles".equals(command)) {
                String userId = parameters.get("user_id");

                result = buildResult("console_list",
                                     "consoles",
                                     availableConsoles(userId));
            } else if ("public_keys".equals(command)) {
                String keyFingerPrint = parameters.get("key_fp");
                String keyType = parameters.get("key_type");
                String keyContent = parameters.get("key_content");

                result = buildResult("key_list",
                                     "keys",
                                     availablePublicKeys(
                                        (keyFingerPrint != null) ? keyFingerPrint : "",
                                        (keyType != null) ? keyType : "",
                                        (keyContent != null) ? keyContent : ""));
            } else {
                log.error("Unknown command: ", command);
            }
        } else {
            log.error("Unsupported version: ", version);
        }

        return result;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            String stringParameters = validateTicket(readBody(request.getReader()));

            ObjectMapper mapper = new ObjectMapper();

            Map<String, String> parameters = mapper.readValue(
                    stringParameters,
                    TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, String.class)
            );

            Map<String, Object> result = produceContentFromParameters(parameters);

            if (result != null) {
                response.setContentType("application/json");
                mapper.writeValue(response.getOutputStream(), result);
            } else {
                response.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
            }
        } catch (GeneralSecurityException e) {
            log.error("Error validating ticket: ", e);
            response.setStatus(HttpURLConnection.HTTP_FORBIDDEN);
        } catch (IOException e) {
            log.error("Error decoding ticket: ", e);
            response.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
        } catch (Exception e) {
            log.error("Error processing request: ", e);
            response.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
    }
}
