package com.esports.manager.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;

@Configuration
public class FirebaseConfig {
    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);
    private static final String DEFAULT_CREDENTIALS_PATH = "firebase-service-account.json";

    @Bean
    public Firestore firestore() {
        try {
            // Check current directory, classpath, or env var
            File file = Paths.get(DEFAULT_CREDENTIALS_PATH).toFile();
            InputStream serviceAccountStream = null;

            if (file.exists()) {
                log.info("Found Firebase service account file at: {}", file.getAbsolutePath());
                serviceAccountStream = new FileInputStream(file);
            } else {
                InputStream resourceStream = getClass().getClassLoader().getResourceAsStream(DEFAULT_CREDENTIALS_PATH);
                if (resourceStream != null) {
                    log.info("Found Firebase service account in resources.");
                    serviceAccountStream = resourceStream;
                }
            }

            if (serviceAccountStream != null) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
                        .build();

                if (FirebaseApp.getApps().isEmpty()) {
                    FirebaseApp.initializeApp(options);
                    log.info("Firebase App initialized successfully.");
                }
                return FirestoreClient.getFirestore();
            } else {
                log.info("No Firebase service account key provided. Local JSON persistence is active.");
                return null;
            }
        } catch (Exception e) {
            log.warn("Could not initialize Firebase ({}); continuing in local mode.", e.getMessage());
            return null;
        }
    }

    @Bean
    @org.springframework.context.annotation.Primary
    public com.esports.manager.repository.DataStore dataStore(Firestore firestore, com.esports.manager.repository.LocalJsonDataStore localDataStore) {
        if (firestore != null) {
            log.info("🔥 Activating Cloud Firestore as the primary real-time DataStore.");
            com.esports.manager.repository.FirestoreDataStore store = new com.esports.manager.repository.FirestoreDataStore(firestore);
            store.init();
            return store;
        }
        log.info("⚡ Activating Local JSON DataStore as primary DataStore.");
        return localDataStore;
    }
}
