import tensorflow as tf
import sys
import os

# Forcer tensorflow.keras à être accessible
if not hasattr(tf, 'keras'):
    tf.keras = __import__('keras')

print("✅ Fix appliqué - tensorflow.keras est maintenant disponible")