package org.ovirt.engine.core.bll;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.errors.VdcFault;
import org.ovirt.engine.core.common.queries.IRegisterQueryUpdatedData;
import org.ovirt.engine.core.common.queries.ListIVdcQueryableUpdatedData;
import org.ovirt.engine.core.common.queries.ValueObjectPair;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.utils.linq.DefaultMapper;
import org.ovirt.engine.core.utils.linq.LinqUtils;

public class ListIQueryableQueryData extends QueryData {
    protected java.util.Map<Object, IVdcQueryable> _cache;

    public ListIQueryableQueryData() {
        _cache = Collections.emptyMap();
    }

    @Override
    public IRegisterQueryUpdatedData GetQueryUpdatedDataFromQueryReturnValue(VdcQueryReturnValue QueryReturnValue,
                                                                             RefObject<Boolean> changed) {
        // LINQ FIX 29456
        // Dictionary<object, IVdcQueryable> newData =
        // (QueryReturnValue.ReturnValue as
        // IList).Cast<IVdcQueryable>().ToDictionary(a => a.QueryableId);
        java.util.Map<Object, IVdcQueryable> newData;
        ListIVdcQueryableUpdatedData queryUpdatedData;
        if (QueryReturnValue.getSucceeded()) {
            List<IVdcQueryable> list = (List<IVdcQueryable>) QueryReturnValue.getReturnValue();
            newData = LinqUtils.toMap(list,
                    new DefaultMapper<IVdcQueryable, Object>() {
                        @Override
                        public Object createKey(IVdcQueryable iVdcQueryable) {
                            return iVdcQueryable.getQueryableId();
                        }
                    });
            // LINQ FIX 29456

            // LINQ FIX 29456
            // List<object> removed = _cache.Keys.Except(newData.Keys).ToList();
            Collection removed = CollectionUtils.subtract(_cache.keySet(), newData.keySet());
            // LINQ FIX 29456

            java.util.Map<Object, IVdcQueryable> added = new java.util.LinkedHashMap<Object, IVdcQueryable>();
            java.util.Map<Object, IVdcQueryable> updated = new java.util.LinkedHashMap<Object, IVdcQueryable>();

            for (IVdcQueryable newValue : newData.values()) {
                if (_cache.containsKey(newValue.getQueryableId())) {
                    if (!SingleIQueryableQueryData.AreIVdcQueryablesEqual(_cache.get(newValue.getQueryableId()),
                            newValue)) {
                        updated.put(newValue.getQueryableId(), newValue);
                    }
                } else {
                    added.put(newValue.getQueryableId(), newValue);
                }
            }

            queryUpdatedData = new ListIVdcQueryableUpdatedData(added, removed, updated);

            changed.argvalue = removed.size() > 0 || added.size() > 0 || updated.size() > 0;
            _cache = newData;
        } else {
            _cache = Collections.emptyMap();
            queryUpdatedData = new ListIVdcQueryableUpdatedData();
            queryUpdatedData.setFaulted(new ValueObjectPair(getQueryType(), new VdcFault(new RuntimeException(QueryReturnValue.getExceptionString()))));
            changed.argvalue = Boolean.TRUE;
        }
        return queryUpdatedData;
    }
}
