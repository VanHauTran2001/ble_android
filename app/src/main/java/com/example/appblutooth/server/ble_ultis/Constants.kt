package com.example.appblutooth.server.ble_ultis

import java.util.UUID

object Constants {
    var SERVICE_STRING = "7D2EA28A-F7BD-485A-BD9D-92AD6ECFE93E"
    var SERVICE_UUID = UUID.fromString(SERVICE_STRING)
    var CHARACTERISTIC_ECHO_STRING = "7D2EBAAD-F7BD-485A-BD9D-92AD6ECFE93E"
    var CHARACTERISTIC_ECHO_UUID = UUID.fromString(CHARACTERISTIC_ECHO_STRING)
    var CHARACTERISTIC_TIME_STRING = "7D2EDEAD-F7BD-485A-BD9D-92AD6ECFE93E"
    var CHARACTERISTIC_TIME_UUID = UUID.fromString(CHARACTERISTIC_TIME_STRING)
    var CLIENT_CONFIGURATION_DESCRIPTOR_STRING = "00002902-0000-1000-8000-00805f9b34fb"
    var CLIENT_CONFIGURATION_DESCRIPTOR_UUID = UUID.fromString(CLIENT_CONFIGURATION_DESCRIPTOR_STRING)
    const val CLIENT_CONFIGURATION_DESCRIPTOR_SHORT_ID = "2902"

}