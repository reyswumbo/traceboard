# Traceboard

<p align="center">
  <img src="docs/mascot.png" alt="Traceboard" width="160" />
</p>

**Traceboard — Clipboard, Writing & Usage Analytics** adalah aplikasi Android native yang menggabungkan pengelolaan *clipboard*, statistik menulis, pelacakan kata kunci, dan analitik penggunaan aplikasi dalam satu pengalaman yang bersih dan modern. Dibangun menggunakan **Kotlin**, **Jetpack Compose**, dan **Material 3**.

---

## Tentang Traceboard

Traceboard adalah aplikasi utilitas produktivitas pribadi yang berfokus pada tiga area utama:

1. **Clipboard** — Menyimpan teks yang disalin secara otomatis, mendukung **folder** untuk mengelompokkan hasil salinan.
2. **Menulis** — Melacak kata kunci yang dipilih secara otomatis setiap kali kamu mengetik di aplikasi mana pun.
3. **Penggunaan** — Menganalisis waktu penggunaan aplikasi dan menampilkan statistiknya.

Ketiga fitur tersebut dirancang agar terasa seperti satu produk yang utuh dan *polished*.

---

## Fitur Aplikasi

- Garansi kecil: **APK ringan** (`minifyEnabled` + `shrinkResources` aktif pada build rilis).
- **Dasbor**: Ringkasan aktivitas hari ini (waktu layar, kata ditulis, item clipboard, kata terlacak). Item yang dihitung hanya dari folder **Semua**.
- **Clipboard Manager**:
  - Tombol **Mulai/Berhenti** untuk merekam teks yang disalin.
  - **Foreground service** sehingga perekaman tetap berjalan saat aplikasi di *background*.
  - Indikator status perekaman.
  - Simpan otomatis, lihat, edit, hapus, salin ulang.
  - Pencarian dan pengurutan (terbaru di atas).
  - **Folder clipboard**: folder "Semua" secara otomatis tersedia; buat folder baru (mis. `facebook`), lalu tambahkan item dari clipboard ke folder tersebut.
  - Menambahkan item ke folder **tidak menghapus** item di "Semua". Menghapus item di "Semua" **tidak** menghapus salinannya di folder lain.
  - **Impor & Ekspor** dalam format JSON (melalui *system file picker*).
- **Menulis & Pelacakan Kata**:
  - Kata yang kamu ketik di aplikasi mana pun dihitung **otomatis** lewat layanan aksesibilitas (tanpa tombol Mulai).
  - Pelacakan kata kunci (**tidak membedakan huruf besar/kecil**).
  - Tambah, hapus, dan reset hitungan kata terlacak.
- **Penggunaan Aplikasi**:
  - Periode cepat: Hari Ini, 7 Hari, Semua.
  - Dropdown **1–12 bulan** atau **1 tahun** sebagai periode kustom.
  - Total waktu penggunaan + statistik per aplikasi (jam, menit, detik tiap aplikasi).
  - Detail per aplikasi melalui *bottom sheet*.
  - Informasi **Battery** (level perangkat) dan **Penyimpanan**.
  - Notifikasi karena statistik per aplikasi bergantung pada izin Akses Penggunaan Aplikasi.
- **Bahasa**: Seluruh antarmuka menggunakan Bahasa Indonesia.
- **Mode gelap & terang** (mengikuti sistem, mendukung *dynamic color* di Android 12+).
- **Dua tema**: **Default** (Material You klasik) dan **color_1** (teal/cyan yang tajam). Pilih tema dari ikon palet 🎨 di dasbor.
- Aktivitas di negara kosong (*empty state*) dan instruksi izin yang jelas.

---

## Cara Penghitungan Menulis

Kata terlacak dihitung **otomatis** setiap kali kamu mengetik:

- Setelah layanan aksesibilitas aktif (dari Pengaturan > Aksesibilitas), teks yang sedang kamu ketik di **aplikasi mana pun** dibaca.
- Setiap kata yang kamu lacak dan muncul di teks ketikan akan **menambah hitungan** (`count`) di database — **tanpa** perlu menekan tombol Mulai.
- Pencocokan **tidak peka huruf besar/kecil** menggunakan ekspresi reguler dengan `(?i)` dan `Regex.escape` pada kata kunci.
- Kata yang sama dihitung berulang sesuai kemunculannya (`maaf maaf` → `maaf` bernilai **2×**; `Maaf MAAF maAf` → tetap **3×**).
- `CLEAR` penghitungan dilakukan lewat tombol **Reset hitungan** di pojok atas halaman Menulis.

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
│   ├── model/                   # Entity & data class (ClipboardItem, TrackedWord, ClipboardFolder, dll.)
│   ├── repository/              # DAO, Database, Repository, BackupManager
│   └── util/                    # WritingAnalyzer, TimeFormatter, AccessibilityUtils
├── service/                     # ClipboardRecordingService, WritingAccessibilityService
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

