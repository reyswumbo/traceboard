# Traceboard

**Traceboard — Clipboard, Writing & Usage Analytics** adalah aplikasi Android native yang menggabungkan pengelolaan *clipboard*, statistik menulis, pelacakan kata kunci, dan analitik penggunaan aplikasi dalam satu pengalaman yang bersih dan modern.

Aplikasi ini dibuat sebagai jawaban dari **Traceboard Android App Challenge** menggunakan **Kotlin**, **Jetpack Compose**, dan **Material 3**.

---

## Tentang Traceboard

Traceboard adalah aplikasi utilitas produktivitas pribadi yang berfokus pada tiga area utama:

1. **Clipboard** — Menyimpan teks yang disalin secara otomatis.
2. **Menulis** — Melacak jumlah kata, karakter, dan kata kunci yang dipilih.
3. **Penggunaan** — Menganalisis waktu penggunaan aplikasi dan menampilkan statistiknya.

Ketiga fitur tersebut dirancang agar terasa seperti satu produk yang utuh dan *polished*.

---

## Fitur Aplikasi

- Garansi kecil: **APK ringan** (`minifyEnabled` + `shrinkResources` aktif pada build rilis).
- **Dasbor**: Ringkasan aktivitas hari ini (waktu layar, kata ditulis, item clipboard, kata terlacak).
- **Clipboard Manager**:
  - Tombol **Mulai/Berhenti** untuk merekam teks yang disalin.
  - Indikator status perekaman.
  - Simpan otomatis, lihat, edit, hapus, salin ulang.
  - Pencarian dan pengurutan (terbaru di atas).
  - **Impor & Ekspor** dalam format JSON (melalui *system file picker*).
- **Menulis & Analitik Kata**:
  - Penghitungan *real-time*: kata, karakter, dan huruf (spasi tidak dihitung sebagai kata).
  - Pelacakan kata kunci (**tidak membedakan huruf besar/kecil**).
  - Tambah, hapus, dan reset hitungan kata terlacak.
- **Penggunaan Aplikasi**:
  - Periode waktu: Hari Ini, 7 Hari, 1 Bulan, Semua.
  - Total waktu penggunaan + statistik per aplikasi (jam, menit, detik tiap aplikasi).
  - Detail per aplikasi melalui *bottom sheet*.
  - Informasi **Battery** (level perangkat) dan **Penyimpanan**.
  - Notifikasi karena statistik per aplikasi bergantung pada izin Akses Penggunaan Aplikasi.
- **Bahasa**: Seluruh antarmuka menggunakan Bahasa Indonesia.
- **Mode gelap & terang** (mengikuti sistem, mendukung *dynamic color* di Android 12+).
- Aktivitas di negara kosong (*empty state*) dan instruksi izin yang jelas.

---

## Aturan Penghitungan Menulis

Aturan yang digunakan untuk statistik menulis didokumentasikan secara eksplisit agar konsisten dengan contoh pada *brief*:

- **Total kata**: dihitung dengan memecah teks berdasarkan spasi/whitespace dan menghitung token yang **tidak kosong**. Spasi **tidak** ikut dihitung sebagai kata; spasi berlebih juga tidak menambah jumlah kata.
- **Total karakter**: seluruh jumlah karakter pada teks, **termasuk spasi**.
- **Total huruf**: jumlah karakter yang merupakan huruf (`isLetter()`).
- **Pelacakan kata kunci**: pencocokan **tidak peka huruf besar/kecil** menggunakan ekspresi reguler dengan `(?i)` dan `Regex.escape` pada kata kunci.

Contoh: teks `maaf maaf` menghasilkan **2 kata**, **8 huruf**, **9 karakter** (termasuk spasi; spasi tidak dihitung sebagai kata), dan kata terlacak `maaf` bernilai **2×**. Teks `Maaf MAAF maAf` juga tetap dihitung sebagai kata `maaf` sebanyak **3×**.

---

## Arsitektur Aplikasi

