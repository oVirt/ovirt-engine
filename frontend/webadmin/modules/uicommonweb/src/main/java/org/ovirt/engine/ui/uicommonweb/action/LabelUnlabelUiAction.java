package org.ovirt.engine.ui.uicommonweb.action;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.ovirt.engine.core.common.action.LabelNicParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.network.Bond;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.DataFromHostSetupNetworksModel.LabelOnNic;

public class LabelUnlabelUiAction extends UiAction {

    private final List<LabelOnNic> addedLabels;
    private final List<LabelOnNic> removedLabels;
    private final List<Guid> removedBondIds;
    private final Guid vdsId;

    public LabelUnlabelUiAction(List<LabelOnNic> addedLabels,
        List<LabelOnNic> removedLabels,
        List<Bond> removedBonds,
        Guid vdsId,
        Model model) {
        super(model);

        this.addedLabels = addedLabels;
        this.removedLabels = removedLabels;
        this.removedBondIds = Entities.getIds(removedBonds);
        this.vdsId = vdsId;

        dropAllLabelsToRemoveRelatedToRemovedBond();
    }

    private void dropAllLabelsToRemoveRelatedToRemovedBond() {
        for (Iterator<LabelOnNic> iterator = removedLabels.iterator(); iterator.hasNext(); ) {
            LabelOnNic labelOnNic = iterator.next();
            Guid nicId = labelOnNic.getNicId();
            boolean unlabelingBondBeingRemoved = removedBondIds.contains(nicId);    //TODO MM: not sure it this is sufficient.

            if (unlabelingBondBeingRemoved) {
                iterator.remove();
            }
        }
    }

    @Override
    void internalRunAction() {
        if (needToUpdateMissingNicIds()) {
            queryAllNics_updateLabelsOnNic_CallUpdate();

        } else {
            updateLabels();
        }

        runNextAction();
    }

    private void queryAllNics_updateLabelsOnNic_CallUpdate() {
        AsyncQuery asyncQuery = new AsyncQuery();
        asyncQuery.setModel(this);
        asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValueObj) {
                VdcQueryReturnValue returnValue = (VdcQueryReturnValue) returnValueObj;
                Object returnValue2 = returnValue.getReturnValue();
                @SuppressWarnings("unchecked")
                List<VdsNetworkInterface> allNics = (List<VdsNetworkInterface>) returnValue2;
                updateLabelOnNicInstances(allNics);
                updateLabels();

            }

        };

        IdQueryParameters params = new IdQueryParameters(vdsId);
        params.setRefresh(false);
        Frontend.getInstance().runQuery(VdcQueryType.GetVdsInterfacesByVdsId, params, asyncQuery);
    }

    private boolean needToUpdateMissingNicIds() {
        for (LabelOnNic addedLabel : addedLabels) {
            if (addedLabel.getNicId() == null) {
                return true;
            }
        }

        return false;
    }

    private void updateLabels() {
        //TODO MM: how to properly join it to surrounding UIAction chain? Since now, even if not run as parallel this returns immediately making new/remove labels appear/disappear only after setup host network dialog is already closed and operator thinks there's a bug ;)
        List<LabelNicParameters> addLabelParams = transformToLabelNicParameters(addedLabels);
        List<LabelNicParameters> unlabelNicParameters = transformToLabelNicParameters(removedLabels);

        UiAction uiAction = new UiVdcMultipleAction(VdcActionType.LabelNic, addLabelParams, getModel(), true, false);
        uiAction.then(new UiVdcMultipleAction(VdcActionType.UnlabelNic, unlabelNicParameters, getModel()));

        uiAction.runAction();
    }

    @Override
    protected boolean shouldExecute() {
        return super.shouldExecute() && (!addedLabels.isEmpty() || !removedLabels.isEmpty());
    }

    private void updateLabelOnNicInstances(List<VdsNetworkInterface> allNics) {
        for (LabelOnNic addedLabel : addedLabels) {
            if (addedLabel.getNicId() == null) {
                for (VdsNetworkInterface nic : allNics) {
                    if (nic.getName().equals(addedLabel.getNicName())) {
                        addedLabel.setNicId(nic.getId());
                        break;
                    }
                }

                if (addedLabel.getNicId() == null) {
                    throw new IllegalStateException("Unable to find requested nic."); //$NON-NLS-1$
                }

            }
        }
    }

    private List<LabelNicParameters> transformToLabelNicParameters(List<LabelOnNic> labelOnNicInstances) {
        List<LabelNicParameters> result = new ArrayList<>(labelOnNicInstances.size());
        for (LabelOnNic labelOnNic : labelOnNicInstances) {
            result.add(new LabelNicParameters(labelOnNic.getNicId(), labelOnNic.getLabel()));
        }
        return result;
    }
}







