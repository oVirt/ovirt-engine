package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.uicommonweb.ConsoleOptionsFrontendPersister.ConsoleContext;
import org.ovirt.engine.ui.uicommonweb.models.ConsoleModelsCache;
import org.ovirt.engine.ui.uicommonweb.models.ConsoleProtocol;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.HasConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserSelectedDisplayProtocolManager;

public class WebAdminItemModel extends EntityModel implements HasConsoleModel {

    UserSelectedDisplayProtocolManager userSelectedDisplayProtocolManager;
    ConsoleModelsCache consoleModelsCache;

    private ConsoleModel defaultConsoleModel;
    private ConsoleModel additionalConsoleModel;

    public WebAdminItemModel(
            UserSelectedDisplayProtocolManager userSelectedDisplayProtocolManager,
            ConsoleModelsCache consoleModelsCache) {
        this.userSelectedDisplayProtocolManager = userSelectedDisplayProtocolManager;
        this.consoleModelsCache = consoleModelsCache;
    }

    @Override
    public boolean isPool() {
        return false;
    }

    @Override
    public ConsoleProtocol getUserSelectedProtocol() {
        return userSelectedDisplayProtocolManager.resolveSelectedProtocol(this);
    }

    @Override
    public void setSelectedProtocol(ConsoleProtocol selectedProtocol) {
        userSelectedDisplayProtocolManager.setSelectedProtocol(selectedProtocol, this);
    }

    @Override
    public ConsoleModel getDefaultConsoleModel() {
        return defaultConsoleModel;
    }

    public void setDefaultConsoleModel(ConsoleModel defaultConsoleModel) {
        this.defaultConsoleModel = defaultConsoleModel;
    }

    @Override
    public ConsoleModel getAdditionalConsoleModel() {
        return additionalConsoleModel;
    }

    public void setAdditionalConsoleModel(ConsoleModel additionalConsoleModel) {
        this.additionalConsoleModel = additionalConsoleModel;
    }

    @Override
    public VM getVM() {
        if (getEntity() instanceof VM) {
            return (VM) getEntity();
        }

        return null;
    }

    @Override
    public ConsoleContext getConsoleContext() {
        return ConsoleContext.WA;
    }

    public ConsoleModelsCache getConsoleModelsCache() {
        return consoleModelsCache;
    }

    public UserSelectedDisplayProtocolManager getUserSelectedDisplayProtocolManager() {
        return userSelectedDisplayProtocolManager;
    }



}
