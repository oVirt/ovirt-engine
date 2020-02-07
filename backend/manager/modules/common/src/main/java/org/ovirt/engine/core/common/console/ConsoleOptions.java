package org.ovirt.engine.core.common.console;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;

public class ConsoleOptions implements Serializable{

    private Guid vmId;
    private GraphicsType graphicsType;

    // general
    /**
     * vv file: [virt-viewer] host
     */
    private String host;
    private Integer port;
    private String ticket;
    private String username;
    private String title;
    private String vmName;
    private boolean remapCtrlAltDelete;
    private String toggleFullscreenHotKey;
    private String releaseCursorHotKey;
    private String remoteViewerNewerVersionUrl;
    private String remoteViewerSupportedVersions;
    // spice
    private boolean fullScreen;
    private int numberOfMonitors;
    private int usbListenPort;
    private boolean usbAutoShare;
    protected String usbFilter;
    private boolean adminConsole;
    private int securePort;
    private String sslChanels;
    private String cipherSuite;
    private String hostSubject;
    private String trustStore;
    // if true, user provided his custom certificate for HTTPS communication
    // if false, engine's CA has been used so we can pass it to remote-viewer
    private boolean customHttpsCertificateUsed;
    private boolean noTaskMgrExecution;
    private WanColorDepth wanColorDepth;
    private boolean wanOptionsEnabled;
    private boolean smartcardEnabled = false;
    private String spiceProxy = null;
    // the user can choose to disable the smartcard even when it is enabled, but can not choose to enable it, when it is
    // disabled
    private boolean smartcardEnabledOverridden = false;
    // even the spice proxy is globally configured, user can choose to disable it for specific VM
    private boolean spiceProxyEnabled;
    private List<WanDisableEffects> wanDisableEffects;
    /**
     * engine_host:engine_port
     * vv file: [ovirt] host
     */
    private String ovirtHost;
    private String ssoToken;
    private boolean useSsl;
    // kubevirt
    private String path;
    private String token;

    public static final int TICKET_VALIDITY_SECONDS = 120;
    public static final int SPICE_USB_DEFAULT_PORT = 32023;
    public static final int SET_SPICE_DISABLE_USB_LISTEN_PORT = 0;
    public static final String SECURE_ATTENTION_MAPPING = "ctrl+alt+end";

    public ConsoleOptions() {
        setWanDisableEffects(new ArrayList<WanDisableEffects>());
        setWanColorDepth(WanColorDepth.depth16);
        setWanOptionsEnabled(false);
        setNoTaskMgrExecution(false);
    }

    public ConsoleOptions(GraphicsType graphicsType) {
        this();
        this.graphicsType = graphicsType;
    }

    public Guid getVmId() {
        return vmId;
    }

    public void setVmId(Guid vmId) {
        this.vmId = vmId;
    }

    public GraphicsType getGraphicsType() {
        return graphicsType;
    }

    public void setGraphicsType(GraphicsType graphicsType) {
        this.graphicsType = graphicsType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title + ":%d"; //$NON-NLS-1$
    }

    public String getVmName() {
        return vmName;
    }

