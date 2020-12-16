package org.ovirt.engine.core.sso.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.ovirt.engine.api.extensions.ExtKey;
import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.api.extensions.ExtUUID;
import org.ovirt.engine.api.extensions.aaa.Authz;
import org.ovirt.engine.api.extensions.aaa.Authz.QueryFilterOperator;
import org.ovirt.engine.api.extensions.aaa.Authz.QueryFilterRecord;

/**
 * This class is a helper class to transform searchbackend related search queries to Extension API structures. This
 * class is required as the mechanism of searchbackend is tightly coupled, and heavily relies on strings. In a future
 * rewrite of search mechanism, it is strongly advised to have the conversions to Extension API done at searchbackend
 * itself (construct the extensions API structures instead of constructing a search string).
 *
 */
public class SearchParsingUtils {

    private static final String USERS_QUERY_PREFIX = "(&($USER_ACCOUNT_TYPE)";

    private static final String GROUPS_QUERY_PREFIX = "(&($LDAP_GROUP_CATEGORY)";

    private static Map<String, ExtKey> attributesToKeys = new HashMap<>();

    static {
        attributesToKeys.put("$GIVENNAME", Authz.PrincipalRecord.FIRST_NAME);
        attributesToKeys.put("$SAMACCOUNTNAME", Authz.PrincipalRecord.NAME);
        attributesToKeys.put("$USER_ACCOUNT_NAME", Authz.PrincipalRecord.DISPLAY_NAME);
        attributesToKeys.put("sn", Authz.PrincipalRecord.LAST_NAME);
        attributesToKeys.put("$SN", Authz.PrincipalRecord.LAST_NAME);
        attributesToKeys.put("$DEPARTMENT", Authz.PrincipalRecord.DEPARTMENT);
        attributesToKeys.put("$TITLE", Authz.PrincipalRecord.TITLE);
        attributesToKeys.put("$CN", Authz.GroupRecord.NAME);
        attributesToKeys.put("$PRINCIPAL_NAME", Authz.PrincipalRecord.NAME);
    }

    public static ExtMap generateQueryMap(Collection<String> ids, ExtUUID queryEntity) {
        ExtMap result = new ExtMap().mput(
                Authz.InvokeKeys.QUERY_ENTITY,
                queryEntity);
        ExtKey key = queryEntity.equals(Authz.QueryEntity.GROUP) ? Authz.GroupRecord.ID : Authz.PrincipalRecord.ID;
        List<ExtMap> filter = ids.stream().map(id -> createMapForKeyAndValue(key, id)).collect(Collectors.toList());
        result.mput(
                QueryFilterRecord.OPERATOR,
                QueryFilterOperator.OR)
                .mput(
                        QueryFilterRecord.FILTER,
                        filter);
        return result;
    }

    public static ExtMap generateQueryMap(String query, ExtUUID queryEntity) {
        String queryPrefix = getQueryPrefixByEntity(queryEntity);
        ExtMap result = new ExtMap();
        result.mput(
                Authz.InvokeKeys.QUERY_ENTITY,
                queryEntity);
        List<ExtMap> filter = new ArrayList<>();
        if (query.indexOf(queryPrefix) == 0) {
            query = query.substring(queryPrefix.length(), query.length() - 1);
            int openingBracketIndex = -1;
            int closingBracketIndex = -1;
            while (true) {
                boolean negate = false;
                openingBracketIndex = query.indexOf('(', closingBracketIndex + 1);
                if (openingBracketIndex == -1) {
                    break;
                }
                closingBracketIndex = query.indexOf(')', openingBracketIndex + 1);
                if (query.charAt(openingBracketIndex + 1) == '|') {
                    openingBracketIndex += 2;
                }
                if (query.charAt(openingBracketIndex + 1) == '!') {
                    openingBracketIndex += 2;
                    negate = true;
                }
                String[] betweenBrackets = query.substring(openingBracketIndex + 1, closingBracketIndex).split("=");
                String field = betweenBrackets[0];
                String value = betweenBrackets[1];
                if (negate) {
                    filter.add(new ExtMap().mput(
                            QueryFilterRecord.OPERATOR,
                            QueryFilterOperator.NOT)
                            .mput(
                                    QueryFilterRecord.FILTER,
                                    Arrays.asList(
                                            createMapForKeyAndValue(field, value))));

                } else {
                    filter.add(createMapForKeyAndValue(field, value));

                }
            }
            result.mput(
                    QueryFilterRecord.OPERATOR,
                    QueryFilterOperator.OR)
                    .mput(
                            QueryFilterRecord.FILTER,
                            filter);
        }
        return result;
    }

    private static String getQueryPrefixByEntity(ExtUUID queryEntity) {
        String queryPrefix = Authz.QueryEntity.PRINCIPAL.equals(queryEntity) ? USERS_QUERY_PREFIX : GROUPS_QUERY_PREFIX;
        return queryPrefix;
    }

    public static Collection<? extends Collection<String>> getIdsBatches(final ExtMap context,
            final Collection<String> ids) {

        List<String> idsList = new ArrayList<>(ids);
        int chunk = context.<Integer> get(Authz.ContextKeys.QUERY_MAX_FILTER_SIZE, 100);
        List<List<String>> batchOfIdsList = new ArrayList<>();
        for (int counter = 0; counter < ids.size(); counter = counter + chunk) {
            batchOfIdsList
                    .add(idsList.subList(counter, counter + chunk > idsList.size() ? idsList.size() : counter + chunk));
        }
        return batchOfIdsList;
    }

    private static ExtMap createMapForKeyAndValue(String field, String value) {
        return createMapForKeyAndValue(attributesToKeys.get(field), value);
    }

    private static ExtMap createMapForKeyAndValue(ExtKey key, String value) {
        return new ExtMap().mput(
                QueryFilterRecord.OPERATOR,
                QueryFilterOperator.EQ)
                .mput(
                        QueryFilterRecord.KEY,
                        key)
                .mput(
                        key,
                        value);
    }

}
