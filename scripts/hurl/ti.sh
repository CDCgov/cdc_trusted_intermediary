#!/bin/bash

# Check if $CDCTI_HOME is set
if [ -z "$CDCTI_HOME" ]; then
    echo "Error: CDCTI_HOME is not set. Please set this environment variable before running the script."
    exit 1
fi

# default values
env=local
root=$CDCTI_HOME/examples/
client=report-stream
verbose=""

show_help() {
    echo "Usage: $(basename $0) <HURL_FILE> [OPTIONS]"
    echo
    echo "Options:"
    echo "    -f <REL_PATH>         The path to the hl7/fhir file to submit, relative the root path (Required for orders and results APIs)"
    echo "    -i <SUBMISSION_ID>    The submissionId to call the metadata API with (Required for orders, results and metadata API)"
    echo "    -r <ROOT_PATH>        The root path to the hl7/fhir files (Default: $root)"
    echo "    -e [local | staging]  The environment to run the test in (Default: $env)"
    echo "    -c <CLIENT>           The client id to use (Default: $client)"
    echo "    -j <JWT>              The JWT to use for authentication"
    echo "    -v                    Verbose mode"
    echo "    -h                    Display this help and exit"
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

hurl_file=ti/"$1" # Assign the first argument to hurl_file
shift             # Remove the first argument from the list of arguments

while getopts ':f:r:e:c:j:i:vh' opt; do
    case "$opt" in
    f)
        fpath="$OPTARG"
        ;;
    i)
        submission_id="--variable submissionid=$OPTARG"
        ;;
    r)
        root="$OPTARG"
        ;;
    e)
        env="$OPTARG"
        ;;
    c)
        client="$OPTARG"
        ;;
    j)
        jwt="$OPTARG"
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
    url=http://$host:8080
    if [ -z "$jwt" ] && [ "$client" = "report-stream" ]; then
        jwt=$(cat "$CDCTI_HOME/mock_credentials/report-stream-valid-token.jwt")
    fi
elif [ "$env" = "staging" ]; then
    host=cdcti-stg-api.azurewebsites.net
    url=https://$host:443
else
    echo "Error: Invalid environment $env"
    show_help
    exit 1
fi

if [ -z "$jwt" ]; then
    echo "Error: Please provide the JWT for $client"
    exit 1
fi

hurl \
    --variable fpath=$fpath \
    --file-root $root \
    --variable url=$url \
    --variable client=$client \
    --variable jwt=$jwt \
    $submission_id \
    $verbose \
    $hurl_file \
    $@
