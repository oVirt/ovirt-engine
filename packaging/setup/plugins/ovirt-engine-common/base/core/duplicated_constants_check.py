#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#

"""Duplicate constants plugin."""

import gettext

from otopi import plugin
from otopi import util

from ovirt_engine_setup import constants as osetupcons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
class Plugin(plugin.PluginBase):

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            osetupcons.CoreEnv.FAIL_ON_DUPLICATED_CONSTANT,
            False
        )

    def _get_class_attr(self, aclass):
        dic = {}

        # getting all the attributes of the class (variables and methods )
        # excluding built-in methods
        attrs = [x for x in aclass.__dict__.keys() if '__' not in x]

        for attr in attrs:
            dic[
                getattr(
                    aclass,
                    attr
                )
            ] = {
                'module': aclass.__module__,
                'class': aclass.__name__,
                'attr': attr,
            }

        return dic

    @plugin.event(
        stage=plugin.Stages.STAGE_CLEANUP,
        priority=plugin.Stages.PRIORITY_LAST,
    )
    def _cleanup(self):

        # constants_dict is a dictionary that contains all the constants
        # key: constant value (e.g OVESETUP_CORE/answerFile)
        # value: list of all paths that constant appear in.
        # ('constant path' is the file and class of the constant)
        constants_dict = {}

        # All the constants that appear in more then one class
        dup_const = set()

        # running on all the constants.py modules
        for module in self.environment[
            osetupcons.CoreEnv.SETUP_ATTRS_MODULES
        ]:
            # running on all the classes in each module
            for c in module.__dict__['__osetup_attrs__']:
                class_dict = self._get_class_attr(c)

                # running on all the constants in each class
                for const, kls in class_dict.items():
                    if const in constants_dict:
                        dup_const.add(const)
                    constants_dict.setdefault(const, []).append(kls)

        # writing all the duplications to the log
        for const in dup_const:
            self.logger.debug(
                "ConstDup : The constant :'%s' appears in %d places: %s",
                const,
                len(constants_dict[const]),
                str([
                    "{}.{}.{}".format(
                        dup['module'],
                        dup['class'],
                        dup['attr']
                    ) for dup in constants_dict[const]
                ])
            )

        if dup_const and self.environment[
            osetupcons.CoreEnv.FAIL_ON_DUPLICATED_CONSTANT
        ]:
            raise Exception(
                "%d constant duplications were found:%s" % (
                    len(dup_const), str(dup_const)
                )
            )
