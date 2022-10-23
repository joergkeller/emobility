# General Status

http://platz13.local/api/generalStatus/

Headers:
````
Authorization: Bearer **************
````
Response:
````
{
  "authorization": {
    "type": "ChargerWhitelistOnly"
  },
  "centralSystem": {
    "status": "Disabled"
  },
  "evses": [
    {
      "evseId": 1,
      "evseCode": "1",
      "connectors": [
        {
          "connectorId": 1,
          "numberOfPhases": "Three",
          "type": "CableType2",
          "maxCurrentL1": 0,
          "maxCurrent1Phase": 16,
          "maxCurrent3Phase": 29,
          "hardwareMaxCurrent": 29,
          "hardwareMaxVoltage": 230.0,
          "onlineData": {
            "status": "Available",
            "mode": "Fast",
            "sessionId": 34,
            "numberOfPhases": "Three",
            "targetCurrent": 6,
            "phase1Current": 0.0,
            "phaseUsed": "L123",
            "power": 0.0,
            "consumedEnergy": 62.832631,
            "chargingDuration": "1.12:44:26",
            "plannedDepartureTime": "2022-10-07T05:30:00Z",
            "departureTimeMode": "Selected",
            "reserverdTime": "0001-01-01T00:00:00",
            "faultEventType": null,
            "faultEventDescription": null,
            "totalOperatingTme": "11.17:02:14"
          },
          "lastSession": {
            "chargingDuration": "13:15:54",
            "chargingEndedTime": "2022-10-07T09:22:18.738429Z",
            "energyConsumed": 62.83263,
            "chargingSessionStopReason": "LocalStopUserDisconnectedCableFromVehicle"
          }
        }
      ]
    }
  ],
  "gsm": {
    "isInstalled": false,
    "isEnabled": false,
    "linkIsUp": false,
    "linkQuality": 0
  },
  "wifi": {
    "isInstalled": true,
    "isEnabled": true,
    "linkIsUp": true,
    "linkQuality": 81
  },
  "powerManagement": {
    "loadGuard": {
      "isInstalled": true,
      "isConnected": true,
      "buildingTotalPower": 1.5322,
      "maxCurrentL1": 60.0,
      "maxCurrentL2": 60.0,
      "maxCurrentL3": 60.0
    },
    "cluster": {
      "isEnabled": true,
      "allChargersConnected": true,
      "totalPower": 0.0,
      "maxCurrentL1": 25.0,
      "maxCurrentL2": 25.0,
      "maxCurrentL3": 25.0
    }
  },
  "latestFaults": 0,
  "chargerTotalOperatingTime": "8170.12:32:04"
}
````

# Sessions

http://platz13.local/api/chargingSession/

Fields:
````
pageSize: 100
pageNumber: 1
orderByColumn: chargingStartedTime
orderDirection: Descending
chargingStartedTimeFrom: 2022-10-05 00:00:00
chargingStartedTimeTo: 2022-10-09 23:59:59
identificationSource: 
identificationType: 
chargingSessionStopReason: 
identificationCode: 
userId: 
````
Headers:
````
````
Response:
````
{
  "errorCode": 0,
  "errorMessage": "",
  "content": [
    {
      "userIdentification": {
        "userIdentificationId": 1,
        "userId": 2,
        "userIdentificationType": "RfidCard",
        "userIdentificationSource": "ChargerLocal",
        "userIdentificationStatus": "Active",
        "identificationCode": "74592454",
        "number": "0055",
        "expirationDate": null,
        "whiteListVersion": null,
        "insertedTime": "2022-04-14T13:31:19Z",
        "insertedByOperatorId": null,
        "deletedTime": null,
        "deletedByOperatorId": null,
        "deleted": false
      },
      "user": {
        "UserId": 2,
        "FirstName": "Parkplatz",
        "LastName": "13",
        "Blocked": false,
        "InsertedTime": "2022-04-14T13:31:02Z",
        "InsertedByOperatorId": null,
        "DeletedTime": null,
        "DeletedByOperatorId": null,
        "Deleted": false
      },
      "chargingSessionId": 34,
      "connectorId": 1,
      "identificationCode": "74592454",
      "parkingStartedTime": null,
      "vehicleConnectedTime": "2022-10-06T20:06:21.9926007Z",
      "chargingStartedTime": "2022-10-06T20:06:24.0617736Z",
      "chargingEndedTime": "2022-10-07T09:22:18.738429Z",
      "vehicleDisconnectedTime": "2022-10-07T09:22:16.6515391Z",
      "parkingEndedTime": null,
      "timeSpentCharging": "13:15:54",
      "meterValueStart": 1032561897,
      "meterValueEnd": 1095394528,
      "activeEnergyConsumed": 62.83263,
      "maxSessionPower": 10.688,
      "chargingSessionStatus": "Finished",
      "chargingAuthorizationId": 51,
      "chargingReservationId": null,
      "userId": 2,
      "userIdentificationId": 1,
      "userVehicleId": null,
      "vehicleMeasuredMinCurrent": null,
      "vehicleMeasuredMaxCurrent": null,
      "vehicleMeasuredNumberOfPhases": null,
      "chargingMode": "Fast",
      "selectedDepartureTime": "2022-10-07T05:30:00Z",
      "proposedDepartureTime": null,
      "requiredEnergy": null,
      "chargingSessionStopReason": "LocalStopUserDisconnectedCableFromVehicle",
      "energyCosts": null,
      "gridConnectionCosts": null,
      "powerManagementSavings": null,
      "powerManagementOverloadInterventions": null,
      "backendTransactionId": null,
      "sentToPrimaryMaster": true,
      "sentToSecondaryMaster": false
    },
    ...
  ],
  "pagingInfo": {
    "numOfRows": 2,
    "pageCount": 1
  }
}
````

# Power Profile

http://platz13.local/api/powerProfile/

Fields:
````
    fromTime: 2022-10-08T02:49:36.989Z
    measure: connector1,building,cluster
    toTime: 2022-10-08T08:50:46.895Z
````

Headers:
````
Authorization: Bearer **************
````

Response:
````
[
    {
        "time": "2022-10-08T02:50:00+00:00",
        "connector1": 0.0,
        "building": 0.71041,
        "cluster": 0.0
    },
    {
        "time": "2022-10-08T02:55:00+00:00",
        "connector1": 0.0,
        "building": 0.79635996,
        "cluster": 0.0
    },
    ...
]
````
* Power values are kW
* Time interval 5 min
