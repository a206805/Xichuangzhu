# A206805_XuYouyang_CikguIzwan_Project2

## Project Description
This repository contains the source code for Project 2 of the TK2323 / TM2213 Mobile Application Programming course. The application, **"Connected Global Learning Assistant"**, is a fully connected, persistent, and hardware-aware mobile application built with Kotlin and Jetpack Compose. It upgrades a basic classical literature reading app into an interactive learning platform that allows users to read text, write and save notes locally, fetch daily inspiration dynamically from the internet, and share their insights to a real-time global cloud community.

## SDG Theme
**SDG 4: Quality Education**
* **Focus:** Empowering students through accessible classical literature tools and building a connected, global knowledge-sharing community to prevent isolated learning.

## Feature List
This application successfully implements the four advanced technical pillars required for Project 2:
* **UI Expansion (7 Screens):** A seamless multi-screen flow using Jetpack Compose Navigation, including Search, Library, Detail, Write, Local Collection, Sensor API, and Cloud Community screens.
* **Local Persistence (Room Database):** Users can permanently save their custom study notes and translations on their device for offline access.
* **Web API Integration (Retrofit):** Connects to a free public REST API (`api.quotable.io`) using Retrofit and Gson to fetch and display live, dynamic inspirational quotes.
* **Hardware Sensor Integration (Accelerometer):** Utilizes the on-device accelerometer. Users can physically "shake" their smartphone to trigger the Web API call, making the discovery of new knowledge interactive.
* **Cloud Integration (Firebase Firestore):** Users can seamlessly push their locally saved notes to a public "Global Community Hall". The app uses real-time snapshot listeners to synchronize and display shared community data remotely.

## Setup Instructions
To run this project locally on your machine, please follow these steps:

1. **Clone the Repository:**
```bash
   git clone [https://github.com/yourusername/A206805_XuYouyang_CikguIzwan_Project2.git](https://github.com/yourusername/A206805_XuYouyang_CikguIzwan_Project2.git)
