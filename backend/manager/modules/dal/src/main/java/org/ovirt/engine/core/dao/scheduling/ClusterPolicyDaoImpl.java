package org.ovirt.engine.core.dao.scheduling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DefaultGenericDao;
import org.ovirt.engine.core.utils.SerializationFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class ClusterPolicyDaoImpl extends DefaultGenericDao<ClusterPolicy, Guid> implements ClusterPolicyDao {

    @Inject
    private PolicyUnitDao policyUnitDao;

    public ClusterPolicyDaoImpl() {
        super("ClusterPolicy");
    }

    @Override
    public void save(ClusterPolicy clusterPolicy) {
        super.save(clusterPolicy);
        List<ClusterPolicyUnit> clusterPolicyUnits = getclusterPolicyUnit(clusterPolicy);
        if (clusterPolicyUnits != null) {
            clusterPolicyUnits.forEach(this::saveClusterPolicyUnit);
        }
    }

    @Override
    public void update(ClusterPolicy clusterPolicy) {
        super.update(clusterPolicy);
        getCallsHandler().executeModification("DeleteClusterPolicyUnitsByClusterPolicyId",
                getCustomMapSqlParameterSource().addValue("id", clusterPolicy.getId()));
        List<ClusterPolicyUnit> clusterPolicyUnits = getclusterPolicyUnit(clusterPolicy);
        if (clusterPolicyUnits != null) {
            clusterPolicyUnits.forEach(this::saveClusterPolicyUnit);
        }
    }

    @Override
    public List<ClusterPolicy> getAll(@NotNull Map<Guid, PolicyUnitType> internalUnitTypes) {
        List<ClusterPolicy> clusterPolicies = super.getAll();
        Map<Guid, ClusterPolicy> map = new HashMap<>();
        for (ClusterPolicy clusterPolicy : clusterPolicies) {
            map.put(clusterPolicy.getId(), clusterPolicy);
        }
        List<ClusterPolicyUnit> clusterPolicyUnits =
                getCallsHandler().executeReadList("GetAllFromClusterPolicyUnits",
                createClusterPolicyUnitRowMapper(),
                getCustomMapSqlParameterSource());
        fillClusterPolicy(map, clusterPolicyUnits, Collections.unmodifiableMap(internalUnitTypes));
        return clusterPolicies;
    }

    @Override
    public ClusterPolicy get(Guid id, @NotNull Map<Guid, PolicyUnitType> internalUnitTypes) {
        ClusterPolicy clusterPolicy = super.get(id);
        if (clusterPolicy == null) {
            return null;
        }
        List<ClusterPolicyUnit> clusterPolicyUnits =
                getCallsHandler().executeReadList("GetClusterPolicyUnitsByClusterPolicyId",
                createClusterPolicyUnitRowMapper(),
                        createIdParameterMapper(id));
        Map<Guid, ClusterPolicy> map = new HashMap<>();
        map.put(clusterPolicy.getId(), clusterPolicy);
        fillClusterPolicy(map, clusterPolicyUnits, Collections.unmodifiableMap(internalUnitTypes));
        return clusterPolicy;
    }

    private void fillClusterPolicy(Map<Guid, ClusterPolicy> map, List<ClusterPolicyUnit> clusterPolicyUnits,
            @NotNull Map<Guid, PolicyUnitType> internalUnitTypes) {
        Map<Guid, PolicyUnitType> policyUnitTypeMap = new HashMap<>(internalUnitTypes);

        for (PolicyUnit policyUnit : policyUnitDao.getAll()) {
            policyUnitTypeMap.put(policyUnit.getId(), policyUnit.getPolicyUnitType());
        }

        for (ClusterPolicyUnit clusterPolicyUnit : clusterPolicyUnits) {
            ClusterPolicy clusterPolicy = map.get(clusterPolicyUnit.getClusterPolicyId());

            if (policyUnitTypeMap.get(clusterPolicyUnit.getPolicyUnitId()) == PolicyUnitType.FILTER) {
                if (clusterPolicy.getFilters() == null) {
                    clusterPolicy.setFilters(new ArrayList<>());
                }
                clusterPolicy.getFilters().add(clusterPolicyUnit.getPolicyUnitId());
                if (clusterPolicyUnit.getFilterSequence() != 0) {
                    if (clusterPolicy.getFilterPositionMap() == null) {
                        clusterPolicy.setFilterPositionMap(new HashMap<>());
                    }
                    clusterPolicy.getFilterPositionMap().put(clusterPolicyUnit.getPolicyUnitId(),
                            clusterPolicyUnit.getFilterSequence());
                }
            }
            if (policyUnitTypeMap.get(clusterPolicyUnit.getPolicyUnitId()) == PolicyUnitType.WEIGHT) {
                if(clusterPolicy.getFunctions() == null){
                    clusterPolicy.setFunctions(new ArrayList<>());
                }
                clusterPolicy.getFunctions().add(new Pair<>(clusterPolicyUnit.getPolicyUnitId(),
                        clusterPolicyUnit.getFactor()));
            }
            if (policyUnitTypeMap.get(clusterPolicyUnit.getPolicyUnitId()) == PolicyUnitType.LOAD_BALANCING) {
                clusterPolicy.setBalance(clusterPolicyUnit.getPolicyUnitId());
            }
        }
    }

    private List<ClusterPolicyUnit> getclusterPolicyUnit(ClusterPolicy entity) {
        Map<Guid, ClusterPolicyUnit> map = new HashMap<>();
        ClusterPolicyUnit unit;
        if (entity.getFilters() != null) {
            for (Guid policyUnitId : entity.getFilters()) {
                unit = getClusterPolicyUnit(entity, policyUnitId, map);
                if (entity.getFilterPositionMap() != null) {
                    Integer position = entity.getFilterPositionMap().get(policyUnitId);
                    unit.setFilterSequence(position != null ? position : 0);
                }
            }
        }
        if (entity.getFunctions() != null) {
            for (Pair<Guid, Integer> pair : entity.getFunctions()) {
                unit = getClusterPolicyUnit(entity, pair.getFirst(), map);
                unit.setFactor(pair.getSecond());
            }
        }
        if (entity.getBalance() != null) {
            getClusterPolicyUnit(entity, entity.getBalance(), map);
        }
        return new ArrayList<>(map.values());
    }

    private void saveClusterPolicyUnit(ClusterPolicyUnit clusterPolicyUnit) {
        getCallsHandler().executeModification("InsertClusterPolicyUnit",
                getClusterPolicyUnitParameterMap(clusterPolicyUnit));
    }

    private MapSqlParameterSource getClusterPolicyUnitParameterMap(ClusterPolicyUnit clusterPolicyUnit) {
        return getCustomMapSqlParameterSource().addValue("cluster_policy_id", clusterPolicyUnit.getClusterPolicyId())
                .addValue("policy_unit_id", clusterPolicyUnit.getPolicyUnitId())
                .addValue("filter_sequence", clusterPolicyUnit.getFilterSequence())
                .addValue("factor", clusterPolicyUnit.getFactor());
    }

    protected RowMapper<ClusterPolicyUnit> createClusterPolicyUnitRowMapper() {
        return (rs, arg1) -> {
            ClusterPolicyUnit unit = new ClusterPolicyUnit();
            unit.setClusterPolicyId(getGuid(rs, "cluster_policy_id"));
            unit.setPolicyUnitId(getGuid(rs, "policy_unit_id"));
            unit.setFilterSequence(rs.getInt("filter_sequence"));
            unit.setFactor(rs.getInt("factor"));
            return unit;
        };
    }

    private ClusterPolicyUnit getClusterPolicyUnit(ClusterPolicy clusterPolicy,
            Guid policyUnitId,
            Map<Guid, ClusterPolicyUnit> map) {
        ClusterPolicyUnit clusterPolicyUnit = map.get(policyUnitId);
        if (clusterPolicyUnit == null) {
            clusterPolicyUnit = new ClusterPolicyUnit(clusterPolicy.getId(), policyUnitId);
            map.put(policyUnitId, clusterPolicyUnit);
        }
        return clusterPolicyUnit;
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(ClusterPolicy entity) {
        return createIdParameterMapper(entity.getId())
                .addValue("name", entity.getName())
                .addValue("description", entity.getDescription())
                .addValue("is_locked", entity.isLocked())
                .addValue("is_default", entity.isDefaultPolicy())
                .addValue("custom_properties",
                        SerializationFactory.getSerializer().serialize(entity.getParameterMap()));
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return getCustomMapSqlParameterSource().addValue("id", id);
    }

    @Override
    protected RowMapper<ClusterPolicy> createEntityRowMapper() {
        return (rs, arg1) -> {
            ClusterPolicy clusterPolicy = new ClusterPolicy();
            clusterPolicy.setId(getGuid(rs, "id"));
            clusterPolicy.setName(rs.getString("name"));
            clusterPolicy.setDescription(rs.getString("description"));
            clusterPolicy.setLocked(rs.getBoolean("is_locked"));
            clusterPolicy.setDefaultPolicy(rs.getBoolean("is_default"));
            clusterPolicy.setParameterMap(SerializationFactory.getDeserializer()
                    .deserializeOrCreateNew(rs.getString("custom_properties"), LinkedHashMap.class));
            return clusterPolicy;
        };
    }

    /**
     * Helper class for cluster's policy units. this class is hidden from users of this dao. mapped into cluster policy
     * class.
     */
    private static class ClusterPolicyUnit {
        Guid clusterPolicyId;
        Guid policyUnitId;
        int filterSequence;
        int factor;

        public ClusterPolicyUnit() {
        }

        public ClusterPolicyUnit(Guid clusterPolicyId, Guid policyUnitId) {
            this.clusterPolicyId = clusterPolicyId;
            this.policyUnitId = policyUnitId;
        }

        public Guid getClusterPolicyId() {
            return clusterPolicyId;
        }

        public void setClusterPolicyId(Guid clusterPolicyId) {
            this.clusterPolicyId = clusterPolicyId;
        }

        public Guid getPolicyUnitId() {
            return policyUnitId;
        }

        public void setPolicyUnitId(Guid policyUnitId) {
            this.policyUnitId = policyUnitId;
        }

        public int getFilterSequence() {
            return filterSequence;
        }

        public void setFilterSequence(int filterSequence) {
            this.filterSequence = filterSequence;
        }

        public int getFactor() {
            return factor;
        }

        public void setFactor(int factor) {
            this.factor = factor;
        }

    }
}