Traceboard mengikuti **arsitektur Android modern** dengan pemisahan lapisan yang jelas:

- **UI Layer**: Jetpack Compose dengan `StateFlow` dan `ViewModel` (`collectAsStateWithLifecycle`).
- **Domain/Data Layer**: `Repository` + **Room** untuk penyimpanan lokal yang efisien, serta **DataStore Preferences** untuk preferensi.
- **Single Activity**: navigasi menggunakan `Navigation Compose` dengan *bottom navigation*.

Struktur paket:

```
app/src/main/java/com/traceboard/app/
├── MainActivity.kt              # Entry point + navigation
├── TraceboardApplication.kt     # Application + Database singleton
├── data/
│   ├── model/                   # Entity & data class (ClipboardItem, TrackedWord, dll.)
│   ├── repository/              # DAO, Database, Repository, BackupManager
│   └── util/                    # WritingAnalyzer, TimeFormatter
├── ui/
│   ├── components/              # Komponen UI bersama (StatCard, EmptyState)
│   ├── navigation/              # Definisi screen
│   ├── screens/
│   │   ├── dashboard/           # Dasbor
│   │   ├── clipboard/           # Clipboard Manager
│   │   ├── writing/             # Menulis & kata terlacak
│   │   └── usage/               # Penggunaan aplikasi
│   └── theme/                   # Material 3 theme (gelap/terang)
└── viewmodel/                   # ViewModel + ViewModelFactory
```

---

## Teknologi yang Digunakan

| Komponen          | Versi            |
|-------------------|------------------|
| Kotlin            | 1.9.22           |
| Jetpack Compose   | BOM 2024.01.00   |
| Material 3        | BOM 2024.01.00   |
| Room              | 2.6.1            |
| Navigation Compose| 2.7.6            |
| DataStore         | 1.0.0            |
| Gson              | 2.10.1           |
| AGP               | 8.2.2            |
| Min SDK / Target  | 26 / 34          |

---

## Cara Instalasi

1. Unduh APK dari **GitHub Actions artifacts** (tab *Actions → workflow → build → artifacts*).
2. Salin file `.apk` ke perangkat Android (Android 8.0+/API 26+).
3. Izinkan instalasi dari sumber tidak dikenal bila diminta.
4. Buka Traceboard.

---

## Cara Menjalankan Project

Prasyarat: Android Studio (atau JDK 17 + Gradle).

```bash
# Linux/macOS
./gradlew assembleDebug

# Windows
gradlew.bat assembleDebug
```

APK debug dihasilkan di `app/build/outputs/apk/debug/`.

Android Studio: *Open project → Traceboard* lalu jalankan dengan tombol ▶ pada emulator/perangkat.

---

## Clipboard Implementation

- Teks dipantau dengan polling `ClipboardManager` (tanpa izin khusus; hanya saat **Mulai** ditekan).
- Data disimpan di tabel **Room** `clipboard_items` (teks, panjang, jumlah kata, cap waktu).
- Item dapat: disalin ulang, diedit, dihapus, dihapus semua, dicari, ditambah manual.
- **Impor/Ekspor**: format **JSON**. `Traceboard` -> Ekspor menghasilkan file `traceboard-backup.json` berisi seluruh entri clipboard + cap waktu. Impor memvalidasi struktur data; tautan yang sama tidak diimpor dua kali; file rusak ditangani dengan aman (menampilkan pesan gagal, tanpa crash).

---

## Usage Statistics & Permissions

Statistik penggunaan aplikasi membutuhkan akses khusus Android:

1. Buka hal **Penggunaan** → tombol **Buka Pengaturan**.
2. Pilih aplikasi **Traceboard** dan aktifkan **Akses Penggunaan Aplikasi** (*Usage Access*).
3. Kembali ke aplikasi, lalu tarik data (tombol refresh).

