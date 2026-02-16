from flask import Flask, request, jsonify
import cv2
import numpy as np
import mediapipe as mp
import base64
import math
from collections import Counter

app = Flask(__name__)

# Initialiser MediaPipe Face Mesh
mp_face_mesh = mp.solutions.face_mesh
face_mesh = mp_face_mesh.FaceMesh(
    static_image_mode=False,
    max_num_faces=1,
    min_detection_confidence=0.3,
    min_tracking_confidence=0.3,
    refine_landmarks=True
)

# Historique pour lisser
emotion_history = []
history_size = 3

def analyze_emotion_mediapipe(landmarks, img_shape):
    """Analyse MediaPipe avec seuils TRÈS bas"""
    h, w, _ = img_shape

    # INDICES DES POINTS CLÉS
    MOUTH_LEFT = 61
    MOUTH_RIGHT = 291
    MOUTH_TOP = 13
    MOUTH_BOTTOM = 14
    LEFT_EYEBROW_INNER = 70
    RIGHT_EYEBROW_INNER = 336
    LEFT_EYE_TOP = 159
    LEFT_EYE_BOTTOM = 145
    RIGHT_EYE_TOP = 386
    RIGHT_EYE_BOTTOM = 374
    NOSE_TIP = 4
    NOSE_BRIDGE = 168

    # Calculs
    mouth_left = landmarks[MOUTH_LEFT]
    mouth_right = landmarks[MOUTH_RIGHT]
    mouth_top = landmarks[MOUTH_TOP]
    mouth_bottom = landmarks[MOUTH_BOTTOM]

    mouth_width = abs(mouth_right.x - mouth_left.x) * w
    mouth_height = abs(mouth_bottom.y - mouth_top.y) * h
    mouth_open = mouth_height / (mouth_width * 0.2 + 0.001)

    smile_curvature = (mouth_left.y + mouth_right.y) / 2 - mouth_top.y

    left_eyebrow = landmarks[LEFT_EYEBROW_INNER]
    right_eyebrow = landmarks[RIGHT_EYEBROW_INNER]
    left_eye_top = landmarks[LEFT_EYE_TOP]

    eyebrow_height = ((left_eyebrow.y - left_eye_top.y) + (right_eyebrow.y - left_eye_top.y)) / 2 * h

    left_eye_open = abs(landmarks[LEFT_EYE_TOP].y - landmarks[LEFT_EYE_BOTTOM].y) * h
    right_eye_open = abs(landmarks[RIGHT_EYE_TOP].y - landmarks[RIGHT_EYE_BOTTOM].y) * h
    eye_open = (left_eye_open + right_eye_open) / 2

    nose_tip = landmarks[NOSE_TIP]
    nose_bridge = landmarks[NOSE_BRIDGE]
    head_tilt = nose_tip.x - nose_bridge.x

    # AFFICHAGE DEBUG
    print(f"\n📊 VALEURS:", flush=True)
    print(f"   Sourire: {smile_curvature:.4f}", flush=True)
    print(f"   Bouche ouverte: {mouth_open:.2f}", flush=True)
    print(f"   Sourcils hauteur: {eyebrow_height:.1f}px", flush=True)
    print(f"   Yeux ouverts: {eye_open:.1f}px", flush=True)
    print(f"   Tête inclinée: {head_tilt:.3f}", flush=True)

    # SCORES AVEC SEUILS EXTRÊMEMENT BAS
    scores = {}

    # JOIE 😊
    joy = 0
    if smile_curvature < -0.001:
        joy = min(1.0, abs(smile_curvature) * 80)
    scores["😊 Joie"] = joy

    # TRISTESSE 😢
    tristesse = 0
    if smile_curvature > 0.001:
        tristesse = min(1.0, smile_curvature * 80)
    scores["😢 Tristesse"] = tristesse

    # SURPRISE 😲
    surprise = 0
    if mouth_open > 0.1:
        surprise += min(1.0, mouth_open * 2)
    if eyebrow_height > 8:
        surprise += min(0.5, (eyebrow_height - 5) / 20)
    scores["😲 Surprise"] = min(1.0, surprise)

    # COLÈRE 😠
    colere = 0
    if eyebrow_height < 12:
        colere += (12 - eyebrow_height) / 15
    if head_tilt < -0.02:
        colere += 0.3
    scores["😠 Colère"] = min(1.0, colere)

    # PEUR 😨
    peur = 0
    if eyebrow_height > 10:
        peur += (eyebrow_height - 8) / 20
    if eye_open > 15:
        peur += 0.3
    scores["😨 Peur"] = min(1.0, peur)

    # DÉGOÛT 🤢
    degout = 0
    if head_tilt > 0.02:
        degout += min(0.5, head_tilt * 10)
    if mouth_open < 0.1:
        degout += 0.3
    scores["🤢 Dégout"] = min(1.0, degout)

    # NEUTRE 😐
    neutre = 0.2
    if abs(smile_curvature) < 0.01:
        neutre += 0.2
    if eye_open > 10 and eye_open < 20:
        neutre += 0.2
    scores["😐 Neutre"] = min(1.0, neutre)

    # Afficher tous les scores
    print("\n📊 SCORES:", flush=True)
    for emotion, score in sorted(scores.items(), key=lambda x: x[1], reverse=True):
        bar = "█" * int(score * 20)
        print(f"   {emotion}: {score:.2f} {bar}", flush=True)

    best = max(scores.items(), key=lambda x: x[1])
    return best[0], best[1]

@app.route('/analyze', methods=['POST'])
def analyze():
    try:
        data = request.json
        image_data = base64.b64decode(data['image'])

        nparr = np.frombuffer(image_data, np.uint8)
        img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

        if img is None:
            return jsonify({'error': 'Image invalide'}), 400

        print(f"📸 Image reçue: {img.shape}", flush=True)

        # Analyse MediaPipe
        img_rgb = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
        results = face_mesh.process(img_rgb)

        if not results.multi_face_landmarks:
            return jsonify({
                'emotion': '👤 Aucun visage',
                'confidence': 0,
                'face_detected': False
            })

        landmarks = results.multi_face_landmarks[0].landmark
        emotion, confidence = analyze_emotion_mediapipe(landmarks, img.shape)

        # Lissage avec historique
        global emotion_history
        emotion_history.append(emotion)
        if len(emotion_history) > history_size:
            emotion_history.pop(0)

        most_common = Counter(emotion_history).most_common(1)[0][0]

        return jsonify({
            'emotion': most_common,
            'confidence': confidence,
            'face_detected': True
        })

    except Exception as e:
        print(f"❌ Erreur: {str(e)}", flush=True)
        return jsonify({'error': str(e)}), 500

@app.route('/health', methods=['GET'])
def health():
    return jsonify({'status': 'ok'})

if __name__ == '__main__':
    print("=" * 50)
    print("🚀 Serveur d'émotions MEDIAPIPE SEUL")
    print("📡 http://localhost:5000")
    print("😊 7 émotions avec seuils TRÈS bas")
    print("=" * 50)
    app.run(host='0.0.0.0', port=5000, debug=True)