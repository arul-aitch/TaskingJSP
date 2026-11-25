# REBUILD V2
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copy seluruh file proyek ke dalam container
COPY . .

# Jalankan Maven untuk membuat file .war (lewati testing agar cepat)
RUN mvn clean package -DskipTests

# ==========================================
# Tahap 2: Jalankan menggunakan Tomcat
# ==========================================
FROM tomcat:11.0-jdk17-temurin

# Hapus aplikasi default Tomcat
RUN rm -rf /usr/local/tomcat/webapps/*

# Ambil file WAR hasil build dari Tahap 1, masukkan ke Tomcat
COPY --from=build /app/target/*.war /usr/local/tomcat/webapps/ROOT.war

# Buka port 8080
EXPOSE 8080

# Jalankan Tomcat
CMD ["catalina.sh", "run"]