## MapQuest Navigation SDK and Sample App: Change Log

### Sample App

#### 3.2.2

* NavSDK Update: updated to use v3.2.2 of the MQ Navigation SDK
* Dependency Updates: also updated to use v2.0.4 of the MQ MappingSDK for the MapView; and v3.0.4 of MapZen Lost (for GPS Location)
* Multi-Stop Route Support: updated to allow creation (and arrival handling) for new "multi-stop" routes; i.e. a route with more than one destination
* Advance to Next Route Leg: added button to allow user to "advance" to the next route-leg (when navigating a multi-stop route)
* Pause/Resume Navigation: added UI to control navigation per these new NavigationManager API methods
* Language Option: specify US English as the language option, but show how to specify, say, "US Spanish" as an option
* Search Ahead Feature: simplified the implementation of this MapQuest feature in sample app

#### 3.1.2

* Updated okhttp dependency to latest version 3.9.1. This brings in the fix for the fatal exception made in version [3.8.1](https://github.com/square/okhttp/blob/master/CHANGELOG.md)  as well as other updates to the library.

#### 3.1.1

* Moved mapzen dependency & the default location provider implementation out of the sdk and into the testharness.

#### 3.1.0

* significant architectural changes to improve integration of SDK, Location Acquisition, Activity task-stack, and Background Service lifecycle
* other sample-app fixes to prevent additional duplicate instances from being created when tapping on any navigation notification (i.e. shown in the system notifications pulldown)
* prompt playback: example implementation using _external_ text-to-speech engine (per changes to SDK, below); also some changes to audio-stream used, so that audio does not play out of the phone when headphones are engaged
* in sample-app implementation, when in “follow mode”, always use tilt and zoom appropriately
* correctly re-draw route on re-route & fix race condition with map controller

#### 3.0.0

* Sample App version for initial release



### MapQuest Navigation SDK

#### 3.2.2

* Multi-Stop Routes: SDK now supports navigation of a route with more than one destination (i.e. multiple "route legs")
* Destination Reached: per the above, the onDestinationReached listener method in NavigationProgressListener has changed; also now has DestinationAcceptanceHandler used to determine navigation behavior between route-legs
* Language Option: the language used for all "text" portions (i.e. maneuver text, spoken prompts) of the routes returned from the NavigationRouteService can now also be specified as an option

#### 3.1.0

* API_KEY now specified using mapquest.properties file (and build.gradle in host app)
* display units used by prompt text: now configurable in `RouteOptions` as `SystemOfMeasurement.UNITED_STATES_CUSTOMARY` or `METRIC`
* AAR lib name now includes flavor and build-type
* updated versions of various dependencies 
* added non-null & nullable annotations to public API model classes and method params
* bug fixes w.r.to speed-limit spans; now correctly clears speed limits when off-route
* moved text-to-speech implementation out of SDK (i.e. now provided by host app, note example implementation in sample-app, per above)
* prompt playback: minor adjustments to prompt-timing; also fixed issue where after prompts may not play if you don't travel exactly over them
* prompts can now correctly interrupt previous prompts
* cancelPrompts: this method is now correctly being called

#### 3.0.0-beta.1
- We've overhauled our navigation SDKs to have cleaner APIs, full consistency across platforms, and improved behavior under the hood.

#### 3.0.0-beta.2

#### 3.0.0
- Public API Changes
    - Added TrafficOverview enum
    - trafficOverview field added to Route
    - name field added to Route
    - Fixed start callback to fire only when a new navigation route is started
    - Added Ferries type to RouteOptions