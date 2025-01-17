# 25. Automated RS Integration Test

Date: 2024-10-02

## Decisions

1. We decided to create a full end-to-end test that covers the interaction of ReportStream and TI, using
    existing ingestion and delivery mechanisms
2. We decided to create Github Action workflows to schedule two tasks:
   - One workflow submits sample HL7 files to ReportStream in staging, with output files expected to be delivered to an Azure blob container
   - Another workflow later runs the integration tests on the output and input files
3. We decided to use MSH-10 to match the input and output files, and to filter the receivers in ReportStream when MSH-6.2 not available, and MSH-3 to distinguish between our golden copy and assertion tests.

## Status

Accepted.

## Context

### Decision 1

The RS and TI applications each have their own unit and integration tests, but we didn't have any tests
that cover the interaction between RS and TI, and we also didn't have a way to know when changes in
RS have unintended consequences that impact our workflows.

Submitting data to RS using their existing REST endpoints and receiving it using their existing delivery
mechanisms helps make these tests realistic.

### Decision 2

Since we decided to use RS's existing REST endpoints, we needed a way to submit data to them, and a way
to trigger the data flow and subsequent tests on some kind of schedule. We chose Github Actions for this
because it's easy to both schedule them based on a CRON expression and to run them manually as needed. Github
Actions also gave us a lightweight way to send the files to RS without having to add a new service.

We are using two separate actions - the first one sends data to RS, and the second one (currently
scheduled 2 hours after the first) triggers the tests to run. The length of time it takes a file to
run through the whole workflow (from RS to TI to RS to final delivery) usually doesn't take long, but we
built in extra time in case of any issues that cause delays.

### Decision 3

We're using the value in MSH-10 for two purposes: matching input and output files, and some filtering in RS. We use MSH-3 for a similar reason, specifically for filtering and renaming the file to include a flag.

We chose MSH-10 to match files on because it's a value that shouldn't change and should be unique to
a particular message. We're also using it to route these test messages because in some cases, we apply
transformations that will overwrite HL7 fields used for routing (MSH-5 and MSH-6), so we can't rely on those.

## Impact

### Positive

- We will have a way to test the integration between RS and TI
- We will be able to catch issues early when changes in RS break our workflows

### Negative

- We will run daily tests in RS' and our staging infrastructure, which will take up resources

### Risks

- If we forget to add additional assertions when new transformations are added, these tests may give us
  a false sense of confidence
- Because we rely on MSH-10 for matching files, engineers will have to take care in setting this field
  when they create additional tests in future
- If we don't maintain the filtering in RS based on MSH-6.2 and MSH-10, we may not be able to route the test messages
  correctly
- Because we're using RS's existing REST endpoints and staging set up, if RS changes their endpoints or
  the way they handle staging, these tests may break

## Related ADRs

- [Assertion Engine](024-assertion-engine.md)
