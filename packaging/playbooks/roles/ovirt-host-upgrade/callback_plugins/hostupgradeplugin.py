from __future__ import absolute_import

import json

from ansible.plugins.callback import CallbackBase

__metaclass__ = type


class CallbackModule(CallbackBase):
    """
    This callback module print list of packages which yum module report to be
    updated. It checks only tasks with are tagged by 'updatecheck' tag.
    """
    CALLBACK_VERSION = 2.0
    CALLBACK_TYPE = 'stdout'
    CALLBACK_NAME = 'hostupgradeplugin'

    FETCH_PACKAGES_WITH_TAG = 'updatecheck'

    def __init__(self, display=None):
        super(CallbackModule, self).__init__(display)
        self.packages = []

    def v2_runner_on_ok(self, result, **kwargs):
        if self.FETCH_PACKAGES_WITH_TAG in result._task.tags:
            changes = result._result.get('changes', dict())
            self.packages.extend(changes.get('installed', []))
            self.packages.extend([
                pkg[0] for pkg in changes.get('updated', [])
            ])

    def v2_playbook_on_stats(self, stats):
        self._display.display(json.dumps(self.packages))

    def v2_runner_on_failed(self, result, ignore_errors=False):
        self._handle_exception(result._result, use_stderr=True)
        self._display.display(
            "fatal: [%s]: FAILED! => %s" % (
                result._host.get_name(), self._dump_results(result._result)
            ), stderr=True
        )

    v2_runner_on_unreachable = v2_runner_on_failed
    v2_runner_on_skipped = v2_runner_on_ok
