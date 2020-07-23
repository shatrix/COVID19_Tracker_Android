# Coronavirus COVID-19 Tracker Android Application [![HitCount](http://hits.dwyl.com/shatrix/COVID19_Tracker_Android.svg)](http://hits.dwyl.com/shatrix/COVID19_Tracker_Android)
This application displays the latest updates based on numbers from https://www.worldometers.info/coronavirus/

## Download APK & install: https://tinyurl.com/tsvjowr
As Google suspended all COVID-19 Tracking applications from PlayStore except the ones from WHO or an official governmental organization, that's why I had to build it as an APK to be installed manually and latest version will be always available on my GoogleDrive here https://tinyurl.com/tsvjowr

<p align="center">
  <img src="screenshots/covid-19_tracker_screen_001.png" width="400" title="COVID-19 Tracker">
</p>

## Required Permissions
    android.permission.INTERNET

## Project Config
    package: com.shatrix.covid19tracker
    compileSdkVersion: 29
    buildToolsVersion: 29.0.3
    minSdkVersion: 16
    targetSdkVersion: 29
    appcompat: 1.0.2

## External libs
jsoup: Java HTML Parser 1.13.1 https://jsoup.org

## Description
The source code is pretty simple, so simple 😅, with each refresh I get the full #HTML page of worldometers tracking page using #jsoup library, then I parse the table contents to get all the numbers I need (total cases, recovered, active, deaths; globally and for each country) the I just display them 📲

I know it's not the best way to depend on a website as it might change in the future, but when I started to code there was no APIs available to get the numbers in json or any other format, but now there's, so maybe I'll update the code to use one of them. Plus I'm not even an Android active developer 🤷

PS: the App can't be uploaded to #Google #PlayStore as they've suspected all Coronavirus tracking apps, except the ones from #WHO or an official governmental organization.

## Contributing
COVID-19 Tracker maintainer: Sherif Mousa <sherif.e.mousa@gmail.com>
