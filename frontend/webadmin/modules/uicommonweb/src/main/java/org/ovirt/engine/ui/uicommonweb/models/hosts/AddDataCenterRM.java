package org.ovirt.engine.ui.uicommonweb.models.hosts;

import org.ovirt.engine.core.common.action.ChangeVDSClusterParameters;
import org.ovirt.engine.core.common.action.StoragePoolManagementParameter;
import org.ovirt.engine.core.common.action.StoragePoolParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VdsGroupParametersBase;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterModel;
import org.ovirt.engine.ui.uicompat.Enlistment;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IEnlistmentNotification;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.PreparingEnlistment;

@SuppressWarnings("unused")
public class AddDataCenterRM implements IEnlistmentNotification {

    @Override
    public void prepare(PreparingEnlistment enlistment) {

        context.enlistment = enlistment;

        // Fetch all necessary data to make code flat.
        prepare1();
    }

    private void prepare1() {

        EnlistmentContext enlistmentContext = (EnlistmentContext) context.enlistment.getContext();
        HostListModel model = enlistmentContext.getModel();
        ConfigureLocalStorageModel configureModel = (ConfigureLocalStorageModel) model.getWindow();

        DataCenterModel dataCenterModel = configureModel.getDataCenter();
        String dataCenterName = (String) dataCenterModel.getName().getEntity();

        if (!StringHelper.isNullOrEmpty(dataCenterName)) {

            AsyncDataProvider.GetDataCenterListByName(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object model, Object returnValue) {

                        context.dataCenterFoundByName = Linq.FirstOrDefault((Iterable<storage_pool>) returnValue);
                        prepare2();
                    }
                }),
                dataCenterName);
        } else {
            prepare2();
        }
    }

    private void prepare2() {

        PreparingEnlistment enlistment = (PreparingEnlistment) context.enlistment;
        EnlistmentContext enlistmentContext = (EnlistmentContext) enlistment.getContext();
        HostListModel model = enlistmentContext.getModel();
        ConfigureLocalStorageModel configureModel = (ConfigureLocalStorageModel) model.getWindow();

        storage_pool candidate = configureModel.getCandidateDataCenter();
        DataCenterModel dataCenterModel = configureModel.getDataCenter();
        String dataCenterName = (String) dataCenterModel.getName().getEntity();

        if (candidate == null || candidate.getname() != dataCenterName) {

            // Try to find existing data center with the specified name.
            storage_pool dataCenter = context.dataCenterFoundByName;

            if (dataCenter != null) {

                enlistmentContext.setDataCenterId(dataCenter.getId());

                context.enlistment = null;
                enlistment.Prepared();
            } else {

                dataCenter = new storage_pool();
                dataCenter.setname(dataCenterName);
                dataCenter.setdescription((String) dataCenterModel.getDescription().getEntity());
                dataCenter.setstorage_pool_type((StorageType) dataCenterModel.getStorageTypeList().getSelectedItem());
                dataCenter.setcompatibility_version((Version) dataCenterModel.getVersion().getSelectedItem());

                Frontend.RunAction(VdcActionType.AddEmptyStoragePool, new StoragePoolManagementParameter(dataCenter),
                    new IFrontendActionAsyncCallback() {
                        @Override
                        public void Executed(FrontendActionAsyncResult result) {

                            VdcReturnValueBase returnValue = result.getReturnValue();

                            context.addDataCenterReturnValue = returnValue;
                            prepare3();
                        }
                    });
            }
        } else {
            enlistmentContext.setDataCenterId(configureModel.getDataCenter().getDataCenterId().getValue());

            context.enlistment = null;
            enlistment.Prepared();
        }
    }

    private void prepare3() {

        PreparingEnlistment enlistment = (PreparingEnlistment) context.enlistment;
        EnlistmentContext enlistmentContext = (EnlistmentContext) enlistment.getContext();
        VdcReturnValueBase returnValue = context.addDataCenterReturnValue;

        context.enlistment = null;

        if (returnValue != null && returnValue.getSucceeded()) {

            enlistmentContext.setDataCenterId((Guid) returnValue.getActionReturnValue());
            enlistment.Prepared();

        } else {
            enlistment.ForceRollback();
        }
    }

    @Override
    public void commit(Enlistment enlistment) {
        enlistment.Done();
    }

    @Override
    public void rollback(Enlistment enlistment) {
        context.enlistment = enlistment;

        // Fetch all necessary data to make code flat.
        rollback1();
    }

    public void rollback1() {

        Enlistment enlistment = context.enlistment;
        EnlistmentContext enlistmentContext = (EnlistmentContext) enlistment.getContext();

        if (enlistmentContext.getDataCenterId() != null) {

            AsyncDataProvider.GetDataCenterById(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object model, Object returnValue) {

                        context.dataCenterFoundById = (storage_pool) returnValue;
                        rollback2();
                    }
                }),
                enlistmentContext.getDataCenterId());
        } else {
            rollback3();
        }
    }

    public void rollback2() {

        Enlistment enlistment = context.enlistment;
        EnlistmentContext enlistmentContext = (EnlistmentContext) enlistment.getContext();
        HostListModel model = enlistmentContext.getModel();

        VDS host = (VDS) model.getSelectedItem();

        // Retrieve host to make sure we have an updated status etc.
        AsyncDataProvider.GetHostById(new AsyncQuery(this,
            new INewAsyncCallback() {
                @Override
                public void OnSuccess(Object model, Object returnValue) {

                    context.hostFoundById = (VDS) returnValue;
                    rollback3();
                }
            }),
            host.getId());
    }

    public void rollback3() {

        Enlistment enlistment = context.enlistment;
        EnlistmentContext enlistmentContext = (EnlistmentContext) enlistment.getContext();
        HostListModel model = enlistmentContext.getModel();
        ConfigureLocalStorageModel configureModel = (ConfigureLocalStorageModel) model.getWindow();

        VDS host = context.hostFoundById;

        boolean abort = false;
        if (model != null && model.getSelectedItem() != null) {

            // Perform rollback only when the host is in maintenance.
            if (host.getstatus() != VDSStatus.Maintenance) {
                abort = true;
            }
        } else {
            abort = true;
        }

        if (abort) {

            context.enlistment = null;
            enlistment.Done();
            return;
        }


        storage_pool dataCenter = context.dataCenterFoundById;

        // Perform rollback only when the Data Center is un uninitialized.
        if (dataCenter.getstatus() != StoragePoolStatus.Uninitialized) {

            context.enlistment = null;
            enlistment.Done();
            return;
        }

        if (enlistmentContext.getOldClusterId() != null) {

            // Switch host back to previous cluster.
            Frontend.RunAction(VdcActionType.ChangeVDSCluster, new ChangeVDSClusterParameters(enlistmentContext.getOldClusterId(), host.getId()),
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendActionAsyncResult result) {

                        VdcReturnValueBase returnValue = result.getReturnValue();

                        context.changeVDSClusterReturnValue = returnValue;
                        rollback4();
                    }
                });

        } else {
            context.enlistment = null;
            enlistment.Done();
        }
    }

    private void rollback4() {

        Enlistment enlistment = context.enlistment;
        EnlistmentContext enlistmentContext = (EnlistmentContext) enlistment.getContext();
        VdcReturnValueBase returnValue = context.changeVDSClusterReturnValue;

        if (returnValue != null && returnValue.getSucceeded()) {

            // Remove cluster.
            if (enlistmentContext.getClusterId() != null) {

                Frontend.RunAction(VdcActionType.RemoveVdsGroup, new VdsGroupParametersBase(enlistmentContext.getClusterId()),
                    new IFrontendActionAsyncCallback() {
                        @Override
                        public void Executed(FrontendActionAsyncResult result) {

                            VdcReturnValueBase returnValue = result.getReturnValue();

                            context.removeVDSGroupReturnValue = returnValue;
                            rollback5();
                        }
                    });
            }
        } else {
            context.enlistment = null;
            enlistment.Done();
        }
    }

    private void rollback5() {

        Enlistment enlistment = context.enlistment;
        EnlistmentContext enlistmentContext = (EnlistmentContext) enlistment.getContext();
        VdcReturnValueBase returnValue = context.changeVDSClusterReturnValue;

        // Try to remove data center.
        if (enlistmentContext.getDataCenterId() != null) {
            Frontend.RunAction(VdcActionType.RemoveStoragePool, new StoragePoolParametersBase(enlistmentContext.getDataCenterId()));
        }

        // Call done, no matter whether the data center deletion was successful.
        context.enlistment = null;
        enlistment.Done();
    }

    //    @Override
    //    public void Prepare(PreparingEnlistment enlistment)
    //    {
    //        ConfigureLocalStorageModel model = (ConfigureLocalStorageModel) getModel().getWindow();
    //        if (!model.getDontCreateDataCenter())
    //        {
    //            DataCenterModel m = model.getDataCenter();
    //            String name = (String) m.getName().getEntity();
    //            // Try to find existing data center with the specified name.
    //            storage_pool dataCenter = DataProvider.GetDataCenterByName(name);
    //            if (dataCenter != null)
    //            {
    //                getData().setDataCenterId(dataCenter.getId());
    //                enlistment.Prepared();
    //            }
    //
    //            else
    //            {
    //                dataCenter = new storage_pool();
    //                dataCenter.setname(name);
    //                dataCenter.setdescription((String) m.getDescription().getEntity());
    //                dataCenter.setstorage_pool_type((StorageType) m.getStorageTypeList().getSelectedItem());
    //                dataCenter.setcompatibility_version((Version) m.getVersion().getSelectedItem());
    //
    //                VdcReturnValueBase returnValue =
    //                    Frontend.RunAction(VdcActionType.AddEmptyStoragePool,
    //                        new StoragePoolManagementParameter(dataCenter));
    //
    //                if (returnValue != null && returnValue.getSucceeded())
    //                {
    //                    getData().setDataCenterId((Guid) returnValue.getActionReturnValue());
    //                    enlistment.Prepared();
    //                } else
    //                {
    //                    enlistment.ForceRollback();
    //                }
    //            }
    //        }
    //        else
    //        {
    //            enlistment.Prepared();
    //        }
    //    }

    //    @Override
    //    public void Rollback(Enlistment enlistment)
    //    {
    //        if (getModel() == null || getModel().getSelectedItem() == null)
    //        {
    //            return;
    //        }
    //        VDS host = (VDS) getModel().getSelectedItem();
    //        // perform rollback only when the host is in maintenance
    //        if (host.getstatus() != VDSStatus.Maintenance)
    //        {
    //            return;
    //        }
    //
    //        storage_pool dataCenter = DataProvider.GetDataCenterById(getData().getDataCenterId());
    //
    //        // perform rollback only when the Data Center is un uninitialized
    //        if (dataCenter.getstatus() != StoragePoolStatus.Uninitialized)
    //        {
    //            return;
    //        }
    //
    //        if (getData().getOldClusterId() != null
    //            && !host.getvds_group_id().getValue().equals(getData().getOldClusterId().getValue()))
    //        {
    //            // Switch host back to previous cluster.
    //            VdcReturnValueBase returnValue =
    //                Frontend.RunAction(VdcActionType.ChangeVDSCluster,
    //                    new ChangeVDSClusterParameters(getData().getOldClusterId(), host.getvds_id()));
    //
    //            if (returnValue != null && returnValue.getSucceeded())
    //            {
    //                // Remove cluster.
    //                if (getData().getClusterId() != null)
    //                {
    //                    Frontend.RunAction(VdcActionType.RemoveVdsGroup,
    //                        new VdsGroupParametersBase(getData().getClusterId()));
    //                }
    //
    //                // Remove data center.
    //                if (getData().getDataCenterId() != null)
    //                {
    //                    Frontend.RunAction(VdcActionType.RemoveStoragePool,
    //                        new StoragePoolParametersBase(getData().getDataCenterId()));
    //                }
    //            }
    //        }
    //
    //        enlistment.Done();
    //    }


    private final Context context = new Context();

    public final class Context {

        public Enlistment enlistment;
        public storage_pool dataCenterFoundByName;
        public storage_pool dataCenterFoundById;
        public VDS hostFoundById;
        public VdcReturnValueBase addDataCenterReturnValue;
        public VdcReturnValueBase changeVDSClusterReturnValue;
        public VdcReturnValueBase removeVDSGroupReturnValue;
    }
}
