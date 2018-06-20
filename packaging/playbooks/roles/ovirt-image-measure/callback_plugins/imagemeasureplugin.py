from __future__ import absolute_import

from ansible.plugins.callback import CallbackBase

__metaclass__ = type


class CallbackModule(CallbackBase):
    """
    This callback module prints the output of the single task of the
    ovirt-image-measure role which is the actual and virtual sizes of an image.
    """
    CALLBACK_VERSION = 2.0
    CALLBACK_TYPE = 'stdout'
    CALLBACK_NAME = 'imagemeasureplugin'

    def __init__(self, display=None):
        super(CallbackModule, self).__init__(display)

    def v2_runner_on_ok(self, result, **kwargs):
        self.info = result._result.get('stdout', '')

    def v2_playbook_on_stats(self, stats):
        self._display.display(self.info)

    v2_runner_on_failed = v2_runner_on_ok
    v2_runner_on_unreachable = v2_runner_on_ok
    v2_runner_on_skipped = v2_runner_on_ok
