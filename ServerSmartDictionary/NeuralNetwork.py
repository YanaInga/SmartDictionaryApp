import keras
import numpy as np
import tensorflow as tf
from keras import models
import TextAnalizator

model = models.load_model('model.h5', compile=False)
model.compile(optimizer='adam',
              loss='categorical_crossentropy',
              metrics=[tf.keras.metrics.Precision()])

def predict(value):
    level, word_freq, pred_values, pred_vectors = TextAnalizator.preprocessing_text(value)
    indx = 0
    for i, j in enumerate(pred_values):
        if np.nansum(pred_values[indx]) == 0:
            indexes = word_freq.index
            pred_values = np.delete(pred_values, indx, 0)
            pred_vectors = np.delete(pred_vectors, indx, 0)
            word_freq = word_freq.drop(index=indexes[indx], axis=0)
        else:
            indx = indx + 1
    if pred_values.shape[0] != 0:
        print(pred_vectors.shape[0])
        scores = model.predict([pred_vectors, pred_values])
    else:
        return "\n"
    answer = ""
    for i, j in enumerate(scores):
        max = scores[i].max()
        _level = np.where(scores[i] == max)
        if (_level[0][0] >= level):
            answer = answer + word_freq.index[i] + ":" + str(word_freq.iloc[i, 0]) + ":" + str(_level[0][0]) + "^"
    return answer + "\n"