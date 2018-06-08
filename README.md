MapQuest Navigation SDK: Reference Sample App
========================

A simple navigation app built as a reference design for customers, and to demonstrate best practices for architecture and UX required
for a typical navigation application.

Please also refer to the "Getting Started" documentation and javadocs for the MapQuest Navigation SDK  on the MapQuest Developer Portal, here:
https://developer.mapquest.com/documentation/nav-sdk/android/


Building the Sample Application Project
---
### MapQuest API Key

In order to use MapQuest APIs and SDKs you will need a MapQuest key. We use this key to associate your requests to APIs with your account. You can find your existing keys or create a new one at: https://developer.mapquest.com/user/me/apps

Once you have a MapQuest Key, per above, create a new file named mapquest.properties in the app directory of your project, containing the following single-line entry:

    api_key=[PUT_YOUR_API_KEY_HERE]

### Building the Project

Select app in the Android Studio build configurations drop down and hit the play button to run the app through the IDE -- or to build an apk in the app/build/outputs/ directory, run:

    ./gradlew assembleDebug
alternately, to build and install the app on a connected device, run:

    ./gradlew installDebug

### How to use this project

This project has several components you can use as a basis for building your own MapQuest-based Navigation application. These areas are designed so that you can reuse the concepts and code in your own projects. Comments have been added to the sample code where appropriate to clarify "why" something is necessary -- and where the intention or logic might not be obvious from the code itself.

## Sample Activities and Supporting Components: Implementation Notes

-----

#### The Navigation UI: Sample App Activities
##### RouteSelectionActivity
The `RouteSelectionActivity` is responsible for allowing a user to define the starting and destination locations for a desired route. It provides a search form to allow a user to search for a location to add to a route request. The map also handles long presses to drop a pin on the map and add a location to the route.

A network request is made to the MapQuest `RouteService` with these coordinates as parameters -- along with any `RouteOptions`, which can be used to specify restrictions such as "avoid highways", "avoid toll roads", etc.

One or more alternate `Routes` are returned from the `RouteService`, and these are plotted on the `MapView`. The user chooses one desired `Route` to navigate -- this is passed along to the `NavigationActivity` to start navigation (using the `NavigationManager`).

Before a route is passed to the `NavigationActivity` we present a UI Dialog to get the user's consent for traffic data collection. Their preference is to be set by calling `NavigationManager.setUserLocationTrackingConsentStatus()` with the corresponding `UserLocationTrackingConsentStatus` value. Setting this value is required before a route can be started.

##### NavigationActivity
The `NavigationActivity` is responsible for navigating a route. It sets up listeners for navigation events and updates the UI when they occur. The `NavigationActivity` binds to the `NavigationNotificationService` (described above) -- which manages the NavigationManager instance -- and once connected, starts navigation using the route provided by the `RouteSelectionActivity`.


#### Supplying a LocationProviderAdapter
When navigating a route using the `NavigationManager`, the user's location updates are provided to the SDK by a _location provider_. The abstract class `LocationProviderAdapter` in the SDK provides a generic "wrapper" for any location-provider of your choice -- for example, a native Google location service, a "mocked" location-provider (e.g. used during testing), or one of the commonly used 3rd-party location libraries such as _Mapzen Lost_. 

Note: The Navigation SDK sample app provides a recommended, default location provider implementation, namely `GoogleLocationProviderAdapter`.  It is recommended that developers use this implementation, or, if they wish to provide their own, adhere to the recommended guidelines specified in the LocationProviderAdapter javadocs to ensure that other components in the Navigation SDK behave correctly. 

Here's a description of the key methods in the `LocationProviderAdapter` class:

##### Abstract Methods
`public void requestLocationUpdates()`  
The concrete implementation of this method should immediately initiate recurring location updates at an appropriate interval, typically at a minimum interval of approximately <i>once per second</i>. It should also set up the LocationListener member variable to call `onLocationChanged(Location location)` on every location update.

CAVEAT: If the location update interval is too long, the Navigation Manager might not perform as expected; conversely, if it is much shorter than once per second, this could prove wasteful of battery-life without any real benefits to navigation accuracy or user-experience.

`public void cancelLocationUpdates()`  
The concrete implementation of this method should immediately cancel all recurring location updates; until `requestLocationUpdates()` is called again.

`public Location getLastKnownLocation()`  
This method's implementation should return the last location update that was received -- if none has occurred, then it may return _null_.

##### Concrete Methods

`public void initialize()`  
Initialize the adapter. This is called by the navigation manager when it is initialized. Sub-classes that override this method should always call `super.initialize()` before performing any implementation-specific initialization tasks.

`public void deinitialize()`  
Deinitialize the adapter. This is done when as soon as possible after the navigation manager is deinitialized. Note that if navigation is in progress, the call to deinitialize the adapter will be deferred until navigation ends. Sub-classes that override this method should always call `super.deinitialize()` before performing any implementation-specific initialization tasks.

`public void addLocationListener(locationListener listener)`  
Adds a listener that is called when a location update is received. If no listeners existed, this will cause recurring location updates to be requested from the underlying location service.

The Navigation SDK sample app provides an example implementation of `LocationProviderAdapter` -- specifically the class `GoogleLocationProviderAdapter`. We use _Mapzen Lost_ as the default location provider because we feel it provides good location results without needing to pull in the Google Play Services dependency. For more info on the _Mapzen Lost_ library, see: [https://github.com/mapzen/lost](https://github.com/mapzen/lost)

Developers can study the implementation of the `GoogleLocationProviderAdapter` as a reference for how the `LocationProviderAdapter` abstract class should be extended -- and construct their own implementation using whichever location provider they prefer. Our team also uses custom "mocked" implementations of `LocationProviderAdapter` that fire a pre-defined set of route-locations -- which are useful, for example, when writing functional tests for a navigation application.


#### Using a Foreground (Notification) Service to Ensure Location Updates
Normally, the Navigation SDK can be used only while the application is in the foreground. However, if developers want to have the navigation experience continue while the app has been backgrounded -- and also to persist through various Android OS actions (e.g. if the app being killed to free memory, or per Android Oreo location-update restrictions), some extra setup is required. To handle this use-case correctly, a _Foreground Service_ should be created, started, and bound to your application.

In the sample app provided, `NavigationNotificationService` is an example Android Service which maintains an instance of `NavigationManager`. It is used to ensure navigation continues in the case that the app is backgrounded and reclaimed by the OS. Activities in the sample app can bind to the service and retrieve the navigation manager.

In addition, the example `NavigationNotificationService` is also responsible for building & updating various  _notifications_ that occur during navigation -- which appear, of course, in the system Notification Drawer
-- and which are typically shown from an application running in the background.

#### Navigation Prompts: Text to Speech Implementation
The SDK is _not_ shipped with an integrated TTS (text-to-speech) implementation, but instead allows the developer to specify their own TTS engine per their application’s specific requirements. This is accomplished by means of a simple interface, PromptListener, which can be added to the NavigationManager to handle each prompt event.

Nevertheless, an _example_ implementation of TTS is provided as part of the sample app here, which uses Android’s native TextToSpeech Engine -- see the `TextToSpeechPromptListener` class. This class delegates to `TextToSpeechManager` which handles all aspects of TTS playback, including “overlapping” prompt-events, managing the “audio focus”, and Android API backwards-compatibility concerns.
