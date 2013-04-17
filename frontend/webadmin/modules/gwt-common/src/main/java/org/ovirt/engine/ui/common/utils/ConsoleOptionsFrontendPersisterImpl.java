package org.ovirt.engine.ui.common.utils;

import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.uicommonweb.ConsoleOptionsFrontendPersister;
import org.ovirt.engine.ui.uicommonweb.ConsoleUtils;
import org.ovirt.engine.ui.uicommonweb.models.ConsoleProtocol;
import org.ovirt.engine.ui.uicommonweb.models.HasConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.IRdp;
import org.ovirt.engine.ui.uicommonweb.models.vms.ISpice;
import org.ovirt.engine.ui.uicommonweb.models.vms.RdpConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.SpiceConsoleModel;

import com.google.inject.Inject;

public class ConsoleOptionsFrontendPersisterImpl implements ConsoleOptionsFrontendPersister {

    private final ClientStorage clientStorage;

    private final ConsoleUtils consoleUtils;

    // spice options
    private static final String SPICE_CLIENT_MODE = "_spiceClientMode"; //$NON-NLS-1$
    private static final String CTRL_ALT_DEL = "_ctrlAltDel"; //$NON-NLS-1$
    private static final String OPEN_IN_FULL_SCREEN = "_openInFullScreen"; //$NON-NLS-1$
    private static final String SMARTCARD_ENABLED_OVERRIDDEN = "_smartcardEnabledOverridden"; //$NON-NLS-1$
    private static final String WAN_OPTIONS = "_wanOptions"; //$NON-NLS-1$
    private static final String USB_AUTOSHARE = "_usbAutoshare"; //$NON-NLS-1$
    private static final String SPICE_PROXY_ENABLED = "_spiceProxyEnabled"; //$NON-NLS-1$

    // rdp options
    private static final String RDP_CLIENT_MODE = "_rdpClientMode"; //$NON-NLS-1$
    private static final String USE_LOCAL_DRIVES = "_useLocalDrives"; //$NON-NLS-1$

    // common options
    private static final String SELECTED_PROTOCOL = "_selectedProtocol"; //$NON-NLS-1$

    @Inject
    public ConsoleOptionsFrontendPersisterImpl(ClientStorage clientStorage, ConsoleUtils consoleUtils) {
        this.clientStorage = clientStorage;
        this.consoleUtils = consoleUtils;
    }

    @Override
    public void storeToLocalStorage(HasConsoleModel model) {
        if (model.isPool()) {
            // this class works only for VMs, not for pools
            return;
        }

        ConsoleProtocol selectedProtocol = model.getUserSelectedProtocol();
        ConsoleContext context = model.getConsoleContext();
        String id = model.getVM().getId().toString();
        KeyMaker keyMaker = new KeyMaker(id, context);

        clientStorage.setLocalItem(keyMaker.make(SELECTED_PROTOCOL), selectedProtocol.toString());

        if (selectedProtocol == ConsoleProtocol.SPICE) {
            storeSpiceData(model, keyMaker);
        } else if (selectedProtocol == ConsoleProtocol.RDP) {
            storeRdpData(model, keyMaker);
        }
    }

    @Override
    public void loadFromLocalStorage(HasConsoleModel model) {

        String vmId = model.getVM().getId().toString();
        ConsoleContext context = model.getConsoleContext();

        KeyMaker keyMaker = new KeyMaker(vmId, context);

        String selectedProtocolString = clientStorage.getLocalItem(keyMaker.make(SELECTED_PROTOCOL));
        if (selectedProtocolString == null || "".equals(selectedProtocolString)) {
            // if the protocol is not set, nothing is set - ignore
            return;
        }

        ConsoleProtocol selectedProtocol = ConsoleProtocol.valueOf(selectedProtocolString);

        if (selectedProtocol == ConsoleProtocol.SPICE) {
            loadSpiceData(model, keyMaker);
        } else if (selectedProtocol == ConsoleProtocol.RDP) {
            loadRdpData(model, keyMaker);
        } else {
            // VNC is available all the time
            model.setSelectedProtocol(selectedProtocol);
        }
    }

