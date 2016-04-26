#!/usr/bin/python

# Copyright 2016 Red Hat, Inc.

# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at

#    http://www.apache.org/licenses/LICENSE-2.0

# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

"""
Fail the build if someone adds a helptag that spans multiple lines.
"""


import argparse
import os
import re
import sys


HELPTAG_SOURCE = (
    'frontend/webadmin/modules/uicommonweb/src/main/java/org/'
    'ovirt/engine/ui/uicommonweb/help/HelpTag.java'
)

__RE_HELPTAG = re.compile(
    flags=re.VERBOSE,
    pattern=r"""
        \s*
        [^\."\(@]+
        \(
            \s*
            "(?P<name>[^"]+)"
            \s*,\s*
            HelpTagType\..*
            \s*
            (
                ,\s*
                    ".*"
                \s*
            )?
        \)
        \s*
    """
)


def findTags(filename):
    """
    look for help tags in the source code, making sure they are
    1 per line.
    """
    if filename.endswith('.java') and os.path.isfile(filename):
        with open(filename, 'r') as f:
            for line in f:
                m = __RE_HELPTAG.match(line)
                if "HelpTagType." in line and not m:
                    sys.stderr.write(
                        'ERROR: help tags must be one per line. '
                        'Check ' + filename + '\n' +
                        'ERROR: line ' + line
                    )
                    return 1
    return 0


def main():

    parser = argparse.ArgumentParser(
        description=(
            'Fail the build if someone adds a helptag that '
            'spans multiple lines.'
        ),
    )
    parser.add_argument(
        '--sourcefile',
        metavar='FILE',
        dest='sourcefile',
        default=HELPTAG_SOURCE,
        help='the source code file to scan',
    )
    args = parser.parse_args()

    ret = 1
    if (not args.sourcefile.endswith('.java') or
            not os.path.isfile(args.sourcefile)):
        sys.stderr.write(
            'ERROR: help tags file not found. Check ' + args.sourcefile
        )
        ret = 2
    else:
        ret = findTags(args.sourcefile)

    sys.exit(ret)


if __name__ == "__main__":
    main()


# vim: expandtab tabstop=4 shiftwidth=4
