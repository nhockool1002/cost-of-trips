# Cost Of Trips - Chi Phí Chuyến Đi

Ứng dụng Android thuần (Kotlin + Jetpack Compose) giúp ghi lại chi phí của từng chuyến đi. Toàn bộ dữ liệu được lưu trong storage của điện thoại, không cần tạo tài khoản.

## Tính năng

- Tạo chuyến đi và ghi lại số tiền đã chi cho từng hạng mục (di chuyển, chỗ ở, ăn uống, giải trí, mua sắm, khác)
- Xuất toàn bộ dữ liệu ra file JSON trong thư mục Downloads
- Cài đặt ngôn ngữ Tiếng Anh / Tiếng Việt
- Giao diện Sáng / Tối / Theo hệ thống

## Công nghệ

- Kotlin + Jetpack Compose (Material 3)
- Room (SQLite) cho lưu trữ cục bộ
- DataStore Preferences cho cài đặt giao diện
- Navigation Compose

## Build

Yêu cầu Android SDK (compileSdk 34) và JDK 17.

```bash
./gradlew assembleDebug
```

APK debug sẽ nằm ở `app/build/outputs/apk/debug/app-debug.apk`.
