#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""base core plugin."""


from otopi import util

from . import answerfile
from . import duplicated_constants_check
from . import filter_secrets
from . import misc
from . import offlinepackager
from . import postinstall
from . import reconfigure
from . import uninstall


@util.export
def createPlugins(context):
    answerfile.Plugin(context=context)
    duplicated_constants_check.Plugin(context=context)
    filter_secrets.Plugin(context=context)
    misc.Plugin(context=context)
    offlinepackager.Plugin(context=context)
    postinstall.Plugin(context=context)
    reconfigure.Plugin(context=context)
    uninstall.Plugin(context=context)


# vim: expandtab tabstop=4 shiftwidth=4
