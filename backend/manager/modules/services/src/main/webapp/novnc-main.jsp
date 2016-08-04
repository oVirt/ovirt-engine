<!DOCTYPE html>
<html>
<!--
    noVNC client with automatic initialization for oVirt/RHEV-M

    Based on a client by Joel Martin.

    Copyright (C) 2011 Joel Martin
    Licensed under LGPL-3 (see LICENSE.txt)

    Connect parameters are provided in query string:
        http://example.com/?host=HOST&port=PORT&encrypt=1&true_color=1
    -->
<head>
    <title>noVNC</title>
    <meta http-equiv="X-UA-Compatible" content="chrome=1">

    <script>var INCLUDE_URI="files/novnc/include/";</script>
    <script src="files/novnc/include/util.js"></script>
    <link rel="stylesheet" href="files/novnc/include/base.css" title="plain">

    <script src="theme/00-ovirt.brand/patternfly/components/jquery/dist/jquery.min.js"></script>
    <script src="theme/00-ovirt.brand/patternfly/components/bootstrap/dist/js/bootstrap.min.js"></script>
    <script src="theme/00-ovirt.brand/patternfly/js/patternfly.min.js"></script>
    <link rel="stylesheet" type="text/css" href="theme/00-ovirt.brand/patternfly/components/bootstrap/dist/css/bootstrap.min.css" />
    <link rel="stylesheet" type="text/css" href="theme/00-ovirt.brand/patternfly/css/patternfly.min.css" />

    <%@ include file="WEB-INF/warning-template.html"%>
    <script src="html-console-common.js"></script>
    <link rel="stylesheet" type="text/css" href="html-console-common.css" />
</head>

