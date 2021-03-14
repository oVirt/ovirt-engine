package org.ovirt.engine.core.common.businessentities.gluster;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.ovirt.engine.core.common.utils.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GlusterEvent implements Serializable {

    private static final long serialVersionUID = 4155566886829884858L;

    private String event;

    @JsonProperty("ts")
    private Date timestamp;

    @JsonProperty("nodeid")
    private String nodeId;

    @JsonProperty("message")
    private Map<String, Object> message = new HashMap<>();

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public Map<String, Object> getMessage() {
        return message;
    }

    public void setMessage(Map<String, Object> message) {
        this.message = message;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof GlusterEvent)) {
            return false;
        }
        GlusterEvent other = (GlusterEvent) obj;
        return Objects.equals(event, other.event)
                && Objects.equals(timestamp, other.timestamp)
                && Objects.equals(nodeId, other.nodeId)
                && Objects.equals(message, other.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                event,
                timestamp,
                nodeId,
                message);
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("Gluster event", event)
                .append("message", message)
                .append("timestamp", timestamp)
                .build();
    }




}
