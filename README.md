# GNSS_DR

GNSS_DR tracks and logs global navigation satellite systems (GNSS) 

## Features
### GPS Features
1. Ability to track GPS information via [GNSS](https://developer.android.com/guide/topics/sensors/gnss)
    - Latitude, Longitude, Speed, Height, # of Sats, Bearing, Horizontal Accuracy, Vertical Accuracy, Speed Accuracy
    - Individual Satellites (ID, GNSS Type, Elevation, Azim, C/NO)
2. Ability to track GPS information via [FusedLocationProvider](https://developers.google.com/location-context/fused-location-provider)
    - Latitude, Longitude, Speed, Height, Bearing, Horizontal Accuracy, Vertical Accuracy, Speed Accuracy
3. Uses GNSS by default and swtiches to FusedLocationProvider when gps fix is unavailable
4. Ability to choose GPS update frequency (in milliseconds)

### Logging Features
1. Logs GPS information in CSV format to phone's Internal Storage folder

## System Requirements
### Android Device with [support for GNSS](https://docs.google.com/spreadsheets/d/1z6Yt9c4cyev1PB6VWEkbZtJGfoxAQ5UJnHyP24sFwlk/edit#gid=0)
- Target SDK: API 30 (Android 11)
- Minimum SDK: API 28 (Android 9)

### Software
- Android Studio Bumblebee

### API Requirements
- Google Maps API Key (add to google_maps_api.xml)

## Authors
- Hawon Park
- Jeong Ho Shin
