# Tugas Kecil 1 Strategi Algoritma

## Deskripsi Program
IQ Puzzler Pro adalah sebuah program yang dirancang untuk menyelesaikan puzzle berbasis grid dengan menggunakan algoritma Brute Force. Program ini dapat membaca konfigurasi papan dan blok puzzle dari file input, kemudian mencoba menempatkan blok-blok tersebut ke dalam papan hingga menemukan solusi yang valid. Program juga menyediakan opsi untuk menyimpan solusi dalam bentuk file teks (.txt) atau gambar (.png). Program ini sangat berguna untuk menyelesaikan puzzle secara otomatis, memvisualisasikan solusi puzzle dalam bentuk teks atau gambar, serta menyimpan solusi untuk referensi atau analisis lebih lanjut.

## Cara Menjalankan Program

### Clone Repository
Lakukan clone pada repository Github dengan mengetikkan:
```bash
git clone https://github.com/graceevelyns/Tucil1_13523087.git
```

### Pastikan Java Development Kit (JDK) Telah Terinstal
```bash
java -version
javac -version
```
Jika perintah ini menampilkan versi Java, maka JDK telah terinstal. Jika belum, silahkan download JDK terlebih dahulu sebelum menjalankan program.

### Buka Terminal/Command Prompt
Buka terminal maupun command prompt pada direktori utama (Tucil1_13523087/).

### Kompilasi Program
Kompilasi file IQPuzzlerPro.java yang terdapat pada folder src/ dengan mengetikkan:
```bash
javac -d bin src/IQPuzzlerPro.java
```

### Jalankan Program
Setelah dikompilasi, jalankan program dengan mengetikkan case yang ingin diuji:
```bash
java -cp bin src.IQPuzzlerPro test/testcase/case1.txt
java -cp bin src.IQPuzzlerPro test/testcase/case2.txt
java -cp bin src.IQPuzzlerPro test/testcase/case3.txt
java -cp bin src.IQPuzzlerPro test/testcase/case4.txt
java -cp bin src.IQPuzzlerPro test/testcase/case5.txt
java -cp bin src.IQPuzzlerPro test/testcase/case6.txt
java -cp bin src.IQPuzzlerPro test/testcase/case7.txt
```

## Identitas Pembuat
**Nama**    : Grace Evelyn Simon
**NIM**     : 13523087
**Jurusan** : Teknik Informatika