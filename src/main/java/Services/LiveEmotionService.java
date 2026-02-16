package com.auticare.services;

import javafx.scene.image.Image;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

public class LiveEmotionService {

    private VideoCapture camera;
    private CascadeClassifier faceDetector;
    private Mat currentFrame;
    private String currentEmotion = "😐 En attente...";
    private volatile boolean isRunning = false;
    private Thread captureThread;
    private Process pythonProcess;
    private Thread serverThread;
    private boolean serverAvailable = false;

    public LiveEmotionService() {
        try {
            // Charger OpenCV
            nu.pattern.OpenCV.loadLocally();
            System.out.println("✅ OpenCV chargé !");

            // Charger le détecteur de visages
            loadFaceDetector();

            // Démarrer le serveur Python
            startPythonServer();

            // Initialiser la caméra avec DirectShow
            initCamera();

            currentFrame = new Mat();
            System.out.println("✅ Service prêt !");

        } catch (Exception e) {
            System.err.println("❌ Erreur initialisation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadFaceDetector() {
        try {
            String path = "src/main/resources/haarcascade_frontalface_default.xml";
            File file = new File(path);

            if (file.exists()) {
                faceDetector = new CascadeClassifier(file.getAbsolutePath());
                if (!faceDetector.empty()) {
                    System.out.println("✅ Détecteur de visages chargé !");
                }
            } else {
                System.out.println("⚠️ Détecteur de visages non trouvé (optionnel)");
            }
        } catch (Exception e) {
            System.out.println("⚠️ Erreur chargement détecteur: " + e.getMessage());
        }
    }

    private void startPythonServer() {
        try {
            // Vérifier si le serveur est déjà en cours
            if (checkServerHealth()) {
                System.out.println("✅ Serveur Python déjà actif");
                serverAvailable = true;
                return;
            }

            // Lancer le script Python
            ProcessBuilder pb = new ProcessBuilder("python", "emotion_server.py");
            pb.redirectErrorStream(true);
            pythonProcess = pb.start();

            // Lire la sortie du processus
            serverThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(pythonProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("[Python] " + line);
                        if (line.contains("démarré sur")) {
                            serverAvailable = true;
                        }
                    }
                } catch (Exception e) {
                    System.err.println("❌ Erreur lecture Python: " + e.getMessage());
                }
            });
            serverThread.setDaemon(true);
            serverThread.start();

            // Attendre que le serveur démarre
            for (int i = 0; i < 10; i++) {
                Thread.sleep(1000);
                if (checkServerHealth()) {
                    serverAvailable = true;
                    System.out.println("✅ Serveur Python démarré !");
                    return;
                }
            }

            System.out.println("⚠️ Serveur Python non détecté, vérifie qu'il est lancé manuellement");

        } catch (Exception e) {
            System.err.println("❌ Erreur démarrage serveur: " + e.getMessage());
            System.out.println("ℹ️ Lance le serveur manuellement avec: python emotion_server.py");
        }
    }

    private boolean checkServerHealth() {
        try {
            URL url = new URL("http://localhost:5000/health");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(1000);
            conn.connect();
            return conn.getResponseCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    private void initCamera() {
        try {
            camera = new VideoCapture();

            // Essayer d'abord avec DirectShow (évite MSMF)
            System.out.println("📷 Tentative avec DirectShow...");
            if (camera.open(0, Videoio.CAP_DSHOW)) {
                System.out.println("✅ Caméra ouverte avec DirectShow !");
                configureCamera();
                return;
            }

            // Fallback sur les indices standards
            for (int i = 0; i < 3; i++) {
                if (camera.open(i)) {
                    System.out.println("✅ Caméra " + i + " ouverte !");
                    configureCamera();
                    return;
                }
            }

            System.err.println("❌ Impossible d'ouvrir la caméra");

        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
        }
    }

    private void configureCamera() {
        // Configurer les paramètres
        camera.set(Videoio.CAP_PROP_FRAME_WIDTH, 640);
        camera.set(Videoio.CAP_PROP_FRAME_HEIGHT, 480);
        camera.set(Videoio.CAP_PROP_FPS, 30);

        // Vérifier que la caméra est bien ouverte
        if (camera.isOpened()) {
            System.out.println("✅ Configuration terminée");
        }
    }

    public void start() {
        if (isRunning) return;

        if (camera == null || !camera.isOpened()) {
            System.err.println("❌ Impossible de démarrer: caméra non disponible");
            return;
        }

        isRunning = true;

        captureThread = new Thread(() -> {
            System.out.println("🎥 Thread de capture démarré");

            Mat frame = new Mat();
            Mat gray = new Mat();
            MatOfRect faces = new MatOfRect();

            while (isRunning) {
                try {
                    if (!camera.read(frame) || frame.empty()) {
                        Thread.sleep(10);
                        continue;
                    }

                    // Détecter les visages pour le rectangle
                    if (faceDetector != null && !faceDetector.empty()) {
                        Imgproc.cvtColor(frame, gray, Imgproc.COLOR_BGR2GRAY);
                        faceDetector.detectMultiScale(gray, faces);
                        Rect[] facesArray = faces.toArray();

                        if (facesArray.length > 0) {
                            Rect face = facesArray[0];

                            // Agrandir le rectangle de 30% pour inclure le contexte
                            int paddingX = (int)(face.width * 0.3);
                            int paddingY = (int)(face.height * 0.3);

                            int x = Math.max(face.x - paddingX, 0);
                            int y = Math.max(face.y - paddingY, 0);
                            int w = Math.min(face.width + 2 * paddingX, frame.width() - x);
                            int h = Math.min(face.height + 2 * paddingY, frame.height() - y);

                            Rect enlargedFace = new Rect(x, y, w, h);

                            // Rectangle ROUGE épais (pour le visage original)
                            Imgproc.rectangle(frame, face, new Scalar(0, 0, 255), 3);

                            // Rectangle VERT fin (pour la zone agrandie)
                            Imgproc.rectangle(frame, enlargedFace, new Scalar(0, 255, 0), 1);

                            // Texte
                            Imgproc.putText(frame, "VISAGE",
                                    new Point(face.x, face.y - 10),
                                    Imgproc.FONT_HERSHEY_SIMPLEX, 0.7,
                                    new Scalar(0, 255, 0), 2);

                            // Analyser avec la zone agrandie
                            if (serverAvailable) {
                                String emotion = analyzeWithPython(frame, enlargedFace);
                                currentEmotion = emotion;
                                System.out.println("🎭 Émotion: " + emotion);
                            } else {
                                currentEmotion = "😐 Serveur non connecté";
                                System.out.println("⚠️ Serveur Python non disponible");
                            }
                        } else {
                            currentEmotion = "👤 Aucun visage";
                        }
                    }

                    // Sauvegarder pour l'affichage
                    synchronized (this) {
                        if (currentFrame != null) {
                            currentFrame.release();
                        }
                        currentFrame = frame.clone();
                    }

                    Thread.sleep(30);

                } catch (Exception e) {
                    System.err.println("Erreur: " + e.getMessage());
                }
            }

            System.out.println("🛑 Thread de capture arrêté");
        });

        captureThread.setDaemon(true);
        captureThread.start();
        System.out.println("✅ Détection démarrée");
    }

    private String analyzeWithPython(Mat frame, Rect face) {
        try {
            System.out.println("📤 Envoi au serveur Python...");

            // Agrandir le rectangle de 30% pour inclure le contexte
            int paddingX = (int)(face.width * 0.3);
            int paddingY = (int)(face.height * 0.3);

            int x = Math.max(face.x - paddingX, 0);
            int y = Math.max(face.y - paddingY, 0);
            int w = Math.min(face.width + 2 * paddingX, frame.width() - x);
            int h = Math.min(face.height + 2 * paddingY, frame.height() - y);

            Rect enlargedFace = new Rect(x, y, w, h);

            // Extraire la région du visage et redimensionner à 256x256
            Mat faceROI = new Mat(frame, enlargedFace);
            Mat resizedFace = new Mat();
            Imgproc.resize(faceROI, resizedFace, new Size(256, 256));

            // Encoder en JPEG et Base64
            MatOfByte mob = new MatOfByte();
            Imgcodecs.imencode(".jpg", resizedFace, mob);
            String base64Image = Base64.getEncoder().encodeToString(mob.toArray());
            System.out.println("📦 Taille image: " + base64Image.length() + " caractères");

            // Préparer JSON
            String jsonInput = "{\"image\":\"" + base64Image + "\"}";

            // Connexion au serveur
            URL url = new URL("http://localhost:5000/analyze");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonInput.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            System.out.println("📤 Requête envoyée");

            int responseCode = conn.getResponseCode();
            System.out.println("📥 Code réponse: " + responseCode);

            if (responseCode == 200) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }

                    String resp = response.toString();
                    System.out.println("📨 Réponse reçue: " + resp);

                    // Parser avec Gson (fiable pour les emojis)
                    JsonObject jsonObject = JsonParser.parseString(resp).getAsJsonObject();

                    if (jsonObject.has("emotion")) {
                        String emotion = jsonObject.get("emotion").getAsString();
                        System.out.println("🎭 Émotion extraite: " + emotion);
                        return emotion;
                    } else {
                        System.out.println("⚠️ Pas de champ 'emotion' dans la réponse");
                    }
                }
            } else {
                // Lire l'erreur
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                    StringBuilder error = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        error.append(line);
                    }
                    System.err.println("❌ Erreur serveur: " + error.toString());
                }
            }

            return "😐 Analyse...";

        } catch (Exception e) {
            System.err.println("❌ Exception: " + e.getMessage());
            e.printStackTrace();
            return "😐 Erreur";
        }
    }


    public void stop() {
        System.out.println("🛑 Arrêt du service...");
        isRunning = false;

        if (captureThread != null) {
            try {
                captureThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        synchronized (this) {
            if (camera != null && camera.isOpened()) {
                camera.release();
                camera = null;
            }
            if (currentFrame != null) {
                currentFrame.release();
                currentFrame = null;
            }
        }

        System.out.println("✅ Service arrêté");
    }

    public Mat getCurrentFrame() {
        synchronized (this) {
            if (currentFrame == null || currentFrame.empty()) {
                return null;
            }
            return currentFrame.clone();
        }
    }

    public String getCurrentEmotion() {
        return currentEmotion;
    }

    public boolean isCameraOpen() {
        return camera != null && camera.isOpened();
    }

    public boolean isRunning() {
        return isRunning;
    }

    public Image matToFxImage(Mat mat) {
        if (mat == null || mat.empty()) return null;

        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".png", mat, buffer);
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }
}