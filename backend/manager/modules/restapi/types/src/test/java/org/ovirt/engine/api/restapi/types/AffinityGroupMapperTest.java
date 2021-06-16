package org.ovirt.engine.api.restapi.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.api.model.AffinityGroup;
import org.ovirt.engine.api.model.AffinityRule;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.Hosts;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.model.Vms;
import org.ovirt.engine.core.common.scheduling.EntityAffinityRule;
import org.ovirt.engine.core.compat.Guid;

public class AffinityGroupMapperTest extends AbstractInvertibleMappingTest<AffinityGroup, org.ovirt.engine.core.common.scheduling.AffinityGroup, org.ovirt.engine.core.common.scheduling.AffinityGroup> {

    public AffinityGroupMapperTest() {
        super(AffinityGroup.class,
                org.ovirt.engine.core.common.scheduling.AffinityGroup.class,
                org.ovirt.engine.core.common.scheduling.AffinityGroup.class);
    }

    @Override
    protected AffinityGroup postPopulate(AffinityGroup model) {
        // Derandomize dependent and fixed values
        model.setEnforcing(model.getVmsRule().isEnforcing());
        model.setPositive(model.getVmsRule().isEnabled() ? model.getVmsRule().isPositive() : null);
        model.getHostsRule().setEnabled(true);
        return super.postPopulate(model);
    }

    @Override
    protected void verify(AffinityGroup model, AffinityGroup transform) {
        assertNotNull(transform);
        assertEquals(model.getName(), transform.getName());
        assertEquals(model.getId(), transform.getId());
        assertEquals(model.getDescription(), transform.getDescription());
        assertEquals(model.getCluster().getId(), transform.getCluster().getId());
        assertEquals(model.isPositive(), transform.isPositive());
        assertEquals(model.isEnforcing(), transform.isEnforcing());
        assertNotNull(transform.getHostsRule());
        assertEquals(model.getHostsRule().isEnabled(), transform.getHostsRule().isEnabled());
        assertEquals(model.getHostsRule().isEnforcing(), transform.getHostsRule().isEnforcing());
        assertEquals(model.getHostsRule().isPositive(), transform.getHostsRule().isPositive());
        assertNotNull(transform.getVmsRule());
        assertEquals(model.getVmsRule().isEnabled(), transform.getVmsRule().isEnabled());
        assertEquals(model.getVmsRule().isEnforcing(), transform.getVmsRule().isEnforcing());

        // The positive field is coupled with the enabled field internally. Disabled
        // group will always return false in the positive field.
        if (transform.getVmsRule().isEnabled()) {
            assertEquals(model.getVmsRule().isPositive(), transform.getVmsRule().isPositive());
        } else {
            assertEquals(false, transform.getVmsRule().isPositive());
        }
    }

    @Test
    public void testVmsRule() {
        AffinityGroup model = new AffinityGroup();
        model.setEnforcing(true);
        model.setPositive(false);

        org.ovirt.engine.core.common.scheduling.AffinityGroup entity =
                new org.ovirt.engine.core.common.scheduling.AffinityGroup();

        AffinityGroupMapper.map(model, entity);

        assertEquals(EntityAffinityRule.NEGATIVE, entity.getVmAffinityRule());
        assertTrue(entity.isVmEnforcing());
        assertTrue(entity.isVmAffinityEnabled());
    }

    @Test
    public void testVmsRuleNeg() {
        AffinityGroup model = new AffinityGroup();
        model.setEnforcing(false);
        model.setPositive(true);

        org.ovirt.engine.core.common.scheduling.AffinityGroup entity =
                new org.ovirt.engine.core.common.scheduling.AffinityGroup();

        AffinityGroupMapper.map(model, entity);

        assertEquals(EntityAffinityRule.POSITIVE, entity.getVmAffinityRule());
        assertFalse(entity.isVmEnforcing());
        assertTrue(entity.isVmAffinityEnabled());
    }

