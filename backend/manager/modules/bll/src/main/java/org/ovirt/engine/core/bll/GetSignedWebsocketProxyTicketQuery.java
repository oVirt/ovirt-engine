package org.ovirt.engine.core.bll;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.GraphicsInfo;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.queries.GetSignedWebsocketProxyTicketParams;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.SignStringParameters;
import org.ovirt.engine.core.dao.VdsDynamicDao;
import org.ovirt.engine.core.dao.VmDao;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * It returns a signed ticket that allows to connect to websocket proxy (ovirt-websocket-proxy.py)
 *
 * @see SignStringQuery
 * @see org.ovirt.engine.ui.common.uicommon.WebClientConsoleInvoker
 */
public class GetSignedWebsocketProxyTicketQuery<P extends GetSignedWebsocketProxyTicketParams> extends QueriesCommandBase<P> {

    @Inject
    private VmDao vmDao;

    @Inject
    private VdsDynamicDao vdsDynamicDao;

    public GetSignedWebsocketProxyTicketQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override protected void executeQueryCommand() {
        try {
            executeQueryCommandChecked();
        } catch (IOException ex) {
            throw new EngineException(EngineError.FailedToCreateWebsocketProxyTicket, new RuntimeException(ex));
        }
    }

    private void executeQueryCommandChecked() throws IOException {
        final VM vm = vmDao.get(getParameters().getVmId(), getUserID(), getParameters().isFiltered());
        if (vm == null) {
            throw new EngineException(EngineError.VMCantBeObtained,
                    String.format("vmid=%s", getParameters().getVmId()));
        }
        final GraphicsInfo graphicsInfo = getGraphicsInfo(vm);
        final Map<String, Object> ticketModel = createTicket(vm, graphicsInfo);
        final String jsonTicket = new ObjectMapper().writeValueAsString(ticketModel);
        final String encodedTicket = URLEncoder.encode(jsonTicket, StandardCharsets.UTF_8.name());
        final String signedTicket = backend
                .runInternalQuery(QueryType.SignString, new SignStringParameters(encodedTicket))
                .getReturnValue();
        setReturnValue(signedTicket);
    }

    private GraphicsInfo getGraphicsInfo(VM vm) {
        final GraphicsInfo graphicsInfo = vm.getGraphicsInfos().get(getParameters().getGraphicsType());
        if (graphicsInfo == null) {
            throw new EngineException(EngineError.GraphicsConsoleCantBeObtained,
                    String.format(
                            "vmid=%s console=%s", getParameters().getVmId(), getParameters().getGraphicsType()));
        }
        return graphicsInfo;
    }

    private Map<String, Object> createTicket(VM vm, GraphicsInfo graphicsInfo) {
        Map<String, Object> jsonModel = new HashMap<>();
        jsonModel.put("host", graphicsInfo.getIp());
        Integer tlsPort = graphicsInfo.getTlsPort();
        final GraphicsType type = getParameters().getGraphicsType();
        boolean useSsl = false;
        if (type == GraphicsType.SPICE) {
            useSsl = tlsPort != null ? tlsPort != -1 : false;
        } else {
            final VdsDynamic host = vdsDynamicDao.get(vm.getRunOnVds());
            useSsl = host != null && host.isVncEncryptionEnabled();
            tlsPort = graphicsInfo.getPort();
        }
        int port = useSsl ? tlsPort : graphicsInfo.getPort();
        jsonModel.put("port", String.valueOf(port));
        jsonModel.put("ssl_target", useSsl);
        return jsonModel;
    }
}
