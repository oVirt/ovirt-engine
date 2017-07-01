package org.ovirt.engine.core.bll.executor;

import javax.enterprise.inject.Alternative;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.SearchParameters;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.exception.HystrixRuntimeException;

@Alternative
public class HystrixBackendQueryExecutor implements BackendQueryExecutor {

    @Override
    public QueryReturnValue execute(final QueriesCommandBase<?> query, final QueryType queryType) {
        String key = queryType.name();
        if (queryType == QueryType.Search) {
            key = key + ((SearchParameters) query.getParameters()).getSearchTypeValue().name();
        }
        final HystrixCommand.Setter setter = HystrixSettings.setter(key);
        final HystrixCommand<QueryReturnValue> hystrixCommand = new HystrixCommand<QueryReturnValue>(setter) {
            @Override
            protected QueryReturnValue run() throws Exception {
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

        QueryReturnValue returnValue;

        public QueryFailedException(QueryReturnValue returnValue) {
            this.returnValue = returnValue;
        }

        public QueryReturnValue getReturnValue() {
            return returnValue;
        }
    }
}