    @Test
    public void testVmsRuleStructure() {
        AffinityGroup model = new AffinityGroup();

        AffinityRule rule = new AffinityRule();
        rule.setEnforcing(true);
        rule.setPositive(false);
        rule.setEnabled(true);

        model.setVmsRule(rule);

        org.ovirt.engine.core.common.scheduling.AffinityGroup entity =
                new org.ovirt.engine.core.common.scheduling.AffinityGroup();

        AffinityGroupMapper.map(model, entity);

        assertEquals(EntityAffinityRule.NEGATIVE, entity.getVmAffinityRule());
        assertTrue(entity.isVmEnforcing());
        assertTrue(entity.isVmAffinityEnabled());
    }

    @Test
    public void testVmsRuleStructureNeg() {
        AffinityGroup model = new AffinityGroup();

        AffinityRule rule = new AffinityRule();
        rule.setEnforcing(false);
        rule.setPositive(true);
        rule.setEnabled(true);

        model.setVmsRule(rule);

        org.ovirt.engine.core.common.scheduling.AffinityGroup entity =
                new org.ovirt.engine.core.common.scheduling.AffinityGroup();

        AffinityGroupMapper.map(model, entity);

        assertEquals(EntityAffinityRule.POSITIVE, entity.getVmAffinityRule());
        assertFalse(entity.isVmEnforcing());
        assertTrue(entity.isVmAffinityEnabled());
    }

    @Test
    public void testVmsRuleStructureDisabled() {
        AffinityGroup model = new AffinityGroup();

        AffinityRule rule = new AffinityRule();
        rule.setEnforcing(true);
        rule.setPositive(false);
        rule.setEnabled(false);

        model.setVmsRule(rule);

        org.ovirt.engine.core.common.scheduling.AffinityGroup entity =
                new org.ovirt.engine.core.common.scheduling.AffinityGroup();

        AffinityGroupMapper.map(model, entity);

        assertEquals(EntityAffinityRule.DISABLED, entity.getVmAffinityRule());
        assertTrue(entity.isVmEnforcing());
        assertFalse(entity.isVmAffinityEnabled());
    }

    @Test
    public void testVmsRuleStructureWins() {
        AffinityGroup model = new AffinityGroup();
        model.setEnforcing(false);
        model.setPositive(false);

        AffinityRule rule = new AffinityRule();
        rule.setEnforcing(true);
        rule.setPositive(true);
        rule.setEnabled(true);

        model.setVmsRule(rule);

        org.ovirt.engine.core.common.scheduling.AffinityGroup entity =
                new org.ovirt.engine.core.common.scheduling.AffinityGroup();

        AffinityGroupMapper.map(model, entity);

        assertEquals(EntityAffinityRule.POSITIVE, entity.getVmAffinityRule());
        assertTrue(entity.isVmEnforcing());
        assertTrue(entity.isVmAffinityEnabled());
    }

    @Test
    public void testHostsRuleStructure() {
        AffinityGroup model = new AffinityGroup();

        AffinityRule rule = new AffinityRule();
        rule.setEnabled(true);
        rule.setEnforcing(true);
        rule.setPositive(false);

        model.setHostsRule(rule);

        org.ovirt.engine.core.common.scheduling.AffinityGroup entity =
                new org.ovirt.engine.core.common.scheduling.AffinityGroup();

        AffinityGroupMapper.map(model, entity);

        assertEquals(EntityAffinityRule.NEGATIVE, entity.getVdsAffinityRule());
        assertTrue(entity.isVdsEnforcing());
    }

    @Test
    public void testHostsRuleStructureNeg() {
        AffinityGroup model = new AffinityGroup();

        AffinityRule rule = new AffinityRule();
        rule.setEnabled(true);
        rule.setEnforcing(false);
        rule.setPositive(true);

        model.setHostsRule(rule);

        org.ovirt.engine.core.common.scheduling.AffinityGroup entity =
                new org.ovirt.engine.core.common.scheduling.AffinityGroup();

        AffinityGroupMapper.map(model, entity);

        assertEquals(EntityAffinityRule.POSITIVE, entity.getVdsAffinityRule());
        assertFalse(entity.isVdsEnforcing());
    }

