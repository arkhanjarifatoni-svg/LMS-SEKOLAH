# CBT Sekolah — Tahap 1

Project Android Native menggunakan Kotlin + Jetpack Compose. Tahap ini menyediakan tampilan login dan dashboard demo lokal, serta GitHub Actions untuk membangun APK debug.

## Penting
- Login pada tahap ini BELUM terhubung ke server. Tombol masuk hanya memvalidasi bahwa kolom tidak kosong lalu membuka dashboard demo.
- Jangan gunakan APK ini untuk ujian sungguhan atau memasukkan password asli.
- Belum ada database, sinkronisasi, timer ujian, penyimpanan jawaban, maupun penguncian kiosk.
- APK debug hanya untuk pengujian. Distribusi resmi nantinya perlu APK release yang ditandatangani dengan keystore yang dijaga aman.

## Build lewat HP dengan GitHub Actions
1. Buat repository baru di GitHub, misalnya `cbt-sekolah`.
2. Upload seluruh isi folder project ini ke repository (termasuk folder `.github/workflows`).
3. Buka tab **Actions** pada repository dan izinkan Actions jika diminta.
4. Jalankan workflow **Build CBT Android APK** dengan tombol **Run workflow**, atau push ke branch `main`.
5. Setelah status workflow sukses, buka hasil run dan unduh artifact `CBT-Sekolah-debug-apk`.
6. Ekstrak ZIP artifact, lalu instal `app-debug.apk` di HP Android. Jika diminta, izinkan instalasi aplikasi dari sumber tersebut untuk aplikasi pengelola file/browser yang digunakan.

## Pengembangan lokal
- Android Gradle Plugin 8.13.2
- Gradle 8.13
- JDK 17
- Compile SDK 36
- Minimum Android API 23

## Identitas aplikasi
- Application ID: `id.sekolah.cbt`
- Nama: `CBT Sekolah`
- Versi: `0.1.0`