    public void setVmName(String vmName) {
        this.vmName = vmName;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public String getTicket() {
        return ticket;
    }

    public String getUsername() {
        return username;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isRemapCtrlAltDelete() {
        return remapCtrlAltDelete;
    }

    public void setRemapCtrlAltDelete(boolean remapCtrlAltDelete) {
        this.remapCtrlAltDelete = remapCtrlAltDelete;
    }

    public String getToggleFullscreenHotKey() {
        return toggleFullscreenHotKey;
    }

    public void setToggleFullscreenHotKey(String toggleFullscreenHotKey) {
        this.toggleFullscreenHotKey = toggleFullscreenHotKey;
    }

    public String getReleaseCursorHotKey() {
        return releaseCursorHotKey;
    }

    public void setReleaseCursorHotKey(String releaseCursorHotKey) {
        this.releaseCursorHotKey = releaseCursorHotKey;
    }

    public boolean isFullScreen() {
        return fullScreen;
    }

    public void setFullScreen(boolean fullScreen) {
        this.fullScreen = fullScreen;
    }

    public int getNumberOfMonitors() {
        return numberOfMonitors;
    }

    public void setNumberOfMonitors(int numberOfMonitors) {
        this.numberOfMonitors = numberOfMonitors;
    }

    public boolean isUsbAutoShare() {
        return usbAutoShare;
    }

    public void setUsbAutoShare(boolean usbAutoShare) {
        this.usbAutoShare = usbAutoShare;
    }

    public String getUsbFilter() {
        return usbFilter;
    }

    public void setUsbFilter(String usbFilter) {
        this.usbFilter = usbFilter;
    }

    public boolean isAdminConsole() {
        return adminConsole;
    }

    public void setAdminConsole(boolean adminConsole) {
        this.adminConsole = adminConsole;
    }

    public int getSecurePort() {
        return securePort & 0xffff;
    }

    public void setSecurePort(int securePort) {
        this.securePort = securePort;
    }

    public int getRawSecurePort() {
        return securePort;
    }

    public String getSslChanels() {
        return sslChanels;
    }

    public void setSslChanels(String sslChanels) {
        this.sslChanels = adjustLegacySecureChannels(sslChanels);
    }

    public String getCipherSuite() {
        return cipherSuite;
    }

    public void setCipherSuite(String cipherSuite) {
        this.cipherSuite = cipherSuite;
    }

    public String getHostSubject() {
        return hostSubject;
    }

    public void setHostSubject(String hostSubject) {
        this.hostSubject = hostSubject;
    }

    public String getTrustStore() {
        return trustStore;
    }

    public void setTrustStore(String trustStore) {
        this.trustStore = trustStore;
    }

    public boolean isCustomHttpsCertificateUsed() {
        return customHttpsCertificateUsed;
    }

    public void setCustomHttpsCertificateUsed(boolean customHttpsCertificateUsed) {
        this.customHttpsCertificateUsed = customHttpsCertificateUsed;
    }

    public boolean isNoTaskMgrExecution() {
        return noTaskMgrExecution;
    }

    public void setNoTaskMgrExecution(boolean noTaskMgrExecution) {
        this.noTaskMgrExecution = noTaskMgrExecution;
    }

    public WanColorDepth getWanColorDepth() {
        return wanColorDepth;
    }

    public int colorDepthAsInt() {
        if (getWanColorDepth() != null) {
            return getWanColorDepth().asInt();
        }

        return ConsoleOptions.WanColorDepth.depth16.asInt();
    }

    public void setWanColorDepth(WanColorDepth wanColorDepth) {
        this.wanColorDepth = wanColorDepth;
    }

    public boolean isWanOptionsEnabled() {
        return wanOptionsEnabled;
    }

    public void setWanOptionsEnabled(boolean wanOptionsEnabled) {
        this.wanOptionsEnabled = wanOptionsEnabled;
    }

    public boolean isSmartcardEnabled() {
        return smartcardEnabled;
    }

    public void setSmartcardEnabled(boolean smartcardEnabled) {
        this.smartcardEnabled = smartcardEnabled;
    }

    public boolean passSmartcardOption() {
        return isSmartcardEnabled() && !isSmartcardEnabledOverridden();
    }

    public String getSpiceProxy() {
        return spiceProxy;
    }

    public void setSpiceProxy(String spiceProxy) {
        this.spiceProxy = spiceProxy;
    }

    /**
     * Returns true if the user has choosen to disable the smartcard even it is by default enabled
     */
    public boolean isSmartcardEnabledOverridden() {
        return smartcardEnabledOverridden;
    }

    public void setSmartcardEnabledOverridden(boolean smartcardEnabledOverridden) {
        this.smartcardEnabledOverridden = smartcardEnabledOverridden;
    }

    public boolean isSpiceProxyEnabled() {
        return spiceProxyEnabled;
    }

    public void setSpiceProxyEnabled(boolean spiceProxyEnabled) {
        this.spiceProxyEnabled = spiceProxyEnabled;
    }

    public List<WanDisableEffects> getWanDisableEffects() {
        return wanDisableEffects;
    }

    public void setWanDisableEffects(List<WanDisableEffects> wanDisableEffects) {
        this.wanDisableEffects = wanDisableEffects;
    }

    public String getRemoteViewerNewerVersionUrl() {
        return remoteViewerNewerVersionUrl;
    }

    public void setRemoteViewerNewerVersionUrl(String remoteViewerNewerVersionUrl) {
        this.remoteViewerNewerVersionUrl = remoteViewerNewerVersionUrl;
    }

    public String getRemoteViewerSupportedVersions() {
        return remoteViewerSupportedVersions;
    }

    public void setRemoteViewerSupportedVersions(String remoteViewerSupportedVersions) {
        this.remoteViewerSupportedVersions = remoteViewerSupportedVersions;
    }

    public String getOvirtHost() {
        return ovirtHost;
    }

    public void setOvirtHost(String ovirtHost) {
        this.ovirtHost = ovirtHost;
    }

    public String getSsoToken() {
        return ssoToken;
    }

    public void setSsoToken(String ssoToken) {
        this.ssoToken = ssoToken;
    }

    public boolean isUseSsl() {
        return useSsl;
    }

    public void setUseSsl(boolean useSsl) {
        this.useSsl = useSsl;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public enum WanColorDepth {
        depth16(16),
        depth32(32);
        private final int depth;

        private WanColorDepth(int depth) {
            this.depth = depth;
        }
        public static WanColorDepth fromInt(int integerDepth) {
            for (WanColorDepth value : values()) {
                if (value.depth == integerDepth) {
                    return value;
                }
            }

            throw new IllegalArgumentException("Illegal int value: " + integerDepth);
        }
        public int asInt() {
            return depth;
        }
    }

    public enum WanDisableEffects {
        animation("animation"),
        wallpaper("wallpaper"),
        fontSmooth("font-smooth"),
        all("all");

        private final String stringValue;

        private WanDisableEffects(String stringValue) {
            this.stringValue = stringValue;
        }

        public static WanDisableEffects fromString(String str) {
            for (WanDisableEffects value : values()) {
                if (value.stringValue.equals(str)) {
                    return value;
                }
            }

            throw new IllegalArgumentException("Illegal string value: " + str);
        }

        public String asString() {
            return stringValue;
        }
    }

    /**
     * Reformat secure channels string if they are in legacy ('s'-prefixed) format.
     * @param legacySecureChannels (e.g. "smain,sinput")
     * @return secure channels in correct format (e.g. "main,input")
     */
    static String adjustLegacySecureChannels(String legacySecureChannels) {
        if (StringHelper.isNullOrEmpty(legacySecureChannels)) {
            return legacySecureChannels;
        }

        String secureChannels = legacySecureChannels;
        List<String> legacyChannels = Arrays.asList(
                new String[]{"smain", "sdisplay", "sinputs", "scursor", "splayback", "srecord", "ssmartcard", "susbredir"});

        for (String channel : legacyChannels) {
            secureChannels = secureChannels.replace(channel, channel.substring(1));
        }

        return secureChannels;
    }

    public String disableEffectsAsString() {
        StringBuilder disableEffectsBuffer = new StringBuilder();
        int countdown = getWanDisableEffects().size();
        for (ConsoleOptions.WanDisableEffects disabledEffect : getWanDisableEffects()) {
            disableEffectsBuffer.append(disabledEffect.asString());

            if (countdown != 1) {
                disableEffectsBuffer.append(", ");
            }
            countdown--;
        }

        return disableEffectsBuffer.toString();
    }
}
