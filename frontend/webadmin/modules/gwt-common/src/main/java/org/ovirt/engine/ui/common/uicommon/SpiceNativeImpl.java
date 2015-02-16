package org.ovirt.engine.ui.common.uicommon;

import org.ovirt.engine.core.common.console.ConsoleOptions;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.uicommonweb.models.vms.ConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ISpiceNative;

public class SpiceNativeImpl extends AbstractSpice implements ISpiceNative {

    public SpiceNativeImpl() {
        super();
    }

    @Override
    public void invokeClient() {
        ConsoleOptions options = getOptions();
        StringBuilder configBuilder = new StringBuilder("[virt-viewer]"); //$NON-NLS-1$

        int fullscreen = 0;
        if (options.isFullScreen()) {
            fullscreen = 1;
        }

        int enableSmartcard = 0;
        if (options.isSmartcardEnabled()) {
            enableSmartcard = 1;
        }

        int usbAutoShare = 0;
        if (options.isUsbAutoShare()) {
            usbAutoShare = 1;
        }

        configBuilder.append("\ntype=spice") //$NON-NLS-1$
            .append("\nhost=").append(options.getHost()) //$NON-NLS-1$
            .append("\nport=").append(options.getPort()) //$NON-NLS-1$
            .append("\npassword=").append(options.getTicket())//$NON-NLS-1$
            .append("\n# Password is valid for ").append(ConsoleOptions.TICKET_VALIDITY_SECONDS).append(" seconds.") //$$NON-NLS-1$NON-NLS-2$
            .append("\ntls-port=").append(getOptions().getSecurePort()) //$NON-NLS-1$
            .append("\nfullscreen=").append(fullscreen) //$NON-NLS-1$
            .append("\ntitle=").append(options.getTitle()) //$NON-NLS-1$
            .append("\nenable-smartcard=").append(enableSmartcard) //$NON-NLS-1$
            .append("\nenable-usb-autoshare=").append(usbAutoShare) //$NON-NLS-1$
            .append("\ndelete-this-file=1") //$NON-NLS-1$
            .append("\nusb-filter=").append(options.getUsbFilter()); //$NON-NLS-1$

        if (options.getCipherSuite() != null) {
            configBuilder.append("\ntls-ciphers=").append(options.getCipherSuite()); //$NON-NLS-1$
        }

        if (!StringHelper.isNullOrEmpty(options.getHostSubject())) {
            configBuilder.append("\nhost-subject=").append(options.getHostSubject()); //$NON-NLS-1$
        }

        if (options.getTrustStore() != null) {
            //virt-viewer-file doesn't want newlines in ca
            String trustStore= options.getTrustStore().replace("\n", "\\n");  //$NON-NLS-1$ $NON-NLS-2$
            configBuilder.append("\nca=").append(trustStore); //$NON-NLS-1$
        }

        if (options.isWanOptionsEnabled()) {
            configBuilder.append("\ncolor-depth=").append(options.colorDepthAsInt()) //$NON-NLS-1$
                .append("\ndisable-effects=").append(options.disableEffectsAsString()); //$NON-NLS-1$
        }

        if (!StringHelper.isNullOrEmpty(options.getToggleFullscreenHotKey())) {
            configBuilder.append("\ntoggle-fullscreen=").append(options.getToggleFullscreenHotKey()); //$NON-NLS-1$
        }

        if (!StringHelper.isNullOrEmpty(options.getReleaseCursorHotKey())) {
            configBuilder.append("\nrelease-cursor=").append(options.getReleaseCursorHotKey()); //$NON-NLS-1$
        }

        if (options.isRemapCtrlAltDelete()) {
            configBuilder.append("\nsecure-attention=").append(ConsoleOptions.SECURE_ATTENTION_MAPPING); //$NON-NLS-1$
        }

        if (!StringHelper.isNullOrEmpty(options.getSpiceProxy())) {
            configBuilder.append("\nproxy=").append(options.getSpiceProxy()); //$NON-NLS-1$
        }

        if (!StringHelper.isNullOrEmpty(options.getSslChanels())) {
            configBuilder.append("\nsecure-channels=").append(formatSecureChannels(getOptions().getSslChanels())); //$NON-NLS-1$
        }

        ConsoleModel.makeConsoleConfigRequest("console.vv", "application/x-virt-viewer; charset=UTF-8", configBuilder.toString()); //$NON-NLS-1$ $NON-NLS-2$
    }

    /**
     * Reformats the secure channels - the .vv file accepts semicolon-separated values (unlike SPICE browser plugin).
     */
    private static String formatSecureChannels(String sslChanels) {
        return (sslChanels == null)
                ? "" //$NON-NLS-1$
                : sslChanels.replace(',', ';');
    }

}
