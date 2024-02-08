# Ring A Friend

Ring A Friend is an app designed to call make a phone ring, even when it's on silent. It is designed for people who leave their phone on silent or tend to not pick up calls. It rings till the user dismisses the ringer.

## Installation

Ring A Friend requires [Android Studio](https://developer.android.com/studio) (I'm using Android Studio Hedgehog | 2023.1.1 Patch 1)

You will also have to add [google-services.json](https://developers.google.com/android/guides/google-services-plugin#adding_the_json_file) to the app

Add the following properties to the `local.properties`
```bash
BACKEND_URL="https://server.url/" # Must be https
PHONE_NUMBER="+11234567890" # Must have 
```