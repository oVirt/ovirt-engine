package org.ovirt.engine.ui.common.uicommon;

import org.ovirt.engine.ui.frontend.Frontend;


public class AbstractRdp {
    private String address;
    private String guestID;

    private Boolean fullScreen = true;
    private String fullScreenTitle;
    private Integer width = 640;
    private Integer height = 480;
    private Integer authenticationLevel = 2;
    private Boolean enableCredSspSupport = false; // Disable 'Credential Security Support Provider (CredSSP)' to enable
                                                  // SSO.
    private boolean useLocalDrives;
    private Boolean redirectPrinters = false;
    private Boolean redirectClipboard = true;
    private Boolean redirectSmartCards = false;

    public String getVdcUserNameAndDomain() {
        String username = Frontend.getLoggedInUser().getUserName();
        String domain = Frontend.getLoggedInUser().getDomainControler();

        return username.contains("@") ? username : username + "@" + domain;//$NON-NLS-1$ //$NON-NLS-2$
    }

    public String getVdcUserName() {
        return Frontend.getLoggedInUser().getUserName();
    }

    public String getVdcUserPassword() {
        String password = Frontend.getLoggedInUser().getPassword();
        return password == null ? "" : password;
    }

    public String getVdcUserDomainController() {
        return Frontend.getLoggedInUser().getDomainControler();
    }

    public Boolean getFullScreen() {
        return fullScreen;
    }

    public void setFullScreen(Boolean fullScreen) {
        this.fullScreen = fullScreen;
    }

    public String getFullScreenTitle() {
        return fullScreenTitle;
    }

    public void setFullScreenTitle(String fullScreenTitle) {
        this.fullScreenTitle = fullScreenTitle;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer getAuthenticationLevel() {
        return authenticationLevel;
    }

    public void setAuthenticationLevel(Integer authenticationLevel) {
        this.authenticationLevel = authenticationLevel;
    }

    public Boolean getEnableCredSspSupport() {
        return enableCredSspSupport;
    }

    public void setEnableCredSspSupport(Boolean enableCredSspSupport) {
        this.enableCredSspSupport = enableCredSspSupport;
    }

    public Boolean getRedirectPrinters() {
        return redirectPrinters;
    }

    public void setRedirectPrinters(Boolean redirectPrinters) {
        this.redirectPrinters = redirectPrinters;
    }

    public Boolean getRedirectClipboard() {
        return redirectClipboard;
    }

    public void setRedirectClipboard(Boolean redirectClipboard) {
        this.redirectClipboard = redirectClipboard;
    }

    public Boolean getRedirectSmartCards() {
        return redirectSmartCards;
    }

    public void setRedirectSmartCards(Boolean redirectSmartCards) {
        this.redirectSmartCards = redirectSmartCards;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String value) {
        address = value;
    }

    public String getGuestID() {
        return guestID;
    }

    public void setGuestID(String value) {
        guestID = value;
    }

    public boolean getUseLocalDrives() {
        return useLocalDrives;
    }

    public void setUseLocalDrives(boolean value) {
        useLocalDrives = value;
    }
}
