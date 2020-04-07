package org.ovirt.engine.core.bll.storage.disk.image;

import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.ovirt.engine.core.common.businessentities.storage.ImageTicketInformation;
import org.ovirt.engine.core.common.businessentities.storage.TransferType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.JsonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ImageTicketInformationHelper {
    private static final Logger log = LoggerFactory.getLogger(ImageTicketInformationHelper.class);

    public static ImageTicketInformation fromJson(String json) {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode ticketJson;
        ImageTicketInformation ticketInfo = new ImageTicketInformation();

        try {
            ticketJson = objectMapper.readTree(json);
            if (ticketJson == null) {
                throw new RuntimeException();
            }

            JsonHelper.invokeIfExistsLong(ticketJson, "size", ticketInfo::setSize);
            JsonHelper.invokeIfExistsString(ticketJson, "url", ticketInfo::setUrl);
            JsonHelper.invokeIfExistsInt(ticketJson, "timeout", ticketInfo::setTimeout);
            JsonHelper.invokeIfExistsString(ticketJson, "filename", ticketInfo::setFileName);
            JsonHelper.invokeIfExistsBoolean(ticketJson, "active", ticketInfo::setActive);
            JsonHelper.invokeIfExistsLong(ticketJson, "transferred", ticketInfo::setTransferred);
            JsonHelper.invokeIfExistsInt(ticketJson, "idle_time", ticketInfo::setIdleTime);
            JsonHelper.invokeIfExistsInt(ticketJson, "expires", ticketInfo::setExpires);
            JsonHelper.invokeIfExistsStringTransformed(ticketJson, "uuid", ticketInfo::setId, Guid::createGuidFromString);

            if (ticketJson.has("ops")) {
                ticketInfo.setTransferTypes(jsonToCollection(ticketJson.get("ops")));
            }

        } catch (IOException e) {
            log.error("Exception parsing ImageTicketInformation json '{}': {}", json, e.getMessage());
        }

        return ticketInfo;
    }

    private static Collection<TransferType> jsonToCollection(JsonNode arrNode) {
        return StreamSupport.stream(arrNode.spliterator(), false)
                .map(n -> TransferType.getTransferType(n.asText()))
                .collect(Collectors.toList());
    }
}
