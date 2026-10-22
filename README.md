# APK Exporter

A small Android app that lists the apps installed on your phone and exports their APK files to your Downloads folder, so you can back them up, share them, or sideload them elsewhere.

## Features

- Lists user-installed apps and updated system apps (icon, package name, version, size)
- Search by app name or package name
- Multi-select and export in one tap
- Handles **split APKs**: apps made of several APKs are bundled into a single `.apks` archive
- Saves to `Downloads/APK Exports/` via MediaStore, so no storage permission is required
- No network access, no analytics, no accounts

## Output formats

| App type | Output file | How to install |
|---|---|---|
| Single APK | `AppName_1.2.3.apk` | Open the file on any Android device |
| Split APKs (most Play Store apps) | `AppName_1.2.3.apks` | Use a split-APK installer such as [SAI](https://github.com/Aefyr/SAI) |

An `.apks` file is a plain zip containing `base.apk` and the `split_config.*.apk` files.

## Requirements

- Android 10 (API 29) or newer
- Android Studio (Koala or newer recommended) with JDK 17

## Build

1. Clone the repo and open it in Android Studio.
2. Let Gradle sync. Android Studio generates the Gradle wrapper if it's missing.
3. Run the `app` configuration on a device or emulator, or build from the command line:

   ```bash
   ./gradlew assembleDebug
   ```

   The APK is written to `app/build/outputs/apk/debug/`.

## Permissions

| Permission | Why |
|---|---|
| `QUERY_ALL_PACKAGES` | Android 11+ hides other apps by default; this is needed to list everything that's installed |

Note that Google Play restricts this permission to specific use cases, so the app is intended for sideloading or F-Droid-style distribution unless you can justify it in a Play review.

## Limitations

- Exported APKs keep the original developer's signature. They install as updates or on other devices, but they can't be modified and re-signed without breaking that signature.
- Apps with DRM or device-bound licensing may not run after being copied to another device.
- Only copy apps you have the right to copy.

## Project structure

```
app/src/main/java/com/example/apkexporter/
├── MainActivity.kt   # app list, search, selection, export flow
├── AppAdapter.kt     # RecyclerView adapter with filtering
├── AppItem.kt        # data model for one installed app
└── ApkExporter.kt    # copies/zips APKs into Downloads via MediaStore
```


## License

MIT License
