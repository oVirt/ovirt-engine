package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.ovirt.engine.core.common.businessentities.storage.ImageTicketInformation;
import org.ovirt.engine.core.common.businessentities.storage.TransferType;
import org.ovirt.engine.core.common.vdscommands.GetImageTicketVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;

public class GetImageTicketVDSCommand<P extends GetImageTicketVDSCommandParameters> extends VdsBrokerCommand<P> {

    private static final String UUID = "uuid";

    private static final String SIZE = "size";

    private static final String URL = "url";

    private static final String TIMEOUT = "timeout";

    private static final String OPS = "ops";

    private static final String FILENAME = "filename";

    private static final String ACTIVE = "active";

    private static final String TRANSFERRED = "transferred";

    private static final String IDLE_TIME = "idle_time";

    private ImageTicketInformationReturn imageTicketInformationReturn;

    public GetImageTicketVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        imageTicketInformationReturn = getBroker().getImageTicket(getParameters().getTicketId().toString());

        proceedProxyReturnValue();
        setReturnValue(parseImageTicketInformationReturn());
    }

    @Override
    protected Status getReturnStatus() {
        return imageTicketInformationReturn.getStatus();
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return imageTicketInformationReturn;
    }

    private ImageTicketInformation parseImageTicketInformationReturn() {
        ImageTicketInformation ticketInfo = new ImageTicketInformation();
        Map<String, Object> ticketInfoMap = imageTicketInformationReturn.getImageTicketInformation();

        if (ticketInfoMap.containsKey(UUID)) {
            ticketInfo.setId(Guid.createGuidFromString((String) ticketInfoMap.get(UUID)));
        }
        if (ticketInfoMap.containsKey(SIZE)) {
            ticketInfo.setSize(((Number) ticketInfoMap.get(SIZE)).longValue());
        }
        if (ticketInfoMap.containsKey(URL)) {
            ticketInfo.setUrl((String) ticketInfoMap.get(URL));
        }
        if (ticketInfoMap.containsKey(TIMEOUT)) {
            ticketInfo.setTimeout((Integer) ticketInfoMap.get(TIMEOUT));
        }
        if (ticketInfoMap.containsKey(OPS)) {
            List<TransferType> transferTypes = Stream.of((Object[]) ticketInfoMap.get(OPS))
                    .map(String.class::cast)
                    .map(TransferType::getTransferType)
                    .collect(Collectors.toList());
            ticketInfo.setTransferTypes(transferTypes);
        }
        if (ticketInfoMap.containsKey(FILENAME)) {
            ticketInfo.setFileName((String) ticketInfoMap.get(FILENAME));
        }
        if (ticketInfoMap.containsKey(ACTIVE)) {
            ticketInfo.setActive((Boolean) ticketInfoMap.get(ACTIVE));
        }
        if (ticketInfoMap.containsKey(TRANSFERRED)) {
            ticketInfo.setTransferred(((Number) ticketInfoMap.get(TRANSFERRED)).longValue());
        }
        if (ticketInfoMap.containsKey(IDLE_TIME)) {
            ticketInfo.setIdleTime((Integer) ticketInfoMap.get(IDLE_TIME));
        }

        return ticketInfo;
    }
}
