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
    New lines, if at all, are added only once per param - so this isn't
    suitable for ini files with multiple seections with the same item,
    or similar cases.
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

    # Find params which:
    # - appear uncommented in the input, and
    # - were passed to us to set
    needed_params_that_are_uncommented = set()
    for line in content:
        match = re_obj.match(line)
        if (
            match is not None and
            match.group('param') in params and
            not match.group('comment')
        ):
            needed_params_that_are_uncommented.add(match.group('param'))

    if changed_lines is None:
        changed_lines = []
    if added_params is None:
        added_params = []
    newcontent = []
    processed_params = set()
    for line in content:

        def add_current_line():
            newcontent.append(line)

        match = re_obj.match(line)
        if match is None:
            add_current_line()
        else:
            current_param = match.group('param')
            line_is_commented = bool(match.group('comment'))
            line_sets_uncommented_param = (
                current_param in needed_params_that_are_uncommented and
                not line_is_commented
            )
            required_value = params.get(current_param)
            current_content_value = match.group('value')

            def add_current_line_commented():
                current_line_commented = new_comment_tpl.format(
                    spaces=match.group('spaces'),
                    original=match.group('original'),
                )
                changed_lines.append(
                    {
                        'added': current_line_commented,
                        'removed': line,
                    }
                )
                newcontent.append(current_line_commented)

            def add_new_line():
                new_line = new_line_tpl.format(
                    spaces=match.group('spaces'),
                    param=current_param,
                    value=required_value,
                )
                changed_lines.append(
                    {
                        'added': new_line,
                    }
                )
                newcontent.append(new_line)

            if line_sets_uncommented_param:
                if required_value is None:
                    # Delete it, by adding the current line commented
                    add_current_line_commented()
                elif (
                    str(current_content_value) == str(required_value) or
                    keep_existing
                ):
                    # Value is already ok, just add current line as-is
                    add_current_line()
                else:
                    # Value needs to be replaced. Always add current line
                    # commented, but only add a new line if we didn't
                    # already process current param. E.g., if asked to set
                    # p1 to newv1, replace:
                    #
                    # p1=v1
                    # p1=v2
                    #
                    # with:
                    #
                    # # p1=v1
                    # p1=newv1
                    # # p1=v2
                    #
                    # Please note that this does not work well with files
                    # that have the same param (same name) in different
                    # "sections" of whatever kind where we want to replace
                    # more than the first occurrence (all, or some, etc.).
                    # E.g. An ini file, or an apache httpd conf file.
                    # Also note, that depending on taste, some people might
                    # prefer to replace the above with:
                    # # p1=v1
                    # # p1=v2
                    # p1=newv1
                    # But this is slightly more complex and I decided not to.
                    add_current_line_commented()
                    if current_param not in processed_params:
                        add_new_line()
                # In all of the above cases, current param is considered
                # "processed" - either removing it, or finding out it's
                # ok, or replacing it.
                processed_params.add(current_param)
            elif current_param in params:
                # This means that we are in a commented line that has
                # a param we were asked to set.
                if (
                    required_value is not None and
                    current_param not in processed_params and
                    # If there is some other line setting this param that is
                    # not commented, we handle this param on that other line,
                    # in the parent 'if'.
                    current_param not in needed_params_that_are_uncommented
                ):
                    add_current_line()
                    add_new_line()
                    # Among the cases in the containing upper 'elif', this
                    # is the only one where we consider the param 'processed'.
                    # The others, below, just add the line - which is already
                    # commented - as-is, and this isn't considered
                    # 'processing'.
                    processed_params.add(current_param)
                else:
                    add_current_line()
            else:
                add_current_line()

    # Add in the end non-processed params that we were not asked to remove
    for param, value in params.items():
        if param not in processed_params and value is not None:
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
        ) from ValueError
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
            ) from ImportError


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
