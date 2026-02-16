package com.auticare.test;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

public class TestCamera {
    public static void main(String[] args) {
        System.out.println("🔍 Test de caméra...");

        // Charger OpenCV
        nu.pattern.OpenCV.loadLocally();
        System.out.println("✅ OpenCV chargé");

        // Ouvrir caméra
        VideoCapture camera = new VideoCapture(0);
        if (camera.isOpened()) {
            System.out.println("✅ Caméra ouverte!");

            Mat frame = new Mat();
            if (camera.read(frame) && !frame.empty()) {
                System.out.println("✅ Image capturée! Dimensions: " + frame.width() + "x" + frame.height());
            } else {
                System.out.println("❌ Impossible de capturer une image");
            }

            camera.release();
        } else {
            System.out.println("❌ Impossible d'ouvrir la caméra");
        }
    }
}