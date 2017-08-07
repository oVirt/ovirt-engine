package org.ovirt.engine.ui.frontend.server.gwt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.constants.SessionConstants;
import org.ovirt.engine.core.common.interfaces.BackendLocal;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.utils.CorrelationIdTracker;
import org.ovirt.engine.ui.frontend.gwtservices.GenericApiGWTService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.user.client.rpc.SerializationException;

public class GenericApiGWTServiceImpl extends OvirtXsrfProtectedServiceServlet implements GenericApiGWTService {

    private static final long serialVersionUID = 7395780289048030855L;

    private static final Logger log = LoggerFactory.getLogger(GenericApiGWTServiceImpl.class);

    private static final String UI_PREFIX = "UI_"; //$NON-NLS-1$

    private static final String CORRELATION_ID_HEADER = "Correlation-Id"; //$NON-NLS-1$
    private static final Pattern INVALID_CORRELATION_ID_CHARACTERS_RE = Pattern.compile("[^0-9a-zA-Z_-]+"); //$NON-NLS-1$

    private BackendLocal backend;

    @EJB(beanInterface = BackendLocal.class,
            mappedName = "java:global/engine/bll/Backend!org.ovirt.engine.core.common.interfaces.BackendLocal")
    public void setBackend(BackendLocal backend) {
        this.backend = backend;
    }

    public BackendLocal getBackend() {
        return backend;
    }

    private static String filterCorrelationIdCharacters(String correlationId) {
        if (StringUtils.isNotEmpty(correlationId)) {
            correlationId = INVALID_CORRELATION_ID_CHARACTERS_RE.matcher(correlationId).replaceAll("");
            if (StringUtils.isNotEmpty(correlationId)) {
                return correlationId.substring(0, Math.min(correlationId.length(), 36));
            }
        }
        return null;
    }

    private static String getCorrelationId(HttpServletRequest request) {
        String correlationId = filterCorrelationIdCharacters(request.getHeader(CORRELATION_ID_HEADER));
        if (StringUtils.isEmpty(correlationId)) {
            correlationId = UUID.randomUUID().toString();
        }
        return correlationId;
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String correlationId = getCorrelationId(request);
        CorrelationIdTracker.setCorrelationId(correlationId);
        response.addHeader(CORRELATION_ID_HEADER, correlationId);
        super.service(request, response);
    }

    @Override
    public QueryReturnValue runQuery(QueryType search,
            QueryParametersBase searchParameters) {
        log.debug("Server: RunQuery invoked!"); //$NON-NLS-1$
        debugQuery(search, searchParameters);
        searchParameters.setSessionId(getEngineSessionId());
        if (searchParameters.getCorrelationId() == null) {
            searchParameters.setCorrelationId(CorrelationIdTracker.getCorrelationId());
        }
        return getBackend().runQuery(search, searchParameters);
    }

    @Override
    public QueryReturnValue runPublicQuery(QueryType queryType,
            QueryParametersBase params) {
        log.debug("Server: runPublicQuery invoked! '{}'", queryType); //$NON-NLS-1$
        if (params.getCorrelationId() == null) {
            params.setCorrelationId(CorrelationIdTracker.getCorrelationId());
        }
        debugQuery(queryType, params);
        return getBackend().runPublicQuery(queryType, params);
    }

    @Override
    public ArrayList<QueryReturnValue> runMultipleQueries(
            ArrayList<QueryType> queryTypeList,
            ArrayList<QueryParametersBase> queryParamsList) {
        int size = queryTypeList == null ? 0 : queryTypeList.size();
        log.debug("Server: RunMultipleQuery invoked! [amount of queries: {}]", size); //$NON-NLS-1$
        ArrayList<QueryReturnValue> ret = new ArrayList<>();

        if (queryTypeList != null
                && queryParamsList != null
                && queryTypeList.size() == queryParamsList.size()) {
            String correlationId = CorrelationIdTracker.getCorrelationId();
            for (int i = 0; i < queryTypeList.size(); i++) {
                if (queryParamsList.get(i).getCorrelationId() == null) {
                    queryParamsList.get(i).setCorrelationId(correlationId);
                }
                debugQuery(queryTypeList.get(i), queryParamsList.get(i));
                ret.add(runQuery(queryTypeList.get(i), queryParamsList.get(i)));
            }
        } else {
            log.error(
                    "Wrong multi query usage: the query types and parameters must not be null " //$NON-NLS-1$
                            + "or be equally long. Types length '{}' vs params length '{}'",    //$NON-NLS-1$
                    queryTypeList == null ? 0 : queryTypeList.size(),
                    queryParamsList == null ? 0 : queryParamsList.size()
            );
        }

        for (QueryReturnValue vqrv : ret) {
            log.debug("QueryReturnValue '{}'", vqrv); //$NON-NLS-1$
        }

        log.debug("Server: RunMultipleQuery result [amount of queries: {}]", ret.size()); //$NON-NLS-1$

        return ret;
    }

