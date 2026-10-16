# কথারবাক্স • KotharBaksho

<p align="center">
  <img src="app/src/main/res/drawable/app_logo.png" alt="KotharBaksho Logo" width="120" height="120" style="border-radius: 24px;" />
</p>

<p align="center">
  <b>দ্রুত • রিয়েল-টাইম • নিরাপদ</b><br>
  <i>Connect. Converse. Celebrate.</i>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=flat&logo=android&logoColor=white" alt="Platform: Android" />
  <img src="https://img.shields.io/badge/Language-Java-ED8B00?style=flat&logo=java&logoColor=white" alt="Language: Java" />
  <img src="https://img.shields.io/badge/Backend-Firebase-FFCA28?style=flat&logo=firebase&logoColor=black" alt="Backend: Firebase" />
  <img src="https://img.shields.io/badge/License-MIT-blue.svg?style=flat" alt="License: MIT" />
</p>

---

## 📖 About

**KotharBaksho (কথারবাক্স)** is a modern, lightweight, real-time messaging Android application designed for seamless communication. Infused with Bangladeshi cultural touches, vibrant Rickshaw Art inspired palettes, and clean floating card aesthetics, it delivers a secure and responsive chat experience.

---

## ✨ Features

- 📱 **Phone & OTP Verification**: 3-step phone authentication with automated code detection, resend timers, and 🇧🇩 `+880` default support.
- ✉️ **Email Login & Registration**: Optional email authentication path with instant password reset via email.
- 💬 **Real-Time 1-on-1 Chat**: Instant messaging powered by Firebase Realtime Database with millisecond synchronization.
- 📷 **Photo & Media Sharing**: Send images directly in conversations with cloud storage and preview caching.
- 🟢 **Live Online Presence**: Dynamic online/offline presence tracking with real-time indicators.
- ✍️ **Typing Indicators**: Live "typing..." feedback when conversation partners are composing messages.
- 🎨 **Modern Floating UI**:
  - Consistent elevated CardView design system (`@color/surface_card`, soft shadows, rounded corners).
  - Clean Poppins typography and emerald green (`#00A884`) primary branding.
  - Strict language separation: native Bangla on the main screen & splash; sleek English across authentication and settings.
- 🔒 **Safe Cloud Sync & Logout**: Secure session management with smart sync reassurance dialogs.

---

## 🛠️ Tech Stack & Architecture

- **Language**: Java
- **Target OS**: Android (API 24+ / Android 7.0 and above)
- **Backend Services**:
  - **Firebase Authentication**: Phone Auth with SMS OTP & Email/Password Auth
  - **Firebase Realtime Database**: Message queues, user presence, typing status
  - **Firebase Cloud Storage**: Profile avatars and media attachments
- **UI Components & Libraries**:
  - `androidx.cardview.widget.CardView` for hardware-accelerated elevated surfaces
  - `de.hdodenhof:circleimageview` for profile circular cropping
  - `com.squareup.picasso:picasso` for image loading and caching

---

## 🚀 Getting Started

### Prerequisites

- Android Studio Giraffe | Hedgehog | Iguana or newer
- JDK 17 or higher
- Android SDK with API 34 installed
- A Firebase project with Authentication, Realtime Database, and Cloud Storage enabled

### Installation

1. **Clone the repository**:
   ```bash
   git clone https://github.com/marufahmedanik/KotharBaksho.git
   cd KotharBaksho
   ```

2. **Add Firebase Configuration**:
   - Go to the [Firebase Console](https://console.firebase.google.com/).
   - Create an Android App with package name `com.kotharbaksho`.
   - Download the generated `google-services.json` file.
   - Place `google-services.json` inside the `app/` directory:
     ```
     KotharBaksho/
     └── app/
         └── google-services.json
     ```

3. **Build the Project**:
   ```bash
   # On Windows PowerShell
   .\gradlew.bat assembleDebug

   # On macOS / Linux
   ./gradlew assembleDebug
   ```

4. **Run on Device / Emulator**:
   - Open the project in Android Studio.
   - Select an emulator or physical device.
   - Click **Run** (`Shift + F10`).

---

## 📂 Project Structure

```
KotharBaksho/
├── app/
│   ├── src/main/
│   │   ├── java/com/kotharbaksho/
│   │   │   ├── splash.java               # Launcher splash with auth gate
│   │   │   ├── login.java                # Phone number entry screen
│   │   │   ├── OtpActivity.java          # 6-digit OTP verification screen
│   │   │   ├── ProfileSetupActivity.java # User profile completion screen
│   │   │   ├── EmailLoginActivity.java   # Email & password login screen
│   │   │   ├── registration.java         # Email registration screen
│   │   │   ├── MainActivity.java         # Main conversations list & search
│   │   │   ├── chatwindo.java            # Real-time chat & media messaging
│   │   │   ├── setting.java              # Profile editing & settings screen
│   │   │   ├── UserAdpter.java           # Recycler adapter for chat list
│   │   │   ├── messagesAdpter.java       # Recycler adapter for message bubbles
│   │   │   ├── Users.java                # User data model
│   │   │   └── msgModelclass.java        # Message data model
│   │   └── res/
│   │       ├── layout/                   # Clean, responsive XML layouts
│   │       ├── drawable/                 # Custom button shapes, badges, icons
│   │       └── values/                   # Colors, strings, themes, and styles
│   └── build.gradle
├── gradle/
├── build.gradle
├── settings.gradle
├── LICENSE
└── README.md
```

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

<p align="center">
  Developed with ❤️ by <a href="https://github.com/marufahmedanik">Maruf Ahmed Anik</a>
</p>
