# Haier MatrixTV DE: cleanup log

**Date:** 10 Nov 2026
**TV:** Haier MatrixTV DE (product HR9676EUF, device `omonia`), Google TV, Android 14, 1.8 GB RAM
**Connected via:** Wireless debugging, <TV_IP> (port changes after each restart)

Nothing was uninstalled. Every app was **turned off** (`pm disable-user`) and can be turned back on.

---

## 1. Apps turned off (9)

| App | Package | Status after restart | Undo command |
|---|---|---|---|
| YouTube Music | `com.google.android.youtube.tvmusic` | Off | `adb shell pm enable com.google.android.youtube.tvmusic` |
| Google Play Games | `com.google.android.play.games` | Off | `adb shell pm enable com.google.android.play.games` |
| Google Feedback | `com.google.android.feedback` | Off | `adb shell pm enable com.google.android.feedback` |
| Retail demo mode | `com.mediatek.tv.retaildemo` | Off | `adb shell pm enable com.mediatek.tv.retaildemo` |
| Anoki ACR (viewing tracking) | `tv.anoki.acr.anokiacroptin` | Off | `adb shell pm enable tv.anoki.acr.anokiacroptin` |
| Haier e-Manual | `com.haier.emanual9603` | Off | `adb shell pm enable com.haier.emanual9603` |
| Haier TV Housekeeper | `com.haier.tvhousekeeper` | Off | `adb shell pm enable com.haier.tvhousekeeper` |
| Miracast screen mirroring | `com.mediatek.miracast` | Off | `adb shell pm enable com.mediatek.miracast` |
| Haier Gaming Bar | `com.haier.gamingbar` | **Came back on.** Haier's app manager (`com.haier.appmanager.dispatcher`) turns it back on at every startup. Left on. | n/a |

## 2. Settings changed

| Setting | Before | After | Undo command |
|---|---|---|---|
| Window animation scale | 1.0 | 0 (off) | `adb shell settings put global window_animation_scale 1.0` |
| Transition animation scale | 1.0 | 0 (off) | `adb shell settings put global transition_animation_scale 1.0` |
| Animator duration scale | 1.0 | 0 (off)* | `adb shell settings put global animator_duration_scale 1.0` |
| TV sleep (`secure sleep_timeout`) | 20 min (1200000) | **5 min** (300000) | `adb shell settings put secure sleep_timeout 1200000` |
| Screensaver starts (`system screen_off_timeout`) | 10 min (600000) | **3 min** (180000) | `adb shell settings put system screen_off_timeout 600000` |
| Screensaver | On | **On (unchanged)**: you weren't sure whether the screen is OLED | n/a |

\* Animator duration scale went back to 1.0 by itself after the restart. I set it to 0 again and it held, but it may reset after a future restart. To set it permanently by hand, go to **Settings → System → Developer options → Animator duration scale → Animation off**.

## 3. Home screen installed (tv-detox)

- Installed `com.ansub.tvhome` from https://github.com/Ansub/tv-detox (latest release APK).
- App row order: Netflix, JioHotstar, Prime Video, YouTube, Zee5, ETV Win, Aha, Sun NXT, SonyLIV, NetMirrorTV.
- Because this is a Google TV, **Google TV's home screen (`launcherx`) was kept on**, so the Home button still opens Google TV's screen. Open the new home screen from your Apps list.
- tv-detox turned **nothing** off.
- **Undo:** `sh tv-detox/undo.sh <TV_IP:PORT>` (removes the app).

## 4. Left on deliberately

