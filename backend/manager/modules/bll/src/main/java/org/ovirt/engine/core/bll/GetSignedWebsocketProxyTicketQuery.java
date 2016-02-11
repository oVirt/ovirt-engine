package org.ovirt.engine.core.bll;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.codehaus.jackson.map.ObjectMapper;
import org.ovirt.engine.core.common.businessentities.GraphicsInfo;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.queries.GetSignedWebsocketProxyTicketParams;
import org.ovirt.engine.core.common.queries.SignStringParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.dao.VmDao;

/**
 * It returns a signed ticket that allows to connect to websocket proxy (ovirt-websocket-proxy.py)
 *
 * @see SignStringQuery
 * @see org.ovirt.engine.ui.common.uicommon.WebClientConsoleInvoker
 */
public class GetSignedWebsocketProxyTicketQuery<P extends GetSignedWebsocketProxyTicketParams> extends QueriesCommandBase<P> {

    @Inject
    private VmDao vmDao;

    public GetSignedWebsocketProxyTicketQuery(P parameters) {
        super(parameters);
    }

    @Override protected void executeQueryCommand() {
        try {
            executeQueryCommandChecked();
        } catch (IOException ex) {
            throw new EngineException(EngineError.FailedToCreateWebsocketProxyTicket, new RuntimeException(ex));
        }
    }

    private void executeQueryCommandChecked() throws IOException {
        final GraphicsInfo graphicsInfo = getGraphicsInfo();
        final Map<String, Object> ticketModel = createTicket(graphicsInfo);
        final String jsonTicket = new ObjectMapper().writeValueAsString(ticketModel);
        final String encodedTicket = URLEncoder.encode(jsonTicket, StandardCharsets.UTF_8.name());
        final String signedTicket = Backend.getInstance()
                .runInternalQuery(VdcQueryType.SignString, new SignStringParameters(encodedTicket))
                .getReturnValue();
        setReturnValue(signedTicket);
    }

    private GraphicsInfo getGraphicsInfo() {
        final VM vm = vmDao.get(getParameters().getVmId(), getUserID(), getParameters().isFiltered());
        if (vm == null) {
            throw new EngineException(EngineError.VMCantBeObtained,
                    String.format("vmid=%s", getParameters().getVmId()));
        }
        final GraphicsInfo graphicsInfo = vm.getGraphicsInfos().get(getParameters().getGraphicsType());
        if (graphicsInfo == null) {
            throw new EngineException(EngineError.GraphicsConsoleCantBeObtained,
                    String.format(
                            "vmid=%s console=%s", getParameters().getVmId(), getParameters().getGraphicsType()));
        }
        return graphicsInfo;
    }

    private Map<String, Object> createTicket(GraphicsInfo graphicsInfo) {
        Map<String, Object> jsonModel = new HashMap<>();
        jsonModel.put("host", graphicsInfo.getIp());
        Integer tlsPort = graphicsInfo.getTlsPort();
        boolean useSsl = tlsPort != null ? tlsPort != -1 : false;
        int port = useSsl ? tlsPort : graphicsInfo.getPort();
        jsonModel.put("port", String.valueOf(port));
        jsonModel.put("ssl_target", useSsl);
        return jsonModel;
    }
}
