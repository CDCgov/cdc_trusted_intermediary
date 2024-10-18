# ReportStream Hurl Script

## Usage

```
Usage: ./hrl <HURL_FILE> [OPTIONS]

Options:
    -f <REL_PATH>                       The path to the hl7/fhir file to submit, relative the root path (Required for waters API)
    -r <ROOT_PATH>                      The root path to the hl7/fhir files (Default: $CDCTI_HOME/examples/)
    -t <CONTENT_TYPE>                   The content type for the message (e.g. 'application/hl7-v2' or 'application/fhir+ndjson') (Default: application/hl7-v2)
    -e [local | staging | production ]  The environment to run the test in (Default: local)
    -c <CLIENT_ID>                      The client id to use (Default: flexion)
    -s <CLIENT_SENDER>                  The client sender to use (Default: simulated-sender)
    -x <KEY_PATH>                       The path to the client private key for the environment
    -i <SUBMISSION_ID>                  The submissionId to call the history API with (Required for history API)
    -v                                  Verbose mode
    -h                                  Display this help and exit
```

## Examples

Sending an order to local environment

```
./hrl waters.hurl -f Test/Orders/003_AL_ORM_O01_NBS_Fully_Populated_0_initial_message.hl7
```

Sending a result to local environment

```
./hrl waters.hurl -f Test/Results/002_AL_ORU_R01_NBS_Fully_Populated_0_initial_message.hl7
```

Sending an order to staging

```
./hrl waters.hurl -f Test/Orders/003_AL_ORM_O01_NBS_Fully_Populated_0_initial_message.hl7 -e staging -x /path/to/staging/private/key
```

Checking the history in local environment for a submission id

```
./hrl history.hurl -i 100
```

Checking the history in staging for a submission id

```
./hrl history.hurl -i 100 -e staging -x /path/to/staging/private/key
```
