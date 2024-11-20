#!/bin/bash

[ -z "${CDCTI_HOME}" ] && echo "Error: Environment variable CDCTI_HOME is not set. Please refer to /scripts/README.md for instructions" && exit 1
source "$CDCTI_HOME/scripts/lib/common.sh"
source "$CDCTI_HOME/scripts/lib/api.sh"

# default values
DEFAULT_SENDER_ORG=flexion
DEFAULT_SENDER_PRIVATE_KEY_PATH="$TI_LOCAL_PRIVATE_KEY_PATH"
ENVIRONMENT=local
ROOT_PATH=$CDCTI_HOME/examples/
CONTENT_TYPE=application/hl7-v2
SENDER_ORG=flexion
SENDER="$DEFAULT_SENDER_ORG.simulated-sender"

parse_args "rs" "$@" || {
    show_usage "$(basename "$0")"
    exit 0
}

setup_credentials

handle_api_request "rs" "$@"