Google Play services, Google Play Store, Settings, System UI, the setup app (setupwraith), Gboard, Bluetooth (including Haier's BT speaker/pairing helpers), HDMI/TV input and tuner framework (MediaTek TIS, DVB tuner, TV Center, TV provider), Google TV's home screen and screensaver, Chromecast built-in, Google Assistant/voice search, the USB media player, the remote service, the backlight and Dolby services, system updater, and every app the system keeps always running (Haier smart device, DIAL, hOn service, etc.).

Apps Haier had **already turned off** before this session (not touched): `com.haier.apps.evoSmart`, `ru.haier.evo`, `it.hon.onboarding`, `com.mediatek.dtv.tvinput.atsctuner`, `com.mediatek.dtv.tvinput.isdbtuner`, `com.mediatek.tv.oneworld.tvcenter`.

## 5. Free memory (MemAvailable)

| | Free memory |
|---|---|
| Before (TV had been on a while, JioHotstar open in background) | ~480 MB (492,204 kB) |
| After restart, ~8 min uptime | ~490–500 MB (500,000–509,748 kB) |

The gain is small. Most of the apps turned off were not running much. The biggest memory users are Google TV's own home screen, Google Play services and Assistant, and those were kept on for safety. The disabled apps mainly cut background activity and tracking (Anoki ACR). The animations-off setting is what makes menus *feel* faster.

## 6. How to undo everything

1. On the TV, turn on **Settings → System → Developer options → Wireless debugging**. Note the new IP and port it shows.
2. If the Mac isn't paired any more: on the TV choose **Pair device with pairing code**, then run
   `tools/platform-tools/adb pair <TV_IP>:<pairing port> <code>`
3. Turn all apps and settings back on:
   `sh undo-my-changes.sh <TV_IP>:<port>`
4. Remove the tv-detox home screen:
   `PATH=$PWD/tools/platform-tools:$PATH sh tv-detox/undo.sh <TV_IP>:<port>`

If the TV ever gets stuck at the logo, the last resort is a factory reset from the TV's recovery menu (on most Android TVs, hold the power button on the TV itself while plugging it in). Steps 1–4 above should cover everything else.

## 7. Live TV row added to the home screen (11 Nov 2026)

The tv-detox home screen (`com.ansub.tvhome`) was rebuilt from source with a **Live TV row** under the apps, and reinstalled. Version `1.5.7-livetv`, signed with a new key made on this Mac (`tv-detox/key.jks`; keep it, because future updates must be signed with the same key).

**Channels (27):**

| App | Channels | How the tile opens it |
|---|---|---|
| Zee5 | TV9 Telugu, Zee Telugu HD, Zee Cinemalu HD, &Pictures HD, Zee Cinema, Zee Classic, Zee Action, Zee Bollywood, &TV HD | Straight into the live channel (all 9 tested) |
| Sun NXT | Gemini HD, Gemini Movies HD, Gemini Movies, Gemini Music HD, Gemini Comedy, Gemini Life, Kushi TV, Surya HD, Surya Movies, Udaya HD, Udaya Movies, Sun Neo HD | Straight into the live channel (link `sunnxt://playback?source=deeplink&id=<id>`) |
| ETV Win | ETV Telugu, ETV Plus, ETV Cinema, ETV Music, ETV Life, ETV Andhra Pradesh | **Opens the ETV Win app only.** ETV Win accepts no channel links, so pick the channel inside the app. |
| JioHotstar | (none) | JioHotstar has no live TV links. A `hotstar.com` link can still be added if that changes. |

**Using it on the TV:**
- Open **Home** from the TV's Apps row, then scroll down to **Live TV**. Press OK on a channel to start it.
- **Add channel** (the + tile at the end) offers three choices:
  - **Add a channel link:** a name plus a Zee5 / Sun NXT / JioHotstar link. You can type it with the remote, or with your phone's keyboard in the Google TV app. Sun NXT web links (`sunnxt.com/live/.../<id>/...`) are converted for the TV app automatically.
  - **Add an app as a channel:** a tile that just opens an app.
  - **Restore my channel list:** goes back to the 27 channels above.
- **Hold OK (or press Menu) on a channel** to get Move left, Move right, Edit and Remove from home.
- **From the Mac:** `sh add-channel.sh <TV_IP:PORT> "Name" "link"`, or `sh add-channel.sh <TV_IP:PORT> reset`.

**Code changed** (in `tv-detox/`): new `src/com/ansub/tvhome/LiveTv.java` (channel list and link handling). `MainActivity.java` gained the Live TV row, the menus, the add/edit form and the `addchannel`/`channels` install extras. `AndroidManifest.xml` gained link queries and the version name.

**Undo:** `PATH=$PWD/tools/platform-tools:$PATH sh tv-detox/undo.sh <TV_IP:PORT>` removes the home screen entirely. To go back to the official tv-detox version, run `sh tv-detox/undo.sh` and then the official `install.sh` from https://github.com/Ansub/tv-detox.

**Installed on the Mac for this:** Java 17 (`brew install openjdk@17`), jadx (`brew install jadx`, used to read how Sun NXT handles links), Android SDK platform 34 + build-tools 34 in `tools/android-sdk/`. To remove them: `brew uninstall jadx openjdk@17` and delete `tools/android-sdk`.

## Files in this folder

- `CHANGES.md`: this file
- `changes-undo-list.txt`: machine-readable undo list used by `undo-my-changes.sh`
- `undo-my-changes.sh`: turns everything back on
- `tv-detox/`: tv-detox project with the Live TV row added, including its `undo.sh`, the build output `out/tvhome.apk` and the signing key `key.jks`
- `add-channel.sh`: adds a Live TV channel from the Mac
- `backup/`: app lists from before any changes
- `tools/platform-tools/`: Google's adb
