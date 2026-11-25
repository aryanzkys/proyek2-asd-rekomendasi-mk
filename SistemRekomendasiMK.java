import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SistemRekomendasiMK {

    // =========================================================================
    // 1. STRUKTUR DATA MANDIRI: SINGLY LINKED LIST (Tanpa java.util.LinkedList)
    // =========================================================================
    static class NodeMK {
        MataKuliah data;
        NodeMK next;

        NodeMK(MataKuliah data) {
            this.data = data;
            this.next = null;
        }
    }

    static class LinkedListMK {
        NodeMK head;
        NodeMK tail;
        int size;

        void add(MataKuliah mk) {
            NodeMK baru = new NodeMK(mk);
            if (head == null) {
                head = tail = baru;
            } else {
                tail.next = baru;
                tail = baru;
            }
            size++;
        }

        boolean isEmpty() {
            return size == 0;
        }
    }

    // =========================================================================
    // 2. VERTEX GRAF: MATA KULIAH
    // =========================================================================
    static class MataKuliah {
        int index;
        String kode;
        String nama;
        int sks;
        int semester;
        String periode; // Ganjil, Genap, Keduanya
        boolean sudahLulus;
        LinkedListMK prasyaratList; // Menyimpan edge masuk (prasyarat langsung)

        MataKuliah(int index, String kode, String nama, int sks, int semester, String periode) {
            this.index = index;
            this.kode = kode;
            this.nama = nama;
            this.sks = sks;
            this.semester = semester;
            this.periode = periode;
            this.sudahLulus = false;
            this.prasyaratList = new LinkedListMK();
        }
    }

    // =========================================================================
    // 3. STRUKTUR DATA MANDIRI: DIRECTED ACYCLIC GRAPH (DAG)
    // =========================================================================
    static class GraphKurikulum {
        MataKuliah[] daftarMK;
        int jumlahMK;

        GraphKurikulum(int kapasitasAwal) {
            this.daftarMK = new MataKuliah[kapasitasAwal];
            this.jumlahMK = 0;
        }

        // Menambah vertex dengan dynamic array resizing manual (tanpa ArrayList)
        void tambahMK(String kode, String nama, int sks, int semester, String periode) {
            if (jumlahMK == daftarMK.length) {
                MataKuliah[] arrayBaru = new MataKuliah[daftarMK.length * 2];
                for (int i = 0; i < jumlahMK; i++) {
                    arrayBaru[i] = daftarMK[i];
                }
                daftarMK = arrayBaru;
            }
            daftarMK[jumlahMK] = new MataKuliah(jumlahMK, kode, nama, sks, semester, periode);
            jumlahMK++;
        }

        // Pencarian linear O(V) berdasarkan kode MK
        MataKuliah cariMK(String kode) {
            for (int i = 0; i < jumlahMK; i++) {
                if (daftarMK[i].kode.equalsIgnoreCase(kode.trim())) {
                    return daftarMK[i];
                }
            }
            return null;
        }

        // Menambahkan directed edge: kodePrasyarat -> kodeTujuan
        void tambahPrasyarat(String kodePrasyarat, String kodeTujuan) {
            MataKuliah pras = cariMK(kodePrasyarat);
            MataKuliah tujuan = cariMK(kodeTujuan);
            if (pras != null && tujuan != null) {
                tujuan.prasyaratList.add(pras);
            }
        }

        // Reset status kelulusan jika ingin menguji beberapa file mahasiswa sekaligus
        void resetStatusLulus() {
            for (int i = 0; i < jumlahMK; i++) {
                daftarMK[i].sudahLulus = false;
            }
        }

        // ALGORITMA POST-ORDER DFS: Mengumpulkan seluruh prasyarat transitif yang belum lulus
        void kumpulkanPrasyaratBelumLulus(MataKuliah current, boolean[] visited, LinkedListMK hasil) {
            NodeMK ptr = current.prasyaratList.head;
            while (ptr != null) {
                MataKuliah prasyarat = ptr.data;
                if (!visited[prasyarat.index]) {
                    visited[prasyarat.index] = true;
                    // Telusuri ke tingkat prasyarat lebih awal terlebih dahulu
                    kumpulkanPrasyaratBelumLulus(prasyarat, visited, hasil);
                    // Catat saat backtracking jika prasyarat tersebut belum lulus
                    if (!prasyarat.sudahLulus) {
                        hasil.add(prasyarat);
                    }
                }
                ptr = ptr.next;
            }
        }
    }

    // =========================================================================
    // 4. PARSER INPUT & EVALUASI REKOMENDASI
    // =========================================================================
    public static void muatKurikulum(GraphKurikulum graf, String fileMK, String filePrasyarat) throws IOException {
        BufferedReader brMK = new BufferedReader(new FileReader(fileMK));
        String line = brMK.readLine(); // Lewati header
        while ((line = brMK.readLine()) != null) {
            if (line.trim().isEmpty()) continue;
            String[] col = line.split(";");
            graf.tambahMK(
                col[0].trim(),
                col[1].trim(),
                Integer.parseInt(col[2].trim()),
                Integer.parseInt(col[3].trim()),
                col[4].trim()
            );
        }
        brMK.close();

        BufferedReader brPras = new BufferedReader(new FileReader(filePrasyarat));
        line = brPras.readLine(); // Lewati header
        while ((line = brPras.readLine()) != null) {
            if (line.trim().isEmpty()) continue;
            String[] col = line.split(";");
            graf.tambahPrasyarat(col[0].trim(), col[1].trim());
        }
        brPras.close();
    }

    public static void prosesMahasiswa(GraphKurikulum graf, String fileMhs) throws IOException {
        graf.resetStatusLulus();
        String semesterBerikutnya = "";

        BufferedReader brMhs = new BufferedReader(new FileReader(fileMhs));
        String line;
        while ((line = brMhs.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("semester_berikutnya=")) {
                semesterBerikutnya = line.substring("semester_berikutnya=".length()).trim();
            } else if (line.startsWith("lulus=")) {
                String daftarRaw = line.substring("lulus=".length()).trim();
                if (!daftarRaw.isEmpty()) {
                    String[] kodeLulus = daftarRaw.split(",");
                    for (int i = 0; i < kodeLulus.length; i++) {
                        MataKuliah mk = graf.cariMK(kodeLulus[i].trim());
                        if (mk != null) {
                            mk.sudahLulus = true;
                        }
                    }
                }
            }
        }
        brMhs.close();

        // Hitung total MK wajib yang sudah lulus untuk syarat khusus Skripsi (SKR)
        int totalLulus = 0;
        for (int i = 0; i < graf.jumlahMK; i++) {
            if (graf.daftarMK[i].sudahLulus) {
                totalLulus++;
            }
        }

        LinkedListMK bisaDiambil = new LinkedListMK();
        LinkedListMK belumBisaDiambil = new LinkedListMK();
        String[] alasanBelumBisa = new String[graf.jumlahMK];

        for (int i = 0; i < graf.jumlahMK; i++) {
            MataKuliah mk = graf.daftarMK[i];

            // Aturan 1: Abaikan MK yang sudah lulus
            if (mk.sudahLulus) continue;

            // Aturan 2: Filter penawaran semester berikutnya (Ganjil/Genap/Keduanya)
            boolean sesuaiSemester = mk.periode.equalsIgnoreCase(semesterBerikutnya)
                                  || mk.periode.equalsIgnoreCase("Keduanya");
            if (!sesuaiSemester) continue;

            // Aturan 3: Persyaratan khusus Skripsi (SKR)
            if (mk.kode.equalsIgnoreCase("SKR")) {
                int syaratWajib = graf.jumlahMK - 1; // 36 MK wajib selain SKR
                if (totalLulus == syaratWajib) {
                    bisaDiambil.add(mk);
                } else {
                    belumBisaDiambil.add(mk);
                    int kurang = syaratWajib - totalLulus;
                    alasanBelumBisa[mk.index] = "belum lulus semua MK wajib (kurang " + kurang + " MK)";
                }
                continue;
            }

            // Aturan 4: Penelusuran prasyarat berantai dengan Post-Order DFS
            boolean[] visited = new boolean[graf.jumlahMK];
            LinkedListMK kurangPrasyarat = new LinkedListMK();
            graf.kumpulkanPrasyaratBelumLulus(mk, visited, kurangPrasyarat);

            if (kurangPrasyarat.isEmpty()) {
                bisaDiambil.add(mk);
            } else {
                belumBisaDiambil.add(mk);
                StringBuilder sb = new StringBuilder("belum lulus ");
                NodeMK ptr = kurangPrasyarat.head;
                while (ptr != null) {
                    sb.append(ptr.data.kode);
                    if (ptr.next != null) sb.append(", ");
                    ptr = ptr.next;
                }
                alasanBelumBisa[mk.index] = sb.toString();
            }
        }

        // Cetak Output Sesuai Format Instruksi
        System.out.println("======================================================================");
        System.out.println("HASIL REKOMENDASI: " + fileMhs + " (Semester Berikutnya: " + semesterBerikutnya.toUpperCase() + ")");
        System.out.println("======================================================================");
        System.out.println("Daftar MK yang bisa diambil:");
        NodeMK ptrBisa = bisaDiambil.head;
        int no = 1;
        int totalSksTersedia = 0;
        while (ptrBisa != null) {
            MataKuliah m = ptrBisa.data;
            System.out.printf("%2d. %-6s %-45s %d SKS%n", no++, m.kode, m.nama, m.sks);
            totalSksTersedia += m.sks;
            ptrBisa = ptrBisa.next;
        }
        System.out.println("Total SKS yang ditawarkan & memenuhi syarat: " + totalSksTersedia + " SKS");

        System.out.println("\nDaftar MK yang belum bisa diambil semester depan:");
        NodeMK ptrBelum = belumBisaDiambil.head;
        while (ptrBelum != null) {
            MataKuliah m = ptrBelum.data;
            System.out.println(m.kode + ": " + alasanBelumBisa[m.index]);
            ptrBelum = ptrBelum.next;
        }
        System.out.println();
    }

    public static void main(String[] args) {
        try {
            GraphKurikulum graf = new GraphKurikulum(50);
            muatKurikulum(graf, "matakuliah.csv", "prasyarat.csv");

            if (args.length > 0) {
                for (int i = 0; i < args.length; i++) {
                    prosesMahasiswa(graf, args[i]);
                }
            } else {
                // Default: jalankan pengujian untuk mhs_01.txt
                prosesMahasiswa(graf, "mhs_01.txt");
            }
        } catch (IOException e) {
            System.err.println("Terjadi kesalahan pembacaan file: " + e.getMessage());
        }
    }
}
