#
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


import gettext
import re

from . import util


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
def get_total_mb():

    _RE_MEMINFO_MEMTOTAL = re.compile(
        flags=re.VERBOSE,
        pattern=r"""
            ^
            MemTotal:
            \s+
            (?P<value>\d+)
            \s+
            (?P<unit>\w+)
        """
    )
    with open('/proc/meminfo', 'r') as f:
        content = f.read()

    match = _RE_MEMINFO_MEMTOTAL.match(content)
    if match is None:
        raise RuntimeError(_("Unable to parse /proc/meminfo"))

    mem = int(match.group('value'))
    if match.group('unit') == "kB":
        mem //= 1024

    return mem


@util.export
def javaX_mb(xvalue):
    """
    Return the int size in MB of a value intended for java -Xms or -Xmx.
    """
    _RE_JAVA_X = re.compile(
        flags=re.VERBOSE,
        pattern=r"""
            ^
            (?P<value>\d+)
            (?P<unit>\w*)
        """
    )

    match = _RE_JAVA_X.match(xvalue)
    if match is None:
        raise RuntimeError(_("Unable to parse %s" % xvalue))
    mem = int(match.group('value'))
    if match.group('unit') in ('g', 'G'):
        mem *= 1024
    elif match.group('unit') in ('m', 'M'):
        pass  # Value is already in MB
    elif match.group('unit') in ('k', 'K'):
        mem //= 1024
    elif match.group('unit') == '':
        mem //= 1024*1024
    else:
        raise RuntimeError("javaX_mb: Unknown unit in value %s" % xvalue)

    return mem


# vim: expandtab tabstop=4 shiftwidth=4
