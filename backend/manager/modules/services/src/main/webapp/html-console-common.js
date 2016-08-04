'use strict';

/**
 * @param {jQuery} content warning dialog content
 * @param {jQuery} targetContainer element into which the warning will be inserted
 */
function showWarning(content, targetContainer) {
    var templateText = $('#warning-template').html()
    var template = $($.parseHTML(templateText)
        .filter(function(node) { return node.nodeType === 1 })[0]);
    var placeholder = template.find('.placeholder');
    placeholder.replaceWith(function() { return content; });
    targetContainer.append(template);
}

/**
 * It checks accessibility of websocket server.
 * @param {string} url websocket server url
 * @param {Function} onSuccess callback called if server can be connected to
 * @param {Function} onFail callback called if server can't be connected to
 */
function checkConnection(url, onSuccess, onFail) {
    var done = false;
    var socket;
    try {
        socket = new WebSocket(url, 'binary');
    } catch (ex) {
        onFail();
        return;
    }
    socket.onopen = function() {
        if (done) {
            return;
        }
        done = true;
        socket.close();
        onSuccess();
    };
    socket.onerror = function() {
        if (done) {
            return;
        }
        done = true;
        socket.close();
        onFail();
    };
}

/**
 * @param {Array<string>} bullets
 * @return {jQuery} `ul` list
 */
function toBulletList(bullets) {
    var ul = $('<ul>');
    bullets.forEach(function (bullet) {
        ul.append('<li>' + bullet + '</li>');
    });
    return ul;
}
