# HesKit: İşçi Ödemeleri ve Çalışma Yönetim Sistemi

[![HesKit](https://github.com/menasy/HesKit-MobilApp/blob/main/OutFiles/heskit.png)](https://github.com/menasy/HesKit-MobilApp/blob/main/OutFiles/heskit.png)

[🎥 Tanıtım Videosunu İzle](https://github.com/menasy/HesKit-MobilApp/raw/main/OutFiles/HesKitVideo.mkv)

[📥 APK Dosyasını İndir](https://github.com/menasy/HesKit-MobilApp/raw/main/OutFiles/HesKitV1.apk)

---

Günümüz iş dünyasında, çalışanların maaş ödemeleri, mesai takibi ve ek ödemeler gibi finansal süreçleri yönetmek, işletmeler için büyük bir önem taşımaktadır. **HesKit**, bu süreci kolaylaştırmak ve işverenlerin çalışanlarıyla ilgili finansal işlemleri daha düzenli bir şekilde takip etmelerini sağlamak için geliştirilmiş kapsamlı bir mobil uygulamadır. **Android platformunda Java diliyle geliştirilmiş** olan bu uygulama, kullanıcı dostu arayüzü ve güçlü veritabanı altyapısı ile iş süreçlerini optimize eder.

---

## **Öne Çıkan Özellikler**

### **1. Çalışan Yönetimi**

- **Çalışan Takibi:** Uygulama açıldığında kullanıcıyı karşılayan ana ekranda, tüm çalışanlar için yapılan **toplam havale miktarı, toplam harçlık ödemeleri, tüm işçilere verilen toplam para ve toplam işçi sayısı** gibi finansal özet bilgiler yer alır. Kullanıcı, **"Çalışanlar"** butonuna tıklayarak kayıtlı çalışanların listelendiği **RecyclerView** ekranına yönlendirilir.
- **Çalışan Ekleme:** Yeni çalışan eklemek isteyen kullanıcılar, **"Çalışan Ekle"** butonu ile **isim, soyisim ve başlangıç tarihini** girerek kayıt oluşturabilirler. Kaydedilen bilgiler hem **veritabanına** hem de çalışan listesine eklenir.
- **Çalışan Detayları:** Çalışan profiline tıklayan kullanıcılar, **EmployeeProcess** ekranına yönlendirilir. Bu ekranda çalışanın başlangıç tarihi, toplam çalışma süresi, yapılan havale işlemleri ve verilen harçlıklar gibi finansal bilgiler detaylı şekilde gösterilir.

### **2. Ödeme ve Finansal İşlemler**

- **Havale İşlemleri:** Çalışanlara yapılan havale işlemleri **Havale Fragmenti** üzerinden gerçekleştirilebilir. Kullanıcı, havale miktarını ve alıcı bilgisini girerek işlemi tamamlar. Her havale işlemi **Transfer** nesnesi olarak oluşturulup **veritabanına** kaydedilir. **RecyclerView** üzerinden tüm transfer geçmişi görüntülenebilir.
- **Harçlık Yönetimi:** Çalışanlara verilen harçlık ödemeleri, **Harçlık İşlemleri** ekranında kayıt altına alınır ve görüntülenir.
- **İşlem Güncellenmesi ve Silme:** Kullanıcılar, ödeme geçmişinde herhangi bir kayda tıkladığında **AlertDialog** ile silme işlemini onaylayabilirler. Ayrıca "Tümünü Sil" butonu ile geçmiş işlemler tamamen temizlenebilir.

### **3. Çalışma ve Mesai Günleri Takibi**

- **Çalışılmayan Günlerin Yönetimi:** Çalışanların mazeretli veya mazeretsiz olarak işe gelmediği günler **NotWorkDaysProcess** ekranında yönetilir. Kullanıcı, **çalışmadığı gün sayısını, tarihini ve sebebini** girerek kayıt oluşturur. Bu veriler **veritabanına** kaydedilir ve geçmiş kayıtlar **RecyclerView** ile görüntülenir.
- **Mesai Günleri ve Fazla Çalışma:** Çalışanlar listesinde belirli çalışanlara uzun basılı tutarak seçim yapılabilir ve **Mesai Ekle** butonu aktif hale gelir. Burada girilen **fazladan çalışma süresi** ile **OverDay** nesnesi oluşturulur. Kaydedilen mesailer **OverDayProcess** ekranında listelenir.

### **4. Gerçek Zamanlı Veri Güncelleme ve Yönetimi**

- **Dinamik Veritabanı Yönetimi:** **DBHelper** sınıfı sayesinde uygulamadaki tüm veriler, çalışan profilleri ve ana ekranlar ile senkronize bir şekilde güncellenir.
- **Singleton Yapısı ile Verimli Veri İşleme:** **Singleton** tasarım deseni kullanılarak, tüm fragmentler veritabanına güvenli ve verimli bir şekilde erişebilir.
- **Seçim ve Toplu İşlemler:** Çalışanlar listesinde uzun basılı tutarak birden fazla çalışan seçilebilir ve toplu işlemler gerçekleştirilebilir.

---

## **Sonuç**

**HesKit**, işçi ödemelerinin düzenlenmesi, finansal kayıtların takibi ve çalışma günlerinin yönetimi gibi iş süreçlerini optimize eden kapsamlı bir mobil uygulamadır. Kullanıcı dostu tasarımı sayesinde işverenler ve yöneticiler, çalışanlara yönelik finansal işlemleri hızlı ve hatasız bir şekilde gerçekleştirebilirler. Gerçek zamanlı veri güncelleme ve güçlü veritabanı altyapısıyla **HesKit**, işletmelerin en güvenilir işçi yönetim araçlarından biri olmaya adaydır.

📥 **[HesKit APK Dosyasını İndir](https://github.com/menasy/HesKit-MobilApp/blob/main/OutFiles/HesKitV1.apk)**

📺 **[Uygulama Tanıtım Videosunu İndir](https://github.com/menasy/HesKit-MobilApp/blob/main/OutFiles/HesKitVideo.mkv)**
