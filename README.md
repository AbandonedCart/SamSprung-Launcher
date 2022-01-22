![SamSprung Logo](assets/feature_graphic.png)

## SamSprung (Launcher) Widget

#### Current limitations:

- Does NOT work with Android 12 / OneUI 4 or firmware with Samsung Health verification.
    - New version at https://github.com/SamSprung/SamSprung-TooUI

- Running apps requires Screen lock type set to None / Swipe in Settings -> Lock screen
    - Bixby unlock can be used as a workaround for other lock types  

- Keyboards will not open on the cover screen and some app functionality is limited
    - Firmware limitation by Samsung - Secondary IME support is disabled  

- Switching apps requires tapping power and waiting for a few seconds to exit the app and widget.
    - The app list, however, can be closed by swiping to another widget or the clock.  

- Samsung Health must be uninstalled to free up the package name for the whitelisted widgets.
    - Firmware limitation by Samsung - Widget package name or signature  

[Thanks to CarudiBu for discovering the exploit to load a custom widget](https://forum.xda-developers.com/t/app-subui-browser-browse-the-web-on-the-cover-screen.4325963/)

#### How to Install

- [Download apk from the latest release Assets](https://github.com/SamSprung/SamSprung-Widget/releases/tag/latest)
- Open the apk with your favorite file manager
- Follow the system prompts to install the apk

#### How to Activate

###### From Settings
- Open Settings -> Cover screen -> Widgets
- Enable SamSprung Widget

OR

###### From Cover Screen
- Turn on cover screen
- Swipe to the +
- Select SamSprung Widget
