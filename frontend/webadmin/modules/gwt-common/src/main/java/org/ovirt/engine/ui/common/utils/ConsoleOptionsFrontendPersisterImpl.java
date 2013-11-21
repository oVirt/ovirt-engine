package org.ovirt.engine.ui.common.utils;

import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.uicommonweb.ConsoleOptionsFrontendPersister;
import org.ovirt.engine.ui.uicommonweb.ConsoleUtils;
import org.ovirt.engine.ui.uicommonweb.models.ConsoleProtocol;
import org.ovirt.engine.ui.uicommonweb.models.VmConsoles;
import org.ovirt.engine.ui.uicommonweb.models.vms.IRdp;
import org.ovirt.engine.ui.uicommonweb.models.vms.ISpice;
import org.ovirt.engine.ui.uicommonweb.models.vms.IVnc;
import org.ovirt.engine.ui.uicommonweb.models.vms.RdpConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.SpiceConsoleModel;

import com.google.inject.Inject;
import org.ovirt.engine.ui.uicommonweb.models.vms.VncConsoleModel;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ConsoleOptionsFrontendPersisterImpl implements ConsoleOptionsFrontendPersister {

    private final ClientStorage clientStorage;

    private final ConsoleUtils consoleUtils;

    // spice options
    private static final String SPICE_CLIENT_MODE = "_spiceClientMode"; //$NON-NLS-1$
    private static final String OPEN_IN_FULL_SCREEN = "_openInFullScreen"; //$NON-NLS-1$
    private static final String SMARTCARD_ENABLED_OVERRIDDEN = "_smartcardEnabledOverridden"; //$NON-NLS-1$
    private static final String WAN_OPTIONS = "_wanOptions"; //$NON-NLS-1$
    private static final String USB_AUTOSHARE = "_usbAutoshare"; //$NON-NLS-1$
    private static final String SPICE_PROXY_ENABLED = "_spiceProxyEnabled"; //$NON-NLS-1$
    private static final String REMAP_CAD_SPICE = "_remapCtrlAltDelSpice"; //$NON-NLS-1$

    // vnc options
    private static final String VNC_CLIENT_MODE = "_vncClientMode"; //$NON-NLS-1$
    private static final String REMAP_CAD_VNC = "_remapCtrlAltDelVnc"; //$NON-NLS-1$

    // rdp options
    private static final String RDP_CLIENT_MODE = "_rdpClientMode"; //$NON-NLS-1$
    private static final String USE_LOCAL_DRIVES = "_useLocalDrives"; //$NON-NLS-1$

    // common options
    private static final String SELECTED_PROTOCOL = "_selectedProtocol"; //$NON-NLS-1$

    private final Logger logger = Logger.getLogger(ConsoleOptionsFrontendPersisterImpl.class.getName());

    @Inject
    public ConsoleOptionsFrontendPersisterImpl(ClientStorage clientStorage, ConsoleUtils consoleUtils) {
        this.clientStorage = clientStorage;
        this.consoleUtils = consoleUtils;
    }

    @Override
    public void storeToLocalStorage(VmConsoles vmConsoles) {
        ConsoleProtocol selectedProtocol = vmConsoles.getSelectedProcotol();
        ConsoleContext context = vmConsoles.getConsoleContext();

        String id = vmConsoles.getVm().getId().toString();
        KeyMaker keyMaker = new KeyMaker(id, context);

        clientStorage.setLocalItem(keyMaker.make(SELECTED_PROTOCOL), selectedProtocol.toString());

        if (selectedProtocol == ConsoleProtocol.SPICE) {
            storeSpiceData(vmConsoles, keyMaker);
        } else if (selectedProtocol == ConsoleProtocol.VNC) {
            storeVncData(vmConsoles, keyMaker);
        } else if (selectedProtocol == ConsoleProtocol.RDP) {
            storeRdpData(vmConsoles, keyMaker);
        }
    }

    @Override
    public void loadFromLocalStorage(VmConsoles vmConsoles) {

        String vmId = vmConsoles.getVm().getId().toString();
        ConsoleContext context = vmConsoles.getConsoleContext();

        KeyMaker keyMaker = new KeyMaker(vmId, context);

        String selectedProtocolString = clientStorage.getLocalItem(keyMaker.make(SELECTED_PROTOCOL));
        if (selectedProtocolString == null || "".equals(selectedProtocolString)) {
            // if the protocol is not set, nothing is set - ignore
            return;
        }

        ConsoleProtocol selectedProtocol = ConsoleProtocol.valueOf(selectedProtocolString);

        if (!vmConsoles.canSelectProtocol(selectedProtocol)) {
            return;
        }

        if (selectedProtocol == ConsoleProtocol.SPICE) {
            loadSpiceData(vmConsoles, keyMaker);
        } else if (selectedProtocol == ConsoleProtocol.VNC) {
            loadVncData(vmConsoles, keyMaker);
        } else if (selectedProtocol == ConsoleProtocol.RDP) {
            loadRdpData(vmConsoles, keyMaker);
        }
    }

    private void loadVncData(VmConsoles vmConsoles, KeyMaker keyMaker) {
        vmConsoles.selectProtocol(ConsoleProtocol.VNC);

        try {
            vmConsoles.getConsoleModel(VncConsoleModel.class).setVncImplementation(VncConsoleModel.ClientConsoleMode
                    .valueOf(clientStorage.getLocalItem(keyMaker.make(VNC_CLIENT_MODE))));
            asVnc(vmConsoles).setRemapCtrlAltDelete(readBool(keyMaker.make(REMAP_CAD_VNC)));
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed loading VNC data. Exception message: " + e.getMessage()); //$NON-NLS-1$
        }
    }

    protected void storeSpiceData(VmConsoles vmConsoles, KeyMaker keyMaker) {
        SpiceConsoleModel consoleModel = vmConsoles.getConsoleModel(SpiceConsoleModel.class);
        ISpice spice = asSpice(vmConsoles);

        clientStorage.setLocalItem(keyMaker.make(SPICE_CLIENT_MODE),
                consoleModel.getClientConsoleMode().toString());

        storeBool(keyMaker.make(OPEN_IN_FULL_SCREEN), spice.isFullScreen());
        storeBool(keyMaker.make(SMARTCARD_ENABLED_OVERRIDDEN), spice.isSmartcardEnabledOverridden());
        storeBool(keyMaker.make(WAN_OPTIONS), spice.isWanOptionsEnabled());
        storeBool(keyMaker.make(USB_AUTOSHARE), spice.getUsbAutoShare());
        storeBool(keyMaker.make(SPICE_PROXY_ENABLED), spice.isSpiceProxyEnabled());
        storeBool(keyMaker.make(REMAP_CAD_SPICE), spice.isRemapCtrlAltDel());
    }

    private void storeVncData(VmConsoles vmConsoles, KeyMaker keyMaker) {
        VncConsoleModel consoleModel = vmConsoles.getConsoleModel(VncConsoleModel.class);
        if (consoleModel != null) {
            clientStorage.setLocalItem(keyMaker.make(VNC_CLIENT_MODE), consoleModel.getClientConsoleMode().toString());
            storeBool(keyMaker.make(REMAP_CAD_VNC), consoleModel.getVncImpl().isRemapCtrlAltDelete());
        }
    }

    protected void loadRdpData(VmConsoles vmConsoles, KeyMaker keyMaker) {
        vmConsoles.selectProtocol(ConsoleProtocol.RDP);

        try {
            RdpConsoleModel.ClientConsoleMode consoleMode =
                    RdpConsoleModel.ClientConsoleMode.valueOf(clientStorage.getLocalItem(keyMaker.make(RDP_CLIENT_MODE)));
            vmConsoles.getConsoleModel(RdpConsoleModel.class).setRdpImplementation(consoleMode);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed loading RDP data. Exception message: " + e.getMessage()); //$NON-NLS-1$
        }

        IRdp rdp = vmConsoles.getConsoleModel(RdpConsoleModel.class).getrdp();

        rdp.setUseLocalDrives(readBool(keyMaker.make(USE_LOCAL_DRIVES)));
    }

    protected void loadSpiceData(VmConsoles vmConsoles, KeyMaker keyMaker) {
        vmConsoles.selectProtocol(ConsoleProtocol.SPICE);

        try {
            SpiceConsoleModel.ClientConsoleMode consoleMode = SpiceConsoleModel.ClientConsoleMode.valueOf(clientStorage.getLocalItem(keyMaker.make(SPICE_CLIENT_MODE)));
            vmConsoles.getConsoleModel(SpiceConsoleModel.class).setConsoleClientMode(consoleMode);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed loading SPICE data. Exception message: " + e.getMessage()); //$NON-NLS-1$
        }

        ISpice spice = asSpice(vmConsoles);
        if (vmConsoles.getConsoleModel(SpiceConsoleModel.class).isWanOptionsAvailableForMyVm()) {
            spice.setWanOptionsEnabled(readBool(keyMaker.make(WAN_OPTIONS)));
        }

        if (consoleUtils.isSpiceProxyDefined(vmConsoles.getVm())) {
            spice.setSpiceProxyEnabled(readBool(keyMaker.make(SPICE_PROXY_ENABLED)));
        }

        spice.setFullScreen(readBool(keyMaker.make(OPEN_IN_FULL_SCREEN)));
        spice.setOverrideEnabledSmartcard(readBool(keyMaker.make(SMARTCARD_ENABLED_OVERRIDDEN)));
        spice.setUsbAutoShare(readBool(keyMaker.make(USB_AUTOSHARE)));
        spice.setRemapCtrlAltDel(readBool(keyMaker.make(REMAP_CAD_SPICE)));
    }

    protected ISpice asSpice(VmConsoles vmConsoles) {
        return (vmConsoles.getConsoleModel(SpiceConsoleModel.class)).getspice();
    }

    protected IVnc asVnc(VmConsoles vmConsoles) {
        return (vmConsoles.getConsoleModel(VncConsoleModel.class)).getVncImpl();
    }

    protected void storeRdpData(VmConsoles vmConsoles, KeyMaker keyMaker) {
        RdpConsoleModel consoleModel = vmConsoles.getConsoleModel(RdpConsoleModel.class);
        IRdp rdpImpl = consoleModel.getrdp();

        clientStorage.setLocalItem(keyMaker.make(RDP_CLIENT_MODE),
                consoleModel.getClientConsoleMode().toString());

        storeBool(keyMaker.make(USE_LOCAL_DRIVES), rdpImpl.getUseLocalDrives());
    }

    private boolean readBool(String key) {
        return Boolean.parseBoolean(clientStorage.getLocalItem(key));
    }

    private void storeBool(String key, boolean value) {
        clientStorage.setLocalItem(key, Boolean.toString(value));
    }

    class KeyMaker {

        private final String vmId;

        private final ConsoleContext context;

        public KeyMaker(String vmId, ConsoleContext context) {
            this.vmId = vmId;
            this.context = context;
        }

        public String make(String key) {
            return vmId + context + key;
        }
    }
}