<body style="margin: 0px;">
    <div id="alert-container"></div>
    <div id="noVNC_screen">
        <div id="noVNC_status_bar" class="noVNC_status_bar"
            style="margin-top: 0px;">
            <table border=0 width="100%">
                <tr>
                        <td>
                            <div id="noVNC_status">Loading</div>
                        </td>
                        <td width="1%">
                            <div id="noVNC_buttons">
                                        <input type=button value="Send CtrlAltDel"
                                                id="sendCtrlAltDelButton">
                            </div>
                        </td>
                </tr>
            </table>
        </div>
        <canvas id="noVNC_canvas" width="640px" height="20px">
            Canvas not supported.
        </canvas>
    </div>

        <script>
        /*jslint white: false */
        /*global window, $, Util, RFB, */
        "use strict";

        var rfb, isOldNoVnc, loadedScripts = false, eventData = null;
        var alertContainer = $('#alert-container');

        loadNoVnc();

        function loadNoVnc() {
            if (!Util) {
                showWarning($('<strong>Page can\'t be loaded</strong>.' +
                        ' Please make sure that <code>novnc</code> package is installed.'),
                        alertContainer);
                return;
            }


            // Load supporting scripts
            if (Util.load_scripts !== undefined) {
                // for noVNC 0.5

                isOldNoVnc = false;

                Util.load_scripts(["webutil.js", "base64.js", "websock.js", "des.js",
                    "keysymdef.js", "keyboard.js", "input.js", "display.js",
                    "jsunzip.js", "rfb.js", "keysym.js"]);
            } else {
                // for noVNC 0.4

                isOldNoVnc = true;

                var extra = "", start, end;

                start = "<script src='files/novnc/include/";
                end = "'><\/script>";

                extra += start + "webutil.js" + end;
                extra += start + "base64.js" + end;
                extra += start + "websock.js" + end;
                extra += start + "des.js" + end;
                extra += start + "input.js" + end;
                extra += start + "display.js" + end;
                extra += start + "rfb.js" + end;
                extra += start + "jsunzip.js" + end;

                document.write(extra);
            }
        }

        function passwordRequired(rfb) {
            var msg;
            msg = '<form onsubmit="return setPassword();"';
            msg += '  style="margin-bottom: 0px">';
            msg += 'Password Required: ';
            msg += '<input type=password size=10 id="password_input" class="noVNC_status">';
            msg += '<\/form>';
            $D('noVNC_status_bar').setAttribute("class", "noVNC_status_warn");
            $D('noVNC_status').innerHTML = msg;
        }
        function setPassword() {
            rfb.sendPassword($D('password_input').value);
            return false;
        }
        function sendCtrlAltDel() {
            rfb.sendCtrlAltDel();
            return false;
        }
        function updateState(rfb, state, oldstate, msg) {
            var s, sb, cad, level;
            s = $D('noVNC_status');
            sb = $D('noVNC_status_bar');
            cad = $D('sendCtrlAltDelButton');
            switch (state) {
                case 'failed':       level = "error";  break;
                case 'fatal':        level = "error";  break;
                case 'normal':       level = "normal"; break;
                case 'disconnected': level = "normal"; break;
                case 'loaded':       level = "normal"; break;
                default:             level = "warn";   break;
            }

            if (state === "normal") { cad.disabled = false; }
            else                    { cad.disabled = true; }

            if (typeof(msg) !== 'undefined') {
                sb.setAttribute("class", "noVNC_status_" + level);
                s.innerHTML = msg;
            }
        }

        function getHost() {
            return WebUtil.getQueryVar('host', window.location.hostname);
        }

        function connectToConsole () {
            try {
                var host = getHost();
                var port = WebUtil.getQueryVar('port', window.location.port);
                var password = eventData.password;
                var path = eventData.connectionTicket;

                if ((!host) || (!port)) {
                    updateState('failed',
                        "Must specify host and port in URL");
                    return;
                }

                var rfbParams = {'target':       $D('noVNC_canvas'),
                           'encrypt':      true,
                           'true_color':   WebUtil.getQueryVar('true_color', true),
                           'local_cursor': WebUtil.getQueryVar('cursor', true),
                           'shared':       WebUtil.getQueryVar('shared', true),
                           'view_only':    WebUtil.getQueryVar('view_only', false),
                           'onPasswordRequired':  passwordRequired};

                if (isOldNoVnc) {
                    rfbParams.updateState = updateState;
                } else {
                    rfbParams.onUpdateState = updateState;
                }

                rfb = new RFB(rfbParams);
                rfb.connect(host, port, password, path);
            } catch(e) {
                alert(e);
            }
        }

        function receiveEvtData(evt) {
            if (evt.data === null || evt.data.password === null || evt.data.connectionTicket === null) {
                alert("Incorrect connection data");
                return;
            }

            eventData = evt.data;

            checkAndConnect();
        };

        if (window.addEventListener) {
            // For standards-compliant web browsers
            window.addEventListener("message", receiveEvtData, false);
        } else {
            window.attachEvent("onmessage", receiveEvtData);
        }

        window.onscriptsload = function () {
            loadedScripts = true;

            checkAndConnect();
        };

        function checkAndConnect() {
            if (!loadedScripts || !eventData) {
                return;
            }
            var path = eventData.connectionTicket;
            var url = new URL('wss://' + getHost() + ':6100/' + path);
            checkConnection(url.toString(), connectToConsole, reportServerUnreachable.bind(undefined, url.origin));
        }

        function reportServerUnreachable(serverUrl) {
            var certificateUrl = window.location.origin
                    + '/ovirt-engine/services/pki-resource?resource=ca-certificate&format=X509-PEM-CA';

            var bullets = [];
            bullets.push('websocket proxy service is running,');
            bullets.push('firewalls are properly set,');
            if (location.hostname != getHost()) {
                bullets.push('application is accessed using predefined hostname <code>' + getHost() + '</code>');
            }
            bullets.push('websocket proxy certificate is trusted by your browser. <a href="' + certificateUrl + '">Default CA certificate</a>.');
            var warningContent = $($.parseHTML('<strong>Can\'t connect to websocket proxy server</strong> <code>' + serverUrl + '</code>. Please check that:'))
                    .add(toBulletList(bullets));
            showWarning(warningContent, alertContainer);
        }

        window.onload = function () {
            $D('sendCtrlAltDelButton').style.display = "inline";
            $D('sendCtrlAltDelButton').onclick = sendCtrlAltDel;

            document.title = unescape(WebUtil.getQueryVar('title', 'noVNC'));

            if (isOldNoVnc) {
                window.onscriptsload();
            }
        };
        </script>

</body>
</html>

