package org.ovirt.engine.core.common.console;

import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.compat.StringHelper;

public class ConsoleDescriptorGenerator {

    public static String generateDescriptor(ConsoleOptions options) {
        StringBuilder configBuilder = new StringBuilder("[virt-viewer]");
        configBuilder.append("\ntype=").append(graphicsTypeToString(options.getGraphicsType()))
                .append("\nhost=").append(options.getHost())
                .append("\nport=").append(options.getPort())
                .append("\npassword=").append(options.getTicket())
                .append("\n# Password is valid for ")
                .append(ConsoleOptions.TICKET_VALIDITY_SECONDS).append(" seconds.")
                .append("\ndelete-this-file=1")
                .append("\nfullscreen=").append(boolToInt(options.isFullScreen()));

        if (!StringHelper.isNullOrEmpty(options.getTitle())) {
            configBuilder.append("\ntitle=").append(options.getTitle());
        }

        if (!StringHelper.isNullOrEmpty(options.getToggleFullscreenHotKey())) {
            configBuilder.append("\ntoggle-fullscreen=").append(options.getToggleFullscreenHotKey());
        }

        String releaseCursorHotKey = options.getReleaseCursorHotKey();
        if (!StringHelper.isNullOrEmpty(releaseCursorHotKey)) {
            configBuilder.append("\nrelease-cursor=").append(releaseCursorHotKey);
        }

        if (options.isRemapCtrlAltDelete()) {
            configBuilder.append("\nsecure-attention=").append(ConsoleOptions.SECURE_ATTENTION_MAPPING);
        }

        if (options.getGraphicsType() == GraphicsType.SPICE) {
            configBuilder.append(generateSpicePart(options));
        }

        if (options.getGraphicsType() == GraphicsType.VNC) {
            configBuilder.append(generateVncPart(options));
        }

        String remoteViewerSupportedVersions = options.getRemoteViewerSupportedVersions();
        String remoteViewerNewerVersionUrl = options.getRemoteViewerNewerVersionUrl();
        if (!StringHelper.isNullOrEmpty(remoteViewerSupportedVersions) && !StringHelper.isNullOrEmpty(remoteViewerNewerVersionUrl)) {
            configBuilder.append("\nversions=").append(remoteViewerSupportedVersions);
            configBuilder.append("\nnewer-version-url=").append(remoteViewerNewerVersionUrl);
        }

        configBuilder.append(generateOvirtSection(options));

        return configBuilder.append("\n").toString();
    }

    private static String generateOvirtSection(ConsoleOptions options) {
        final StringBuilder builder = new StringBuilder();
        builder.append("\n")
                .append("\n[ovirt]")
                .append("\nhost=").append(options.getOvirtHost())
                .append("\nvm-guid=").append(options.getVmId())
                .append("\nsso-token=").append(options.getSsoToken())
                .append("\nadmin=").append(boolToInt(options.isAdminConsole()));

        /*
         * If custom certificate is used for https then we suppose that the ca certificate is available in client system
         * thrust store for remote-viewer to check against. Since remote-viewer prefers ca from vv file to system ca,
         * 'ca' record is not included for custom https certificate.
         */
        if (options.getTrustStore() != null && !options.isCustomHttpsCertificateUsed()) {
            builder.append("\nca=").append(toOnelineCertificate(options.getTrustStore()));
        }

        return builder.toString();

    }

    private static String generateSpicePart(ConsoleOptions options) {
        StringBuilder configBuilder = new StringBuilder();

        configBuilder.append("\ntls-port=").append(options.getSecurePort())
            .append("\nenable-smartcard=").append(boolToInt(options.passSmartcardOption()))
            .append("\nenable-usb-autoshare=").append(boolToInt(options.isUsbAutoShare()))
            .append("\nusb-filter=").append(options.getUsbFilter());

        if (options.getCipherSuite() != null) {
            configBuilder.append("\ntls-ciphers=").append(options.getCipherSuite());
        }

        if (!StringHelper.isNullOrEmpty(options.getHostSubject())
                && !StringHelper.isNullOrEmpty(options.getTrustStore())) {
            configBuilder.append("\nhost-subject=").append(options.getHostSubject());
            configBuilder.append("\nca=").append(toOnelineCertificate(options.getTrustStore()));
        }

        if (options.isWanOptionsEnabled()) {
            configBuilder.append("\ncolor-depth=").append(options.colorDepthAsInt())
                .append("\ndisable-effects=").append(options.disableEffectsAsString());
        }

        if (!StringHelper.isNullOrEmpty(options.getSpiceProxy())) {
            configBuilder.append("\nproxy=").append(options.getSpiceProxy());
        }

        if (!StringHelper.isNullOrEmpty(options.getSslChanels())) {
            configBuilder.append("\nsecure-channels=").append(formatSecureChannels(options.getSslChanels()));
        }

        return configBuilder.toString();
    }

    private static String generateVncPart(ConsoleOptions options) {
        StringBuilder configBuilder = new StringBuilder();

        if (!StringHelper.isNullOrEmpty(options.getUsername())) {
            configBuilder.append("\nusername=").append(options.getUsername());
        }

        return configBuilder.toString();
    }

    private static String toOnelineCertificate(String certificate) {
        return certificate.replace("\n", "\\n");
    }

    private static int boolToInt(Boolean b) {
        return Boolean.TRUE.equals(b)
            ? 1
            : 0;
    }

    private static String graphicsTypeToString(GraphicsType graphicsType) {
        if (graphicsType == null) {
            return null;
        }

        switch (graphicsType) {
            case SPICE:
                return "spice";
            case VNC:
                return "vnc";
            default:
                return null;
        }
    }

    /**
     * Reformats the secure channels - the .vv file accepts semicolon-separated values (unlike SPICE browser plugin).
     */
    private static String formatSecureChannels(String sslChanels) {
        return (sslChanels == null)
                ? ""
                : sslChanels.replace(',', ';');
    }

}