    @Override
    public List<ActionReturnValue> runMultipleActions(ActionType actionType,
            ArrayList<ActionParametersBase> multipleParams, boolean isRunOnlyIfAllValidationPass) {
        return runMultipleActions(actionType, multipleParams, isRunOnlyIfAllValidationPass, false);
    }

    @Override
    public List<ActionReturnValue> runMultipleActions(ActionType actionType,
            ArrayList<ActionParametersBase> multipleParams, boolean isRunOnlyIfAllValidationPass, boolean isWaitForResult) {
        log.debug("Server: RunMultipleAction invoked! [amount of actions: {}]", multipleParams.size()); //$NON-NLS-1$

        String correlationId = CorrelationIdTracker.getCorrelationId();
        for (ActionParametersBase params : multipleParams) {
            params.setSessionId(getEngineSessionId());
            if (params.getCorrelationId() == null) {
                params.setCorrelationId(correlationId);
            }
        }

        List<ActionReturnValue> returnValues =
                getBackend().runMultipleActions(actionType, multipleParams, isRunOnlyIfAllValidationPass, isWaitForResult);

        return returnValues;
    }

    @Override
    public ActionReturnValue runAction(ActionType actionType,
            ActionParametersBase params) {
        log.debug("Server: RunAction invoked!"); //$NON-NLS-1$
        debugAction(actionType, params);
        params.setSessionId(getEngineSessionId());
        if (params.getCorrelationId() == null) {
            params.setCorrelationId(CorrelationIdTracker.getCorrelationId());
        }

        return getBackend().runAction(actionType, params);
    }

    private String getEngineSessionId() {
        return (String) getSession().getAttribute(SessionConstants.HTTP_SESSION_ENGINE_SESSION_ID_KEY);
    }

    private HttpSession getSession() {
        HttpServletRequest request = this.getThreadLocalRequest();
        HttpSession session = request.getSession();

        log.debug("IP '{}', Session ID '{}'", request.getRemoteAddr(), session.getId()); //$NON-NLS-1$

        return session;
    }

    @Override
    protected void doUnexpectedFailure(Throwable error) {
        // If the user is using a version of the application different to what
        // the server expects the names of the RPC serialization policy files
        // will not match, and in that case GWT just sends the exception to the
        // log, which is not very user or admin friendly, so we replace that
        // with a more friendly message:
        if (error instanceof SerializationException) {
            error = new SerializationException(
                "Can't find the serialization policy file. " + //$NON-NLS-1$
                "This probably means that the user has an old " + //$NON-NLS-1$
                "version of the application loaded in the " + //$NON-NLS-1$
                "browser. To solve the issue the user needs " + //$NON-NLS-1$
                "to close the browser and open it again, so " + //$NON-NLS-1$
                "that the application is reloaded.", //$NON-NLS-1$
                error
            );
        }

        // Now that we replaced the message let GWT do what it uses to do:
        super.doUnexpectedFailure(error);
    }

    private void debugQuery(QueryType queryType, QueryParametersBase parameters) {
        log.debug("Query type '{}', Parameters '{}'", queryType, parameters); //$NON-NLS-1$
    }

    private void debugAction(ActionType actionType, ActionParametersBase params) {
        log.debug("Action type '{}', Parameters '{}'", actionType, params); //$NON-NLS-1$
    }

}
