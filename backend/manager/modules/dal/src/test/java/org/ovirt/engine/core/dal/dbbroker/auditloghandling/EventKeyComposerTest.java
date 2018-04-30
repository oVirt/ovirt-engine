package org.ovirt.engine.core.dal.dbbroker.auditloghandling;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.compat.Guid;

public class EventKeyComposerTest {
    @Test
    public void composeObjectIdForAnEmptyAuditLogForInjectedImpl() {
        composeObjectIdForAnEmptyAuditLog(new AuditLogableBase());
    }

    @Test
    public void composeObjectIdForAnEmptyAuditLogForNonInjectedImpl() {
        composeObjectIdForAnEmptyAuditLog(new AuditLogableImpl());
    }

    private void composeObjectIdForAnEmptyAuditLog(AuditLogable logable) {
        String composedObjectId = EventKeyComposer.composeObjectId(logable, AuditLogType.UNASSIGNED);
        assertEquals("type=UNASSIGNED," +
                     "sd=," +
                     "dc=," +
                     "user=," +
                     "cluster=," +
                     "vds=," +
                     "vm=," +
                     "template=," +
                     "customId=", composedObjectId);
    }

    @Test
    public void composeObjectIdWithAuditLogableBase() {
        AuditLogableBase event = new AuditLogableBase();
        createEventAndAssert(event);
    }

    @Test
    public void composeObjectIdWithAuditLogableImpl() {
        AuditLogable event = new AuditLogableImpl();
        createEventAndAssert(event);
    }

    private void createEventAndAssert(AuditLogable event) {
        event.setStorageDomainId(Guid.createGuidFromString("11111111-1111-1111-1111-111111111111"));
        event.setStoragePoolId(Guid.createGuidFromString("22222222-2222-2222-2222-222222222222"));
        event.setUserId(Guid.createGuidFromString("33333333-3333-3333-3333-333333333333"));
        event.setClusterId(Guid.createGuidFromString("44444444-4444-4444-4444-444444444444"));
        event.setVdsId(Guid.createGuidFromString("55555555-5555-5555-5555-555555555555"));
        event.setVmId(Guid.createGuidFromString("66666666-6666-6666-6666-666666666666"));
        event.setVmTemplateId(Guid.createGuidFromString("77777777-7777-7777-7777-777777777777"));
        event.setCustomId("123456");
        String composedObjectId = EventKeyComposer.composeObjectId(event, AuditLogType.USER_RUN_VM);

        assertEquals("type=USER_RUN_VM," +
                "sd=11111111-1111-1111-1111-111111111111," +
                "dc=22222222-2222-2222-2222-222222222222," +
                "user=33333333-3333-3333-3333-333333333333," +
                "cluster=44444444-4444-4444-4444-444444444444," +
                "vds=55555555-5555-5555-5555-555555555555," +
                "vm=66666666-6666-6666-6666-666666666666," +
                "template=77777777-7777-7777-7777-777777777777," +
                "customId=123456", composedObjectId);
    }
}
