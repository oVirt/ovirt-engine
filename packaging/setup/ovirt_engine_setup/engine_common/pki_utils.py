#
# ovirt-engine-setup -- ovirt engine setup
#
# Copyright oVirt Authors
# SPDX-License-Identifier: Apache-2.0
#
#


"""PKI Utilities"""


import datetime
import os

from cryptography import x509
from cryptography.exceptions import InvalidSignature
from cryptography.hazmat.backends import default_backend
from cryptography.hazmat.primitives import serialization
from cryptography.hazmat.primitives.asymmetric import padding
from cryptography.x509.extensions import ExtensionNotFound

from ovirt_engine_setup.engine import constants as oenginecons
from ovirt_engine_setup.engine import vdcoption
from ovirt_engine_setup.engine_common import database


def x509_load_cert(fname):
    # input: fname: File name
    # return: cryptography.x509.Certificate object
    with open(fname, 'rb') as f:
        return x509.load_pem_x509_certificate(
            f.read(),
            backend=default_backend(),
        )


def cert_expires(x509cert, short_life, environment, logger):
    # input:
    # - logger: The logger to use for logging
    # - x509cert: cryptography.x509.Certificate object
    # - short_life: Whether the certificate is a short-life (e.g. browser)
    #   certificate.
    # - environment: Environment to pass to database.Statement
    # return: bool

    #
    # LEGACY NOTE
    # Since 3.0 and maybe before the CA certificate's
    # notBefore attribute was set using timezone offset
    # instead of Z
    # in this case we need to reissue CA certificate.
    #
    # py cryptography does not currently allow getting
    # a certificate's notBefore/notAfter timezone - its
    # docs say it returns "A naïve datetime". So we can't
    # use it to verify old certs' timezone. Hopefully that's
    # not needed anymore, because upgrade to 3.5.4 should have
    # prompted the user to renew PKI, and even if they initially
    # postponed this renewal, by now it should have been done.
    # CA Cert is set to expire in 10 years.
    # The only risk is with people that used the old code and
    # still didn't renew.
    # TODO: Either decide that this is ok and remove above
    # comment, or implement this check - either by fixing
    # py cryptography to support timezones or by using other
    # means, such as calling the openssl utility, e.g.
    # openssl x509 -in ca.pem -noout -startdate
    if short_life:
        days = 60
    else:
        try:
            days_str = vdcoption.VdcOption(
                statement=database.Statement(
                    dbenvkeys=oenginecons.Const.ENGINE_DB_ENV_KEYS,
                    environment=environment,
                ),
            ).getVdcOption(
                'CertExpirationWarnPeriodInDays',
                ownConnection=True,
            )
            days = int(days_str)
        except Exception as e:
            logger.info("CertExpirationWarnPeriodInDays config value not "
                        "available, using 365 days: %s", e)
            days = 365
    return (
        x509cert.not_valid_after.replace(tzinfo=None) -
        datetime.datetime.utcnow() <
        datetime.timedelta(days=days)
    )


SAN_extension_name = 'subjectAltName'


def cert_has_SAN(logger, x509cert):
    # input: x509cert: cryptography.x509.Certificate object
    # return: bool
    res = False
    try:
        ext = x509cert.extensions.get_extension_for_oid(
            x509.oid.ExtensionOID.SUBJECT_ALTERNATIVE_NAME
        )
        res = True
        logger.debug(
            '%s: %s',
            SAN_extension_name,
            ext.value[0].value,
        )
    except ExtensionNotFound:
        logger.debug('%s is missing', SAN_extension_name)
    return res


def ok_to_renew_cert(logger, x509cert, ca_cert, name, extract, short_life,
                     environment):
    # input:
    # - logger: The logger to use for logging
    # - x509cert: cryptography.x509.Certificate object
    # - ca_cert: cryptography.x509.Certificate object
    # - name: A base name (--name param of pki-* scripts)
    # - extract: bool. If True, we need to check the extracted cert
    # - short_life: Whether the certificate is a short-life (e.g. browser)
    #   certificate.
    # - environment: Environment to pass to database.Statement
    # return: bool
    res = False
    if x509cert and (
        cert_expires(x509cert, short_life, environment, logger) or
        not cert_has_SAN(logger, x509cert)
    ):
        if not extract or ca_cert is None:
            # In remote machines (websocket-proxy/grafana), we do not
            # have a copy of the engine CA, and 'extract' is True (we
            # do not keep there a PKCS12 file). In this case, return
            # True - meaning, do allow renew/recreation. Callers should
            # first ask the user, clarifying this will use the engine CA.
            res = True
        else:
            internal = False
            try:
                if ca_cert.public_key().verify(
                    x509cert.signature,
                    x509cert.tbs_certificate_bytes,
                    padding.PKCS1v15(),
                    x509cert.signature_hash_algorithm,
                ) is not None:
                    raise RuntimeError('Certificate validation failed')
                internal = True
            except InvalidSignature:
                pass
            if internal:
                logger.debug(
                    'certificate is an internal certificate'
                )

                # sanity check, make sure user did not manually
                # change cert
                extracted_x509cert = x509_load_cert(
                    os.path.join(
                        (
                            oenginecons.FileLocations.
                            OVIRT_ENGINE_PKICERTSDIR
                        ),
                        '%s.cer' % name,
                    )
                )

                if (
                    extracted_x509cert.public_bytes(
                        encoding=serialization.Encoding.PEM
                    )
                ) == (
                    x509cert.public_bytes(
                        encoding=serialization.Encoding.PEM
                    )
                ):
                    logger.debug('certificate is sane')
                    res = True
    return res


# vim: expandtab tabstop=4 shiftwidth=4
