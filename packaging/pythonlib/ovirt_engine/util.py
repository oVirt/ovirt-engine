#
# Copyright (C) 2013-2015 Red Hat, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
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
