# Gunakan image resmi Tomcat 11 dengan JDK 17 (sesuai proyekmu)
FROM tomcat:11.0-jdk17-temurin

# Hapus aplikasi default Tomcat
RUN rm -rf /usr/local/tomcat/webapps/*

# Salin file WAR hasil build ke dalam direktori webapps Tomcat
# Ganti 'TaskingJSP.war' dengan nama file WAR kamu yang sebenarnya di folder target
COPY target/TaskingJSP.war /usr/local/tomcat/webapps/ROOT.war

# Ekspos port 8080 (Railway akan mengarahkan traffic ke port ini)
EXPOSE 8080

# Perintah untuk menjalankan Tomcat
CMD ["catalina.sh", "run"]