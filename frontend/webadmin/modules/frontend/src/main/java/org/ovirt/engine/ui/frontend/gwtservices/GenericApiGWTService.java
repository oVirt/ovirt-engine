package org.ovirt.engine.ui.frontend.gwtservices;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;

import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.google.gwt.user.client.rpc.XsrfProtectedService;
import com.google.gwt.user.server.rpc.NoXsrfProtect;

@RemoteServiceRelativePath("GenericApiGWTService")
public interface GenericApiGWTService extends XsrfProtectedService {

    QueryReturnValue runQuery(QueryType search,
            QueryParametersBase searchParameters);

    ActionReturnValue runAction(ActionType actionType,
            ActionParametersBase params);

    @NoXsrfProtect QueryReturnValue runPublicQuery(QueryType queryType,
            QueryParametersBase params);

    ArrayList<QueryReturnValue> runMultipleQueries(
            ArrayList<QueryType> queryTypeList,
            ArrayList<QueryParametersBase> paramsList);

    List<ActionReturnValue> runMultipleActions(
            ActionType actionType,
            ArrayList<ActionParametersBase> multipleParams,
            boolean isRunOnlyIfAllValidationPass);

    List<ActionReturnValue> runMultipleActions(
            ActionType actionType,
            ArrayList<ActionParametersBase> multipleParams,
            boolean isRunOnlyIfAllValidationPass, boolean isWaitForResult);
}
