package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class VmSessionsModel extends EntityModel {

    String guestUserName;
    String consoleUserName;

    public VmSessionsModel() {
        super();

        setTitle(ConstantsManager.getInstance().getConstants().sessionsTitle());
        setHashName("sessions"); //$NON-NLS-1$
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();

        if (getEntity() != null)
        {
            updateProperties();
        }
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e) {
        super.entityPropertyChanged(sender, e);

        updateProperties();
    }

    private void updateProperties() {
        VM vm = (VM) getEntity();

        this.consoleUserName = vm.getConsoleCurentUserName();
        this.guestUserName = vm.getGuestCurentUserName();
    }

    public String getGuestUserName() {
        return guestUserName;
    }

    public void setGuestUserName(String guestUserName) {
        this.guestUserName = guestUserName;
    }

    public String getConsoleUserName() {
        return consoleUserName;
    }

    public void setConsoleUserName(String consoleUserName) {
        this.consoleUserName = consoleUserName;
    }

}
