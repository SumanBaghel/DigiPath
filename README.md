# DigiPath - Digital Pathology Cell Segmentation Android App

DigiPath is an Android Studio developed app that leverages TensorFlow Lite (tf.lite) to perform cell segmentation for Digital Pathology applications. The app uses the Ki-67 patches dataset and employs the UNet algorithm to accurately identify three classes of cells: tumor, non-tumor, and background. This project was developed during an internship at NETR AI PVT. LTD., in collaboration with the Indian Institute of Technology (IIT), Bhilai.

## Table of Contents

- [Introduction](#introduction)
- [Prerequisites](#Prerequisites)
- [Installation](#installation)
- [Usage](#usage)
- [Features](#features)

## Introduction

Digital Pathology is a cutting-edge field that utilizes digital technology to analyze and interpret pathology slides and medical images. DigiPath is a user-friendly Android app that plays a vital role in Digital Pathology by offering real-time cell segmentation. By harnessing the power of the Ki-67 patches dataset and the UNet algorithm with TensorFlow Lite, DigiPath accurately classifies cells into three categories: tumor, non-tumor, and background. This app is designed to assist researchers and medical professionals in their endeavors to study and diagnose diseases more efficiently.

## Prerequisites

1. The Android Studio IDE (Android Studio 2021.2.1 or newer). This sample has been tested on Android Studio Chipmunk.
2. A physical Android device with a minimum OS version of SDK 23 (Android 6.0 - Marshmallow) with developer mode enabled. The process of enabling developer mode may vary by device.

## Installation

To use DigiPath on your Android device, follow these steps:

1. Clone this repository to your local machine: git clone https://github.com/SumanBaghel/DigiPath.git
2. Open the project in Android Studio.
3. Build and install the app on your Android device.

## Usage

1. Launch DigiPath on your Android device.
   ![Screenshot (66)](https://github.com/SumanBaghel/DigiPath/assets/89180252/cda95884-8da4-4419-ac0c-204bf61cfc5c)

2. Capture an image using the camera or choose one from the gallery and Initiate the cell segmentation process.
   ![1](https://github.com/SumanBaghel/DigiPath/assets/89180252/6c9258f0-df0b-41b7-affb-98c486dd2e9e =300x)

4. View the segmentation results, where cells are categorized into three classes: tumor, non-tumor, and background.
   ![1 1](https://github.com/SumanBaghel/DigiPath/assets/89180252/03a61da6-3419-4a32-a977-51875d49fda1 =300x)

## Features

- Real-time cell segmentation for Digital Pathology applications using the UNet algorithm and the Ki-67 patches dataset.
- Image capture through the gallery or camera.
- Segmentation results with three classes: tumor, non-tumor, and background.
- Export functionality for further analysis.





