package org.ovirt.engine.core.common.utils.ansible;

import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * This class is utility class for parsing the Json output of the Ansible runner service.
 */
public final class RunnerJsonNode {

    // playbook parsing

    /**
     * Those are example output for playbook parsing.
     *
     * Running the playbook:
     * "status": "STARTED",
     * "msg": "starting",
     * "data": {
     *   "play_uuid": "1733c3ac-b483-11e8-ad05-c85b7671906d"
     * }
     *
     * Get playbook status:
     * "status": "OK",
     * "msg": "running",
     * "data": {
     *   "task": ...,
     *   "task_metadata": ...,
     *   "role": ...,
     *   "last_task_num": ...,
     *   "skipped": ...,
     *   "failed": ...,
     *   "ok": ...,
     *   "failures": ...,
     * }
     */

    /**
     * Return playbook status.
     */
    public static String status(JsonNode node) {
        return node.get("status").textValue();
    }

    /**
     * Return playbook message.
     */
    public static String msg(JsonNode node) {
        return node.get("msg").textValue();
    }

    /**
     * Return playbook uuid.
     */
    public static String playUuid(JsonNode node) {
        return node.get("data").get("play_uuid").textValue();
    }

    /**
     * Return if status is notfound.
     */
    public static boolean isStatusNotFound(JsonNode node) {
        return status(node).equalsIgnoreCase("NOTFOUND");
    }

    /**
     * Return if status is OK.
     */
    public static boolean isStatusOk(JsonNode node) {
        return status(node).equalsIgnoreCase("OK");
    }

    // event parsing
    /**
     * Here is example of the /events endpoint response data:
     *
     * "status": "OK",
     * "msg": "",
     * "data": {
     *   "total_events": 7,
     *   "events": {
     *     "2-0eaf70cd-0d86-4209-a3ca-73c0633afa27": {
     *       "event": "playbook_on_start"
     *     },
     *     "3-aced5c65-2dd1-7634-7812-00000000000b": {
     *       "event": "playbook_on_play_start"
     *     },
     *     "4-aced5c65-2dd1-7634-7812-00000000000d": {
     *       "event": "playbook_on_task_start",
     *       "task": "Step 1"
     *     }
     *   }
     * }
     */

    /**
     * Return number of total events.
     */
    public static int totalEvents(JsonNode node) {
        return node.get("data").get("total_events").intValue();
    }

    /**
     * Return all currently parsed events.
     */
    public static JsonNode eventNodes(JsonNode node) {
        return node.get("data").get("events");
    }

    /**
     * Return true if the event is 'runner_on_ok', which means it's event of the executed task in the playbook.
     */
    public static boolean isEventOk(JsonNode node) {
        return node.get("event").textValue().equals("runner_on_ok");
    }

    /**
     * Return true if the event is 'playbook_on_task_start', which means it's event of started.
     */
    public static boolean isEventStart(JsonNode node) {
        return node.get("event").textValue().equals("playbook_on_task_start");
    }


    /**
     * Return true if the event is 'error', which means it's event unexpectedly failed.
     */
    public static boolean isEventError(JsonNode node) {
        return node.get("event").textValue().equals("error");
    }

    /**
     * Return true if the event is 'runner_on_failed', which means it's event failed.
     */
    public static boolean isEventFailed(JsonNode node) {
        return node.get("event").textValue().equals("runner_on_failed");
    }

    /**
     * Return true if the event is 'runner_on_unreachable', which means the host is unreachable
     */
    public static boolean isEventUnreachable(JsonNode node) {
        return node.get("event").textValue().equals("runner_on_unreachable");
    }

    /**
     * Return true if the event is 'playbook_on_stats', which contains playbook statistics.
     */
    public static boolean playbookStats(JsonNode node) {
        return node.get("event").textValue().equals("playbook_on_stats");
    }

