package org.ovirt.engine.ui.frontend.gwtservices;

import java.util.ArrayList;

import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.users.VdcUser;

import com.google.gwt.rpc.client.RpcService;

public interface GenericApiGWTService extends RpcService {

    public VdcQueryReturnValue RunQuery(VdcQueryType search,
            VdcQueryParametersBase searchParameters);

    public VdcReturnValueBase RunAction(VdcActionType actionType,
            VdcActionParametersBase params);

    public VdcQueryReturnValue RunPublicQuery(VdcQueryType queryType,
            VdcQueryParametersBase params);

    public ArrayList<VdcQueryReturnValue> RunMultipleQueries(
            ArrayList<VdcQueryType> vdcQueryTypeList,
            ArrayList<VdcQueryParametersBase> paramsList);

    public ArrayList<VdcReturnValueBase> RunMultipleActions(
            VdcActionType actionType,
            ArrayList<VdcActionParametersBase> multipleParams);

    public VdcUser getLoggedInUser();

    public VdcReturnValueBase logOff(VdcUser userToLogoff);

    public VdcReturnValueBase Login(String user, String password, String domain);

    // TODO: Should be implemented (most of these methods are required by
    // UiCommon)
    /*
     * public ArrayList<VdcReturnValueBase> RunMultipleActions( VdcActionType actionType,
     * ArrayList<VdcActionParametersBase> prms);
     *
     * public void UnregisterQuery(Guid asyncSearchId);
     *
     * public Guid RegisterSearch(String searchString, SearchType cluster, int searchPageSize,
     * RefObject<ObservableCollection<IVdcQueryable>> tempRefObject);
     *
     * public VdcUser Login(String entity, String entity2, String selectedItem);
     *
     * public VdcReturnValueBase RunActionAsyncroniousely( VdcActionType addsanstoragedomain, VdcActionParametersBase
     * param);
     *
     * public Guid RegisterQuery(VdcQueryType getallbookmarks, VdcQueryParametersBase vdcQueryParametersBase,
     * RefObject<ObservableCollection<IVdcQueryable>> tempRefObject);
     *
     * public void Logoff(LogoutUserParameters tempVar);
     *
     * public boolean getIsUserLoggedIn();
     *
     * public VdcUser getLoggedInUser();
     */
}
