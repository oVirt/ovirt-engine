#!/usr/bin/python
"""
Compare code help tags to help mapping files.
"""


import argparse
import json
import os
import re
import sys


try:
    import configparser
except ImportError:
    import ConfigParser as configparser


COMMAND_DIFF = 'diff'
COMMAND_TEMPLATE = 'template'
FORMAT_JSON = 'json'
FORMAT_INI = 'ini'
TYPE_WEBADMIN = 'webadmin'
TYPE_USERPORTAL = 'userportal'
TYPE_COMMON = 'common'
TYPE_UNKNOWN = 'unknown'
HELPTAG_SECTION = 'helptags'

__RE_HELPTAG = re.compile(
    flags=re.VERBOSE,
    pattern=r"""
        \s*
        [^\."\(@]+
        \(
            \s*
            "(?P<name>[^"]+)"
            \s*,\s*
            HelpTagType\.(?P<type>%s|%s|%s|%s)
            \s*
            (
                ,\s*
                    "(?P<comment>.*)"
                \s*
            )?
        \)
        \s*
    """
    %
    (
        TYPE_WEBADMIN.upper(), TYPE_USERPORTAL.upper(), TYPE_COMMON.upper(),
        TYPE_UNKNOWN.upper(),
    )
)


def loadTags(file, format):
    if format == FORMAT_JSON:
        with open(file, 'r') as f:
            ret = set(json.load(f).keys())
    elif format == FORMAT_INI:
        config = configparser.ConfigParser()
        config.optionxform = str
        config.read((file,))
        ret = set(config.options(HELPTAG_SECTION))
    else:
        raise RuntimeError("Invalid format '%s'" % format)

    return ret


def codeTags(filename, type):
    """
    look for help tags in the source code.
    """
    tags = {}

    if filename.endswith('.java') and os.path.isfile(filename):
        with open(filename, 'r') as f:
            for line in f:
                comment = ""
                m = __RE_HELPTAG.match(line)
                if m:
                    if (
                        m.group("type") == type.upper() or
                        m.group("type") == TYPE_COMMON.upper()
                    ):
                        name = m.group("name")
                        if m.group("comment"):
                            comment = m.group("comment")
                        tags[name] = comment
    return tags


def produceTemplate(tags, out, format=FORMAT_JSON):
    if format == FORMAT_JSON:
        # python 2.4 does not support auto map
        for key in tags:
            tags[key] = ''
        json.dump(
            tags,
            out,
            sort_keys=True,
            indent=4,
        )
    elif format == FORMAT_INI:
        out.write("[%s]\n\n" % HELPTAG_SECTION)
        for k in sorted(tags):
            out.write("; %s\n%s=\n\n" % (tags[k], k))
    else:
        raise RuntimeError('Invalid template format')


def produceDiff(left, right, out):
    ret = False

    if left == right:
        ret = True
    else:
        out.write('The following tags are changed:\n')
        for t in sorted(right - left):
            out.write('+%s\n' % t)
        for t in sorted(left - right):
            out.write('-%s\n' % t)

    return ret


def main():
    ret = 1

    parser = argparse.ArgumentParser(
        description=(
            'Compare code help tags to help mapping files, '
            'or produce template of mapping files.'
        ),
    )
    parser.add_argument(
        '--type',
        metavar='COMMAND',
        dest='type',
        choices=[TYPE_WEBADMIN, TYPE_USERPORTAL],
        help='Type (%(choices)s)',
        required=True
    )
    parser.add_argument(
        '--sourcefile',
        metavar='FILE',
        dest='sourcefile',
        default=(
            'frontend/webadmin/modules/uicommonweb/src/main/java/org/ovirt/'
            'engine/ui/uicommonweb/help/HelpTag.java'
        ),
        help='the source code file to scan',
    )
    parser.add_argument(
        '--command',
        metavar='COMMAND',
        dest='command',
        default=COMMAND_DIFF,
        choices=[COMMAND_DIFF, COMMAND_TEMPLATE],
        help='Command (%(choices)s)',
    )
    parser.add_argument(
        '--format',
        metavar='FORMAT',
        dest='format',
        default=FORMAT_JSON,
        choices=[FORMAT_JSON, FORMAT_INI],
        help='Format of files (%(choices)s)',
    )
    parser.add_argument(
        '--load',
        metavar='FILE',
        dest='load',
        default=[],
        action='append',
        help=(
            'Load existing files, may be used several times to '
            'load multiple files'
        ),
    )
    args = parser.parse_args()

    neededTags = codeTags(args.sourcefile, args.type)

    if args.command == COMMAND_TEMPLATE:
        produceTemplate(
            tags=neededTags,
            out=sys.stdout,
            format=args.format,
        )
        ret = 0
    elif args.command == COMMAND_DIFF:
        loadedTags = set()
        for f in args.load:
            loadedTags |= loadTags(f, format=args.format)
        ret = (
            0 if produceDiff(
                left=loadedTags,
                right=set(neededTags.keys()),
                out=sys.stdout,
            ) else 1
        )
    else:
        raise RuntimeError("Invalid command '%s'" % args.command)

    sys.exit(ret)


if __name__ == "__main__":
    main()


# vim: expandtab tabstop=4 shiftwidth=4
