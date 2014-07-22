package org.ovirt.engine.ui.frontend.gwtservices;

import java.util.ArrayList;

import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;

import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.google.gwt.user.server.rpc.NoXsrfProtect;

@RemoteServiceRelativePath("GenericApiGWTService")
public interface GenericApiGWTService extends XsrfProtectedRpcService {

    public VdcQueryReturnValue runQuery(VdcQueryType search,
            VdcQueryParametersBase searchParameters);

    public VdcReturnValueBase runAction(VdcActionType actionType,
            VdcActionParametersBase params);

    @NoXsrfProtect
    public VdcQueryReturnValue runPublicQuery(VdcQueryType queryType,
            VdcQueryParametersBase params);

    public ArrayList<VdcQueryReturnValue> runMultipleQueries(
            ArrayList<VdcQueryType> vdcQueryTypeList,
            ArrayList<VdcQueryParametersBase> paramsList);

    public ArrayList<VdcReturnValueBase> runMultipleActions(
            VdcActionType actionType,
            ArrayList<VdcActionParametersBase> multipleParams,
            boolean isRunOnlyIfAllCanDoPass);

    public ArrayList<VdcReturnValueBase> runMultipleActions(
            VdcActionType actionType,
            ArrayList<VdcActionParametersBase> multipleParams,
            boolean isRunOnlyIfAllCanDoPass, boolean isWaitForResult);

    public DbUser getLoggedInUser();

    @NoXsrfProtect
    public VdcReturnValueBase logOff(DbUser userToLogoff);

    @NoXsrfProtect
    public VdcReturnValueBase login(String userName, String password, String profileName, VdcActionType loginType);

    public void storeInHttpSession(String key, String value);

    public String retrieveFromHttpSession(String key);
}