    @Test
    public void testHostAffinityRestOutput() {
        org.ovirt.engine.core.common.scheduling.AffinityGroup entity =
                new org.ovirt.engine.core.common.scheduling.AffinityGroup();
        entity.setId(Guid.Empty);
        entity.setClusterId(Guid.Empty);
        entity.setVdsEnforcing(false);
        entity.setVdsAffinityRule(EntityAffinityRule.POSITIVE);

        AffinityGroup model = new AffinityGroup();

        AffinityGroupMapper.map(entity, model);

        assertNotNull(model.getHostsRule());
        assertEquals(true, model.getHostsRule().isEnabled());
        assertEquals(true, model.getHostsRule().isPositive());
        assertEquals(false, model.getHostsRule().isEnforcing());
    }

    @Test
    public void testHostAffinityNegRestOutput() {
        org.ovirt.engine.core.common.scheduling.AffinityGroup entity =
                new org.ovirt.engine.core.common.scheduling.AffinityGroup();
        entity.setId(Guid.Empty);
        entity.setClusterId(Guid.Empty);
        entity.setVdsEnforcing(true);
        entity.setVdsAffinityRule(EntityAffinityRule.NEGATIVE);

        AffinityGroup model = new AffinityGroup();

        AffinityGroupMapper.map(entity, model);

        assertNotNull(model.getHostsRule());
        assertEquals(true, model.getHostsRule().isEnabled());
        assertEquals(false, model.getHostsRule().isPositive());
        assertEquals(true, model.getHostsRule().isEnforcing());
    }

    @Test
    public void testHostAffinityDisabledRestOutput() {
        org.ovirt.engine.core.common.scheduling.AffinityGroup entity =
                new org.ovirt.engine.core.common.scheduling.AffinityGroup();
        entity.setId(Guid.Empty);
        entity.setClusterId(Guid.Empty);
        entity.setVdsEnforcing(true);
        entity.setVdsAffinityRule(EntityAffinityRule.DISABLED);

        AffinityGroup model = new AffinityGroup();

        AffinityGroupMapper.map(entity, model);

        assertNotNull(model.getHostsRule());
        assertEquals(false, model.getHostsRule().isEnabled());
        assertEquals(true, model.getHostsRule().isEnforcing());
    }

    @Test
    public void testVmAffinityRestOutput() {
        org.ovirt.engine.core.common.scheduling.AffinityGroup entity =
                new org.ovirt.engine.core.common.scheduling.AffinityGroup();
        entity.setId(Guid.Empty);
        entity.setClusterId(Guid.Empty);
        entity.setVmEnforcing(false);
        entity.setVmAffinityRule(EntityAffinityRule.POSITIVE);

        AffinityGroup model = new AffinityGroup();

        AffinityGroupMapper.map(entity, model);

        assertNotNull(model.getVmsRule());
        assertEquals(true, model.getVmsRule().isEnabled());
        assertEquals(true, model.getVmsRule().isPositive());
        assertEquals(false, model.getVmsRule().isEnforcing());
        assertEquals(true, model.isPositive());
        assertEquals(false, model.isEnforcing());
    }

    @Test
    public void testVmAffinityNegRestOutput() {
        org.ovirt.engine.core.common.scheduling.AffinityGroup entity =
                new org.ovirt.engine.core.common.scheduling.AffinityGroup();
        entity.setId(Guid.Empty);
        entity.setClusterId(Guid.Empty);
        entity.setVmEnforcing(true);
        entity.setVmAffinityRule(EntityAffinityRule.NEGATIVE);

        AffinityGroup model = new AffinityGroup();

        AffinityGroupMapper.map(entity, model);

        assertNotNull(model.getVmsRule());
        assertEquals(true, model.getVmsRule().isEnabled());
        assertEquals(false, model.getVmsRule().isPositive());
        assertEquals(true, model.getVmsRule().isEnforcing());
        assertEquals(false, model.isPositive());
        assertEquals(true, model.isEnforcing());
    }

    @Test
    public void testBrokenRestOutput() {
        org.ovirt.engine.core.common.scheduling.AffinityGroup entity =
                new org.ovirt.engine.core.common.scheduling.AffinityGroup();
        entity.setId(Guid.Empty);
        entity.setClusterId(Guid.Empty);
        entity.setVmEnforcing(false);
        entity.setBroken(true);

        AffinityGroup model = new AffinityGroup();

        AffinityGroupMapper.map(entity, model);

        assertEquals(true, model.isBroken());
    }

