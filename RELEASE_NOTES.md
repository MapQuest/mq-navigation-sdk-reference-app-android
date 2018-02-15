## MapQuest Navigation SDK 

Version 3 of the Mapquest Navigation SDK is a ground-up rewrite aimed at creating a brand new, modern navigation SDK. Initial releases include a core set of initial features, with a particular focus on consistent behavior and implementation across iOS and Android.

#### Core Features
- Turn-by-turn Guidance
- Single Leg Routes
- Alternate Route Selection
- Route Options (Avoid Tolls, etc)
- Traffic Along-the-route
- Proactive Traffic Updates & Reroutes
- Reroutes Biased on Direction of Travel
- Directions List
- Speed Limits
- Consistent API across iOS & Android
- Background System Services Support

#### Each Release Includes
- MQNavigation Android SDK library (AAR)
- Android Sample App
- API Documentation (javadoc)

## Release Notes

### MQNavigation 3.2.2 (Android)
#### Notable Updates
- Multi-Stop Routes: SDK now supports navigation of a route with more than one destination (i.e. multiple "route legs")
- Destination Reached: per the above, the onDestinationReached listener method in NavigationProgressListener has changed; also now has DestinationAcceptanceHandler used to determine navigation behavior between route-legs
- Language Option: the language used for all "text" portions (i.e. maneuver text, spoken prompts) of the routes returned from the NavigationRouteService can now also be specified as an option

### MQNavigation 3.1.2 (Android)
#### Notable Updates
- Update okhttp version to 3.9.1 to include fatal exception fix (fixed in version 3.8.1) and to point to the latest version.

### MQNavigation 3.1.1 (Android)
#### Notable Updates
- Move LocationProviderAdapter into location package.
- Remove mapzen dependency & move default LocationProviderAdapter out of sdk & into sample app.

### MQNavigation 3.1.0 (Android)
#### Notable Updates
- Better support for metric and US systems of measurement, as the display-units used in prompt text is now configurable.
- Sample App: various architectural changes to improve integration of SDK, including changes to Location Acquisition, the Activity task-stack, and background Service lifecycle -- please review your own host app implementation accordingly.
- Note that text-to-speech engine used for prompt-playback is now implemented externally from the SDK itself -- refer to the “sample app” for example usage.
- Various bug-fixes including adjustments to prompt-timing, speed-limit spans, etc. (refer to the changelog for specific details).

#### Known Issues
- **Unoptimized ETA** - In some traffic conditions, ETAs can appear low compared to the true travel time.
- **Indirect routes on reroute** - In certain conditions where user is sitting in traffic near a turn and a reroute occurs, a sub-optimal route may be returned with extra turns.

### MQNavigation 3.0.0 (Android)
#### Notable Updates
- Added a property ‘name’ to MQRoute
- Added a property ‘trafficOverview’ to MQRoute
- MQTrafficOverview enum was added to public interface
- Default value for ‘maxRoutes’ property was changed from 0 to 3

#### Notable Fixes

- **Fixed Prompt timing** - prompts sometimes play too close to a turn, sometimes interrupt each other, and sometimes mention subsequent turns prematurely.
- **Fixed Maneuver text** - Some maneuvers have missing text.
- **Fixed Route Options** - Route options (avoid tolls, etc.) don’t persist on subsequent reroutes.
- **Fixed Mismatched ETA** - In some traffic conditions, ETAs between route and traffic service do not match.
- **Fixed Rerouting** - SDKs can be slow to re-route in certain situations.
- **Fixed Traffic Ribbon** - traffic conditions along the route may be positioned incorrectly.
- **Fixed Overlapping route** - If a route overlaps, the SDKs may snap to the overlapping portion, triggering a reroute and confusing prompts to play.
- **Fixed Inefficient First Route** - Fixed: The first route returned by the SDK isn’t necessarily the fastest route.
- **Fixed Location Issues** - sometimes SDK has issues retrieving location from GPS.
- **Fixed Start Route Tracking** - SDK can get stuck at starting position until you’ve moved some ways along the route.
- **Fixed Directions List on Android** - Simplified API for directions list won’t be implemented in time for first beta.

#### Known Issues
- **Unoptimized ETA** - In some traffic conditions, ETAs can appear low compared to the true travel time.
- **Delayed reroute** - SDKs may be slow to reroute under certain rare conditions.
- **Repeating Prompt Issue** - On some complex highway junctions, prompts may repeat many times.
- **Indirect routes on reroute** - In certain conditions where user is sitting in traffic near a turn and a reroute occurs, a sub-optimal route may be returned with extra turns.
- **Traffic & ETA update delays** - Traffic updates may not be happening when app is running in the background.
- **Prompt queuing** - In cases where there are multiple prompts that could be played given user’s speed, the prompts queue and play one after another, causing things to play at the wrong time.
