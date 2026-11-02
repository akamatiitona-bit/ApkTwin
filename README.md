# ApkTwin (Android)

Android Studio / Kotlin project scaffold for creating a separately installable copy of an APK you own or are authorized to modify.

## Current implementation
- Select an APK with Android's Storage Access Framework (no broad storage permission).
- Read and display package name, version, and app label.
- Validate a new Java-style package ID.
- Create a new per-install RSA signing key in Android Keystore.
- Preserve the source APK; output is written as a new file.
- Includes a guarded processing pipeline and clear status/error reporting.

## Important limitation
**This repository is a working project scaffold, not yet a complete APK repackager.** Android's APK format uses compiled/binary AndroidManifest.xml and resource tables. Safely changing the package ID requires decoding and rebuilding the APK with a compatible Apktool build, then signing the rebuilt APK. Apktool's desktop-oriented CLI/library is not guaranteed to run inside an ordinary Android app process. This project deliberately does not pretend that copying ZIP entries or changing a string in the binary manifest is sufficient.

The processing service currently stops before modifying the APK and explains this limitation. The next engineering step is to integrate an Android-compatible Apktool runtime (or move decode/rebuild to a trusted local companion process) and test against real APKs, including split APKs, resource-heavy apps, and modern signing schemes.

A changed signing certificate means Android will not treat the result as an update to the original installation. A changed package ID is also required for side-by-side installation. Some apps use server-side identity, signature checks, licensing, Play Integrity, or hard-coded package IDs and may not work after repackaging.

## Build
1. Open this folder in Android Studio.
2. Use JDK 17 and Android SDK Platform 35.
3. Sync Gradle.
4. Run the `app` configuration on an Android 8.0+ device.

## Repository layout
- `app/src/main/java/dev/apktwin/MainActivity.kt` — UI and APK selection
- `app/src/main/java/dev/apktwin/ApkInspector.kt` — APK metadata inspection
- `app/src/main/java/dev/apktwin/ClonePipeline.kt` — safe processing boundary / explicit TODO
- `app/src/main/res/` — app resources

## Legal and safety
Only modify APKs you created or have permission to modify. Do not use this tool to bypass payment, licensing, anti-cheat, access controls, or another developer's security checks. Keep original APKs and signing keys secure.
