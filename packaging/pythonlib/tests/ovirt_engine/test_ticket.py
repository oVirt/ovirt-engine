"""
test_ticket.py - Tests for ovirt_engine/ticket.py
"""

import base64
import datetime
import json


from unittest import mock
from unittest.mock import mock_open

from cryptography import x509
from cryptography.hazmat.backends import default_backend
from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives import serialization
from cryptography.hazmat.primitives.asymmetric import rsa
from cryptography.x509.oid import NameOID

from ovirt_engine import ticket as under_test

import pytest

builtin_open = open


# Based on:
# https://gist.github.com/adammartinez271828/137ae25d0b817da2509c1a96ba37fc56
def mapped_mock_open(file_contents_dict):
    """Create a mock "open" that will mock open multiple files based on name
    Args:
        file_contents_dict: A dict of 'fname': 'content'
    Returns:
        A Mock opener that will return the supplied content if name is in
        file_contents_dict, otherwise the builtin open
    """
    mock_files = {}
    for fname, content in file_contents_dict.items():
        mock_files[fname] = mock_open(read_data=content).return_value

    def my_open(fname, *args, **kwargs):
        if fname in mock_files:
            return mock_files[fname]
        else:
            return builtin_open(fname, *args, **kwargs)

    mock_opener = mock.Mock()
    mock_opener.side_effect = my_open
    return mock_opener


@pytest.fixture()
def gen_key_pair():
    # noqa
    # Copied from:
    # https://cryptography.io/en/latest/x509/reference.html#x-509-certificate-builder
    one_day = datetime.timedelta(1, 0, 0)
    private_key = rsa.generate_private_key(
        public_exponent=65537,
        key_size=2048,
        backend=default_backend(),
    )
    public_key = private_key.public_key()
    builder = x509.CertificateBuilder()
    builder = builder.subject_name(x509.Name([
        x509.NameAttribute(NameOID.COMMON_NAME, u'test-fqdn'),
    ]))
    builder = builder.issuer_name(x509.Name([
        x509.NameAttribute(NameOID.COMMON_NAME, u'test-fqdn'),
    ]))
    builder = builder.not_valid_before(datetime.datetime.today() - one_day)
    builder = builder.not_valid_after(
        datetime.datetime.today() + (one_day * 30)
    )
    builder = builder.serial_number(x509.random_serial_number())
    builder = builder.public_key(public_key)
    builder = builder.add_extension(
        x509.SubjectAlternativeName(
            [x509.DNSName(u'test-fqdn')]
        ),
        critical=False
    )
    builder = builder.add_extension(
        x509.BasicConstraints(ca=False, path_length=None), critical=True,
    )
    certificate = builder.sign(
        private_key=private_key,
        algorithm=hashes.SHA256(),
        backend=default_backend(),
    )

    rsa_private_key_pem = private_key.private_bytes(
        encoding=serialization.Encoding.PEM,
        format=serialization.PrivateFormat.PKCS8,
        encryption_algorithm=serialization.NoEncryption(),
    )

    cert_pem = certificate.public_bytes(
        encoding=serialization.Encoding.PEM,
    )
    return rsa_private_key_pem, cert_pem


@pytest.fixture()
def my_key_pem(gen_key_pair):
    return gen_key_pair[0]


@pytest.fixture()
def my_cert_pem(gen_key_pair):
    return gen_key_pair[1]


@pytest.fixture()
def mocked_files(mocker, my_key_pem, my_cert_pem):
    mocker.patch(
        'builtins.open',
        mapped_mock_open(
            {
                'dummycert': my_cert_pem,
                'dummykey': my_key_pem,
            }
        )
    )


def test_encode_decode(mocked_files, my_cert_pem):
    encoder = under_test.TicketEncoder('dummycert', 'dummykey')
    decoder = under_test.TicketDecoder(
        ca=None,
        eku=None,
        peer=my_cert_pem.decode()
    )
    data = 'mydata'
    encoded_data = encoder.encode(data)
    decoded_data = decoder.decode(encoded_data)
    assert data == decoded_data


def test_corrupted_ticket(mocked_files, my_cert_pem):
    encoder = under_test.TicketEncoder('dummycert', 'dummykey')
    decoder = under_test.TicketDecoder(
        ca=None,
        eku=None,
        peer=my_cert_pem.decode()
    )
    data = 'mydata'

    encoded_data = encoder.encode(data)
    encoded_data_array = bytearray(encoded_data)
    encoded_data_array[0:4] = b'XXXXX'

    with pytest.raises(Exception):
        decoder.decode(bytes(encoded_data_array))


def test_corrupted_ticket_data(mocked_files, my_cert_pem):
    encoder = under_test.TicketEncoder('dummycert', 'dummykey')
    decoder = under_test.TicketDecoder(
        ca=None,
        eku=None,
        peer=my_cert_pem.decode()
    )
    data = 'mydata'
    encoded_data = encoder.encode(data)

    parsed_data = json.loads(base64.b64decode(encoded_data))
    parsed_data['data'] = 'XXXX' + parsed_data['data'][4:]
    corrupted_encoded_data = base64.b64encode(
        json.dumps(parsed_data).encode('utf-8')
    )

    with pytest.raises(ValueError, match='Invalid ticket signature'):
        decoder.decode(corrupted_encoded_data)

# TODO add more tests, also with a CA
