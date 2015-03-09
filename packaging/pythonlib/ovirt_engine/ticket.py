import base64
import datetime
import json

from M2Crypto import EVP, X509, Rand


class TicketEncoder():

    @staticmethod
    def _formatDate(d):
        return d.strftime("%Y%m%d%H%M%S")

    def __init__(self, cert, key, lifetime=5):
        self._lifetime = lifetime
        self._x509 = X509.load_cert(cert)
        self._pkey = EVP.load_key(key)

    def encode(self, data):
        d = {
            'salt': base64.b64encode(Rand.rand_bytes(8)),
            'digest': 'sha1',
            'validFrom': self._formatDate(datetime.datetime.utcnow()),
            'validTo': self._formatDate(
                datetime.datetime.utcnow() + datetime.timedelta(
                    seconds=self._lifetime
                )
            ),
            'data': data
        }

        self._pkey.reset_context(md=d['digest'])
        self._pkey.sign_init()
        fields = []
        for k, v in d.items():
            fields.append(k)
            self._pkey.sign_update(v)

        d['signedFields'] = ','.join(fields)
        d['signature'] = base64.b64encode(self._pkey.sign_final())
        d['certificate'] = self._x509.as_pem()

        return base64.b64encode(json.dumps(d))


class TicketDecoder():

    _peer = None
    _ca = None

    @staticmethod
    def _parseDate(d):
        return datetime.datetime.strptime(d, '%Y%m%d%H%M%S')

    @staticmethod
    def _verifyCertificate(ca, x509):
        if x509.verify(ca.get_pubkey()) == 0:
            raise ValueError('Untrusted certificate')

        if not (
            x509.get_not_before().get_datetime().replace(tzinfo=None) <=
            datetime.datetime.utcnow() <=
            x509.get_not_after().get_datetime().replace(tzinfo=None)
        ):
            raise ValueError('Certificate expired')

    def __init__(self, ca, eku, peer=None):
        self._eku = eku
        if peer is not None:
            self._peer = X509.load_cert_string(peer)
        if ca is not None:
            self._ca = X509.load_cert(ca)

    def decode(self, ticket):
        decoded = json.loads(base64.b64decode(ticket))

        if self._peer is not None:
            x509 = self._peer
        else:
            x509 = X509.load_cert_string(
                decoded['certificate'].encode('utf8')
            )

        if self._ca is not None:
            self._verifyCertificate(self._ca, x509)

        if self._eku is not None:
            if self._eku not in x509.get_ext(
                'extendedKeyUsage'
            ).get_value().split(','):
                raise ValueError('Certificate is not authorized for action')

        signedFields = [s.strip() for s in decoded['signedFields'].split(',')]
        if len(
            set(['salt', 'data']) &
            set(signedFields)
        ) == 0:
            raise ValueError('Invalid ticket')

        pkey = x509.get_pubkey()
        pkey.reset_context(md=decoded['digest'])
        pkey.verify_init()
        for field in signedFields:
            pkey.verify_update(decoded[field].encode('utf8'))
        if pkey.verify_final(
            base64.b64decode(decoded['signature'])
        ) != 1:
            raise ValueError('Invalid ticket signature')

        if not (
            self._parseDate(decoded['validFrom']) <=
            datetime.datetime.utcnow() <=
            self._parseDate(decoded['validTo'])
        ):
            raise ValueError('Ticket life time expired')

        return decoded['data']


# vim: expandtab tabstop=4 shiftwidth=4
