#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Filter secrets plugin."""

from otopi import constants as otopicons
from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons


@util.export
class Plugin(plugin.PluginBase):
    """Filter secrets plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_BOOT,
        name=osetupcons.Stages.SECRETS_FILTERED_FROM_SETUP_ATTRS_MODULES,
        before=(
            otopicons.Stages.CORE_LOG_INIT,
        ),
    )
    def _boot(self):
        secret_keys = []
        secret_question_names = []
        # ovirt-setup-lib's queryPassword has a custom question name for the
        # first question, based on the key, which each caller has to add (using
        # asked_on), but the second (verification) question is hard-coded to
        # pass a fake key 'second_password' which results in a fixed question
        # name. Add that here.
        #
        # Why is this important? Consider the following flow:
        # User wants to use password 'topsec1' for e.g. grafana admin,
        # and also has a password 'topsec2' for some other service.
        # User runs engine-setup interactively, and when asked about
        # the password:
        # 1. On first prompt, provides topsecc1
        # 2. On second prompt, provides topsec2
        # 3. They mismatch, so user is asked again. On third and fourth
        # prompts, user provides topsec1.
        # Then user uses the generated answer file to run engine-setup
        # unattended.
        # We want to filter out all of topsecc1, topsec1, topsec2 - all of
        # them were provided as 'passwords', so in principle might be -
        # even if we didn't accept them because of a mismatch.
        #
        # Without the mismatch, it's not important - the password will be
        # filtered out correctly due to being provided for the first
        # question.
        secret_question_names.append('queryEnvKey_input_second_password')
        consts = []
        for constobj in self.environment[
            osetupcons.CoreEnv.SETUP_ATTRS_MODULES
        ]:
            consts.extend(constobj.__dict__['__osetup_attrs__'])
        for c in consts:
            for k in c.__dict__.values():
                if (
                    hasattr(k, '__osetup_attrs__') and
                    k.__osetup_attrs__['is_secret']
                ):
                    secret_keys.append(k.fget(None))
                    # If is_secret is set, we now require also passing
                    # asked_on. This should be a list of question names.
                    # We then also filter these out as well if found in
                    # the env, as it means they were passed via an answer
                    # file.
                    for question_name in k.__osetup_attrs__['asked_on']:
                        # E.g.: OVESETUP_CONFIG_ADMIN_SETUP
                        secret_question_names.append(question_name)

        self.environment[
            otopicons.CoreEnv.LOG_FILTER_KEYS
        ].extend(
            secret_keys
        )
        self.environment[
            otopicons.CoreEnv.LOG_FILTER_QUESTIONS
        ].extend(
            secret_question_names
        )


# vim: expandtab tabstop=4 shiftwidth=4
