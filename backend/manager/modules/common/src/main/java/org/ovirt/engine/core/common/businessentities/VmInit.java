package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.List;

import org.ovirt.engine.core.compat.Guid;

public class VmInit implements Serializable, BusinessEntity<Guid> {
    private static final long serialVersionUID = 1416726976934454559L;
    private Guid id;
    private String hostname;
    private String domain;
    private String timeZone;
    private String authorizedKeys;
    private Boolean regenerateKeys;
    private String activeDirectoryOU;
    private String orgName;

    private String dnsServers;
    private String dnsSearch;
    private List<VmInitNetwork> networks;

    private String winKey;
    private String userName;
    private String rootPassword;
    private boolean passwordAlreadyStored;
    private String customScript;

    private String inputLocale;
    private String uiLanguage;
    private String systemLocale;
    private String userLocale;


    public VmInit() {
    }

    public void setCustomScript(String customScript) {
        this.customScript = customScript;
    }
    public String getCustomScript() {
        return customScript;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }
    public String getDomain() {
        return domain;
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

    public void setDnsServers(String dnsServers) {
        this.dnsServers = dnsServers;
    }

    public String getDnsServers() {
        return dnsServers;
    }

    public void setDnsSearch(String dnsSearch) {
        this.dnsSearch = dnsSearch;
    }

    public String getDnsSearch() {
        return dnsSearch;
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

    @Override
    public Guid getId() {
        return id;
    }

    @Override
    public void setId(Guid id) {
        this.id = id;
    }

    public List<VmInitNetwork> getNetworks() {
        return networks;
    }

    public void setNetworks(List<VmInitNetwork> networks) {
        this.networks = networks;
    }

    public String getWinKey() {
        return winKey;
    }

    public void setWinKey(String winKey) {
        this.winKey = winKey;
    }

    public boolean isPasswordAlreadyStored() {
        return passwordAlreadyStored;
    }

    public void setPasswordAlreadyStored(boolean passwordAlreadyStored) {
        this.passwordAlreadyStored = passwordAlreadyStored;
    }

    public String getInputLocale() {
        return inputLocale;
    }

    public void setInputLocale(String inputLocale) {
        this.inputLocale = inputLocale;
    }

    public String getUiLanguage() {
        return uiLanguage;
    }

    public void setUiLanguage(String uiLanguage) {
        this.uiLanguage = uiLanguage;
    }

    public String getSystemLocale() {
        return systemLocale;
    }

    public void setSystemLocale(String systemLocale) {
        this.systemLocale = systemLocale;
    }

    public String getUserLocale() {
        return userLocale;
    }

    public void setUserLocale(String userLocale) {
        this.userLocale = userLocale;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getActiveDirectoryOU() {
        return activeDirectoryOU;
    }

    public void setActiveDirectoryOU(String activeDirectoryOU) {
        this.activeDirectoryOU = activeDirectoryOU;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }
}
