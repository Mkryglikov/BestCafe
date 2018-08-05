# Best Cafe
**Capstone project for the Android developer Nanodegree from Udacity**

That’s the app for minimizing users interactions with the waiters in restaurants, cafes etc. The user makes a booking or comes to a cafe straight away, connects to the Android Things module near the table using Nearby Connections, gets a menu, wifi credentials and connects to cafes network (optionally). The user makes an order on its phone and sends it back to the Android Things module using Nearby Connections, where module sends the order to the Firestore database. Then the app shows estimated waiting time and checks in Android Things module if the waiter has brought the order and if so, shows options to open a menu and order some additional stuff, close the order by paying with Google Pay or call the waiter to pay.

**So the project actually consists of 2 apps: one for clients phone (/app) and one for Android Things module(/things).**

The implication is that the cafes information system will use the same Firestore database as the Android Things module. Cafes information (such as name, menus, table) will be faked and can be changed for a real cafe.

## Installation
**1.** The project needs 3 additional files to be added. **UDACITY REVIEWERS CAN DOWNLOAD THESE FILES FROM THE LINK PROVIDED IN SUBMISSION NOTES AND SKIP THIS STEP**

- *gradle.properties* in the root folder with a few parameters added:

```
GoogleSignInClientId = "YOUR_GOOGLE_OAUTH_2.0_CLIENT_ID"
NearbyServiceId = "YOUR_CUSTOM_NEARBY_SERVICE_ID"
WifiSSID = "YOUR_WIFI_SSID"
WifiPassword = "YOUR_WIFI_PASSWORD"
````
Get your Google Client Id by following [this instruction](https://developers.google.com/identity/sign-in/android/start-integrating#get_your_backend_servers_oauth_20_client_id).

- *google-services.json* in the */app* directory 
- *google-services.json* in the */things* directory

Google services files can be generated automatically by the Android Studio using [Firebase Assistant](https://developer.android.com/studio/write/firebase). Connect **Firebase Authenticate (Email/password and Google sign-in options) and Firebase Firestore** in both project modules. 

**2.** Clients Android app (/app) can be installed on your phone (emulator may not work with Nearby Connections) from Android Studio as usual. 

**3.** In order to run the Android Things module (/things) all you need is [supported Android Things board](https://developer.android.com/things/hardware/) (such as Raspberry Pi 3) with [Android Things installed](https://developer.android.com/things/console/create), connected to the internet and connected to the ADB. Then you can install Android Things module from Android Studio as usual.

In my opinion, the most simple way to install Android Things and connect it to the ADB is the following:
1. [Download](https://drive.google.com/open?id=10TDUoX0KdRSfZEpDlnBW6mkcnlljEGyz) the development Android Things image.
2. Flash it on microSD card using something like [Win32 Disk Imager](https://sourceforge.net/projects/win32diskimager/).
3. Insert card onto your RPi 3, connect the board to the Ethernet (or via WiFi later), external monitor, power and turn it on.
4. When the board is started you should see its local IP address on the monitor (otherwise, you can plug in a USB keyboard to your RPi and connect to the Internet using WiFi), use it to connect to adb using `adb connect IP`.

*No additional peripheral hardware for Android Things is required.*