- **Periode**: Hari Ini / 7 Hari / 1 Bulan / Semua.
- Waktu ditampilkan presisi: contoh `1 jam 24 menit 17 detik` atau `45 detik`.
- Buka *bottom sheet* pada sebuah aplikasi untuk detailnya.

### Battery & Penyimpanan

- **Battery**: menampilkan level baterai perangkat saat ini (`BatteryManager`).
- **Penyimpanan**: total, sisa, dan terpakai dari penyimpanan internal (`StatFs`).
- Statistik **baterai per aplikasi** dan **penyimpanan per aplikasi** **tidak** tersedia melalui API Android standar yang bisa diandalkan — sesuai prinsip di *brief*, aplikasi **tidak membuat data palsu**; informasi tersebut ditampilkan sebagai "tidak tersedia".

---

## Permission & Privasi

`android:required="false"` digunakan untuk izin *usage access* agar aplikasi tetap bisa dipasang tanpa izin tersebut.

- `PACKAGE_USAGE_STATS` — untuk statistik penggunaan aplikasi (wajib diaktifkan manual oleh pengguna).
- `QUERY_ALL_PACKAGES` — untuk membaca nama/ikon aplikasi pada daftar penggunaan.
- Izin penyimpanan lama dibatasi ke API lama; **Impor/Ekspor** menggunakan Storage Access Framework (tidak butuh izin penyimpanan).
- Tidak ada izin yang diminta tanpa keperluan nyata.

### Privasi

Seluruh data disimpan **lokal** di perangkat. Tidak ada pengiriman data ke server, iklan, atau *analytics* pihak ketiga.

---

## GitHub Actions

File workflow di `.github/workflows/build.yml`:

- Trigger: *push* / *pull request* ke branch `main` + *manual dispatch*.
- Menjalankan build Debug **dan** Release di `ubuntu-latest` (JDK 17).
- Mengunggah artefak APK **debug** dan **release** (*Actions → Artifacts*).

Workflow juga otomatis memvalidasi bahwa project selalu dapat di-build.

---

## Cara Build APK / AAB

```bash
# APK Debug
./gradlew assembleDebug

# APK Release (di-minify + dioptimasi ukuran)
./gradlew assembleRelease

# AAB (Android App Bundle)
./gradlew bundleRelease
```

Hasil build:
- Debug: `app/build/outputs/apk/debug/`
- Release: `app/build/outputs/apk/release/`
- AAB: `app/build/outputs/bundle/release/`

Release default **tidak ditandatangani** (unsigned) — untuk distribusi, tambahkan konfigurasi *signingConfig*. Debug APK menggunakan *debug keystore* bawaan Android Studio dan dapat langsung dipasang.

---

## Troubleshooting

- **Statistik penggunaan kosong** → pastikan izin "Akses Penggunaan Aplikasi" sudah diaktifkan untuk Traceboard, lalu *refresh*. Pada perangkat baru, data perlu terkumpul seiring pemakaian.
- **Clipboard tidak tersimpan** → pastikan tombol **Mulai** aktif (tampil "Merekam").
- **Impor gagal** → pastikan file adalah hasil ekspor Traceboard (`traceboard-backup.json`) dan tidak rusak.
- **Build gagal di GitHub Actions** → cek log di tab *Actions*; masalah umum: versi JDK dan izin *executable* `gradlew` (sudah diatur `chmod +x` pada workflow).
- **APK besar** → gunakan build **Release** yang telah diaktifkan R8 + *resource shrinking*.

---

## Batasan Android API

- Statistik penggunaan aplikasi bergantung pada **Akses Penggunaan Aplikasi** yang harus diaktifkan manual oleh pengguna.
- Periode "Semua" dibatasi oleh data historis yang disimpan sistem Android.
- Statistik battery/storage **per aplikasi** tidak tersedia lewat API publik yang stabil.
- Pembacaan clipboard di latar belakang penuh dibatasi sistem sejak Android 10; aplikasi merekam clipboard saat aplikasi aktif dan perekaman **Mulai** dinyalakan.