# Sample message files

> [!IMPORTANT]
> Any new files added to the `examples/` folder should be updated to have `D` in `MSH-11`, except for files added to the `examples/Test/Automated/` folder, which should be updated to have `N` in `MSH-11`

## Naming convention

`<XXX>_<SS>_<HL7_Type>_<HL7_Version>_<SUB_TYPE>_<DESCRIPTION>.[fhir|hl7]`
- `XXX`: incremental id for the message. This id will not change after translation/transformation (e.g. `001`)
- `SS`: state initials or organization name (e.g. `CA`, `Natus`)
- `HL7_Type`: HL7 message type (e.g. `ORU_R01`)
- `HL7_Version`: HL7 version with underscores instead of dots (e.g. `2_5_1`)
- `SUB_TYPE`: specific HL7 message type (e.g. `NBS`)
- `DESCRIPTION`: any additional description to identify the message type. For message flow description, we'll use this convention:
  - `0_initial_message`
  - `1_hl7_translation`
  - `2_fhir_transformation`
  - `3_hl7_translation_final`
- File extension: either `hl7` or `fhir`

## Routing of these files

To avoid routing issues, we have decided to use `MSH-11` for routing of test messages ([more context here](/adr/026-hl7-test-message-routing.md)). The values we have decided to use are:
- `D`: for test files **not** to be sent to partners and to be sent manually. Any files under `examples/` and not in `examples/Test/Automated/Assertion/` should have this value
- `N`: for test files **not** to be sent to partners and sent by a scheduled task. Any files under `examples/Test/Automated/Assertion/` should have this value
- `T`: for test files to be sent manually to partners. `P` will also be routed to partners

**Note**: for some sample files, our transformations **will** rewrite the `MSH-5` and/or `MSH-6` HL7 fields normally used for routing, so we can't rely only on those fields to route. This is the case for most of the files in the `examples/CA` folder. If you are sending any files in that folder and you don't want the message to be delivered to our partner, please make sure `MSH-11` is **not** `T` or `P`. Otherwise the message will be delivered to our partner regardless of what is there in `MSH-5` and `MSH-6`. Please see [this ADR](/adr/026-hl7-test-message-routing.md) for more context

## Regeneration of file snapshots

Files in the `examples/` with the suffix `1_hl7_translation`, `2_fhir_transformation` and `3_hl7_translation_final` are snapshots of its corresponding file ending in `0_initial_message`. They should not be manually updated, only by running them through RS

In order to keep the snapshots up-to-date, we have a script that automates the regeneration of this files: `update_examples.sh`, found [here](/scripts/hurl/). Running that script should update all the necessary files

## Notes

- The `ORM` messages with ids `003`, `004`, `005`, `006`, `007`, `008`, `009`, `010` in the `Test/Orders` folder were modified to comply with current requirements for ReportStream, as it doesn't yet support HL7 `2.3`:
  - Added `MSH-9.3`
  - Changed `MSH-10` to `2.5.1`
- The `MSH-11` value for all sample files in `examples/` (with the exception of files in `examples/Test/Automated/Assertion/`) was changed to `D`. This is to comply with our routing filters in RS for test messages

## Previously renamed files

- `fhir/MN NBS FHIR Order Message.json` => `Test/e2e/orders/002_ORM_O01.fhir`
- `fhir/lab_order.json` => `Test/e2e/orders/001_OML_O21_short.fhir`
- `fhir/newborn_patient.json` => `Test/e2e/demographics/001_Patient_NBS.fhir`
- `fhir/oml_message.json` => `Other/004_OML_O21.fhir`
- `hl7/AL/Baptist ORM 1.txt` => `Oracle/001_Oracle_ORM_O01.hl7`
- `hl7/AL/Baptist ORM 2.txt` => `Oracle/002_Oracle_ORM_O01.hl7`
- `hl7/AL/Baptist ORU.hl7` => `Oracle/004_Oracle_ORU_R01.hl7`
- `hl7/CA/CA NBS OML_O21 Lab Order Message.txt` => `CA/001_CA_OML_O21.hl7`
- `hl7/CA/CA NBS ORU_R01 Lab Result Message.txt` => `CA/002_CA_ORU_R01.hl7`
- `hl7/LA/Ochsner ORM.txt` => `Epic/001_Epic_ORM_O01.hl7`
- `hl7/LA/Ochsner ORU.txt` => `Epic/002_Epic_ORU_R01.hl7`
- `hl7/MN/ADT_A01.txt` => `MN/005_MN_ADT_A01.hl7`
- `hl7/MN/MN NBS OML_O21 Lab Order Message.txt` => `MN/002_MN_OML_O21_NBS.hl7`
- `hl7/MN/MN NBS ORM_O01 Lab Order Message.txt` => `MN/003_MN_ORM_O01_NBS.hl7`
- `hl7/MN/MN NBS ORM_O01 Lab Order Message Extended.txt` => `MN/003_MN_ORM_O01_NBS_extended.hl7`
- `hl7/MN/MN NBS ORU_R01 Lab Result Message.txt` => `MN/004_MN_ORU_R01_NBS_0_initial_message.hl7`
- `hl7/MN/ORM simplified.txt` => `MN/003_MN_ORM_O01_NBS_simplified.hl7`
- `hl7/OML_021.hl7` => `NewSTEPs/001_NewSTEPs_OML_021.hl7`
- `hl7/ORU_R01.hl7` => `NewSTEPs/002_NewSTEPs_ORU_R01.hl7`
- `hl7/ADT_A01.txt` => `Other/001_ADT_A01.hl7`
