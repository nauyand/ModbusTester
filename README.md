# Absensi

Aplikasi Android simulasi absensi karyawan dengan foto selfie & GPS (Kotlin + Jetpack Compose).

Package `com.teknosolvo.absensi` · AGP 8.7.3 · Kotlin 2.0.21 · Gradle 8.11.1 · minSdk 26 · targetSdk 35.

## Fitur

Simulasi offline (tanpa server), semua data disimpan di HP.

- **Pendaftaran karyawan** – nama & NIP saat pertama dibuka.
- **Absen masuk & pulang** – maksimal sekali masuk dan sekali pulang per hari.
- **Foto selfie** – CameraX, kamera depan/belakang, bisa ulangi foto.
- **Watermark foto** – nama, NIP, waktu, koordinat, akurasi, dan alamat tercetak di foto.
- **GPS** – lokasi akurasi tinggi (Fused Location), jarak ke kantor, cek radius.
- **Deteksi fake GPS** – absen ditolak kalau lokasi dari aplikasi mock location.
- **Geofence kantor** – titik kantor + radius diatur di Pengaturan ("Pakai lokasi saat ini").
- **Status kehadiran** – tepat waktu / terlambat X menit / pulang cepat, berdasarkan jam kerja.
- **Alamat** – reverse geocoding (butuh internet, opsional).
- **Riwayat** – dikelompokkan per tanggal, thumbnail foto, jarak, status.
- **Detail** – foto penuh, semua data lokasi, tombol "Buka di Maps", hapus data.
- **Pengaturan** – data karyawan, lokasi kantor, radius, jam masuk/pulang, hapus semua data.

Data: Room (SQLite) untuk catatan absen, foto di penyimpanan internal aplikasi.

## Menjalankan

1. Buka folder repo di Android Studio (Ladybug atau lebih baru), tunggu Gradle sync.
2. Run ▶ konfigurasi **app**.

Absensi paling baik dicoba di HP asli (kamera + GPS). Di emulator, set lokasi lewat
*Extended controls → Location*.

```bash
./gradlew assembleDebug
```
