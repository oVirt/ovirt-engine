package org.ovirt.engine.core.bll.executor;

import javax.enterprise.inject.Alternative;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.exception.HystrixRuntimeException;

@Alternative
public class HystrixBackendQueryExecutor implements BackendQueryExecutor {

    @Override
    public VdcQueryReturnValue execute(final QueriesCommandBase<?> query, final VdcQueryType queryType) {
        String key = queryType.name();
        if (queryType == VdcQueryType.Search) {
            key = key + ((SearchParameters) query.getParameters()).getSearchTypeValue().name();
        }
        final HystrixCommand.Setter setter = HystrixSettings.setter(key);
        final HystrixCommand<VdcQueryReturnValue> hystrixCommand = new HystrixCommand<VdcQueryReturnValue>(setter) {
            @Override
            protected VdcQueryReturnValue run() throws Exception {
                query.execute();
                if (query.getQueryReturnValue().getSucceeded()) {
                    return query.getQueryReturnValue();
                }
                // throw this so that hystrix can see that this command failed
                throw new QueryFailedException(query.getQueryReturnValue());
            }
        };
        try {
            return hystrixCommand.execute();
        } catch (HystrixRuntimeException e) {
            // only thrown for hystrix, so catch it and proceed normally
            if (e.getCause() instanceof QueryFailedException) {
                return ((QueryFailedException) e.getCause()).getReturnValue();
            }
            throw e;
        }
    }

    private static class QueryFailedException extends Exception {

        VdcQueryReturnValue returnValue;

        public QueryFailedException(VdcQueryReturnValue returnValue) {
            this.returnValue = returnValue;
        }

        public VdcQueryReturnValue getReturnValue() {
            return returnValue;
        }
    }
}
