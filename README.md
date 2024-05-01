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

### Installation

1. Clone the repository:

