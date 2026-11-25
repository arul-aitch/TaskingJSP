# ==========================================
# Tahap 1: Build Aplikasi menggunakan Maven
# ==========================================
FROM maven:3.9.6-eclipse-temurin-17 AS builder

# Set working directory di dalam container
WORKDIR /app

# Copy pom.xml dan download dependencies (agar build selanjutnya lebih cepat)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy seluruh source code (src)
COPY src ./src

# Build file WAR (lewati testing agar lebih cepat)
RUN mvn clean package -DskipTests

# ==========================================
# Tahap 2: Jalankan menggunakan Tomcat
# ==========================================
FROM tomcat:11.0-jdk17-temurin

# Hapus aplikasi default Tomcat
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy file WAR hasil build dari Tahap 1 ke Tomcat (sebagai ROOT agar bisa diakses langsung tanpa /TaskingJSP)
COPY --from=builder /app/target/*.war /usr/local/tomcat/webapps/ROOT.war

# Ekspos port 8080
EXPOSE 8080

# Jalankan Tomcat
CMD ["catalina.sh", "run"]