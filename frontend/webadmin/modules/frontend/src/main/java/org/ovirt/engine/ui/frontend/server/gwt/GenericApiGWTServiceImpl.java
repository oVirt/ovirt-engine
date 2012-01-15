package org.ovirt.engine.ui.frontend.server.gwt;

import java.util.ArrayList;
import java.util.Random;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.ovirt.engine.core.common.action.LoginUserParameters;
import org.ovirt.engine.core.common.action.LogoutUserParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.core.common.businessentities.VmPoolType;
import org.ovirt.engine.core.common.interfaces.BackendLocal;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.ui.frontend.gwtservices.GenericApiGWTService;
import org.ovirt.engine.ui.genericapi.GenericApiService;

public class GenericApiGWTServiceImpl extends AbstractGWTServiceImpl implements
        GenericApiGWTService {
    static Random r = new Random();
    boolean noBackend = false;

    private static LogCompat log = LogFactoryCompat
            .getLog(GenericApiGWTServiceImpl.class);

    private BackendLocal backend;

    // @EJB(name = "engine/GenericApi/local")
    private static GenericApiService genericApiService;

    @Override
    public void init() throws ServletException {
        log.debug("Initializing servlet!");
    }

    @Override
    public VdcQueryReturnValue RunQuery(VdcQueryType search,
            VdcQueryParametersBase searchParameters) {
        log.debug("Server: RunQuery invoked!");
        searchParameters.setSessionId(getSessionId());
        return getBackend().RunQuery(search, searchParameters);
    }

    @Override
    public VdcQueryReturnValue RunPublicQuery(VdcQueryType queryType,
            VdcQueryParametersBase params) {
        log.debug("Server: RunPublicQuery invoked!");
        return getBackend().RunPublicQuery(queryType, params);
    }

    @Override
    public ArrayList<VdcQueryReturnValue> RunMultipleQueries(
            ArrayList<VdcQueryType> queryTypeList,
            ArrayList<VdcQueryParametersBase> queryParamsList) {
        log.debug("Server: RunMultipleQuery invoked! [amount of queries: " + queryTypeList.size() + "]");

        ArrayList<VdcQueryReturnValue> ret = new ArrayList<VdcQueryReturnValue>();

        if (queryTypeList == null || queryParamsList == null) {
            // TODO: LOG: "queryTypeList and/or queryParamsList is null."
        }

        else if (queryTypeList.size() != queryParamsList.size()) {
            // TODO: LOG:
            // "queryTypeList and queryParamsList don't have the same amount of items."
        }

        else {
            for (int i = 0; i < queryTypeList.size(); i++) {
                ret.add(RunQuery(queryTypeList.get(i), queryParamsList.get(i)));
            }
        }

        for (VdcQueryReturnValue vqrv : ret) {
            log.debug("VdcQueryReturnValue: " + vqrv);
        }

        log.debug("Server: RunMultipleQuery result [amount of queries: " + ret.size() + "]");

        return ret;
    }

    @Override
    public ArrayList<VdcReturnValueBase> RunMultipleActions(VdcActionType actionType,
            ArrayList<VdcActionParametersBase> multipleParams) {
        log.debug("Server: RunMultipleAction invoked! [amount of actions: " + multipleParams.size() + "]");

        for (VdcActionParametersBase params : multipleParams) {
            params.setSessionId(getSessionId());
        }

        ArrayList<VdcReturnValueBase> returnValues = getBackend().RunMultipleActions(actionType, multipleParams);

        return returnValues;
    }

    @Override
    public VdcReturnValueBase RunAction(VdcActionType actionType,
            VdcActionParametersBase params) {
        log.debug("Server: RunAction invoked!");

        params.setSessionId(getSessionId());

        if (noBackend) {
            VdcReturnValueBase rValue = new VdcReturnValueBase();
            rValue.setSucceeded(true);
            return rValue;
        }

        return getBackend().RunAction(actionType, params);
    }

    @Override
    public VdcUser getLoggedInUser() {
        VdcQueryParametersBase queryParams = new VdcQueryParametersBase();
        queryParams.setSessionId(getSessionId());
        queryParams.setHttpSessionId(getSessionId());

        VdcQueryReturnValue vqrv = RunQuery(VdcQueryType.GetUserBySessionId,
                queryParams);

        if (!vqrv.getSucceeded()) {
            return null;
        } else if (vqrv.getSucceeded()) {
            if (vqrv.getReturnValue() == null)
                return null;
            return (VdcUser) vqrv.getReturnValue();
        } else {
            // For unknown reason the result was failed be returned.
            return null;
        }
    }

    @Override
    public VdcReturnValueBase logOff(VdcUser userToLogoff) {
        LogoutUserParameters params = new LogoutUserParameters(userToLogoff.getUserId());
        params.setSessionId(getSessionId());
        VdcReturnValueBase returnValue = getBackend().Logoff(params);
        return returnValue;
    }

    @Override
    public VdcReturnValueBase Login(String userName, String password, String domain) {
        LoginUserParameters params = new LoginUserParameters(userName, password, domain, null, null, null);
        params.setSessionId(getSessionId());
        params.setActionType(VdcActionType.LoginAdminUser);
        VdcReturnValueBase returnValue = getBackend().Login(params);
        return returnValue;
    }

    private static VMStatus getRandomStatus() {
        VMStatus[] status_values = VMStatus.values();
        VMStatus status = status_values[r.nextInt(status_values.length)];
        return status;
    }

    private static VmOsType getRandomOSType() {
        VmOsType[] os_types = VmOsType.values();
        VmOsType os = os_types[r.nextInt(os_types.length)];
        return os;
    }

    private static VmPoolType getRandomVmPoolType() {
        VmPoolType[] pool_types = VmPoolType.values();
        VmPoolType type = pool_types[r.nextInt(pool_types.length)];

        return type;
    }

    public BackendLocal getBackend() {
        return backend;
    }

    @EJB(beanInterface = BackendLocal.class,
            mappedName = "java:global/engine/engine-bll/Backend!org.ovirt.engine.core.common.interfaces.BackendLocal")
    public void setBackend(BackendLocal backend) {
        this.backend = backend;
    }

    private String getSessionId() {
        HttpServletRequest request = this.getThreadLocalRequest();
        HttpSession session = request.getSession();

        log.debug("IP [" + request.getRemoteAddr() + "], Session ID [" + session.getId() + "]");

        return session.getId();
    }
}
