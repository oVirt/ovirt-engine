package org.ovirt.engine.ui.uicommonweb.models.hosts;

import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.StorageDomainManagementParameter;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.utils.VersionStorageFormatUtil;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicompat.Enlistment;
import org.ovirt.engine.ui.uicompat.IEnlistmentNotification;
import org.ovirt.engine.ui.uicompat.PreparingEnlistment;

import com.google.gwt.user.client.Timer;

@SuppressWarnings("unused")
public class AddStorageDomainRM extends IEnlistmentNotification {

    public AddStorageDomainRM(String correlationId) {
        super(correlationId);
    }

    private static final int WaitInterval = 5000;
    private static final int MaxWaitTries = 6;

    private Timer timer;

    @Override
    public void prepare(PreparingEnlistment enlistment) {

        context.enlistment = enlistment;

        // Fetch all necessary data to make code flat.
        prepare1();
    }

    public void prepare1() {

        EnlistmentContext enlistmentContext = (EnlistmentContext) context.enlistment.getContext();
        HostListModel<?> model = enlistmentContext.getModel();

        final VDS host = model.getSelectedItem();
        VdsActionParameters parameters = new VdsActionParameters(host.getId());
        parameters.setCorrelationId(getCorrelationId());
        Frontend.getInstance().runAction(ActionType.ActivateVds, parameters,
                result -> {
                    ActionReturnValue returnValue = result.getReturnValue();
                    context.activateVdsReturnValue = returnValue;
                    prepare2();
                });
    }

    private void prepare2() {

        PreparingEnlistment enlistment = (PreparingEnlistment) context.enlistment;
        ActionReturnValue returnValue = context.activateVdsReturnValue;

        if (returnValue == null || !returnValue.getSucceeded()) {

            context.enlistment = null;
            enlistment.forceRollback();
        } else {
            prepare3();
        }
    }

    private void prepare3() {

        PreparingEnlistment enlistment = (PreparingEnlistment) context.enlistment;
        EnlistmentContext enlistmentContext = (EnlistmentContext) enlistment.getContext();
        HostListModel<?> model = enlistmentContext.getModel();

        VDS host = model.getSelectedItem();

        if (context.waitTries < MaxWaitTries) {

            context.waitTries++;

            AsyncDataProvider.getInstance().getHostById(new AsyncQuery<>(
                            returnValue -> {

                                context.host = returnValue;

                                timer = new Timer() {
                                    @Override
                                    public void run() {
                                        prepare4();
                                    }
                                };
                                timer.scheduleRepeating(WaitInterval);
                            }),
                    host.getId());
        } else {

            context.enlistment = null;
            enlistment.forceRollback();
        }
    }

    private void prepare4() {

        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        PreparingEnlistment enlistment = (PreparingEnlistment) context.enlistment;
        EnlistmentContext enlistmentContext = (EnlistmentContext) enlistment.getContext();
        HostListModel<?> model = enlistmentContext.getModel();
        ConfigureLocalStorageModel configureModel = (ConfigureLocalStorageModel) model.getWindow();

        if (context.host.getStatus() != VDSStatus.Up) {
            prepare3();
        } else {

            // Add storage domain.
            StorageServerConnections connection = new StorageServerConnections();
            connection.setConnection(configureModel.getStorage().getPath().getEntity());
            connection.setStorageType(StorageType.LOCALFS);
            context.connection = connection;
            StorageServerConnectionParametersBase parameters =
                    new StorageServerConnectionParametersBase(connection, context.host.getId(), false);
            parameters.setCorrelationId(getCorrelationId());
            Frontend.getInstance().runAction(ActionType.AddStorageServerConnection,
                    parameters,
                    result -> {

                        ActionReturnValue returnValue = result.getReturnValue();

                        context.addStorageServerConnectionReturnValue = returnValue;
                        prepare5();
                    });
        }
    }

    private void prepare5() {

        PreparingEnlistment enlistment = (PreparingEnlistment) context.enlistment;
        EnlistmentContext enlistmentContext = (EnlistmentContext) enlistment.getContext();
        HostListModel<?> model = enlistmentContext.getModel();
        ConfigureLocalStorageModel configureModel = (ConfigureLocalStorageModel) model.getWindow();
        ActionReturnValue returnValue = context.addStorageServerConnectionReturnValue;

        if (returnValue == null || !returnValue.getSucceeded()) {

            // Don't rollback.
            context.enlistment = null;
            enlistment.done();
        } else {

            StorageDomainStatic storage = new StorageDomainStatic();
            storage.setStorageType(StorageType.LOCALFS);
            storage.setStorageDomainType(StorageDomainType.Data);
            storage.setStorageName(configureModel.getFormattedStorageName().getEntity());
            storage.setStorage((String) returnValue.getActionReturnValue());
            StorageFormatType storageFormatType =
                    VersionStorageFormatUtil.getForVersion(context.host.getClusterCompatibilityVersion());
            storage.setStorageFormat(storageFormatType);

            StorageDomainManagementParameter parameters = new StorageDomainManagementParameter(storage);
            parameters.setVdsId(context.host.getId());
            parameters.setCorrelationId(getCorrelationId());

            Frontend.getInstance().runAction(ActionType.AddLocalStorageDomain, parameters,
                    result -> {

                        ActionReturnValue returnValue1 = result.getReturnValue();

                        context.addLocalStorageDomainReturnValue = returnValue1;
                        prepare6();
                    });
        }
    }

    private void prepare6() {

        ActionReturnValue returnValue = context.addLocalStorageDomainReturnValue;

        if (returnValue == null || !returnValue.getSucceeded()) {
            StorageServerConnectionParametersBase parameter =
                    new StorageServerConnectionParametersBase(context.connection, context.host.getId(), false);
            parameter.setCorrelationId(getCorrelationId());
            Frontend.getInstance().runAction(ActionType.DisconnectStorageServerConnection,
                    parameter,
                    result -> {

                        ActionReturnValue returnValue1 = result.getReturnValue();

                        context.removeStorageServerConnectionReturnValue = returnValue1;
                        prepare7();
                    });
        } else {
            prepare7();
        }
    }

    private void prepare7() {

        PreparingEnlistment enlistment = (PreparingEnlistment) context.enlistment;
        ActionReturnValue returnValue = context.removeStorageServerConnectionReturnValue;

        context.enlistment = null;

        // ReturnValue not equals null means remove connection occurred.
        if (returnValue != null) {
            // Don't rollback.
            enlistment.done();
        } else {
            enlistment.prepared();
        }
    }

    @Override
    public void commit(Enlistment enlistment) {
        enlistment.done();
    }

    @Override
    public void rollback(Enlistment enlistment) {
        enlistment.done();
    }

    private final Context context = new Context();

    public static final class Context {

        public Enlistment enlistment;
        public VDS host;
        StorageServerConnections connection;
        public ActionReturnValue activateVdsReturnValue;
        public ActionReturnValue addStorageServerConnectionReturnValue;
        public ActionReturnValue addLocalStorageDomainReturnValue;
        public ActionReturnValue removeStorageServerConnectionReturnValue;
        public int waitTries;
    }
}
