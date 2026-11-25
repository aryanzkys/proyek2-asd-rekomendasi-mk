# Sistem Rekomendasi Pengambilan Mata Kuliah (Proyek 2 ASD)

Implementasi sistem rekomendasi pengambilan mata kuliah wajib FILKOM Universitas Brawijaya menggunakan **100% struktur data mandiri di Java** (tanpa library koleksi bawaan seperti `ArrayList`, `LinkedList`, `HashMap`, `TreeMap`, atau `TreeSet`).

## Arsitektur & Algoritma
- **Struktur Data:** *Directed Acyclic Graph* (DAG) dengan *Custom Singly Linked List* (`NodeMK` & `LinkedListMK`) dan *dynamic array resizing* manual (`GraphKurikulum`).
- **Algoritma Penelusuran:** *Post-Order Depth-First Search* (DFS) untuk mendeteksi prasyarat berantai (transitif) dari semester terawal hingga prasyarat langsung.
- **Aturan Khusus:** Validasi kelulusan seluruh 36 MK wajib sebelum mengambil **Skripsi (`SKR`)**.

## Struktur Berkas
- `SistemRekomendasiMK.java` — Source code utama program.
- `matakuliah.csv` — Dataset 37 mata kuliah wajib, SKS, semester, dan periode penawaran.
- `prasyarat.csv` — Relasi sisi berarah prasyarat antar mata kuliah.
- `mhs_01.txt`, `mhs_02.txt`, `mhs_03.txt` — Data uji status kelulusan mahasiswa.
- `Laporan_Proyek2_ASD.pdf` — Laporan analisis struktur data, algoritma, dan hasil pengujian.

## Cara Menjalankan
```bash
javac SistemRekomendasiMK.java
java SistemRekomendasiMK mhs_01.txt