    @Test
    public void testBrokenNegRestOutput() {
        org.ovirt.engine.core.common.scheduling.AffinityGroup entity =
                new org.ovirt.engine.core.common.scheduling.AffinityGroup();
        entity.setId(Guid.Empty);
        entity.setClusterId(Guid.Empty);
        entity.setVmEnforcing(false);
        entity.setBroken(false);

        AffinityGroup model = new AffinityGroup();

        AffinityGroupMapper.map(entity, model);

        assertEquals(false, model.isBroken());
    }

    @Test
    public void testVmAffinityDisabledRestOutput() {
        org.ovirt.engine.core.common.scheduling.AffinityGroup entity =
                new org.ovirt.engine.core.common.scheduling.AffinityGroup();
        entity.setId(Guid.Empty);
        entity.setClusterId(Guid.Empty);
        entity.setVmEnforcing(true);
        entity.setVmAffinityRule(EntityAffinityRule.DISABLED);

        AffinityGroup model = new AffinityGroup();

        AffinityGroupMapper.map(entity, model);

        assertNotNull(model.getVmsRule());
        assertEquals(false, model.getVmsRule().isEnabled());
        assertEquals(false, model.getVmsRule().isPositive());
        assertEquals(true, model.getVmsRule().isEnforcing());
        assertNull(model.isPositive());
        assertEquals(true, model.isEnforcing());
    }

    @Test
    public void testVmIds() {
        AffinityGroup model = new AffinityGroup();

        Vm vm = new Vm();
        final Guid vmGuid = Guid.newGuid();
        vm.setId(vmGuid.toString());
        model.setVms(new Vms());
        model.getVms().getVms().add(vm);

        org.ovirt.engine.core.common.scheduling.AffinityGroup entity =
                new org.ovirt.engine.core.common.scheduling.AffinityGroup();

        AffinityGroupMapper.map(model, entity);

        assertEquals(vmGuid, entity.getVmIds().get(0));
    }

    @Test
    public void testVmIdsReplacement() {
        AffinityGroup model = new AffinityGroup();

        Vm vm = new Vm();
        final Guid vmGuid = Guid.newGuid();
        vm.setId(vmGuid.toString());
        model.setVms(new Vms());
        model.getVms().getVms().add(vm);

        org.ovirt.engine.core.common.scheduling.AffinityGroup entity =
                new org.ovirt.engine.core.common.scheduling.AffinityGroup();
        entity.getVdsIds().add(Guid.newGuid());

        AffinityGroupMapper.map(model, entity);

        assertEquals(vmGuid, entity.getVmIds().get(0));
        assertEquals(1, entity.getVmIds().size());
    }

    @Test
    public void testHostIds() {
        AffinityGroup model = new AffinityGroup();

        Host host = new Host();
        final Guid hostGuid = Guid.newGuid();
        host.setId(hostGuid.toString());
        model.setHosts(new Hosts());
        model.getHosts().getHosts().add(host);

        org.ovirt.engine.core.common.scheduling.AffinityGroup entity =
                new org.ovirt.engine.core.common.scheduling.AffinityGroup();

        AffinityGroupMapper.map(model, entity);

        assertEquals(hostGuid, entity.getVdsIds().get(0));
    }

    @Test
    public void testHostIdsReplacement() {
        AffinityGroup model = new AffinityGroup();

        Host host = new Host();
        final Guid hostGuid = Guid.newGuid();
        host.setId(hostGuid.toString());
        model.setHosts(new Hosts());
        model.getHosts().getHosts().add(host);

        org.ovirt.engine.core.common.scheduling.AffinityGroup entity =
                new org.ovirt.engine.core.common.scheduling.AffinityGroup();
        entity.getVdsIds().add(Guid.newGuid());

        AffinityGroupMapper.map(model, entity);

        assertEquals(1, entity.getVdsIds().size());
        assertEquals(hostGuid, entity.getVdsIds().get(0));
    }
}
