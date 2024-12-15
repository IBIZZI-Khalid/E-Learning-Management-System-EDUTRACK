# Utiliser une image de base OpenJDK
FROM openjdk:23-jdk-slim
# Définir le répertoire de travail
WORKDIR /app

# Mettre à jour et installer Maven, ainsi que les bibliothèques nécessaires pour X11 et VNC
RUN apt-get update && apt-get install -y \
    maven \
    libgl1-mesa-glx \
    libgtk-3-0 \
    wget \
    unzip \
    xvfb \
    x11vnc \
    fluxbox \
    && rm -rf /var/lib/apt/lists/*

# Télécharger et extraire JavaFX
RUN wget https://download2.gluonhq.com/openjfx/23.0.1/openjfx-23.0.1_linux-x64_bin-sdk.zip && \
    unzip openjfx-23.0.1_linux-x64_bin-sdk.zip -d /opt/javafx && \
    rm openjfx-23.0.1_linux-x64_bin-sdk.zip

# Copier le fichier pom.xml et le répertoire source dans l'image Docker
COPY pom.xml /app
COPY src /app/src

# Installer les dépendances Maven et construire le projet Java
RUN mvn clean package
RUN echo "Listing contents of target directory:" && ls -l target/ && echo "Listing contents of the jar file:" && jar tf target/edutrackck-1.0-SNAPSHOT.jar
# Définir la variable d'environnement DISPLAY pour X11
ENV DISPLAY=:99

# Exposer le port VNC
EXPOSE 5900

# Commande par défaut pour exécuter X11 et VNC, ainsi que l'application Java
CMD ["sh", "-c", "Xvfb :99 -screen 0 1280x720x24 & fluxbox & x11vnc -display :99 -nopw -forever & java --module-path /opt/javafx/javafx-sdk-23.0.1/lib --add-modules javafx.controls -jar target/edutrackck-1.0-SNAPSHOT-jar-with-dependencies.jar"]
