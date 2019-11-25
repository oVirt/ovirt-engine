package org.ovirt.engine.core.common.utils.ansible;

import org.codehaus.jackson.JsonNode;

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
        return node.get("status").getTextValue();
    }

    /**
     * Return playbook message.
     */
    public static String msg(JsonNode node) {
        return node.get("msg").getTextValue();
    }

    /**
     * Return playbook uuid.
     */
    public static String playUuid(JsonNode node) {
        return node.get("data").get("play_uuid").getTextValue();
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
        return node.get("data").get("total_events").getIntValue();
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
        return node.get("event").getTextValue().equals("runner_on_ok");
    }

    /**
     * Return true if the event is 'playbook_on_task_start', which means it's event of started.
     */
    public static boolean isEventStart(JsonNode node) {
        return node.get("event").getTextValue().equals("playbook_on_task_start");
    }


    /**
     * Return true if the event is 'error', which means it's event unexpectedly failed.
     */
    public static boolean isEventError(JsonNode node) {
        return node.get("event").getTextValue().equals("error");
    }

    /**
     * Return true if the event is 'runner_on_failed', which means it's event failed.
     */
    public static boolean isEventFailed(JsonNode node) {
        return node.get("event").getTextValue().equals("runner_on_failed");
    }

    /**
     * Return true if the event is 'playbook_on_stats', which contains playbook statistics.
     */
    public static boolean playbookStats(JsonNode node) {
        return node.get("event").getTextValue().equals("playbook_on_stats");
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
     *     'task': u'Update system',
     *     'task_args': u'',
     *     'remote_addr': u'1.2.3.4',
     *     'res': {
     *       u'_ansible_no_log': False,
     *       u'changed': True,
     *       u'results': [],
     *       u'invocation': {
     *         u'module_args': {
     *           u'name': [u'*'],
     *           u'update_only': False,
     *         }
     *       },
     *       u'rc': 0,
     *       u'msg': u'',
     *       u'changes': {
     *         u'updated': [
     *           [u'packagename', u'version.el7.noarch from my-repo']
     *         ],
     *         u'installed': []
     *       }
     *     },
     *     'pid': 22729,
     *     'play_uuid': u'001a4a01-3fc1-6116-1e78-000000000006',
     *     'task_uuid': u'001a4a01-3fc1-6116-1e78-000000000012',
     *     'event_loop': None,
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
     * Returns whether the task reports changed state.
     */
    public static boolean changed(JsonNode node) {
        return node.get("changed").asBoolean();
    }

    /**
     * Returns true if yum task contains changes.
     */
    public static boolean hasChanges(JsonNode node) {
        return node.has("changes");
    }

    /**
     * Returns installed node of yum task.
     */
    public static JsonNode installed(JsonNode node) {
        return node.get("changes").get("installed");
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
        return node.get("stdout").getTextValue();
    }

    /**
     * Return stderr value of the command task.
     */
    public static String getStderr(JsonNode node) {
        return node.get("stderr").getTextValue();
    }
}