    // Yum parsing, parse the output of the 'taskNode()' method.
    /**
     * Example of yum runner_on_ok event:
     *
     * data={
     *   'event': u'runner_on_ok',
     *   'uuid': u'35242d0f-c428-464e-b1e1-c1ea77acc5a6',
     *   'stdout': u'changed: [1.2.3.4]',
     *   'counter': 27,
     *   'pid': 22729,
     *   'created': u'2019-09-26T06:52:24.141827',
     *   'end_line': 25,
     *   'runner_ident': '1e6510e4-e02a-11e9-964b-001a4a013fc1',
     *   'start_line': 24,
     *   'event_data': {
     *     'play_pattern': u'all',
     *     'play': u'all',
     *     'event_loop': null,
     *     'task_args': u'',
     *     'remote_addr': u'1.2.3.4',
     *     'res': {
     *       u'invocation': {
     *         u'module_args': {
     *           u'lock_timeout': 300,
     *           u'update_cache': true,
     *           u'conf_file': null,
     *           u'exclude': [].
     *           u'allow_downgrade': false,
     *           u'disable_gpg_check': false,
     *           u'disable_excludes': null,
     *           u'validate_certs': true,
     *           u'state: latest',
     *           u'disable_repo': [].
     *           u'releasever': null,
     *           u'skip_broken': false,
     *           u'autoremove': false,
     *           u'download_dir': null,
     *           u'installroot': "/",
     *           u'install_weak_deps': true,
     *           u'name': [u'*'],
     *           u'download_only': false,
     *           u'bugfix': false,
     *           u':list': updates
     *           u'install_repoquery': true,
     *           u'update_only': False,
     *           u'disable_plugin': [].
     *           u'enablerepo': [].
     *           u'security': false,
     *           u'enable_plugin': [],
     *         }
     *       },
     *       u'msg': u'',
     *       u'changed': false,
     *       u'_ansible_no_log': false,
     *       u'results': [ {
     *          u'name': u'PackageKit',
     *          u'nevra': u'0:PackageKit-1.1.12-3.el8.x86_64',
     *          u'repo': u'AppStream',
     *          u'epoch': u'0',
     *          u'version': u'1.1.12',
     *          u'release': u'3.el8',
     *          u'yumstate: u'available',
     *          u'arch: u'x86_64'},
     *       ].
     *     },
     *     'pid': 22729,
     *     'play_uuid': u'001a4a01-3fc1-6116-1e78-000000000006',
     *     'task_uuid': u'001a4a01-3fc1-6116-1e78-000000000012',
     *     'task':'u'Update system',
     *     'playbook_uuid': u'8b80ad75-3a51-4ea5-98b9-bf8785fbac25',
     *     'playbook': u'ovirt-host-upgrade.yml',
     *     'task_action': u'yum',
     *     'host': u'1.2.3.4',
     *     'role': u'ovirt-host-upgrade',
     *     'task_path': u'$PREFIX/packaging/ansible-runner-service-project/project/roles/ovirt-host-upgrade/tasks/main.yml:16'
     *   },
     *   'parent_uuid': u'001a4a01-3fc1-6116-1e78-000000000012'
     * }
     */

    /**
     * Return true if the event should be ignored.
     */
    public static boolean ignore(JsonNode node) {
        return node.get("data").get("event_data").get("ignore_errors").asBoolean();
    }

    /**
     * Return the data of the task output.
     */
    public static JsonNode taskNode(JsonNode node) {
        return node.get("data").get("event_data").get("res");
    }

    /**
     * For example, a task titled "Test" containing the following output:
     * ok: [192.168.100.233] => {
     *     "msg": "[WARNING] this is a debug msg"
     * }
     * will return: "Test: [WARNING] this is a debug message"
     * Debug modules not containing severity type will return "Test: debug msg: this is a debug message"
     */
    public static String formatDebugMessage(String name, String output) {
        List<String> severities = Arrays.asList("NORMAL", "WARNING", "ERROR", "ALERT");
        output = output.substring(0, output.lastIndexOf("\""));
        Pattern taskPattern = Pattern.compile("\"msg\": \"(?<message>.*)");
        boolean hasSeverity = false;
        if (severities.stream().anyMatch(output::contains)) {
            taskPattern = Pattern.compile("(?<severity>\\[(NORMAL|WARNING|ERROR|ALERT)\\]) (?<message>.*)");
            hasSeverity = true;
        }
        Matcher matcher = taskPattern.matcher(output);
        matcher.find();
        return hasSeverity ? String.format("%s: %s %s", name, matcher.group("severity"), matcher.group("message"))
                : String.format("%s: debug msg: %s", name, matcher.group("message"));
    }

    /**
     * Returns whether the task reports changed state.
     */
    public static boolean changed(JsonNode node) {
        return node.get("changed").asBoolean();
    }

    /**
     * Returns true if yum task contains results.
     */
    public static boolean hasUpdates(JsonNode node) {
        if (node.get("ansible_facts") != null && node.get("ansible_facts").get("yum_result") != null) {
            return !node.get("ansible_facts").get("yum_result").toString().equals("\"\"");
        }
        return false;
    }

    /**
     * Returns node that contains packages that can be updated.
     */
    public static Set<String> getPackages(JsonNode node) {
        Set<String> yumPackages = new HashSet<>(Arrays.asList(node.get("ansible_facts").get("yum_result").asText().split("\n")));
        yumPackages.remove("");
        return yumPackages;
    }

    /**
     * Return base64 decoded 'content' attribute of the module.
     */
    public static String content(JsonNode node) {
        return new String(Base64.getDecoder().decode(node.get("content").textValue()));
    }

    /**
     * Returns updated node of yum task.
     */
    public static JsonNode updated(JsonNode node) {
        return node.get("changes").get("updated");
    }


    // Command parsing

    /**
     * Return stdout value of the command task.
     */
    public static String getStdout(JsonNode node) {
        return node.get("stdout").textValue();
    }

    /**
     * Return stderr value of the command task.
     */
    public static String getStderr(JsonNode node) {
        JsonNode n = node.get("stderr");
        if (n != null) {
            return n.textValue();
        }

        return null;
    }
}
