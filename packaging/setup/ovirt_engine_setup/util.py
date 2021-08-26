#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""Utils."""


import gettext
import grp
import pwd
import re

import distro

from otopi import constants as otopicons
from otopi import plugin
from otopi import util


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


@util.export
def editConfigContent(
    content,
    params,
    keep_existing=False,
    changed_lines=None,
    comment_re='[#]*\\s*',
    param_re='\\w+',
    new_comment_tpl='{spaces}# {original}',
    separator_re='\\s*=\\s*',
    new_line_tpl='{spaces}{param} = {value}',
    added_params=None,
):
    """Return edited content of a config file.

    Keyword arguments:
    content         - a list of strings, the content prior to calling us
    params          - a dict of params/values that should be in the output
                    If the value for a param is None, param is deleted
    keep_existing   - if True, existing params are not changed, only missing
                    ones are added.
    changed_lines   - an output parameter, a list of dictionaries with
                    added and removed lines.
    comment_re      - a regular expression that a comment marker prefixed
                    to param should match. If a commented param line is found,
                    a new line will be added after it.
    param_re        - a regular expression that should match params
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
            (?P<param>{param_re})
            (?P<separator>{separator_re})
            (?P<value>.*)
        )
        $
    """.format(
        comment_re=comment_re,
        param_re=param_re,
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
                (
                    str(f.group('value')) == str(params[f.group('param')]) or
                    keep_existing
                )
            ):
                # value is not changed, or we do not care. do nothing
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
def getPortTester():
    def test_port(port):
        res = ''
        try:
            parsePort(port)
        except ValueError as e:
            res = e
        return res
    return test_port


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


@util.export
def getPackageManager(logger=None):
    """Return a tuple with the package manager printable name string, the mini
    implementation class and the sink base class, for the preferred package
    manager available in the system.

    The only parameter accepted by this function is a logger instance, that
    can be ommited (or None) if the user don't wants logs.
    """
    try:
        from otopi import minidnf
        minidnf.MiniDNF()
        if logger is not None:
            logger.debug('Using DNF as package manager')
        return 'DNF', minidnf.MiniDNF, minidnf.MiniDNFSinkBase
    except (ImportError, RuntimeError):
        try:
            from otopi import miniyum

            # yum does not raises validation exceptions in constructor,
            # then its not worth instantiating it to test.
            if logger is not None:
                logger.debug('Using Yum as package manager')
            return 'Yum', miniyum.MiniYum, miniyum.MiniYumSinkBase
        except ImportError:
            raise RuntimeError(
                _(
                    'No supported package manager found in your system'
                )
            )


@util.export
def is_ovirt_packaging_supported_distro():
    """Return True if running on a Linux Distribution supported by oVirt
    packaging. In the past, also gentoo was supported by oVirt, only in
    developer-mode.
    """
    # Previously, we used platform.linux_distribution, but this was removed
    # in Python 3.8: https://bugs.python.org/issue1322
    # Also, it does not include 'like' or 'variety'.
    # Python 3.10 added Add platform.freedesktop_os_release, but we can't use
    # that, as EL8 has 3.6. So we rely for now on pypi's 'distro'.
    id_and_like = [distro.id()] + distro.like().split(' ')
    return any(dist in id_and_like for dist in ('rhel', 'fedora'))


# vim: expandtab tabstop=4 shiftwidth=4
