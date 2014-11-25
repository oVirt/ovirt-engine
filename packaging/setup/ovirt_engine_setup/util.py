#
# ovirt-engine-setup -- ovirt engine setup
# Copyright (C) 2013-2015 Red Hat, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


"""Utils."""


import gettext
import grp
import pwd
import re

from otopi import util
from otopi import plugin
from otopi import constants as otopicons


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
def editConfigContent(
    content,
    params,
    changed_lines=None,
    comment_re='[#]*\s*',
    new_comment_tpl='{spaces}# {original}',
    separator_re='\s*=\s*',
    new_line_tpl='{spaces}{param} = {value}',
    added_params=None,
):
    """Return edited content of a config file.

    Keyword arguments:
    content         - a list of strings, the content prior to calling us
    params          - a dict of params/values that should be in the output
                    If the value for a param is None, param is deleted
    changed_lines   - an output parameter, a list of dictionaries with
                    added and removed lines.
    comment_re      - a regular expression that a comment marker prefixed
                    to param should match. If a commented param line is found,
                    a new line will be added after it.
    new_comment_tpl - a template for a comment. {original} will be replaced
                    with this template, {spaces} will be replaced with
                    original whitespace prefix.
    separator_re    - a regular expression that the separator between
                    param and value should match
    new_line_tpl    - a template for a new line. {param} will be replaced
                    with param, {value} with value.
    added_params    - an output parameter, a list of params that were added
                    in the end because they were not found in content.

    Params that appear uncommented in the input, are commented, and new
    values are added after the commented lines. Params that appear only
    commented in the input, the comments are copied as-is, and new lines
    are added after the comments. Params that do not appear in the input
    are added in the end.
    """
    params = params.copy()

    pattern = r"""
        ^
        (?P<spaces>\s*)
        (?P<comment>{comment_re})
        (?P<original>
            (?P<param>\w+)
            (?P<separator>{separator_re})
            (?P<value>.*)
        )
        $
    """.format(
        comment_re=comment_re,
        separator_re=separator_re,
    )
    re_obj = re.compile(flags=re.VERBOSE, pattern=pattern)

    # Find params which are uncommented in the input.
    uncommented = set()
    for line in content:
        f = re_obj.match(line)
        if (
            f is not None and
            f.group('param') in params and
            not f.group('comment')
        ):
            uncommented.add(f.group('param'))

    if changed_lines is None:
        changed_lines = []
    if added_params is None:
        added_params = []
    newcontent = []
    processed = set()
    for line in content:
        f = re_obj.match(line)
        if (
            f is not None and
            f.group('param') in params and
            not (
                f.group('param') in uncommented and
                f.group('comment')
            )
            # If param in uncommented and current line is comment,
            # we do not need to process it - we process the uncommented
            # line when we see it
        ):
            if (
                not f.group('comment') and
                str(f.group('value')) == str(params[f.group('param')])
            ):
                # value is not changed, do nothing
                processed.add(f.group('param'))
            else:
                if (
                    f.group('param') in uncommented and
                    not f.group('comment')
                ):
                    # Add current line, commented, before new line
                    currentline = new_comment_tpl.format(
                        spaces=f.group('spaces'),
                        original=f.group('original'),
                    )
                    changed_lines.append(
                        {
                            'added': currentline,
                            'removed': line,
                        }
                    )
                    newcontent.append(currentline)
                else:
                    # Only possible option here is that current line is
                    # a comment and param is not in uncommented. Keep it.
                    # Other two options are in "if"s above.
                    # The last option - param is not in uncommented
                    # and current line is not a comment - is not possible.
                    newcontent.append(line)

                newline = new_line_tpl.format(
                    spaces=f.group('spaces'),
                    param=f.group('param'),
                    value=params[f.group('param')],
                )
                changed_lines.append(
                    {
                        'added': newline,
                    }
                )
                processed.add(f.group('param'))
                line = newline

        newcontent.append(line)

    # Add remaining params at the end
    for param, value in params.items():
        if param not in processed:
            newline = new_line_tpl.format(
                spaces='',
                param=param,
                value=value,
            )
            newcontent.append(newline)
            changed_lines.append(
                {
                    'added': newline,
                }
            )
            added_params.append(param)
    return newcontent


@util.export
def getUid(user):
    return pwd.getpwnam(user)[2]


@util.export
def getGid(group):
    return grp.getgrnam(group)[2]


@util.export
def parsePort(port):
    try:
        port = int(port)
    except ValueError:
        raise ValueError(
            _('Invalid port {number}').format(
                number=port,
            )
        )
    if port < 0 or port > 0xffff:
        raise ValueError(
            _('Invalid number {number}').format(
                number=port,
            )
        )
    return port


@util.export
def addExitCode(environment, code, priority=plugin.Stages.PRIORITY_DEFAULT):
    environment[
        otopicons.BaseEnv.EXIT_CODE
    ].append(
        {
            'code': code,
            'priority': priority,
        }
    )

# vim: expandtab tabstop=4 shiftwidth=4
