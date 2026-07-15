# Pocket Ledger

Pocket Ledger is an offline-first Android bookkeeping app built with Kotlin, Jetpack Compose, Room, and MVVM.

## Run

1. Open this folder in Android Studio Panda 3 (2025.3.3) or newer.
2. Let Android Studio install JDK 17, Android SDK 36, and sync Gradle 9.4.1 through the included wrapper.
3. Run the `app` configuration on an Android 8.0+ emulator or device.

## Included

- Home ledger with monthly totals, search/filter, transaction detail, edit, and delete
- One-sentence parsing in multiple languages plus voice input through the installed Android keyboard
- Manual income/expense entry with editable confirmation before saving
- Week/month/year charts and category breakdowns
- Analytics, monthly budget tracking, account creation/editing/archiving
- Atomic account balance updates when transactions are added, edited, or removed
- Room database persistence for transactions, accounts, categories, and budgets, with one-time import from the previous SharedPreferences JSON format
- Custom account and income/expense category CRUD, safe archive/delete flows, and historical snapshots
- Password-encrypted full backup and restore for transferring records between devices
- System/light/dark themes, four text sizes, ten currencies, and ten UI languages

## Data migration

Room database version 1 is the first Room schema in this project. Existing releases used a `pocket_ledger` SharedPreferences JSON document. On first launch, that document is imported into Room and the legacy plaintext copy is removed after a successful migration. No destructive migration or `fallbackToDestructiveMigration` is used.

## Backup security

System cloud backup and device-transfer backup are disabled for all app data. Full manual backups use AES-256-GCM with a key derived from a passphrase of at least 12 characters. The passphrase is not stored and cannot be recovered. CSV export neutralizes spreadsheet formula prefixes, but remains plaintext and should only be shared or stored in a trusted location.

## Build a release APK

Debug APKs use Android's public debug key and must not be distributed. To create a signed release:

1. Generate and securely retain a private Android upload keystore.
2. Copy `keystore.properties.example` to `keystore.properties` and replace every placeholder.
3. Run `./gradlew assembleRelease` (or `gradlew.bat assembleRelease` on Windows).
4. Upload `app/build/outputs/apk/release/app-release.apk` only after verifying its signature and version.

The build deliberately refuses to package a release when the private signing configuration is missing. Keystores, signing properties, APKs, and app bundles are excluded from Git.
