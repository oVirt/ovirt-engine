#!/bin/sh

function usage() {
    if [ "$1" == "--help" ] || [ "$1" == "-h" ]; then
		print_usage_text
	    exit 1
    fi
}

function print_usage_text() {
echo "
	DESCRIPTION
	    A script that sends a message to engine's REST API events endpoint.
	    Engine creates an event that is written to the events tab and to
	    engine.log with the message supplied to the script.
	    This script is convenient for debugging or scripting purposes with
	    user events making searching the logs friendlier.

	USAGE
	$0 [MESSAGE]

	MESSAGE
	    Any sequence of characters with or without spaces or quotes.
	    Mandatory.

	EXAMPLE
	    $0 my event message
"
}

function custom_id() {
    echo "\"custom_id\": \"$(date +%s)\""
}

function origin() {
    echo "\"origin\": \"external\""
}

function severity() {
    echo "\"severity\": \"normal\""
}

function description() {
    local DESCRIPTION=$@
    echo "\"description\": \"$DESCRIPTION\""
}

function user() {
    echo "admin@internal:admin"
}

function content_type() {
    echo "Content-Type:application/json"
}

function url() {
    echo "http://localhost:8080/ovirt-engine/api/events"
}

function send_event() {
    local DESCRIPTION=$@
	curl --user $(user) --header $(content_type) $(url) -d "
        {
	        $(description "$DESCRIPTION"),
	        $(origin),
	        $(severity),
	        $(custom_id)
	    }
	"
}

usage "$@"
send_event "$@"
