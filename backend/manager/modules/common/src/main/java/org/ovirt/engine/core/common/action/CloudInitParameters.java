package org.ovirt.engine.core.common.action;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;

public class CloudInitParameters implements Serializable {
    private static final long serialVersionUID = -139795494679460456L;

    private String hostname;
    private String authorizedKeys;
    private Boolean regenerateKeys;

    private List<String> dnsServers;
    private List<String> dnsSearch;
    private Map<String, VdsNetworkInterface> interfaces;
    private List<String> startOnBoot;

    private String timeZone;
    private String rootPassword;

    private Map<String, Attachment> attachments;

    public static class Attachment implements Serializable {
        public static enum AttachmentType {
            BASE64,
            PLAINTEXT,
        }
        AttachmentType type;
        String content;

        public void setAttachmentType(AttachmentType type) {
            this.type = type;
        }
        public AttachmentType getAttachmentType() {
            return type;
        }

        public void setContent(String content) {
            this.content = content;
        }
        public String getContent() {
            return content;
        }
    }

    public CloudInitParameters() {
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }
    public String getHostname() {
        return hostname;
    }

    public void setAuthorizedKeys(String authorizedKeys) {
        this.authorizedKeys = authorizedKeys;
    }
    public String getAuthorizedKeys() {
        return authorizedKeys;
    }

    public void setRegenerateKeys(Boolean regenerateKeys) {
        this.regenerateKeys = regenerateKeys;
    }
    public Boolean getRegenerateKeys() {
        return regenerateKeys;
    }

    public void setDnsServers(List<String> dnsServers) {
        this.dnsServers = dnsServers;
    }
    public List<String> getDnsServers() {
        return dnsServers;
    }

    public void setDnsSearch(List<String> dnsSearch) {
        this.dnsSearch = dnsSearch;
    }
    public List<String> getDnsSearch() {
        return dnsSearch;
    }

    public void setInterfaces(Map<String, VdsNetworkInterface> interfaces) {
        this.interfaces = interfaces;
    }
    public Map<String, VdsNetworkInterface> getInterfaces() {
        return interfaces;
    }

    public void setStartOnBoot(List<String> startOnBoot) {
        this.startOnBoot = startOnBoot;
    }
    public List<String> getStartOnBoot() {
        return startOnBoot;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }
    public String getTimeZone() {
        return timeZone;
    }

    public void setRootPassword(String password) {
        this.rootPassword = password;
    }
    public String getRootPassword() {
        return rootPassword;
    }

    public void setAttachments(Map<String, Attachment> attachments) {
        this.attachments = attachments;
    }
    public Map<String, Attachment> getAttachments() {
        return attachments;
    }
}
