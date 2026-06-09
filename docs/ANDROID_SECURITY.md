# Android Security Notes

MSA IoT Lab stores connection profiles that may contain broker usernames, local network addresses and MQTT passwords. For that reason the Android manifest disables Auto Backup with `android:allowBackup="false"`.

IoT and backend protocol testing often needs cleartext endpoints such as `ws://`, local MQTT brokers, raw TCP sockets and UDP packets. The Android target therefore declares an explicit network security config and enables cleartext traffic intentionally. This is a product decision for a protocol-testing tool, not a default recommendation for consumer apps.

Production release hardening options:

- Add encrypted storage for profile secrets.
- Add a per-profile warning when TLS is disabled.
- Add certificate pinning/custom trust-store support for TLS profiles.
- Add export policies that keep secrets masked unless the user explicitly chooses otherwise.
