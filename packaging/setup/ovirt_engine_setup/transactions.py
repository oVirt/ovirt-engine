# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0

"""
transations helpers
"""

import gettext
import os
import tempfile

from otopi import transaction


def _(m):
    return gettext.dgettext(message=m, domain='ovirt-engine-setup')


class RemoveFileTransaction(transaction.TransactionElement):
    """
    Try to remove a file in a transcation.

    When preparing the transaction, rename to temporary file in the same
    directory:

        filename -> filename.tmp66lBrkjrwl

    If the transaction is aborted, rename back to the original name:

        filename.tmp66lBrkjrwl -> filename

    If the transaction succeeded, remove the temporary file.

    If the process is killed after preparing the transaction, the user can
    recover by renaming the tempoary file, or commit by removing the temporary
    file.
    """

    def __init__(self, path):
        self.orig_path = path
        fd, self.temp_path = tempfile.mkstemp(
            prefix=os.path.basename(path) + ".tmp",
            dir=os.path.dirname(path)
        )
        os.close(fd)

    def __str__(self):
        return _("Remove file transaction for %r") % self.orig_path

    def prepare(self):
        try:
            os.rename(self.orig_path, self.temp_path)
        except FileNotFoundError:
            # The original file does not exist, nothing to do.
            pass

    def abort(self):
        try:
            os.rename(self.temp_path, self.orig_path)
        except FileNotFoundError:
            # The transaction was not prepared, nothing to do.
            pass

    def commit(self):
        try:
            os.unlink(self.temp_path)
        except FileNotFoundError:
            # The transaction was not prepared, nothing to do.
            pass


# vim: expandtab tabstop=4 shiftwidth=4
