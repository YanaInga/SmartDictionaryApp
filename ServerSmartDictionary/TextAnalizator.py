import spacy
import re
import pandas as pd
import numpy as np
from collections import Counter
import en_core_web_sm

nlp = en_core_web_sm.load()
data_values = pd.read_csv("data_values.csv", sep=";")
word_index = np.load('word_index.npy',allow_pickle=True).item()
mean = [ 7.0716609e+04,  6.6366901e+02, -4.4070408e-01]
std = [5.4713944e+05, 7.7382492e+01, 2.5573093e-01]

def remove_names(text):
    doc = nlp(text)
    newString = text
    deleted_ents = []
    for e in reversed(doc.ents):
        if (e.label_ == "PERSON") or (e.label_ == "MONEY") or (e.label_ == "TIME") or (e.label_ == 'ORG') or (e.label_ == 'QUANTITY'):
            newString = newString[:e.start_char] + str(newString[e.start_char + len(e.text):])
            if not (e.text in deleted_ents):
                deleted_ents.append(e.text)
    for ent in deleted_ents:
        if newString.find(ent) != -1:
            newString = newString.replace(ent, "")
    return newString

def decontracted(phrase):
    phrase = re.sub(r"won't", "will not", phrase)
    phrase = re.sub(r"can\'t", "can not", phrase)
    phrase = re.sub(r"n\'t", " not", phrase)
    phrase = re.sub(r"\'re", " are", phrase)
    phrase = re.sub(r"\'s", " is", phrase)
    phrase = re.sub(r"\'d", " would", phrase)
    phrase = re.sub(r"\'ll", " will", phrase)
    phrase = re.sub(r"\'t", " not", phrase)
    phrase = re.sub(r"\'ve", " have", phrase)
    phrase = re.sub(r"\'m", " am", phrase)
    return phrase

def preprocessing_text(value):
    value = value.split('≈')
    repeats = int(value[0])
    level = int(value[1])
    sentence = value[2]
    sentence = remove_names(sentence)
    sentence = decontracted(sentence)
    doc = nlp(sentence)
    sentence = " ".join([word.lemma_ for word in doc])
    sentence = sentence.lower()
    doc = nlp(sentence)
    words = [token.text for token in doc if not (token.is_punct or token.is_space)]
    word_freq = pd.Series(dict(Counter(words).most_common()))
    word_freq = word_freq[word_freq >= repeats]
    word_freq = pd.DataFrame(word_freq, columns=['frequence'])
    pred_values = pd.merge(word_freq, data_values, how='left', left_index=True, right_on="Word")
    pred_values.drop(pred_values.columns[[0, 1]], axis=1, inplace=True)
    pred_values = np.array(pred_values, dtype=np.float32)
    pred_values -= mean
    pred_values /= std
    pred_vectors = np.zeros(word_freq.shape[0], dtype=int)
    for index, value in enumerate(word_freq.index):
      inx = word_index.get(value)
      if inx is not None:
        pred_vectors[index] = inx
    pred_vectors = np.reshape(pred_vectors, (word_freq.shape[0], 1))
    return level, word_freq, pred_values, pred_vectors
