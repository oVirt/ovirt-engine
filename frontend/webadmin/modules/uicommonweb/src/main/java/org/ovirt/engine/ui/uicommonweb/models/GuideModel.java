package org.ovirt.engine.ui.uicommonweb.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ChangeVDSClusterParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.ErrorPopupManager;
import org.ovirt.engine.ui.uicommonweb.TypeResolver;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.hosts.MoveHost;
import org.ovirt.engine.ui.uicommonweb.models.hosts.MoveHostData;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.ObservableCollection;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.gwt.user.client.Timer;

@SuppressWarnings("unused")
public class GuideModel<T> extends EntityModel<T> {

    private ErrorPopupManager errorPopupManager;

    private void setConsoleHelpers() {
        this.errorPopupManager = (ErrorPopupManager) TypeResolver.getInstance().resolve(ErrorPopupManager.class);
    }

    private List<UICommand> compulsoryActions;

    public List<UICommand> getCompulsoryActions() {
        return compulsoryActions;
    }

    public void setCompulsoryActions(List<UICommand> value) {
        if (compulsoryActions != value) {
            compulsoryActions = value;
            onPropertyChanged(new PropertyChangedEventArgs("CompulsoryActions")); //$NON-NLS-1$
        }
    }

    private List<UICommand> optionalActions;

    public List<UICommand> getOptionalActions() {
        return optionalActions;
    }

    public void setOptionalActions(List<UICommand> value) {
        if (optionalActions != value) {
            optionalActions = value;
            onPropertyChanged(new PropertyChangedEventArgs("OptionalActions")); //$NON-NLS-1$
        }
    }

    private EntityModel<String> note;

    public EntityModel<String> getNote() {
        return note;
    }

    public void setNote(EntityModel<String> value) {
        note = value;
    }

    public GuideModel() {
        setCompulsoryActions(new ObservableCollection<UICommand>());
        setOptionalActions(new ObservableCollection<UICommand>());
        setNote(new EntityModel<String>());
        getNote().setIsAvailable(false);
        setConsoleHelpers();
    }

    protected void cancel() {}

    protected void postAction() {}

    protected String getVdsSearchString(final MoveHost moveHost) {
        StringBuilder buf = new StringBuilder("Host: "); //$NON-NLS-1$
        for (MoveHostData hostData : moveHost.getSelectedHosts()) {
            if (buf.length() > 6) {
                buf.append(" or "); //$NON-NLS-1$
            }
            buf.append("name = "); //$NON-NLS-1$
            buf.append(hostData.getEntity().getName());
        }
        return buf.toString();
    }

    protected void checkVdsClusterChangeSucceeded(final String searchStr,
                                                  final List<ActionParametersBase> changeVdsParameterList,
                                                  final List<ActionParametersBase> activateVdsParameterList) {
        final Map<Guid, Guid> hostClusterIdMap = new HashMap<>();
        for (ActionParametersBase param : changeVdsParameterList) {
            hostClusterIdMap.put(((ChangeVDSClusterParameters) param).getVdsId(),
                    ((ChangeVDSClusterParameters) param).getClusterId());
        }
        Frontend.getInstance().runQuery(QueryType.Search,
                new SearchParameters(searchStr, SearchType.VDS),
                new AsyncQuery<QueryReturnValue>(returnValue -> {
                    List<VDS> hosts = returnValue.getReturnValue();
                    boolean succeeded = true;
                    for (VDS host : hosts) {
                        if (!host.getClusterId().equals(hostClusterIdMap.get(host.getId()))) {
                            succeeded = false;
                        }
                    }
                    if (!succeeded) {
                        getWindow().stopProgress();
                        cancel();
                        errorPopupManager.show(ConstantsManager.getInstance().getConstants().hostChangeClusterTimeOut());
                    } else {
                        activateHostsAfterClusterChange(searchStr, activateVdsParameterList);
                    }
                }));
    }

    protected void activateHostsAfterClusterChange(
            final String searchStr,
            final List<ActionParametersBase> activateVdsParameterList) {
        Frontend.getInstance().runMultipleAction(ActionType.ActivateVds, activateVdsParameterList,
                result -> {
                    Timer timer = new Timer() {
                        public void run() {
                            checkVdsActivateSucceeded(searchStr);
                        }
                    };

                    // Execute the timer to expire 5 seconds in the future
                    timer.schedule(5000);
                },
                this);
    }

    protected void checkVdsActivateSucceeded(final String searchStr) {
        Frontend.getInstance().runQuery(QueryType.Search,
                new SearchParameters(searchStr, SearchType.VDS),
                new AsyncQuery<QueryReturnValue>(returnValue -> {
                    List<VDS> hosts = returnValue.getReturnValue();
                    boolean succeeded = true;
                    for (VDS host : hosts) {
                        if (host.getStatus() != VDSStatus.Up) {
                            succeeded = false;
                        }
                    }
                    getWindow().stopProgress();
                    cancel();
                    if (succeeded) {
                        postAction();
                    } else {
                        errorPopupManager.show(ConstantsManager.getInstance().getConstants().hostActivationTimeOut());
                    }
                }));
    }
}
