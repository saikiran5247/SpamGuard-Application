import pandas as pd
import re

from sklearn.model_selection import train_test_split
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.svm import LinearSVC
from sklearn.metrics import accuracy_score, classification_report, confusion_matrix


# Load dataset
df = pd.read_csv("spam_dataset.csv")

# Cleaning
df = df.drop_duplicates()
df = df.dropna(subset=["label","message"])
df["message"] = df["message"].astype(str)
df = df[df["message"].str.strip() != ""]
df["label"] = df["label"].str.lower().str.strip()

# Text preprocessing
def clean_text(text):
    text = text.lower()
    text = re.sub(r"http\S+|www\S+", " ", text)
    text = re.sub(r"\S+@\S+", " ", text)
    text = re.sub(r"\d+", " ", text)
    text = re.sub(r"[^a-z\s]", " ", text)
    text = re.sub(r"\s+", " ", text).strip()
    return text

df["clean_message"] = df["message"].apply(clean_text)

# Train-test split
X_train, X_test, y_train, y_test = train_test_split(
    df["clean_message"],
    df["label"],
    test_size=0.2,
    random_state=42,
    stratify=df["label"]
)

# TF-IDF
vectorizer = TfidfVectorizer(
    max_features=7000,
    stop_words="english",
    ngram_range=(1,2),
    min_df=2,
    sublinear_tf=True
)

X_train_vec = vectorizer.fit_transform(X_train)
X_test_vec = vectorizer.transform(X_test)

# Train Linear SVM
svm_model = LinearSVC()
svm_model.fit(X_train_vec, y_train)

# Prediction
pred = svm_model.predict(X_test_vec)

# Evaluation
print("Accuracy:", accuracy_score(y_test, pred))
print("\nClassification Report:\n")
print(classification_report(y_test, pred))
print("\nConfusion Matrix:\n")
print(confusion_matrix(y_test, pred))

# Extract SVM parameters

vocabulary = vectorizer.vocabulary_
idf_values = vectorizer.idf_

classes = svm_model.classes_

svm_weights = svm_model.coef_
svm_bias = svm_model.intercept_

print("Parameters extracted successfully")

import json

# Convert vocabulary to JSON format
vocab_json = {word: int(index) for word, index in vocabulary.items()}

# Convert IDF values
idf_json = idf_values.tolist()

# Convert SVM parameters
svm_data = {
    "classes": classes.tolist(),
    "weights": svm_weights.tolist(),
    "bias": svm_bias.tolist()
}

print("JSON conversion complete")

# Save vocabulary
with open("vocabulary.json", "w") as f:
    json.dump(vocab_json, f)

# Save IDF values
with open("idf_values.json", "w") as f:
    json.dump(idf_json, f)

# Save SVM parameters
with open("svm_params.json", "w") as f:
    json.dump(svm_data, f)

print("SVM model exported successfully")
