#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""ovirt-host-setup config plugin."""


from otopi import util

from . import aaa
from . import aaainternal
from . import aaajdbc
from . import aaakerbldap
from . import aaaupgrade
from . import appmode
from . import ca
from . import database
from . import domain_type
from . import firewall
from . import iso_domain
from . import java
from . import jboss
from . import notifier
from . import options
from . import protocols
from . import public_glance_repository
from . import sso
from . import storage
from . import tools


@util.export
def createPlugins(context):
    aaa.Plugin(context=context)
    aaainternal.Plugin(context=context)
    aaajdbc.Plugin(context=context)
    aaakerbldap.Plugin(context=context)
    aaaupgrade.Plugin(context=context)
    jboss.Plugin(context=context)
    java.Plugin(context=context)
    database.Plugin(context=context)
    protocols.Plugin(context=context)
    appmode.Plugin(context=context)
    domain_type.Plugin(context=context)
    firewall.Plugin(context=context)
    ca.Plugin(context=context)
    options.Plugin(context=context)
    tools.Plugin(context=context)
    iso_domain.Plugin(context=context)
    public_glance_repository.Plugin(context=context)
    storage.Plugin(context=context)
    sso.Plugin(context=context)
    notifier.Plugin(context=context)


# vim: expandtab tabstop=4 shiftwidth=4
