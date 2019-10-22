#
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Utilities and tools."""


import sys

__all__ = ['export']


def export(o):
    """Decoration to export module symbol.

    Usage:
        import util
        @util.export
        def x():
            pass

    """
    sys.modules[o.__module__].__dict__.setdefault(
        '__all__', []
    ).append(o.__name__)
    return o


@export
def escape(s, chars):
    ret = ''
    for c in s:
        if c in chars:
            ret += '\\'
        ret += c
    return ret


@export
def processTemplate(template, subst={}):
    content = ''
    with open(template, 'r') as f:
        content = f.read()
    for k, v in subst.items():
        content = content.replace(str(k), str(v))
    return content


# vim: expandtab tabstop=4 shiftwidth=4
