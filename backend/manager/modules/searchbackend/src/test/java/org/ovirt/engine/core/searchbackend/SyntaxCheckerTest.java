package org.ovirt.engine.core.searchbackend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ovirt.engine.core.common.businessentities.Tags;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.interfaces.ITagsHandler;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;
import org.ovirt.engine.core.utils.MockedConfig;

@ExtendWith(MockConfigExtension.class)
public class SyntaxCheckerTest {

    private static final String TAG_NAME = "'tag1'";
    private static final String TAG_NAME_WITH_CHILDREN = "'tag1','all'";

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(
                MockConfigDescriptor.of(ConfigValues.DBPagingType, "Range"),
                MockConfigDescriptor.of(ConfigValues.DBSearchTemplate, "SELECT * FROM (%2$s) %1$s) as T1 %3$s"),
                MockConfigDescriptor.of(ConfigValues.DBPagingSyntax, "OFFSET (%1$s -1) LIMIT %2$s"),
                MockConfigDescriptor.of(ConfigValues.DBI18NPrefix, "")
        );
    }

    public boolean contains(SyntaxContainer res, String item) {
        return Arrays.asList(res.getCompletionArray()).contains(item);
    }

    @BeforeEach
    public void setup() {
        BaseConditionFieldAutoCompleter.tagsHandler = mock(ITagsHandler.class);
        Tags tags = new Tags();
        tags.setTagName(TAG_NAME);
        when(BaseConditionFieldAutoCompleter.tagsHandler.getTagByTagName(any())).thenReturn(tags);
        when(BaseConditionFieldAutoCompleter.tagsHandler.getTagNamesAndChildrenNamesByRegExp(any()))
                .thenReturn(TAG_NAME_WITH_CHILDREN);
    }

    /**
     * Test the following where each word should be the completion for the earlier portion Vms : Events =
     */
    @Test
    public void testVMCompletion() {
        SyntaxChecker chkr = new SyntaxChecker();
        SyntaxContainer res = chkr.getCompletion("");
        assertTrue(contains(res, "Vms"), "Vms");
        res = chkr.getCompletion("V");
        assertTrue(contains(res, "Vms"), "Vms2");
        res = chkr.getCompletion("Vms");
        assertTrue(contains(res, ":"), ":");
        res = chkr.getCompletion("Vms : ");
        assertTrue(contains(res, "Events"), "Events");
        res = chkr.getCompletion("Vms : Events");
        assertTrue(contains(res, "="), "=");
    }

    /**
     * Test the following where each word should be the completion for the earlier portion Host : sortby migrating_vms
     * asc
     */
    @Test
    public void testHostCompletion() {
        SyntaxChecker chkr = new SyntaxChecker();
        SyntaxContainer res = chkr.getCompletion("");
        assertTrue(contains(res, "Hosts"), "Hosts");
        res = chkr.getCompletion("H");
        assertTrue(contains(res, "Hosts"), "Hots2");
        res = chkr.getCompletion("Host");
        assertTrue(contains(res, ":"), ":");
        res = chkr.getCompletion("Host : ");
        assertTrue(contains(res, "sortby"), "sortby");
        res = chkr.getCompletion("Host : sortby");
        assertTrue(contains(res, "migrating_vms"), "migrating_vms");
        res = chkr.getCompletion("Host : sortby migrating_vms");
        assertTrue(contains(res, "asc"), "asc");
    }

    @Test
    @MockedConfig("mockConfigurationForPagPhraseWrong")
    public void testGetPagPhraseWrong() {
        SyntaxChecker chkr = new SyntaxChecker();
        SyntaxContainer res = new SyntaxContainer("");
        res.setMaxCount(0);

        // check wrong config values
        assertEquals("", chkr.getPagePhrase(res, "1"));
    }

    public static Stream<MockConfigDescriptor<?>> mockConfigurationForPagPhraseWrong() {
        return Stream.concat(mockConfiguration(),
                Stream.of(
                        MockConfigDescriptor.of(ConfigValues.DBPagingType, "wrongPageType"),
                        MockConfigDescriptor.of(ConfigValues.DBPagingSyntax, "wrongPageSyntax"))
        );
    }


    @Test
    @MockedConfig("mockConfigurationForPagPhrase")
    public void testGetPagPhrase() {
        SyntaxChecker chkr = new SyntaxChecker();
        SyntaxContainer res = new SyntaxContainer("");
        res.setMaxCount(0);

        // check valid config values
        assertNotEquals("", chkr.getPagePhrase(res, "1"));
    }

    public static Stream<MockConfigDescriptor<?>> mockConfigurationForPagPhrase() {
        return Stream.concat(mockConfiguration(),
                Stream.of(
                        MockConfigDescriptor.of(ConfigValues.DBPagingType, "Range"),
                        MockConfigDescriptor.of(ConfigValues.DBPagingSyntax, " WHERE RowNum BETWEEN %1$s AND %2$s"))
        );
    }

    @Test
    public void testHost() {
        testValidSql("Host: sortby cpu_usage desc",
                "SELECT * FROM ((SELECT  vds.* FROM  vds  )  ORDER BY usage_cpu_percent DESC NULLS LAST,vds_name ASC ) as T1 OFFSET (1 -1) LIMIT 0");
        // Before: 19ms
        // "SELECT * FROM (SELECT * FROM vds WHERE ( vds_id IN (SELECT vds_with_tags.vds_id FROM  vds_with_tags   WHERE  vds_with_tags.vds_name LIKE 'test1' ))  ORDER BY usage_cpu_percent DESC NULLS LAST,vds_name ASC ) as T1 OFFSET (1 -1) LIMIT 0"
        // Current: 5ms
        testValidSql("Host: name =\"test1\" sortby cpu_usage desc",
                "SELECT * FROM ((SELECT  vds.* FROM  vds   WHERE  vds.vds_name LIKE test1 )  ORDER BY usage_cpu_percent DESC NULLS LAST,vds_name ASC ) as T1 OFFSET (1 -1) LIMIT 0");
        // Before: 17ms
        // "SELECT * FROM (SELECT * FROM vds WHERE ( vds_id IN (SELECT vds_with_tags.vds_id FROM  vds_with_tags   WHERE  vds_with_tags.usage_cpu_percent > 80 ))  ORDER BY usage_cpu_percent DESC NULLS LAST,vds_name ASC ) as T1 OFFSET (1 -1) LIMIT 0"
        // Current: 5ms
        testValidSql("Host: CPU_USAGE > 80 sortby cpu_usage desc",
                "SELECT * FROM ((SELECT  vds.* FROM  vds   WHERE  vds.usage_cpu_percent > 80 )  ORDER BY usage_cpu_percent DESC NULLS LAST,vds_name ASC ) as T1 OFFSET (1 -1) LIMIT 0");
        // Before: 25ms
        // "SELECT * FROM (SELECT * FROM vds WHERE ( vds_id IN (SELECT vds_with_tags.vds_id FROM  vds_with_tags   LEFT OUTER JOIN vdc_users_with_tags ON vds_with_tags.vds_id=vdc_users_with_tags.vm_guid    WHERE  vdc_users_with_tags.name LIKE user1 ))  ORDER BY usage_cpu_percent DESC NULLS LAST,vds_name ASC ) as T1 OFFSET (1 -1) LIMIT 0"
        // Current: 10ms
        testValidSql("Host: user.name = \"user1\" sortby cpu_usage desc",
                "SELECT * FROM ((SELECT  distinct  vds.* FROM  vds   LEFT OUTER JOIN vdc_users_with_tags ON vds.vds_id=vdc_users_with_tags.vm_guid    WHERE  vdc_users_with_tags.name LIKE user1 )  ORDER BY usage_cpu_percent DESC NULLS LAST,vds_name ASC ) as T1 OFFSET (1 -1) LIMIT 0");
        // Before: 63ms
        // "SELECT * FROM (SELECT * FROM vds WHERE ( storage_pool_id IN (SELECT storage_pool_id FROM storage_domains WHERE  storage_domains.storage_name LIKE 'pool1'))  ORDER BY usage_cpu_percent DESC NULLS LAST,vds_name ASC ) as T1 OFFSET (1 -1) LIMIT 0"
        // Current: 68ms
        testValidSql("Host: STORAGE.name = \"sd1\" sortby cpu_usage desc",
                "SELECT * FROM (SELECT * FROM vds WHERE ( vds_id IN (SELECT distinct vds_with_tags.vds_id FROM  vds_with_tags   LEFT OUTER JOIN storage_domains_with_hosts_view ON vds_with_tags.storage_id=storage_domains_with_hosts_view.id    WHERE  storage_domains_with_hosts_view.storage_name LIKE sd1 ))  ORDER BY usage_cpu_percent DESC NULLS LAST,vds_name ASC ) as T1 OFFSET (1 -1) LIMIT 0");
        // Before: 23ms
        // "SELECT * FROM (SELECT * FROM vds WHERE ( vds_id IN (SELECT vds_with_tags.vds_id FROM  vds_with_tags   LEFT OUTER JOIN audit_log ON vds_with_tags.vds_id=audit_log.vds_id    WHERE (  audit_log.severity = '2'  AND  vds_with_tags.usage_cpu_percent > 80  )))  ORDER BY usage_cpu_percent DESC NULLS LAST,vds_name ASC ) as T1 OFFSET (1 -1) LIMIT 0"
        // Current: 9ms
        testValidSql("Host: EVENT.severity=error and CPU_USAGE > 80 sortby cpu_usage desc",
                "SELECT * FROM ((SELECT  distinct  vds.* FROM  vds   LEFT OUTER JOIN audit_log ON vds.vds_id=audit_log.vds_id    WHERE (  audit_log.severity = '2'  AND  vds.usage_cpu_percent > 80  ))  ORDER BY usage_cpu_percent DESC NULLS LAST,vds_name ASC ) as T1 OFFSET (1 -1) LIMIT 0");
        testValidSql("Host: EVENT.severity=error and tag=tag1 sortby cpu_usage desc",
                "SELECT * FROM (SELECT * FROM vds WHERE ( vds_id IN (SELECT distinct vds_with_tags.vds_id FROM  vds_with_tags   LEFT OUTER JOIN audit_log ON vds_with_tags.vds_id=audit_log.vds_id    WHERE (  audit_log.severity = '2'  AND  vds_with_tags.tag_name IN ('tag1','all')  )))  ORDER BY usage_cpu_percent DESC NULLS LAST,vds_name ASC ) as T1 OFFSET (1 -1) LIMIT 0");
        testValidSql("Host: tag=\"tag1\"",
                "SELECT * FROM (SELECT * FROM vds WHERE ( vds_id IN (SELECT distinct vds_with_tags.vds_id FROM  vds_with_tags   WHERE  vds_with_tags.tag_name IN ('tag1','all') ))  ORDER BY vds_name ASC ) as T1 OFFSET (1 -1) LIMIT 0");
        // Before: 22ms
        // "SELECT * FROM (SELECT * FROM vds WHERE ( vds_id IN (SELECT vds_with_tags.vds_id FROM  vds_with_tags   LEFT OUTER JOIN vms_with_tags ON vds_with_tags.vds_id=vms_with_tags.run_on_vds    WHERE  vms_with_tags.vm_name LIKE 'vm1' ))  ORDER BY vds_name ASC ) as T1 OFFSET (1 -1) LIMIT 0"
        // Current: 11ms
        testValidSql("Host: vm.name=\"vm1\"",
                "SELECT * FROM ((SELECT  distinct  vds.* FROM  vds   LEFT OUTER JOIN vms_with_tags ON vds.vds_id=vms_with_tags.run_on_vds    WHERE  vms_with_tags.vm_name LIKE vm1 )  ORDER BY vds_name ASC ) as T1 OFFSET (1 -1) LIMIT 0");
        testValidSql("Vms: cluster = default and Templates.name = template_1 and Storage.name = storage_1",
                "SELECT * FROM (SELECT * FROM vms WHERE ( vm_guid IN (SELECT distinct vms_with_tags.vm_guid FROM  vms_with_tags   LEFT OUTER JOIN vm_templates_storage_domain ON vms_with_tags.vmt_guid=vm_templates_storage_domain.vmt_guid    LEFT OUTER JOIN storage_domains_with_hosts_view ON vms_with_tags.storage_id=storage_domains_with_hosts_view.id    WHERE ( (  vms_with_tags.cluster_name LIKE default  AND  vm_templates_storage_domain.name LIKE template\\_1  ) AND  storage_domains_with_hosts_view.storage_name LIKE storage\\_1  )))  ORDER BY vm_name ASC ) as T1 OFFSET (1 -1) LIMIT 0");
        testValidSql("Vms: cluster = default and Templates.name = template_1 and Storage.name = storage_1 and Vnic.network_name = vnic_1",
                "SELECT * FROM (SELECT * FROM vms WHERE ( vm_guid IN (SELECT distinct vms_with_tags.vm_guid FROM  vms_with_tags   LEFT OUTER JOIN vm_templates_storage_domain ON vms_with_tags.vmt_guid=vm_templates_storage_domain.vmt_guid    LEFT OUTER JOIN storage_domains_with_hosts_view ON vms_with_tags.storage_id=storage_domains_with_hosts_view.id    LEFT OUTER JOIN vm_interface_view ON vms_with_tags.vm_guid=vm_interface_view.vm_guid    WHERE ( ( (  vms_with_tags.cluster_name LIKE default  AND  vm_templates_storage_domain.name LIKE template\\_1  ) AND  storage_domains_with_hosts_view.storage_name LIKE storage\\_1  ) AND  vm_interface_view.network_name LIKE vnic\\_1  )))  ORDER BY vm_name ASC ) as T1 OFFSET (1 -1) LIMIT 0");
    }

    @Test
    public void testHosts() {
        testValidSql("Hosts: sortby cpu_usage desc",
                "SELECT * FROM ((SELECT  vds.* FROM  vds  )  ORDER BY usage_cpu_percent DESC NULLS LAST,vds_name ASC ) as T1 OFFSET (1 -1) LIMIT 0");
    }

    @Test
    public void testVm() {
        // Before - 184ms
        // "SELECT * FROM (SELECT * FROM vms WHERE ( vm_guid IN (SELECT vms_with_tags.vm_guid FROM  vms_with_tags   WHERE ( ( ( ( ( ( (  vms_with_tags.status = '1'  OR  vms_with_tags.status = '2'  ) OR  vms_with_tags.status = '6'  ) OR  vms_with_tags.status = '9'  ) OR  vms_with_tags.status = '10'  ) OR  vms_with_tags.status = '16'  ) OR  vms_with_tags.status = '4'  ) OR  vms_with_tags.status = '7'  )))  ORDER BY usage_cpu_percent DESC NULLS LAST,vm_name ASC ) as T1 OFFSET (1 -1) LIMIT 0"
        // Current - 15ms
        testValidSql("Vm: status=Up or status=PoweringUp or status=MigratingTo or status=WaitForLaunch or status=RebootInProgress or status=PoweringDown or status=Paused or status=Unknown sortby cpu_usage desc",
                "SELECT * FROM ((SELECT  vms.* FROM  vms   WHERE ( ( ( ( ( ( (  vms.status = '1'  OR  vms.status = '2'  ) OR  vms.status = '6'  ) OR  vms.status = '9'  ) OR  vms.status = '10'  ) OR  vms.status = '16'  ) OR  vms.status = '4'  ) OR  vms.status = '7'  ))  ORDER BY usage_cpu_percent DESC NULLS LAST,vm_name ASC ) as T1 OFFSET (1 -1) LIMIT 0");
        // Before - 20ms
        // "SELECT * FROM (SELECT * FROM vms WHERE ( vm_guid IN (SELECT vms_with_tags.vm_guid FROM  vms_with_tags  ))  ORDER BY vm_name ASC ) as T1 OFFSET (1 -1) LIMIT 0"
        // Current - 16ms
        testValidSql("Vm:",
                "SELECT * FROM ((SELECT  vms.* FROM  vms  )  ORDER BY vm_name ASC ) as T1 OFFSET (1 -1) LIMIT 0");
        // Before - 203ms
        // "SELECT * FROM (SELECT * FROM vms WHERE ( vm_guid IN (SELECT vms_with_tags.vm_guid FROM  vms_with_tags   LEFT OUTER JOIN vdc_users_with_tags ON vms_with_tags.vm_guid=vdc_users_with_tags.vm_guid    WHERE  vdc_users_with_tags.name LIKE user1 ))  ORDER BY vm_name ASC ) as T1 OFFSET (1 -1) LIMIT 0"
        // Current - 15ms
        testValidSql("Vm: user.name = user1",
                "SELECT * FROM ((SELECT  distinct  vms.* FROM  vms   LEFT OUTER JOIN vdc_users_with_tags ON vms.vm_guid=vdc_users_with_tags.vm_guid    WHERE  vdc_users_with_tags.name LIKE user1 )  ORDER BY vm_name ASC ) as T1 OFFSET (1 -1) LIMIT 0");
        testValidSql("Vm: user.name = \"user1\" and user.tag=\"tag1\"",
                "SELECT * FROM (SELECT * FROM vms WHERE ( vm_guid IN (SELECT distinct vms_with_tags.vm_guid FROM  vms_with_tags   LEFT OUTER JOIN vdc_users_with_tags ON vms_with_tags.vm_guid=vdc_users_with_tags.vm_guid    WHERE (  vdc_users_with_tags.name LIKE user1  AND  vdc_users_with_tags.tag_name IN ('tag1','all')  )))  ORDER BY vm_name ASC ) as T1 OFFSET (1 -1) LIMIT 0");

        // Used to validate that searching values not in fields search all fields
        testValidSql("Vm: mac=00:1a:4a:d4:53:94",
                "SELECT * FROM (SELECT * FROM vms WHERE ( vm_guid IN (SELECT distinct vms_with_tags.vm_guid FROM  vms_with_tags   WHERE  (  vms_with_tags.cluster_compatibility_version LIKE '%mac=00:1a:4a:d4:53:94%' OR  vms_with_tags.cluster_name LIKE '%mac=00:1a:4a:d4:53:94%' OR  vms_with_tags.custom_cpu_name LIKE '%mac=00:1a:4a:d4:53:94%' OR  vms_with_tags.custom_emulated_machine LIKE '%mac=00:1a:4a:d4:53:94%' OR  vms_with_tags.description LIKE '%mac=00:1a:4a:d4:53:94%' OR  vms_with_tags.free_text_comment LIKE '%mac=00:1a:4a:d4:53:94%' OR  vms_with_tags.guest_cur_user_name LIKE '%mac=00:1a:4a:d4:53:94%' OR  vms_with_tags.namespace LIKE '%mac=00:1a:4a:d4:53:94%' OR  vms_with_tags.quota_name LIKE '%mac=00:1a:4a:d4:53:94%' OR  vms_with_tags.run_on_vds_name LIKE '%mac=00:1a:4a:d4:53:94%' OR  vms_with_tags.storage_pool_name LIKE '%mac=00:1a:4a:d4:53:94%' OR  vms_with_tags.tag_name LIKE '%mac=00:1a:4a:d4:53:94%' OR  vms_with_tags.vm_fqdn LIKE '%mac=00:1a:4a:d4:53:94%' OR  vms_with_tags.vm_host LIKE '%mac=00:1a:4a:d4:53:94%' OR  vms_with_tags.vm_ip LIKE '%mac=00:1a:4a:d4:53:94%' OR  vms_with_tags.vm_name LIKE '%mac=00:1a:4a:d4:53:94%' OR  vms_with_tags.vm_pool_name LIKE '%mac=00:1a:4a:d4:53:94%' ) ))  ORDER BY vm_name ASC ) as T1 OFFSET (1 -1) LIMIT 0");
        // Testing that in case that function is used in the ORDER BY clause then it is converted with a computed field
        testValidSql("Vms: SORTBY IP DESC",
                "SELECT * FROM ((SELECT  vms.* FROM  vms  )  ORDER BY vm_ip_inet_array DESC NULLS LAST,vm_name ASC ) as T1 OFFSET (1 -1) LIMIT 0");
        // Testing searching Vms with cluster and Host properties
        testValidSql("Vms: Cluster = L0_Group_3 and Host = f17-h29*",
               "SELECT * FROM (SELECT * FROM vms WHERE ( vm_guid IN (SELECT distinct vms_with_tags.vm_guid FROM  vms_with_tags   LEFT OUTER JOIN vds_with_tags ON vms_with_tags.run_on_vds=vds_with_tags.vds_id    WHERE (  vms_with_tags.cluster_name LIKE L0\\_Group\\_3  AND  (  vds_with_tags.cluster_name LIKE '%f17-h29%%' OR  vds_with_tags.cpu_model LIKE '%f17-h29%%' OR  vds_with_tags.free_text_comment LIKE '%f17-h29%%' OR  vds_with_tags.host_name LIKE '%f17-h29%%' OR  vds_with_tags.software_version LIKE '%f17-h29%%' OR  vds_with_tags.storage_pool_name LIKE '%f17-h29%%' OR  vds_with_tags.tag_name LIKE '%f17-h29%%' OR  vds_with_tags.vds_name LIKE '%f17-h29%%' )  )))  ORDER BY vm_name ASC ) as T1 OFFSET (1 -1) LIMIT 0");

    }

    @Test
    public void testVms() {
        testValidSql("Vms:",
                "SELECT * FROM ((SELECT  vms.* FROM  vms  )  ORDER BY vm_name ASC ) as T1 OFFSET (1 -1) LIMIT 0");
        testValidSql("Vms: storage.name = 111",
                "SELECT * FROM (SELECT * FROM vms WHERE ( vm_guid IN (SELECT distinct vms_with_tags.vm_guid FROM  vms_with_tags   LEFT OUTER JOIN storage_domains_with_hosts_view ON vms_with_tags.storage_id=storage_domains_with_hosts_view.id    WHERE  storage_domains_with_hosts_view.storage_name LIKE 111 ))  ORDER BY vm_name ASC ) as T1 OFFSET (1 -1) LIMIT 0");
        testValidSql("Vm: template.name = temp1",
                "SELECT * FROM ((SELECT  distinct  vms.* FROM  vms   LEFT OUTER JOIN vm_templates_storage_domain ON vms.vmt_guid=vm_templates_storage_domain.vmt_guid    WHERE  vm_templates_storage_domain.name LIKE temp1 )  ORDER BY vm_name ASC ) as T1 OFFSET (1 -1) LIMIT 0");
    }

    @Test
    public void testEvent() {
        testValidSql("Event: ",
                "SELECT * FROM ((SELECT  audit_log.* FROM  audit_log   WHERE not deleted)  ORDER BY audit_log_id DESC ) as T1 OFFSET (1 -1) LIMIT 0");
        testValidSql("Event: severity=error ",
                "SELECT * FROM ((SELECT  audit_log.* FROM  audit_log   WHERE  audit_log.severity = '2'  AND not deleted)  ORDER BY audit_log_id DESC ) as T1 OFFSET (1 -1) LIMIT 0");
        testValidSql("Event: severity=alert ",
                "SELECT * FROM ((SELECT  audit_log.* FROM  audit_log   WHERE  audit_log.severity = '10'  AND not deleted)  ORDER BY audit_log_id DESC ) as T1 OFFSET (1 -1) LIMIT 0");
        // Before: 11ms
        // "SELECT * FROM (SELECT * FROM audit_log WHERE ( audit_log_id > 0 and audit_log_id IN (SELECT audit_log.audit_log_id FROM  audit_log   LEFT OUTER JOIN vds_with_tags ON audit_log.vds_id=vds_with_tags.vds_id    WHERE  vds_with_tags.vds_name LIKE host1 ) and not deleted)  ORDER BY audit_log_id DESC ) as T1 OFFSET (1 -1) LIMIT 0"
        // Current: 9ms
        testValidSql("Event: host.name = \"host1\" ",
                "SELECT * FROM ((SELECT  audit_log.* FROM  audit_log   LEFT OUTER JOIN (SELECT distinct vds_id, vds_name FROM vds_with_tags) vds_with_tags_temp ON audit_log.vds_id=vds_with_tags_temp.vds_id    WHERE  vds_with_tags_temp.vds_name LIKE host1  AND not deleted)  ORDER BY audit_log_id DESC ) as T1 OFFSET (1 -1) LIMIT 0");
    }

    @Test
    public void testEvents() {
        testValidSql("Events: ",
                "SELECT * FROM ((SELECT  audit_log.* FROM  audit_log   WHERE not deleted)  ORDER BY audit_log_id DESC ) as T1 OFFSET (1 -1) LIMIT 0");
        testValidSql("Events: Templates.name=Blank",
                "SELECT * FROM (SELECT * FROM audit_log WHERE ( audit_log_id IN (SELECT distinct audit_log.audit_log_id FROM  audit_log   LEFT OUTER JOIN vms_with_tags ON audit_log.vm_id=vms_with_tags.vm_guid    LEFT OUTER JOIN vm_templates_storage_domain ON vms_with_tags.vmt_guid=vm_templates_storage_domain.vmt_guid    WHERE  vm_templates_storage_domain.name LIKE Blank  AND not deleted))  ORDER BY audit_log_id DESC ) as T1 OFFSET (1 -1) LIMIT 0");

    }

    @Test
    public void testTemplate() {
        testValidSql("Template: ",
                "SELECT * FROM ((SELECT distinct vm_templates_view.* FROM  vm_templates_view  )  ORDER BY name ASC ) as T1 OFFSET (1 -1) LIMIT 0");
        testValidSql("Template: hosts.name = fake1",
                "SELECT * FROM ((SELECT distinct vm_templates_view.* FROM  vm_templates_view   LEFT OUTER JOIN vms_with_tags ON vm_templates_view.vmt_guid=vms_with_tags.vmt_guid    LEFT OUTER JOIN vds_with_tags ON vms_with_tags.run_on_vds=vds_with_tags.vds_id    WHERE  vds_with_tags.vds_name LIKE fake1 )  ORDER BY name ASC ) as T1 OFFSET (1 -1) LIMIT 0");
        testValidSql("Templates: storage.name = 111",
                "SELECT * FROM (SELECT * FROM vm_templates_view WHERE ( vmt_guid IN (SELECT distinct vm_templates_storage_domain.vmt_guid FROM  vm_templates_storage_domain   LEFT OUTER JOIN vms_with_tags ON vm_templates_storage_domain.vmt_guid=vms_with_tags.vmt_guid    LEFT OUTER JOIN storage_domains_with_hosts_view ON vm_templates_storage_domain.storage_id=storage_domains_with_hosts_view.id    WHERE  storage_domains_with_hosts_view.storage_name LIKE 111 ))  ORDER BY name ASC ) as T1 OFFSET (1 -1) LIMIT 0");
    }

    @Test
    public void testTemplates() {
        testValidSql("Templates: ",
                "SELECT * FROM ((SELECT distinct vm_templates_view.* FROM  vm_templates_view  )  ORDER BY name ASC ) as T1 OFFSET (1 -1) LIMIT 0");
    }

    @Test
    public void testTemplateUsersUserName() {
        testValidSql("Templates: Users.usrname = *",
                "SELECT * FROM ((SELECT distinct vm_templates_view.* FROM  vm_templates_view   LEFT OUTER JOIN vms_with_tags ON vm_templates_view.vmt_guid=vms_with_tags.vmt_guid    LEFT OUTER JOIN vdc_users_with_tags ON vms_with_tags.vm_guid=vdc_users_with_tags.vm_guid    WHERE  vdc_users_with_tags.username LIKE % )  ORDER BY name ASC ) as T1 OFFSET (1 -1) LIMIT 0");
    }

    @Test
    public void testTemplateUsersAnyField() {
        testValidSql("Templates: Users = *",
                "SELECT * FROM (SELECT * FROM vm_templates_view WHERE ( vmt_guid IN (SELECT distinct vm_templates_storage_domain.vmt_guid FROM  vm_templates_storage_domain   LEFT OUTER JOIN vms_with_tags ON vm_templates_storage_domain.vmt_guid=vms_with_tags.vmt_guid    LEFT OUTER JOIN vdc_users_with_tags ON vms_with_tags.vm_guid=vdc_users_with_tags.vm_guid    WHERE  (  vdc_users_with_tags.department LIKE '%%%' OR  vdc_users_with_tags.domain LIKE '%%%' OR  vdc_users_with_tags.name LIKE '%%%' OR  vdc_users_with_tags.surname LIKE '%%%' OR  vdc_users_with_tags.tag_name LIKE '%%%' OR  vdc_users_with_tags.username LIKE '%%%' OR  vdc_users_with_tags.vm_pool_name LIKE '%%%' ) ))  ORDER BY name ASC ) as T1 OFFSET (1 -1) LIMIT 0");
    }

    @Test
    public void testUser() {
        testValidSql("User:",
                "SELECT * FROM ((SELECT  vdc_users.* FROM  vdc_users  )  ORDER BY name ASC ) as T1 OFFSET (1 -1) LIMIT 0");

        testValidSql("User: host.name=\"host1\"",
                "SELECT * FROM ((SELECT  distinct  vdc_users.* FROM  vdc_users   LEFT OUTER JOIN vms_with_tags ON vdc_users.vm_guid=vms_with_tags.vm_guid    LEFT OUTER JOIN vds_with_tags ON vms.run_on_vds=vds_with_tags.vds_id    WHERE  vds_with_tags.vds_name LIKE host1 )  ORDER BY name ASC ) as T1 OFFSET (1 -1) LIMIT 0");
    }

    @Test
    public void testUsers() {
        testValidSql("Users:",
                "SELECT * FROM ((SELECT  vdc_users.* FROM  vdc_users  )  ORDER BY name ASC ) as T1 OFFSET (1 -1) LIMIT 0");
    }

    @Test
    public void testUsersTypeUser() {
        testValidSql("Users:type=user",
                "SELECT * FROM ((SELECT  vdc_users.* FROM  vdc_users   WHERE  vdc_users.user_group = user )  ORDER BY name ASC ) as T1 OFFSET (1 -1) LIMIT 0");
    }

    @Test
    public void testUsersWithTags() {
        testValidSql("Users:type=user tag=foo",
                "SELECT * FROM (SELECT * FROM vdc_users WHERE ( user_id IN (SELECT distinct vdc_users_with_tags.user_id FROM  vdc_users_with_tags   WHERE  vdc_users_with_tags.user_group = user  AND  vdc_users_with_tags.tag_name IN ('tag1','all') ))  ORDER BY name ASC ) as T1 OFFSET (1 -1) LIMIT 0");
    }

    @Test
    public void testUsersWithVms() {
        testValidSql("Users:type=user vm.id=foo",
                "SELECT * FROM (SELECT * FROM vdc_users WHERE ( user_id IN (SELECT distinct vdc_users_with_tags.user_id FROM  vdc_users_with_tags   LEFT OUTER JOIN vms_with_tags ON vdc_users_with_tags.vm_guid=vms_with_tags.vm_guid    WHERE  vdc_users_with_tags.user_group = user  AND  vms_with_tags.vm_guid = foo ))  ORDER BY name ASC ) as T1 OFFSET (1 -1) LIMIT 0");
    }

    @Test
    public void testGroup() {
        testValidSql("Group:",
                "SELECT * FROM ((SELECT  ad_groups.* FROM  ad_groups  )  ORDER BY name ASC ) as T1 OFFSET (1 -1) LIMIT 0");
        testValidSql("Group: name=group1",
                "SELECT * FROM ((SELECT  ad_groups.* FROM  ad_groups   WHERE  ad_groups.name LIKE group1 )  ORDER BY name ASC ) as T1 OFFSET (1 -1) LIMIT 0");
    }

    @Test
    public void testGroups() {
        testValidSql("Groups:",
                "SELECT * FROM ((SELECT  ad_groups.* FROM  ad_groups  )  ORDER BY name ASC ) as T1 OFFSET (1 -1) LIMIT 0");
    }

    @Test
    public void testPool() {
        testValidSql("Pool: ",
                "SELECT * FROM ((SELECT distinct vm_pools_full_view.* FROM  vm_pools_full_view  )  ORDER BY vm_pool_name ASC ) as T1 OFFSET (1 -1) LIMIT 0");
    }

    @Test
    public void testPools() {
        testValidSql("Pools: ",
                "SELECT * FROM ((SELECT distinct vm_pools_full_view.* FROM  vm_pools_full_view  )  ORDER BY vm_pool_name ASC ) as T1 OFFSET (1 -1) LIMIT 0");
    }

    @Test
    public void testCluster() {
        // Before: 7ms
        // "SELECT * FROM (SELECT * FROM cluster_view WHERE ( cluster_id IN (SELECT cluster_storage_domain.cluster_id FROM  cluster_storage_domain  ))  ORDER BY name ASC ) as T1 OFFSET (1 -1) LIMIT 0"
        // Current: 1ms
        testValidSql("Cluster: ",
                "SELECT * FROM ((SELECT  cluster_view.* FROM  cluster_view  )  ORDER BY name ASC) as T1 OFFSET (1 -1) LIMIT 0");
        testValidSql("Cluster: storage.name = 111",
                "SELECT * FROM (SELECT * FROM cluster_view WHERE ( cluster_id IN (SELECT distinct cluster_storage_domain.cluster_id FROM  cluster_storage_domain   LEFT OUTER JOIN storage_domains_with_hosts_view ON cluster_storage_domain.storage_id=storage_domains_with_hosts_view.id    WHERE  storage_domains_with_hosts_view.storage_name LIKE 111 ))  ORDER BY name ASC) as T1 OFFSET (1 -1) LIMIT 0");
    }

    @Test
    public void testClusters() {
        // Before: 7ms
        // "SELECT * FROM (SELECT * FROM cluster_view WHERE ( cluster_id IN (SELECT cluster_storage_domain.cluster_id FROM  cluster_storage_domain  ))  ORDER BY name ASC ) as T1 OFFSET (1 -1) LIMIT 0"
        // Current: 1ms
        testValidSql("Clusters: ",
                "SELECT * FROM ((SELECT  cluster_view.* FROM  cluster_view  )  ORDER BY name ASC) as T1 OFFSET (1 -1) LIMIT 0");
    }

    @Test
    public void testDatacenter() {
        // Before: 2ms
        // "SELECT * FROM (SELECT * FROM storage_pool WHERE ( id IN (SELECT storage_pool_with_storage_domain.id FROM  storage_pool_with_storage_domain  ))  ORDER BY name,name ASC ) as T1 OFFSET (1 -1) LIMIT 0"
        // Current: 1ms
        testValidSql("DataCenter: sortby name",
                "SELECT * FROM ((SELECT distinct storage_pool.* FROM  storage_pool  )  ORDER BY name ASC NULLS FIRST) as T1 OFFSET (1 -1) LIMIT 0");
        testValidSql("DataCenter: Clusters.name =Default",
                "SELECT * FROM ((SELECT distinct storage_pool.* FROM  storage_pool   LEFT OUTER JOIN cluster_storage_domain ON storage_pool.id=cluster_storage_domain.storage_pool_id    WHERE  cluster_storage_domain.name LIKE Default )  ORDER BY name ASC ) as T1 OFFSET (1 -1) LIMIT 0");
    }

    @Test
    public void testStorage() {
        testValidSql("Storage: ",
                "SELECT * FROM ((SELECT distinct storage_domains_for_search.* FROM  storage_domains_for_search  )  ORDER BY storage_name ASC ) as T1 OFFSET (1 -1) LIMIT 0");
        testValidSql("Storage: datacenter = Default",
                "SELECT * FROM ((SELECT distinct storage_domains_for_search.* FROM  storage_domains_for_search   WHERE Default LIKE ANY(string_to_array(storage_domains_for_search.storage_pool_name::text, ',')))  ORDER BY storage_name ASC ) as T1 OFFSET (1 -1) LIMIT 0");
        testValidSql("Storage: host.name = fake1",
                "SELECT * FROM ((SELECT distinct storage_domains_for_search.* FROM  storage_domains_for_search   LEFT OUTER JOIN vds_with_tags ON storage_domains_for_search.id=vds_with_tags.storage_id    WHERE  vds_with_tags.vds_name LIKE fake1 )  ORDER BY storage_name ASC ) as T1 OFFSET (1 -1) LIMIT 0");
        testValidSql("Storage: datacenter != Default and datacenter != DC42",
                "SELECT * FROM ((SELECT distinct storage_domains_for_search.* FROM  storage_domains_for_search   WHERE ( NOT Default  LIKE ANY(string_to_array(storage_domains_for_search.storage_pool_name::text, ',')) AND NOT DC42  LIKE ANY(string_to_array(storage_domains_for_search.storage_pool_name::text, ',')) ))  ORDER BY storage_name ASC ) as T1 OFFSET (1 -1) LIMIT 0");
    }

    @Test
    public void testStorages() {
        testValidSql("Storages: ",
                "SELECT * FROM ((SELECT distinct storage_domains_for_search.* FROM  storage_domains_for_search  )  ORDER BY storage_name ASC ) as T1 OFFSET (1 -1) LIMIT 0");
    }

    @Test
    public void testDisk() {
        // Before: 280ms
        // "SELECT * FROM (SELECT * FROM all_disks WHERE ( disk_id IN (SELECT all_disks.disk_id FROM  all_disks  ))  ORDER BY disk_alias ASC, disk_id ASC ) as T1 OFFSET (1 -1) LIMIT 0"
        // Current: 12ms
        testValidSql("disk: ",
                "SELECT * FROM ((SELECT distinct all_disks.* FROM  all_disks  )  ORDER BY disk_alias ASC, disk_id ASC ) as T1 OFFSET (1 -1) LIMIT 0");
        testValidSql("disk: alias=vm1",
                "SELECT * FROM ((SELECT distinct all_disks.* FROM  all_disks   WHERE  all_disks.disk_alias LIKE vm1 )  ORDER BY disk_alias ASC, disk_id ASC ) as T1 OFFSET (1 -1) LIMIT 0");
    }

    @Test
    public void testDisks() {
        // Before: 280ms
        // "SELECT * FROM (SELECT * FROM all_disks WHERE ( disk_id IN (SELECT all_disks.disk_id FROM  all_disks  ))  ORDER BY disk_alias ASC, disk_id ASC ) as T1 OFFSET (1 -1) LIMIT 0"
        // Current: 12ms
        testValidSql("disks: ",
                "SELECT * FROM ((SELECT distinct all_disks.* FROM  all_disks  )  ORDER BY disk_alias ASC, disk_id ASC ) as T1 OFFSET (1 -1) LIMIT 0");
    }

    @Test
    public void testVolume() {
        testValidSql("volume: ",
                "SELECT * FROM ((SELECT distinct gluster_volumes_view.* FROM  gluster_volumes_view  )  ORDER BY vol_name ASC ) as T1 OFFSET (1 -1) LIMIT 0");
        testValidSql("volume: name=volume1",
                "SELECT * FROM ((SELECT distinct gluster_volumes_view.* FROM  gluster_volumes_view   WHERE  gluster_volumes_view.vol_name LIKE volume1 )  ORDER BY vol_name ASC ) as T1 OFFSET (1 -1) LIMIT 0");
    }

    @Test
    public void testVolumes() {
        testValidSql("volumes: ",
                "SELECT * FROM ((SELECT distinct gluster_volumes_view.* FROM  gluster_volumes_view  )  ORDER BY vol_name ASC ) as T1 OFFSET (1 -1) LIMIT 0");
    }

    @Test
    public void testQuota() {
        testValidSql("quota: ",
                "SELECT * FROM ((SELECT distinct quota_view.* FROM  quota_view  )  ORDER BY quota_name ASC) as T1 OFFSET (1 -1) LIMIT 0");
        testValidSql("quota: STORAGEPOOLNAME=pool",
                "SELECT * FROM ((SELECT distinct quota_view.* FROM  quota_view   WHERE  quota_view.storage_pool_name LIKE pool )  ORDER BY quota_name ASC) as T1 OFFSET (1 -1) LIMIT 0");
    }

    @Test
    public void testQuotas() {
        testValidSql("quota: ",
                "SELECT * FROM ((SELECT distinct quota_view.* FROM  quota_view  )  ORDER BY quota_name ASC) as T1 OFFSET (1 -1) LIMIT 0");
    }

    @Test
    public void testNetwork() {
        // Before: 63ms
        // "SELECT * FROM (SELECT * FROM network_view WHERE ( id IN (SELECT network_view.id FROM  network_view  ))  ORDER BY storage_pool_name ASC, name ASC) as T1 OFFSET (1 -1) LIMIT 0"
        // Current: 1.5ms
        testValidSql("network: ",
                "SELECT * FROM ((SELECT distinct network_view.* FROM  network_view  )  ORDER BY storage_pool_name ASC, name ASC) as T1 OFFSET (1 -1) LIMIT 0");
        // Before: 5ms
        // "SELECT * FROM (SELECT * FROM network_view WHERE ( id IN (SELECT network_view.id FROM  network_view   LEFT OUTER JOIN network_cluster_view ON network_view.id=network_cluster_view.network_id    WHERE (  network_cluster_view.network_name LIKE 'cluster1'  AND  network_view.name LIKE 'network1'  )))  ORDER BY storage_pool_name ASC, name ASC) as T1 OFFSET (1 -1) LIMIT 0"
        // Current: 4ms
        testValidSql("network: CLUSTER_NETWORK.network_name=cluster1 and name=network1",
                "SELECT * FROM ((SELECT distinct network_view.* FROM  network_view   LEFT OUTER JOIN network_cluster_view ON network_view.id=network_cluster_view.network_id    WHERE (  network_cluster_view.network_name LIKE cluster1  AND  network_view.name LIKE network1  ))  ORDER BY storage_pool_name ASC, name ASC) as T1 OFFSET (1 -1) LIMIT 0");
    }

    @Test
    public void testNetworks() {
        // Before: 63ms
        // "SELECT * FROM (SELECT * FROM network_view WHERE ( id IN (SELECT network_view.id FROM  network_view  ))  ORDER BY storage_pool_name ASC, name ASC) as T1 OFFSET (1 -1) LIMIT 0"
        // Current: 1.5ms
        testValidSql("networks: ",
                "SELECT * FROM ((SELECT distinct network_view.* FROM  network_view  )  ORDER BY storage_pool_name ASC, name ASC) as T1 OFFSET (1 -1) LIMIT 0");
    }

    @Test
    public void testProvider() {
        // Before: 1.5ms
        // "SELECT * FROM (SELECT * FROM providers WHERE ( id IN (SELECT providers.id FROM  providers  ))  ORDER BY name ASC) as T1 OFFSET (1 -1) LIMIT 0"
        // Current: 1ms
        testValidSql("provider: ",
                "SELECT * FROM ((SELECT  providers.* FROM  providers  )  ORDER BY name ASC) as T1 OFFSET (1 -1) LIMIT 0");
        // Before: 1.2ms
        // "SELECT * FROM (SELECT * FROM providers WHERE ( id IN (SELECT providers.id FROM  providers   WHERE  providers.name LIKE 'prov1' ))  ORDER BY name ASC) as T1 OFFSET (1 -1) LIMIT 0"
        // Current: 0.7ms
        testValidSql("provider: name=\"prov1\"",
                "SELECT * FROM ((SELECT  providers.* FROM  providers   WHERE  providers.name LIKE prov1 )  ORDER BY name ASC) as T1 OFFSET (1 -1) LIMIT 0");
    }

    @Test
    public void testProviders() {
        // Before: 1.5ms
        // "SELECT * FROM (SELECT * FROM providers WHERE ( id IN (SELECT providers.id FROM  providers  ))  ORDER BY name ASC) as T1 OFFSET (1 -1) LIMIT 0"
        // Current: 1ms
        testValidSql("providers: ",
                "SELECT * FROM ((SELECT  providers.* FROM  providers  )  ORDER BY name ASC) as T1 OFFSET (1 -1) LIMIT 0");
    }

    @Test
    public void testInstanceType() {
        testValidSql("instancetype: ",
                "SELECT * FROM ((SELECT distinct instance_types_view.* FROM  instance_types_view  )  ORDER BY name ASC ) as T1 OFFSET (1 -1) LIMIT 0");
    }

    @Test
    public void testInstanceTypes() {
        testValidSql("instancetype: ",
                "SELECT * FROM ((SELECT distinct instance_types_view.* FROM  instance_types_view  )  ORDER BY name ASC ) as T1 OFFSET (1 -1) LIMIT 0");
    }

    @Test
    public void testImageType() {
        testValidSql("imagetype: ",
                "SELECT * FROM ((SELECT distinct image_types_view.* FROM  image_types_view  )  ORDER BY name ASC ) as T1 OFFSET (1 -1) LIMIT 0");
    }


    @Test
    public void testImageTypes() {
        testValidSql("imagetypes: ",
                "SELECT * FROM ((SELECT distinct image_types_view.* FROM  image_types_view  )  ORDER BY name ASC ) as T1 OFFSET (1 -1) LIMIT 0");
    }

    @Test
    public void testVmWithTags() {
        testValidSql("VMs:tag=all",
                "SELECT * FROM (SELECT * FROM vms WHERE ( vm_guid IN (SELECT distinct vms_with_tags.vm_guid FROM  vms_with_tags   WHERE  vms_with_tags.tag_name IN ('tag1','all') ))  ORDER BY vm_name ASC ) as T1 OFFSET (1 -1) LIMIT 0");
    }

    @Test
    public void testSearchThatContainsColumnNamePrefix() {
        testValidSql("Cluster:namepref*",
                "SELECT * FROM (SELECT * FROM cluster_view WHERE ( cluster_id IN (SELECT distinct cluster_storage_domain.cluster_id FROM  cluster_storage_domain   WHERE  (  cluster_storage_domain.cpu_name LIKE '%namepref%%' OR  cluster_storage_domain.description LIKE '%namepref%%' OR  cluster_storage_domain.free_text_comment LIKE '%namepref%%' OR  cluster_storage_domain.name LIKE '%namepref%%' ) ))  ORDER BY name ASC) as T1 OFFSET (1 -1) LIMIT 0");
    }

    @Test
    public void testVnicProfile() {
        testValidSql("VnicProfile: ",
            "SELECT * FROM ((SELECT distinct vnic_profiles_view.* FROM  vnic_profiles_view  )  ORDER BY name ASC) as T1 OFFSET (1 -1) LIMIT 0");
    }

    @Test
    public void testVnicProfiles() {
        testValidSql("VnicProfiles: ",
            "SELECT * FROM ((SELECT distinct vnic_profiles_view.* FROM  vnic_profiles_view  )  ORDER BY name ASC) as T1 OFFSET (1 -1) LIMIT 0");
    }

    @Test
    public void testSearchThatContainsVmAndStorage() {
        testValidSql("Vms: status=up and storage=iscsi",
                "SELECT * FROM (SELECT * FROM vms WHERE ( vm_guid IN (SELECT distinct vms_with_tags.vm_guid FROM  vms_with_tags   LEFT OUTER JOIN storage_domains_with_hosts_view ON vms_with_tags.storage_id=storage_domains_with_hosts_view.id    WHERE (  vms_with_tags.status = '1'  AND  (  storage_domains_with_hosts_view.storage_comment LIKE '%iscsi%' OR  storage_domains_with_hosts_view.storage_description LIKE '%iscsi%' OR  storage_domains_with_hosts_view.storage_name LIKE '%iscsi%' OR  storage_domains_with_hosts_view.storage_pool_name::text LIKE '%iscsi%' )  )))  ORDER BY vm_name ASC ) as T1 OFFSET (1 -1) LIMIT 0");
    }

    private void testValidSql(String dynamicQuery, String exepctedSQLResult) {
        SyntaxChecker chkr = new SyntaxChecker();
        ISyntaxChecker curSyntaxChecker = SyntaxCheckerFactory.createBackendSyntaxChecker("foo");
        SyntaxContainer res = curSyntaxChecker.analyzeSyntaxState(dynamicQuery, true);
        assertTrue(res.getvalid(), "Invalid syntax: " + dynamicQuery);
        String query = chkr.generateQueryFromSyntaxContainer(res, true);
        assertEquals(exepctedSQLResult, query);
    }
}
