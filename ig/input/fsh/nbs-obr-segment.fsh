Logical: OBRSegment
Id: segment-obr-logical-model
Title: "OBR Segment"
Description: "The OBR Segment"
* setOBRId 1..1 SU positiveInt "OBR-1 Set ID"
* placerOrderNumber 1..1 SU string "OBR-2 Placer Order Number"
* placerOrderNumberEntityIdentifier 1..1 SU string "OBR-2.1 Placer Order Number Entity Identifier"
* placerOrderNumberNamespaceID 1..1 SU string "OBR-2.2 Placer Order Number Namespace ID"
* placerOrderNumberUniversalID 1..1 SU string "OBR-2.3 Placer Order Number Universal ID"
* placerOrderNumberUniversalIDType 1..1 SU code "OBR-2.4 Placer Order Number Universal ID Type"
* universalServiceIdentifier 1..1 SU string "OBR-4 Universal Service Identifier"
* observationDateTime 1..1 SU string "OBR-7 Observation Date/Time"
// other optional fields


// Provenance Section begins
Instance: segment-obr-logical-model-history-create
InstanceOf: Provenance
Title: "Removal of OBR-3, OBR-8, OBR-15 and OBR-16 segments"
Usage: #definition
* target[+] = Reference(StructureDefinition/segment-obr-logical-model)
* recorded = "2023-09-07T00:00:00.0000Z"
* occurredDateTime = "2023-09-07"
* reason = http://terminology.hl7.org/CodeSystem/v3-ActReason#METAMGT
* reason.text = "Updated segments to match the ones we initially support"
* activity = http://terminology.hl7.org/CodeSystem/v3-DataOperation#CREATE
* agent[+].type = http://terminology.hl7.org/CodeSystem/provenance-participant-type#author
* agent[=].who.display = "jorg3lopez"
