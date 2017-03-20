"""
test_database.py - Tests for ovirt_engine_setup/engine_common/database.py
"""

import sys

import ovirt_engine_setup.engine_common as common

import mock
import pytest

# mock imports
common.constants = mock.Mock()
mock_ovirt_setup_lib = mock.Mock()
mock_ovirt_setup_lib.hostname = mock.Mock()
mock_ovirt_setup_lib.dialog = mock.Mock()
sys.modules['ovirt_setup_lib'] = mock_ovirt_setup_lib

import ovirt_engine_setup.engine_common.database as under_test  # isort:skip # noqa: E402


@pytest.mark.parametrize(
    ('given', 'expected'), [
        ('5', '5'),
        ('5.5', '5.5'),
        ('0.5', '0.5'),
        ('5555.5555', '5555.5555'),
        ('5Gb', '5Gb'),
        ('value-gone$#wild', 'value-gone$#wild'),
    ]
)
def test_value_extraction_from_conf(given, expected):
    match = under_test.RE_KEY_VALUE.match('key=%s' % given)
    assert match.group('value') == expected
