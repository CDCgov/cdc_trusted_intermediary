This folder contains the HL7 sample files that are used in the automated ReportStream
integration tests. The [automated-staging-test-submit.yml](/.github/workflows/automated-staging-test-submit.yml)
Github workflow that runs daily will grab these files and send them to ReportStream in staging.
The files are expected to go through the whole flow and to be delivered to an Azure blob
container that will later be used by the
[automated-staging-test-run.yml](/.github/workflows/automated-staging-test-run.yml) workflow to run tests on them.

## Requirements for the HL7 files

The files are required to:

- Be a valid HL7 file
- Be a supported HL7 message type: `ORM`, `OML` or `ORU`
- Have `automated-staging-test-receiver-id` in `MSH-6.2` in order to be routed correctly
  - If it's a sample file for UCSD, which has transformation that will overwrite `MSH-6.2`, the HL7 is required to have the prefix `AUTOMATEDTEST-` in `MSH-10` as a workaround - there is special routing in ReportStream to handle this
- Each file must have a unique value in `MSH-10`. We use this value to match the input and output files, so if it's not unique, we won't be able to match the files correctly
  - We format `MSH-10` based on the file index, like `001` (or `AUTOMATEDTEST-001` for UCSD)
- `MSH-11` needs to have a value of `N`
