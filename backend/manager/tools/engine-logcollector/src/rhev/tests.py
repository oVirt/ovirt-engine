#!/usr/bin/env python

import unittest
import logcollector


class HyperVisorDataTest(unittest.TestCase):

    def setUp(self):
        self._prep({})

    def _prep(self, params):
        self.conf = logcollector.Configuration()
        self.conf.update(params)
        self.hvd = logcollector.HyperVisorData(
            hostname=params.get("hostname", "dummy_host"),
            configuration=self.conf,
            semaphore=None,
            queue=None)

    def test_format_ssh_user(self):
        self.assertEquals(self.hvd.format_ssh_user(None), "")
        self.assertEquals(self.hvd.format_ssh_user(""), "")
        self.assertEquals(self.hvd.format_ssh_user("foo"), "foo@")
        self.assertEquals(self.hvd.format_ssh_user("foo@"), "foo@")

    def test_format_ssh_command_empty_config(self):
        params = {"hostname": "localhost"}
        self._prep(params)
        self.assertEquals(self.hvd.format_ssh_command(), "ssh localhost")

    def test_format_ssh_command_ssh_user(self):
        params = {"hostname": "localhost", "ssh_user": "foo"}
        self._prep(params)
        self.assertEquals(self.hvd.format_ssh_command(), "ssh foo@localhost")

    def test_format_ssh_command_ssh_port(self):
        params = {"hostname": "localhost", "ssh_user": "foo", "ssh_port": "22"}
        self._prep(params)
        self.assertEquals(self.hvd.format_ssh_command(), "ssh -p 22 foo@localhost")

    def test_format_ssh_command_keyfile(self):
        params = {"hostname": "localhost", "ssh_user": "foo",
                "ssh_port": "22", "key_file": "/tmp/foobar"}
        self._prep(params)
        self.assertEquals(self.hvd.format_ssh_command(), "ssh -p 22 -i /tmp/foobar foo@localhost")

    def test_format_ssh_command_no_port(self):
        params = {"hostname": "localhost", "ssh_user": "foo",
                "key_file": "/tmp/foobar"}
        self._prep(params)
        self.assertEquals(self.hvd.format_ssh_command(), "ssh -i /tmp/foobar foo@localhost")

    def test_format_ssh_command_no_user(self):
        params = {"hostname": "localhost", "key_file": "/tmp/foobar"}
        self._prep(params)
        self.assertEquals(self.hvd.format_ssh_command(), "ssh -i /tmp/foobar localhost")

if __name__ == "__main__":
    unittest.main()
