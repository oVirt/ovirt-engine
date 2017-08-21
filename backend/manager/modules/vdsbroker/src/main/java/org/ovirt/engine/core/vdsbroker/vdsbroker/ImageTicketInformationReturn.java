package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturn;

public class ImageTicketInformationReturn extends StatusReturn {

    private static final String RESULT = "result";

    private Map<String, Object> ticketInfo;

    @SuppressWarnings("unchecked")
    public ImageTicketInformationReturn(Map<String, Object> innerMap) {
        super(innerMap);
        ticketInfo = (Map<String, Object>) innerMap.get(RESULT);
    }

    public Map<String, Object> getImageTicketInformation() {
        return ticketInfo;
    }
}