    protected RdpConsoleModel asRdpConsoleModel(HasConsoleModel model) {
        return (RdpConsoleModel) model.getAdditionalConsoleModel();
    }

    protected void storeSpiceData(HasConsoleModel model, KeyMaker keyMaker) {
        SpiceConsoleModel consoleModel = asSpiceConsoleModel(model);
        ISpice spice = asSpice(model);

        clientStorage.setLocalItem(keyMaker.make(SPICE_CLIENT_MODE),
                consoleModel.getClientConsoleMode().toString());

        storeBool(keyMaker.make(CTRL_ALT_DEL), spice.getSendCtrlAltDelete());
        storeBool(keyMaker.make(OPEN_IN_FULL_SCREEN), spice.isFullScreen());
        storeBool(keyMaker.make(SMARTCARD_ENABLED_OVERRIDDEN), spice.isSmartcardEnabledOverridden());
        storeBool(keyMaker.make(WAN_OPTIONS), spice.isWanOptionsEnabled());
        storeBool(keyMaker.make(USB_AUTOSHARE), spice.getUsbAutoShare());
        storeBool(keyMaker.make(SPICE_PROXY_ENABLED), spice.isSpiceProxyEnabled());
    }

    protected void loadRdpData(HasConsoleModel model, KeyMaker keyMaker) {
        if (!consoleUtils.isRDPAvailable() || model.getAdditionalConsoleModel() == null) {
            // don't read rdp options if the rdp console is not available anymore
            return;
        }

        model.setSelectedProtocol(ConsoleProtocol.RDP);

        try {
            RdpConsoleModel.ClientConsoleMode consoleMode =
                    RdpConsoleModel.ClientConsoleMode.valueOf(clientStorage.getLocalItem(keyMaker.make(RDP_CLIENT_MODE)));
            asRdpConsoleModel(model).setRdpImplementation(consoleMode);
        } catch (Exception e) {
        }

        IRdp rdp = asRdpConsoleModel(model).getrdp();

        rdp.setUseLocalDrives(readBool(keyMaker.make(USE_LOCAL_DRIVES)));
    }

    protected void loadSpiceData(HasConsoleModel model, KeyMaker keyMaker) {
        if (!(model.getDefaultConsoleModel() instanceof SpiceConsoleModel)) {
            // don't read spice options if the spice console is not available anymore
            return;
        }

        model.setSelectedProtocol(ConsoleProtocol.SPICE);

        try {
            SpiceConsoleModel.ClientConsoleMode consoleMode = SpiceConsoleModel.ClientConsoleMode.valueOf(clientStorage.getLocalItem(keyMaker.make(SPICE_CLIENT_MODE)));
            asSpiceConsoleModel(model).setSpiceImplementation(consoleMode);
        } catch (Exception e) {
        }

        ISpice spice = asSpice(model);

        if (consoleUtils.isCtrlAltDelEnabled()) {
            spice.setSendCtrlAltDelete(readBool(keyMaker.make(CTRL_ALT_DEL)));
        }

        if (consoleUtils.isWanOptionsAvailable(model)) {
            spice.setWanOptionsEnabled(readBool(keyMaker.make(WAN_OPTIONS)));
        }

        if (consoleUtils.isSpiceProxyDefined()) {
            spice.setSpiceProxyEnabled(readBool(keyMaker.make(SPICE_PROXY_ENABLED)));
        }

        spice.setFullScreen(readBool(keyMaker.make(OPEN_IN_FULL_SCREEN)));
        spice.setOverrideEnabledSmartcard(readBool(keyMaker.make(SMARTCARD_ENABLED_OVERRIDDEN)));
        spice.setUsbAutoShare(readBool(keyMaker.make(USB_AUTOSHARE)));
    }

    protected ISpice asSpice(HasConsoleModel model) {
        return asSpiceConsoleModel(model).getspice();
    }

    protected SpiceConsoleModel asSpiceConsoleModel(HasConsoleModel model) {
        return ((SpiceConsoleModel) model.getDefaultConsoleModel());
    }

    protected void storeRdpData(HasConsoleModel model, KeyMaker keyMaker) {
        RdpConsoleModel consoleModel = asRdpConsoleModel(model);
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
