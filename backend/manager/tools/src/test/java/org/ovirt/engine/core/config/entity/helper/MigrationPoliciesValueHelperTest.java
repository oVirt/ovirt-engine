package org.ovirt.engine.core.config.entity.helper;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MigrationPoliciesValueHelperTest {

    private MigrationPoliciesValueHelper helper = new MigrationPoliciesValueHelper();

    @Test
    public void emptyIsNotValid() {
        assertEquals(helper.validate(null, "").isOk(), false);
    }

    @Test
    public void incorrectJsonNotValid() {
        assertEquals(helper.validate(null, "this is not a valid json").isOk(), false);
    }

    @Test
    public void notAnyValidJsonIsValid() {
        assertEquals(helper.validate(null, "{}").isOk(), false);
    }

    @Test
    public void listOfMigrationPoliciesIsValid() {
        assertEquals(helper.validate(null, "[{\"id\":{\"uuid\":\"80554327-0569-496b-bdeb-fcbbf52b827b\"},\"maxMigrations\":2,\"name\":\"Safe but not may not converge\",\"description\":\"A safe policy which in typical situations lets the VM converge. The user of the VM should not notice any significant slowdown of the VM. If the VM is not converging for a longer time, the migration will be aborted.\",\"config\":{\"convergenceItems\":[{\"stallingLimit\":1,\"convergenceItem\":{\"action\":\"setDowntime\",\"params\":[\"150\"]}},{\"stallingLimit\":2,\"convergenceItem\":{\"action\":\"setDowntime\",\"params\":[\"200\"]}},{\"stallingLimit\":3,\"convergenceItem\":{\"action\":\"setDowntime\",\"params\":[\"300\"]}},{\"stallingLimit\":4,\"convergenceItem\":{\"action\":\"setDowntime\",\"params\":[\"400\"]}},{\"stallingLimit\":6,\"convergenceItem\":{\"action\":\"setDowntime\",\"params\":[\"500\"]}}],\"initialItems\":[{\"action\":\"setDowntime\",\"params\":[\"100\"]}],\"lastItems\":[{\"action\":\"abort\",\"params\":[]}]}},{\"id\":{\"uuid\":\"80554327-0569-496b-bdeb-fcbbf52b827c\"},\"maxMigrations\":1,\"name\":\"Should converge but guest may notice a pause\",\"description\":\"A safe policy which makes also a highly loaded VM converge in most situations. On the other hand, the user may notice a slowdown. If the VM is still not converging, the migration is aborted.\",\"config\":{\"convergenceItems\":[{\"stallingLimit\":1,\"convergenceItem\":{\"action\":\"setDowntime\",\"params\":[\"150\"]}},{\"stallingLimit\":2,\"convergenceItem\":{\"action\":\"setDowntime\",\"params\":[\"200\"]}},{\"stallingLimit\":3,\"convergenceItem\":{\"action\":\"setDowntime\",\"params\":[\"300\"]}},{\"stallingLimit\":4,\"convergenceItem\":{\"action\":\"setDowntime\",\"params\":[\"400\"]}},{\"stallingLimit\":6,\"convergenceItem\":{\"action\":\"setDowntime\",\"params\":[\"500\"]}}],\"initialItems\":[{\"action\":\"setDowntime\",\"params\":[\"100\"]}],\"lastItems\":[{\"action\":\"setDowntime\",\"params\":[\"5000\"]},{\"action\":\"abort\",\"params\":[]}]}}]").isOk(), true);
    }
}
