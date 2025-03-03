# Ring A Friend

Ring A Friend is an app designed to call make a phone ring, even when it's on silent. It is designed for people who leave their phone on silent or tend to not pick up calls. It rings till the user dismisses the ringer.

## Installation

Ring A Friend requires [Android Studio](https://developer.android.com/studio) (I'm using Android Studio Ladybug | 2024.2.1 Patch 2)

You will also have to add [google-services.json](https://developers.google.com/android/guides/google-services-plugin#adding_the_json_file) to the app.  
**Note: Before generating the `google-services.json` file you must generate the admin sdk json for [RingAFriendBackend](https://github.com/joel122002/RingAFriendBackend). Always generate a new `google-services.json` whenever you create a new admin sdk json**

Add the following properties to the `local.properties`
```bash  
BACKEND_URL="https://server.url/some_path" # Must be https  
BACKEND_SOCKET_URL="https://server.url/" # Must be https and must only contain domain (notice how there's no "some_path")  
BACKEND_SOCKET_PATH="/socket.io/" # Path which responds to socket.io requests. By default it is "/socket.io/"  
PHONE_NUMBER="+11234567890" # Must have   
```