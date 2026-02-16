package com.auticare.services;

public class EmotionResult {
    private String emotion;
    private double confidence;

    public EmotionResult(String emotion, double confidence) {
        this.emotion = emotion;
        this.confidence = confidence;
    }

    public String getEmotion() {
        return emotion;
    }

    public double getConfidence() {
        return confidence;
    }

    public String getEmotionFrench() {
        switch (emotion) {
            case "happy": return "😊 Joie";
            case "sad": return "😢 Tristesse";
            case "angry": return "😠 Colère";
            case "fearful": return "😰 Anxiété";
            case "neutral": return "😐 Neutre";
            case "disgust": return "🤢 Dégoût";
            case "surprise": return "😲 Surprise";
            default: return "🤔 " + emotion;
        }
    }

    public String getEventCategory() {
        switch (emotion) {
            case "happy": return "social";
            case "sad": return "support";
            case "angry": return "sport";
            case "fearful": return "sensoriel";
            case "neutral": return "workshops";
            default: return "all";
        }
    }
}