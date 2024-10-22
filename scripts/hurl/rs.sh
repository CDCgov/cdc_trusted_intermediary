#!/bin/bash

# Check if $CDCTI_HOME is set
if [ -z "$CDCTI_HOME" ]; then
    echo "Error: CDCTI_HOME is not set. Please set this environment variable before running the script."
    exit 1
fi

# default values
env=local
root=$CDCTI_HOME/examples/
content_type=application/hl7-v2
client_id=flexion
client_sender=simulated-sender
verbose=""
submission_id=""

show_help() {
    echo "Usage: $(basename $0) <HURL_FILE> [OPTIONS]"
    echo
    echo "Options:"
    echo "    -f <REL_PATH>                       The path to the hl7/fhir file to submit, relative the root path (Required for waters API)"
    echo "    -r <ROOT_PATH>                      The root path to the hl7/fhir files (Default: $root)"
    echo "    -t <CONTENT_TYPE>                   The content type for the message (e.g. 'application/hl7-v2' or 'application/fhir+ndjson') (Default: $content_type)"
    echo "    -e [local | staging | production ]  The environment to run the test in (Default: $env)"
    echo "    -c <CLIENT_ID>                      The client id to use (Default: $client_id)"
    echo "    -s <CLIENT_SENDER>                  The client sender to use (Default: $client_sender)"
    echo "    -x <KEY_PATH>                       The path to the client private key for the environment"
    echo "    -i <SUBMISSION_ID>                  The submissionId to call the history API with (Required for history API)"
    echo "    -v                                  Verbose mode"
    echo "    -h                                  Display this help and exit"
}

# Check if required HURL_FILE is provided
if [ $# -eq 0 ]; then
    echo "Error: Missing required argument <HURL_FILE>"
    show_help
    exit 1
fi

# Check if first argument is -h
if [ "$1" = "-h" ]; then
    show_help
    exit 0
fi

hurl_file=rs/"$1" # Assign the first argument to hurl_file
shift             # Remove the first argument from the list of arguments

while getopts ':f:r:t:e:c:s:x:i:vh' opt; do
    case "$opt" in
    f)
        fpath="$OPTARG"
        ;;
    r)
        root="$OPTARG"
        ;;
    t)
        content_type="$OPTARG"
        ;;
    e)
        env="$OPTARG"
        ;;
    c)
        client_id="$OPTARG"
        ;;
    s)
        client_sender="$OPTARG"
        ;;
    x)
        secret="$OPTARG"
        ;;
    i)
        submission_id="--variable submissionid=$OPTARG"
        ;;
    v)
        verbose="--verbose"
        ;;
    h)
        show_help
        exit 0
        ;;
    :)
        echo -e "Option requires an argument"
        show_help
        exit 1
        ;;
    ?)
        echo -e "Invalid command option"
        show_help
        exit 1
        ;;
    esac
done
shift "$(($OPTIND - 1))"

if [ "$env" = "local" ]; then
    host=localhost
    url=http://$host:7071
    if [ -z "$secret" ] && [ "$client_id" = "flexion" ]; then
        secret="$CDCTI_HOME/mock_credentials/organization-trusted-intermediary-private-key-local.pem"
    fi
elif [ "$env" = "staging" ]; then
    host=staging.prime.cdc.gov
    url=https://$host:443
elif [ "$env" = "production" ]; then
    host=prime.cdc.gov
    url=https://$host:443
else
    echo "Error: Invalid environment $env"
    show_help
    exit 1
fi

if [ -z "$secret" ]; then
    echo "Error: Please provide the private key for $client_id"
    exit 1
fi

hurl \
    --variable fpath=$fpath \
    --file-root $root \
    --variable url=$url \
    --variable content-type=$content_type \
    --variable client-id=$client_id \
    --variable client-sender=$client_sender \
    --variable jwt=$(jwt encode --exp='+5min' --jti $(uuidgen) --alg RS256 -k $client_id.$client_sender -i $client_id.$client_sender -s $client_id.$client_sender -a $host --no-iat -S @$secret) \
    $submission_id \
    $verbose \
    $hurl_file \
    $@
