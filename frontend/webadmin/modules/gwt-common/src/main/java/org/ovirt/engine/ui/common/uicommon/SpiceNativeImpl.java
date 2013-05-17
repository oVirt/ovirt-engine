package org.ovirt.engine.ui.common.uicommon;

import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.uicommonweb.models.vms.ConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ISpiceNative;

public class SpiceNativeImpl extends AbstractSpice implements ISpiceNative {

    public SpiceNativeImpl() {
        super();
    }

    @Override
    public boolean getIsInstalled() {
        return true;
    }

    @Override
    public void connect() {
        StringBuilder configBuilder = new StringBuilder("[virt-viewer]"); //$NON-NLS-1$

        int fullscreen = 0;
        if (isFullScreen()) {
            fullscreen = 1;
        }

        int enableSmartcard = 0;
        if (isSmartcardEnabled()) {
            enableSmartcard = 1;
        }

        int usbAutoShare = 0;
        if (getUsbAutoShare()) {
            usbAutoShare = 1;
        }

        configBuilder.append("\ntype=spice") //$NON-NLS-1$
            .append("\nhost=").append(getHost()) //$NON-NLS-1$
            .append("\nport=").append(Integer.toString(getPort())) //$NON-NLS-1$
            .append("\npassword=").append(getPassword()) //$NON-NLS-1$
            .append("\ntls-port=").append(getSecurePort()) //$NON-NLS-1$
            .append("\nfullscreen=").append(fullscreen) //$NON-NLS-1$
            .append("\ntitle=").append(getTitle()) //$NON-NLS-1$
            .append("\nenable-smartcard=").append(enableSmartcard) //$NON-NLS-1$
            .append("\nenable-usb-autoshare=").append(usbAutoShare) //$NON-NLS-1$
            .append("\ndelete-this-file=1") //$NON-NLS-1$
            .append("\nusb-filter=").append(getUsbFilter()); //$NON-NLS-1$

        if (getCipherSuite() != null) {
            configBuilder.append("\ntls-ciphers=").append(getCipherSuite()); //$NON-NLS-1$
        }

        if (!StringHelper.isNullOrEmpty(getHostSubject())) {
            configBuilder.append("\nhost-subject=").append(getHostSubject()); //$NON-NLS-1$
        }

        if (getTrustStore() != null) {
            //virt-viewer-file doesn't want newlines in ca
            String trustStore= getTrustStore().replace("\n", "\\n");  //$NON-NLS-1$ $NON-NLS-2$
            configBuilder.append("\nca=").append(trustStore); //$NON-NLS-1$
        }

        if (isWanOptionsEnabled()) {
            configBuilder.append("\ncolor-depth=").append(colorDepthAsInt()) //$NON-NLS-1$
                .append("\ndisable-effects=").append(disableEffectsAsString()); //$NON-NLS-1$
        }

        if (!StringHelper.isNullOrEmpty(getToggleFullscreenHotKey())) {
            configBuilder.append("\ntoggle-fullscreen=").append(getToggleFullscreenHotKey()); //$NON-NLS-1$
        }

        if (!StringHelper.isNullOrEmpty(getReleaseCursorHotKey())) {
            configBuilder.append("\nrelease-cursor=").append(getReleaseCursorHotKey()); //$NON-NLS-1$
        }

        if (!StringHelper.isNullOrEmpty(getSpiceProxy())) {
            configBuilder.append("\nproxy=").append(getSpiceProxy()); //$NON-NLS-1$
        }

        ConsoleModel.makeConsoleConfigRequest("console.vv", "application/x-virt-viewer; charset=UTF-8", configBuilder.toString()); //$NON-NLS-1$ $NON-NLS-2$
    }

    @Override
    public void install() {
    }

}
