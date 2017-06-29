package org.ovirt.engine.ui.uicommonweb.models;

import static org.ovirt.engine.ui.uicommonweb.ConsoleOptionsFrontendPersister.ConsoleContext;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.models.vms.ConsoleModel;

/**
 * This class contains all consoles and related methods for a single VM.
 */
public interface VmConsoles {

    /**
     * Convenience method that determines if vm/pool can be used with given protocol.
     */
    boolean canSelectProtocol(ConsoleProtocol protocol);

    /**
     * Selects given protocol or throws IAE when the protocol cannot be used.
     */
    void selectProtocol(ConsoleProtocol protocol) throws IllegalArgumentException;

    /**
     * Returns currently selected console protocol.
     */
    ConsoleProtocol getSelectedProcotol();

    /**
     * Returns console model for given type.
     * @param type - desired console type
     */
    <T extends ConsoleModel> T getConsoleModel(Class<T> type);

    /**
     * Method determining if it's possible to connect to selected console.
     *
     * @return true if it's possible to connect to selected console.
     */
    boolean canConnectToConsole();

    /**
     * Invokes selected console.
     *
     * @throws IllegalStateException when
     */
    void connect() throws ConsoleConnectException;

    /**
     * Returns the VM associated with consoles.
     */
    VM getVm();

    /**
     * Sets a new vm for these consoles.
     * Due to uicommon design parent object MUST secure freshness of this object. Use this method for this reason only.
     * @param vm - new VM
     */
    void setVm(VM vm);


    /**
     * Get id of underlying entity (various implementation may want to return various names).
     * @return id of underlying entity
     */
    Guid getEntityId();

    /**
     * Get name of underlying entity (various implementation may want to return various names).
     * @return name of underlying entity
     */
    String getEntityName();

    /**
     * Returns the message explaining why it's console cannot be connected to.
     */
    public String cannotConnectReason();

    /**
     * Returns the context where this class is used (webadmin).
     */
    ConsoleContext getConsoleContext();

    /**
     * Thrown by VmConsoles.connect if error occurs when connecting to the console.
     * Contains a localized message with error description.
     */
    class ConsoleConnectException extends Exception {
       private final String localizedErrorMessage;

        public ConsoleConnectException(String localizedErrorMessage) {
            this.localizedErrorMessage = localizedErrorMessage;
        }

        public String getLocalizedErrorMessage() {
            return localizedErrorMessage;
        }
    }

}