- Teks dipantau dengan polling `ClipboardManager` yang berjalan di **foreground service** (tanpa izin khusus; hanya berjalan saat **Mulai** ditekan). Service berjalan di *foreground* sehingga tetap merekam saat aplikasi berada di *background*.
- Data disimpan di tabel **Room** `clipboard_items` (teks, panjang, jumlah kata, cap waktu, folder).
- **Folder**: tabel `clipboard_folders`. Folder **Semua** disediakan otomatis (item `folderId = NULL`). Menambahkan item ke folder lain membuat **salinan**; item di "Semua" tetap ada. Menghapus item di "Semua" tidak menyinggung salinan di folder lain, begitu pula sebaliknya.
- Item dapat: disalin ulang, diedit, dihapus, dihapus semua (hanya scope "Semua"), dicari, ditambah manual.
- **Impor/Ekspor**: format **JSON**. `Traceboard` -> Ekspor menghasilkan file `traceboard-backup.json` berisi seluruh entri clipboard + cap waktu. Impor memvalidasi struktur data; teks yang sama tidak diimpor dua kali; file rusak ditangani dengan aman (menampilkan pesan gagal, tanpa crash). Item hasil impor masuk ke folder **Semua**.

---

## Usage Statistics & Permissions

Statistik penggunaan aplikasi membutuhkan akses khusus Android:

1. Buka hal **Penggunaan** → tombol **Buka Pengaturan**.
2. Pilih aplikasi **Traceboard** dan aktifkan **Akses Penggunaan Aplikasi** (*Usage Access*).
3. Kembali ke aplikasi, lalu tarik data (tombol refresh).

- **Periode**: chip cepat Hari Ini / 7 Hari / Semua, plus dropdown **1–6 bulan** (maksimal data sistem Android).
- Waktu ditampilkan presisi: contoh `1 jam 24 menit 17 detik` atau `45 detik`.
- Buka *bottom sheet* pada sebuah aplikasi untuk detailnya.

### Battery & Penyimpanan

- **Battery**: menampilkan level baterai perangkat saat ini (`BatteryManager`).
- **Penyimpanan**: total, sisa, dan terpakai dari penyimpanan internal (`StatFs`).
- Statistik **baterai per aplikasi** dan **penyimpanan per aplikasi** **tidak** tersedia melalui API Android standar yang bisa diandalkan — aplikasi **tidak membuat data palsu**; informasi tersebut ditampilkan sebagai "tidak tersedia".

---

## Permission & Privasi

`android:required="false"` tidak digunakan lagi karena manifest di atas API 31 menolaknya; aplikasi tetap bisa dipasang tanpa izin khusus.

- `PACKAGE_USAGE_STATS` — untuk statistik penggunaan aplikasi (wajib diaktifkan manual oleh pengguna).
- `QUERY_ALL_PACKAGES` — untuk membaca nama/ikon aplikasi pada daftar penggunaan.
- `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_DATA_SYNC` — untuk perekaman clipboard saat aplikasi di *background*.
- `POST_NOTIFICATIONS` — untuk menampilkan notifikasi status perekaman (Android 13+; diminta saat pertama kali membuka aplikasi).
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

**Hasil dari GitHub Actions sudah ditandatangani** oleh keystore yang dibuat otomatis saat CI, sehingga APK release (`traceboard-release.apk`) langsung bisa dipasang. Saat instalasi, Android akan menampilkan konfirmasi izin instal dari sumber tidak dikenal — pilih **Setujui/Izinkan**. Untuk distribusi publik, ganti keystore CI dengan keystore Anda sendiri.

---

## Troubleshooting

- **Statistik penggunaan kosong** → pastikan izin "Akses Penggunaan Aplikasi" sudah diaktifkan untuk Traceboard, lalu *refresh*. Pada perangkat baru, data perlu terkumpul seiring pemakaian.
- **Clipboard tidak tersimpan** → pastikan tombol **Mulai** aktif (tampil "Merekam"); jika aplikasi ditutup total, perekaman ikut berhenti dan akan lanjut saat aplikasi dibuka kembali.
- **Kata yang diketik tidak dihitung** → pastikan layanan aksesibilitas **Traceboard** sudah aktif di *Pengaturan > Aksesibilitas* (tampilkan banner di halaman Menulis).
- **Impor gagal** → pastikan file adalah hasil ekspor Traceboard (`traceboard-backup.json`) dan tidak rusak.
- **Build gagal di GitHub Actions** → cek log di tab *Actions*; masalah umum: versi JDK dan izin *executable* `gradlew` (sudah diatur `chmod +x` pada workflow).
- **APK besar** → gunakan build **Release** yang telah diaktifkan R8 + *resource shrinking*.

---

## Batasan Android API

- Statistik penggunaan aplikasi bergantung pada **Akses Penggunaan Aplikasi** yang harus diaktifkan manual oleh pengguna.
- Periode "Semua" dibatasi oleh data historis yang disimpan sistem Android (**maksimal ~6 bulan**, batasan sistem Android; data lebih lama otomatis dihapus oleh OS).
- Statistik battery/storage **per aplikasi** tidak tersedia lewat API publik yang stabil.
- Membaca clipboard di latar belakang penuh dibatasi sistem sejak Android 10; aplikasi merekam clipboard lewat **foreground service** saat perekaman **Mulai** menyala, dan berhenti bila aplikasi dimatikan total atau tombol Berhenti ditekan.
- Penghitungan kata menulis membutuhkan **layanan aksesibilitas** yang diaktifkan manual oleh pengguna.