# SpamGuard Application

## Overview

SpamGuard is an Android application that detects spam notifications directly on the device using a lightweight machine learning model. The app monitors incoming notifications and classifies them as **spam, potential spam, or legitimate messages (ham)**.

The model is trained using a TF-IDF text representation and a Linear Support Vector Machine (SVM). After training, the model parameters are exported to JSON and integrated into the Android application for real-time inference.

---

## Features

* Real-time notification monitoring
* On-device spam classification
* Lightweight machine learning model
* No internet connection required for prediction
* Efficient TF-IDF feature extraction
* Fast Linear SVM based classification

---

## Machine Learning Model

The spam detection model is trained using Python and scikit-learn.

Model pipeline:

1. Dataset cleaning and preprocessing
2. Text normalization
3. TF-IDF vectorization
4. Linear SVM training
5. Model parameter extraction
6. Export of parameters as JSON files

Exported model components used in the Android app:

* `vocabulary.json`
* `idf_values.json`
* `svm_params.json`

These files allow the Android application to perform spam detection without needing Python or external ML frameworks.

---

## Project Structure

```
SpamGuard-Application
│
├── app/src/main
│   ├── java/com/example/spamguard
│   │   ├── MainActivity.kt
│   │   ├── SpamNotificationListener.kt
│   │   └── SpamClassifierEngine.kt
│   │
│   ├── res/layout
│   │   └── activity_main.xml
│   │
│   ├── assets
│   │   ├── vocabulary.json
│   │   ├── idf_values.json
│   │   └── svm_params.json
│   │
│   └── AndroidManifest.xml
│
├── model
│   └── train_model.py
│
├── dataset
│   └── spam_dataset.csv
│
├── requirements.txt
└── README.md
```

---

## Technologies Used

Android Development

* Kotlin
* Android SDK
* Notification Listener Service

Machine Learning

* Python
* Scikit-learn
* TF-IDF Vectorization
* Linear SVM

---

## How the App Works

1. The Android app listens for incoming notifications.
2. The notification text is extracted and preprocessed.
3. TF-IDF features are generated using the exported vocabulary and IDF values.
4. The Linear SVM model parameters are used to classify the message.
5. The notification is labeled as spam, potential spam, or ham.

---

## Model Training

To train the model again:

```
python train_model.py
```

This will generate the required JSON parameter files used by the Android application.

---

## Future Improvements

* Improve spam detection accuracy using larger datasets
* Add user feedback for model improvement
* Support multilingual spam detection
* Improve UI for notification management

---

## Author

[Sai Kiran](https://github.com/saikiran5247)
