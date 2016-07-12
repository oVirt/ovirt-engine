package org.ovirt.engine.ui.common.uicommon;

import org.ovirt.engine.ui.frontend.Frontend;


public abstract class AbstractRdp extends AbstractConsole {
    private String address;
    private String guestID;

    private Boolean fullScreen = true;
    private String fullScreenTitle;
    private Integer width = 640;
    private Integer height = 480;
    private Integer authenticationLevel = 2;
    private boolean useLocalDrives;
    private Boolean redirectPrinters = false;
    private Boolean redirectClipboard = true;
    private Boolean redirectSmartCards = false;

    public String getUserNameAndDomain() {
        String username = Frontend.getInstance().getLoggedInUser().getLoginName();
        String domain = Frontend.getInstance().getLoggedInUser().getDomain();

        return username.contains("@") ? username : username + "@" + domain;//$NON-NLS-1$ //$NON-NLS-2$
    }

    public String getUserName() {
        return Frontend.getInstance().getLoggedInUser().getLoginName();
    }

    public String getUserPassword() {
        // TODO(vs) password is not accessible due to SSO
        return ""; //$NON-NLS-1$
    }

    public String getUserDomainController() {
        return Frontend.getInstance().getLoggedInUser().getDomain();
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
