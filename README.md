# ShortMessageSender


ShortMessageSender is an Android background service that hosts a NanoHTTPD server to handle HTTP requests. It allows sending SMS messages via HTTP POST requests.

## Features

- Hosts a NanoHTTPD server on port 8080.
- Handles GET and POST requests.
- Sends SMS messages upon receiving HTTP POST requests to the `/sendMessage` endpoint.
- Runs as a foreground service to ensure continuous operation even when the app is closed.

## Getting Started

### Prerequisites

- Android Studio
- Android device or emulator

### Android-Limit:
    
    Android´s default SMS-Limit are 30 SMS to a single phonenumber within 30 minutes.
    You can change your SMS-Limit for your device (root-permission is not required).

    How to change Android-Limit:
    Make sure you have enabled USB-Debugging on your device and you are ready to use ADB.
    Connect your device to the pc and open the terminal.
    Open the adb-shell via the command: adb shell
    Change the value of the SMS-Limit to the number of SMS you want to send within the 30 minutes timeframe. Via the command:
    settings put global sms_outgoing_check_max_count 100
    This command allows you to send 100 SMS to a phonenumber within the 30 minutes timeframe.
    If you want to also change the timeframe, you can use the command:
    settings put global sms_outgoing_check_interval_ms 900000
    This command reduces the timeframe to 15 minutes.
    If you entered both commands, you would be able to send 100 SMS to a phonenumber within 15 minutes.

### Installation

1. Clone the repository:

