package org.ovirt.engine.core.bll.storage.disk.image;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.storage.ImageTicketInformation;
import org.ovirt.engine.core.common.businessentities.storage.TransferType;
import org.ovirt.engine.core.compat.Guid;
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
            if (ticketJson.has("uuid")) {
                ticketInfo.setId(Guid.createGuidFromString(ticketJson.get("uuid").asText()));
            }
            if (ticketJson.has("size")) {
                ticketInfo.setSize(ticketJson.get("size").asLong());
            }
            if (ticketJson.has("url")) {
                ticketInfo.setUrl(ticketJson.get("url").asText());
            }
            if (ticketJson.has("timeout")) {
                ticketInfo.setTimeout(ticketJson.get("timeout").asInt());
            }
            if (ticketJson.has("ops")) {
                ticketInfo.setTransferTypes(jsonToCollection(ticketJson.get("ops")));
            }
            if (ticketJson.has("filename")) {
                ticketInfo.setFileName(ticketJson.get("filename").asText());
            }
            if (ticketJson.has("active")) {
                ticketInfo.setActive(ticketJson.get("active").asBoolean());
            }
            if (ticketJson.has("transferred")) {
                ticketInfo.setTransferred(ticketJson.get("transferred").asLong());
            }
            if (ticketJson.has("idle_time")) {
                ticketInfo.setIdleTime(ticketJson.get("idle_time").asInt());
            }
            if (ticketJson.has("expires")) {
                ticketInfo.setExpires(ticketJson.get("expires").asInt());
            }
        } catch (IOException e) {
            log.error("Exception parsing ImageTicketInformation json '{}': {}", json, e.getMessage());
        }

        return ticketInfo;
    }

    private static Collection<TransferType> jsonToCollection(JsonNode arrNode) {
        List<TransferType> transferTypes = new ArrayList<>();
        for (final JsonNode objNode : arrNode) {
            transferTypes.add(TransferType.getTransferType(objNode.asText()));
        }
        return transferTypes;
    }
}
