package org.ovirt.engine.ui.uicommonweb.models;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.uicommonweb.ConsoleOptionsFrontendPersister.ConsoleContext;
import org.ovirt.engine.ui.uicommonweb.models.vms.ConsoleModel;

public interface HasConsoleModel {

    public boolean isPool();

    public ConsoleProtocol getUserSelectedProtocol();

    public void setSelectedProtocol(ConsoleProtocol selectedProtocol);

    public ConsoleModel getDefaultConsoleModel();

    public ConsoleModel getAdditionalConsoleModel();

    public VM getVM();

    public ConsoleContext getConsoleContext();

}
