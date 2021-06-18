# RSS Reader Android app


## Requirements
App requires at least Android API 26.
It also requires Google Play Services to be installed in order to establish user's location.


## Description
This project contains a simple RSS reader Android app written as student assignment.
Some quirks in usage are caused by specific assignment requirements or time constraints
(e.g. picked specific RSS channels or lack of unit tests). 

App allows to browse and read entries from a RSS channel, as well as save some of them to favourites.
Read RSS entries will be greyed out to indicate that user have already read them.

App will also check in the background whether new RSS entries are available and display notifications for them.


### Storing user data
User data is stored using [Firebase](https://firebase.google.com/) platform.

To use the app you have to login, either using a Google account, or by creating you own account using email address and password.

All user data is stored in Firebase - both authentication data, as well as favourite and read RSS entries
(as entries in [Firestore](https://firebase.google.com/docs/firestore)).
This means that your data will be synchronized across multiple devices.


### RSS channels
App picks one of the following RSS channels:
 - https://www.polsatnews.pl/rss/polska.xml
 - https://www.polsatnews.pl/rss/swiat.xml

One of these channels is picked based on users location. Polish feed will be loaded when user is located within Poland.
International feed will be loaded when user is located outside of Poland, app wasn't granted location permissions,
or error occurred during establishing location.

Those channels were picked only due to their structure - they were quite easy to parse from XML into objects.
One of the assignment's requirements was to implement parsing direct response from channel as XML,
without using external service parsing RSS to JSON format.


### Background updates
App uses a `androidx.work.Worker` and a `androidx.work.PeriodicWorkRequest` queued by `androidx.work.WorkManager`
to perform it's background operations.

In the background app will use RSS channel loaded before to check whether any new entries can be loaded.
For each new entry a notification will be displayed. Tapping the notification will display detailed view of this entry.

This `Worker` will be stopped when user logs out.

Currently app check for updates every 15 minutes (as often as possible).
It's not great from usability and battery usage perspective, but it works well as a demonstration.


## Building and using the app
In order to use this app you have to plug it in your own [Firebase](https://firebase.google.com/) backend.
To do that you have to [create a new Firebase project](https://console.firebase.google.com)
and [add new `google-services.json` file to the project](https://firebase.google.com/docs/android/setup).

Your Firebase backend has to allow for email and password login as well as login via a Google account
(including [adding app's SHA-1 fingerprint](https://firebase.google.com/docs/auth/android/google-signin?hl=en#before_you_begin))
for all functionalities to work correctly.

Then just initialize Firestore Database so that data regarding favourite and read entries is stored.
All required documents and collections will be created by the app itself.

You can also modify Firestore security rules, so only users with validated email address (or logged in via Google)
can access entries. App won't allow login via email without email verification, so this restriction matches
login restrictions in app itself.

In my testing I've used rules as follows (not the most elegant, but they work):

```js
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{userId}/{document=**} {
      allow read, write: if request.auth != null
          && request.auth.uid == userId
          && request.auth.token != null
          && (request.auth.token.email_verified == true
              || (request.auth.token.firebase != null
                  && request.auth.token.firebase.sign_in_provider == "google.com"));
    }
  }
}
```


## Issues
Unfortunately due to time constraints app has a few issues.

Only Polish language is available, although all interface strings are located in resources and not in Kotlin code.
Unfortunately there still might be some issues will translation due to sentence structure.

Unit tests for the app are missing (again due to time constraints).

App also uses a few deprecated methods/classes, such as `androidx.fragment.app.FragmentStatePagerAdapter`
used to display tabbed view as it's used by default by Android Studio.