#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""ovirt-host-setup upgrade validations plugin."""


from otopi import util

from . import answerfile_fixup
from . import asynctasks
from . import auth_url_validation
from . import dbvalidations


@util.export
def createPlugins(context):
    dbvalidations.Plugin(context=context)
    asynctasks.Plugin(context=context)
    answerfile_fixup.Plugin(context=context)
    auth_url_validation.Plugin(context=context)


# vim: expandtab tabstop=4 shiftwidth=4
