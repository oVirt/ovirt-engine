<!--
   Copyright (C) 2012 by Jeremy P. White <jwhite@codeweavers.com>

   This file is part of spice-html5.

   spice-html5 is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   spice-html5 is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public License
   along with spice-html5.  If not, see <http://www.gnu.org/licenses/>.

   --------------------------------------------------
    Spice Javascript client template.
    Refer to main.js for more detailed information
   --------------------------------------------------

-->

<!doctype html>
<html>
    <head>

        <title>Spice Javascript client</title>
        <script src="files/spice-html5/enums.js"></script>
        <script src="files/spice-html5/atKeynames.js"></script>
        <script src="files/spice-html5/utils.js"></script>
        <script src="files/spice-html5/png.js"></script>
        <script src="files/spice-html5/lz.js"></script>
        <script src="files/spice-html5/quic.js"></script>
        <script src="files/spice-html5/bitmap.js"></script>
        <script src="files/spice-html5/spicedataview.js"></script>
        <script src="files/spice-html5/spicetype.js"></script>
        <script src="files/spice-html5/spicemsg.js"></script>
        <script src="files/spice-html5/wire.js"></script>
        <script src="files/spice-html5/spiceconn.js"></script>
        <script src="files/spice-html5/display.js"></script>
        <script src="files/spice-html5/main.js"></script>
        <script src="files/spice-html5/inputs.js"></script>
        <script src="files/spice-html5/cursor.js"></script>
        <script src="files/spice-html5/thirdparty/jsbn.js"></script>
        <script src="files/spice-html5/thirdparty/rsa.js"></script>
        <script src="files/spice-html5/thirdparty/prng4.js"></script>
        <script src="files/spice-html5/thirdparty/rng.js"></script>
        <script src="files/spice-html5/thirdparty/sha1.js"></script>
        <script src="files/spice-html5/ticket.js"></script>
        <script src="files/spice-html5/filexfer.js"></script>
        <script src="files/spice-html5/playback.js"></script>
        <script src="files/spice-html5/resize.js"></script>
        <script src="files/spice-html5/simulatecursor.js"></script>
        <script src="files/spice-html5/spicearraybuffer.js"></script>
        <script src="files/spice-html5/webm.js"></script>
        <link rel="stylesheet" type="text/css" href="files/spice-html5/spice.css" />

        <script src="theme/00-ovirt.brand/patternfly/components/jquery/dist/jquery.min.js"></script>
        <script src="theme/00-ovirt.brand/patternfly/components/bootstrap/dist/js/bootstrap.min.js"></script>
        <script src="theme/00-ovirt.brand/patternfly/js/patternfly.min.js"></script>
        <link rel="stylesheet" type="text/css" href="theme/00-ovirt.brand/patternfly/components/bootstrap/dist/css/bootstrap.min.css" />
        <link rel="stylesheet" type="text/css" href="theme/00-ovirt.brand/patternfly/css/patternfly.min.css" />

        <%@ include file="WEB-INF/warning-template.html"%>
        <script src="html-console-common.js"></script>
        <link rel="stylesheet" type="text/css" href="html-console-common.css" />

        <script>
            var host = null, port = null;
            var sc;

            function spice_query_var(name, defvalue) {
                var match = new RegExp('[?&]' + name + '=([^&]*)')
                        .exec(window.location.search);
                return match ?
                        decodeURIComponent(match[1].replace(/\+/g, ' '))
                        : defvalue;
            }

            function spice_error(e)
            {
                disconnect();
            }

            function disconnect()
            {
                console.log(">> disconnect");
                if (sc) {
                    sc.stop();
                }
                console.log("<< disconnect");
            }

            /**
             * @return {string} hostname of server to connect to
             */
            function getHost() {
                return spice_query_var('host', window.location.hostname);
            }

            /**
             * @return {URL} websocket url
             */
            function getUrls(connectionTicket) {
                var host = getHost();
                var port = spice_query_var('port', window.location.port);
                var path = connectionTicket;

                if ((!host) || (!port)) {
                    throw new Error('Must specify host and port in URL');
                }

                var urlString = 'wss://' + host + ':' + port + '/' + path;
                return new URL(urlString);
            }

            function connectToConsole (evt) {
                if (!(evt.data instanceof Object) || !evt.data.password || !evt.data.connectionTicket) {
                    alert("Incorrect connection data");
                    return;
                }
                var password = evt.data.password;

                try {
                    // If title is sent as a parameter in the url, set the window title accordingly
                    document.title = spice_query_var('title', 'Spice Javascript client');
                    var url = getUrls(evt.data.connectionTicket);
                    checkConnection(url.toString(),
                            doConnectToConsole.bind(undefined, url.toString(), password),
                            reportServerUnreachable.bind(undefined, url.origin));
                } catch(e) {
                    alert(e);
                }
            }

            function reportServerUnreachable(websocketUrl) {
                var certificateUrl = window.location.origin
                        + '/ovirt-engine/services/pki-resource?resource=ca-certificate&format=X509-PEM-CA';

                var bullets = [];
                bullets.push('websocket proxy service is running,');
                bullets.push('firewalls are properly set,');
                if (location.hostname != getHost()) {
                    bullets.push('application is accessed using predefined hostname <code>' + getHost() + '</code>');
                }
                if (!SpiceMainConn) {
                    bullets.push('package <code>spice-html5</code> is installed,');
                }
                bullets.push('websocket proxy certificate is trusted by your browser. <a href="' + certificateUrl + '">Default CA certificate</a>.');
                var warningContent = $($.parseHTML('<strong>Can\'t connect to websocket proxy server</strong> <code>' + websocketUrl + '</code>. Please check that:'))
                        .add(toBulletList(bullets));
                showWarning(warningContent, $('#alert-container'));
            }

            function doConnectToConsole(url, password) {
                if (sc) {
                    sc.stop();
                }

                try {
                    sc = new SpiceMainConn({uri: url, screen_id: "spice-screen", dump_id: "debug-div",
                        message_id: "message-div", password: password, onerror: spice_error });
                } catch (e) {
                    alert(e.toString());
                    disconnect();
                }
            }

            /*
             * Toggles messages div visibility
             */
            function toggleMessages() {
                var messagesStyle = document.getElementById("message-div").style;
                if (messagesStyle.display == "none") {
                    messagesStyle.visibility = "visible";
                    messagesStyle.display = "block";
                } else {
                    messagesStyle.display = "none";
                }
            }

            /*
             * This functions emulates the sendCtrlAltDel from inputs.js from
             * version 0.1.3. As soon as we upgrade to that version, this function
             * should be removed!
             */
            function sendCtrlAltDel() {
                if (sc && sc.inputs && sc.inputs.state === "ready") {
                    var key = new SpiceMsgcKeyDown();
                    var msg = new SpiceMiniData();

                    update_modifier(true, KEY_LCtrl, sc);
                    update_modifier(true, KEY_Alt, sc);

                    key.code = KEY_KP_Decimal;
                    msg.build_msg(SPICE_MSGC_INPUTS_KEY_DOWN, key);
                    sc.inputs.send_msg(msg);
                    msg.build_msg(SPICE_MSGC_INPUTS_KEY_UP, key);
                    sc.inputs.send_msg(msg);

                    if(Ctrl_state == false) update_modifier(false, KEY_LCtrl, sc);
                    if(Alt_state == false) update_modifier(false, KEY_Alt, sc);
                }
            }

            if (window.addEventListener) {
                // For standards-compliant web browsers
                window.addEventListener("message", connectToConsole, false);
            } else {
                window.attachEvent("onmessage", connectToConsole);
            }
        </script>

        <style type="text/css">
            body {
                /* fix of patternfly override */
                background-color: #999;
            }

            .control-panel {
                margin-top: 5px;
                margin-left: auto;
                margin-right: auto;
                line-height: 1.1em;
                width: 800px;
                min-height: 20px;
                padding: 5px;
                border: solid #222222 1px;
                background-color: #333333;
            }
        </style>
    </head>

    <body>
        <div id="alert-container"></div>

        <div id="login" style="display: none">
            <span class="logo">SPICE</span>
        </div>

        <div id="spice-area">
            <div id="spice-screen" class="spice-screen"></div>
        </div>

        <div class="control-panel">
            <button type="button" onclick="sendCtrlAltDel()">
                Send Ctrl-Alt-Delete
            </button>
            <button type="button" onclick="toggleMessages()">
                Toggle messages output
            </button>
        </div>

        <div id="message-div" class="spice-message" style="display: none"></div>

        <div id="debug-div" style="display: none">
        <!-- If DUMPXXX is turned on, dumped images will go here -->
        </div>

    </body>
</html>
