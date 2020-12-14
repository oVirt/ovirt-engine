import base64
import datetime
import json
import os

from cryptography import x509
from cryptography.exceptions import InvalidSignature
from cryptography.hazmat.backends import default_backend
from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives import serialization
from cryptography.hazmat.primitives.asymmetric import padding
from cryptography.hazmat.primitives.asymmetric import utils


class TicketEncoder():

    @staticmethod
    def _formatDate(d):
        return d.strftime("%Y%m%d%H%M%S")

    def __init__(self, cert, key, lifetime=5):
        self._lifetime = lifetime
        with open(cert, 'rb') as cert_file:
            self._x509 = x509.load_pem_x509_certificate(
                data=cert_file.read(),
                backend=default_backend(),
            )
        with open(key, 'rb') as key_file:
            self._pkey = serialization.load_pem_private_key(
                key_file.read(),
                password=None,
                backend=default_backend(),
            )

    def encode(self, data):
        d = {
            'salt': base64.b64encode(os.urandom(8)).decode('ascii'),
            'digest': 'sha1',
            'validFrom': self._formatDate(datetime.datetime.utcnow()),
            'validTo': self._formatDate(
                datetime.datetime.utcnow() + datetime.timedelta(
                    seconds=self._lifetime
                )
            ),
            'data': data
        }

        fields = []
        data_to_sign = b''
        for k, v in d.items():
            fields.append(k)
            data_to_sign += v.encode('utf-8')
        d['signedFields'] = ','.join(fields)
        signature = self._pkey.sign(
            data_to_sign,
            # TODO replace PKCS1v15 with PSS if/when we know we do not
            # need m2crypto compatibility.
            padding.PKCS1v15(),
            # TODO Replace SHA1 with SHA256 if/when we know this is safe,
            # compatibility-wise (also above).
            hashes.SHA1()
        )
        d['signature'] = base64.b64encode(signature).decode('ascii')
        d['certificate'] = self._x509.public_bytes(
            encoding=serialization.Encoding.PEM
        ).decode('ascii')
        return base64.b64encode(json.dumps(d).encode('utf-8'))


class TicketDecoder():

    _peer = None
    _ca = None

    @staticmethod
    def _parseDate(d):
        return datetime.datetime.strptime(d, '%Y%m%d%H%M%S')

    @staticmethod
    def _verifyCertificate(ca, x509cert):
        try:
            res = ca.public_key().verify(
                x509cert.signature,
                x509cert.tbs_certificate_bytes,
                padding.PKCS1v15(),
                x509cert.signature_hash_algorithm,
            )
            if res is not None:
                raise RuntimeError('Certificate validation failed')
        except InvalidSignature:
            raise ValueError('Untrusted certificate')

        if not (
            x509cert.not_valid_before.replace(tzinfo=None) <=
            datetime.datetime.utcnow() <=
            x509cert.not_valid_after.replace(tzinfo=None)
        ):
            raise ValueError('Certificate expired')

    def __init__(self, ca, eku, peer=None):
        self._eku = eku
        if peer is not None:
            self._peer = x509.load_pem_x509_certificate(
                data=peer.encode(),
                backend=default_backend(),
            )
        if ca is not None:
            with open(ca, 'rb') as ca_file:
                self._ca = x509.load_pem_x509_certificate(
                    data=ca_file.read(),
                    backend=default_backend(),
                )

    def decode(self, ticket):
        decoded = json.loads(base64.b64decode(ticket))

        if self._peer is not None:
            x509cert = self._peer
        else:
            x509cert = x509.load_pem_x509_certificate(
                data=decoded['certificate'].encode('utf8'),
                backend=default_backend(),
            )

        if self._ca is not None:
            self._verifyCertificate(self._ca, x509cert)

        if self._eku is not None:
            certekus = x509cert.extensions.get_extension_for_oid(
                x509.oid.ExtensionOID.EXTENDED_KEY_USAGE
            ).value
            if self._eku not in (eku.dotted_string for eku in certekus):
                raise ValueError('Certificate is not authorized for action')

        signedFields = [s.strip() for s in decoded['signedFields'].split(',')]
        if len(
            set(['salt', 'data']) &
            set(signedFields)
        ) == 0:
            raise ValueError('Invalid ticket')

        pkey = x509cert.public_key()
        if decoded['digest'] == 'sha1':
            md = hashes.SHA1()
        elif decoded['digest'] == 'sha256':
            # TODO: Not implemented yet
            md = hashes.SHA256()
        else:
            raise RuntimeError('Unknown message digest algorithm')
        hasher = hashes.Hash(md, backend=default_backend())
        for field in signedFields:
            hasher.update(decoded[field].encode('utf8'))
        digest = hasher.finalize()
        try:
            res = pkey.verify(
                base64.b64decode(decoded['signature']),
                digest,
                padding.PKCS1v15(),
                utils.Prehashed(md),
            )
            if res is not None:
                raise RuntimeError('Certificate validation failed')
        except InvalidSignature:
            raise ValueError('Invalid ticket signature')

        if not (
            self._parseDate(decoded['validFrom']) <=
            datetime.datetime.utcnow() <=
            self._parseDate(decoded['validTo'])
        ):
            raise ValueError('Ticket life time expired')

        return decoded['data']


# vim: expandtab tabstop=4 shiftwidth=4
