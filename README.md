                                        BreedNode: Modular AI Framework for Livestock Traceability

BreedNode is an end-to-end Computer Vision and Mobile-AI application designed to automate livestock classification, management, and genetic tracking. Built specifically to handle real-world deployment challenges in agricultural and rural environments, the system features a high-throughput deep learning backend paired with a location-aware, user-friendly Android client interface designed for compatibility with platforms like the national Bharat Pashudhan database ecosystem.

🛠️ Tech Stack & Architecture

Backend (Machine Learning & API Gateway)
Core Framework: Python, PyTorch

Model Architecture: MobileNetV3-Small / ResNet-18 Transfer Learning (optimized via a custom training script to automate cattle classification based on physical traits)

API Framework: FastAPI (Asynchronous execution handling via Uvicorn and NumPy for high-throughput concurrent image processing streams)

Data Processing: NumPy, Pandas, Pillow (PIL)

Frontend (Mobile Client)
Language: Kotlin

UI Framework: Jetpack Compose (Modern, declarative UI components featuring bilingual localization support)

Location Services: Fused Location Provider API (Low-battery GPS tracking for mapping field coordinates)

Interactions: Android Explicit Intents (For automated Google Maps routing to veterinary services)

Networking: OkHttp / Retrofit for structured, asynchronous JSON contract communications with the backend gateway

📂 Repository Structure

Plaintext
BreedNode/
             ├── breednode-backend/     # FastAPI server scripts (main.py), model weights (.pth), and API endpoint configuration
             
               ├── breednode-android/     # Complete native Android Studio project container (app, assets, gradle configurations)
               
                     └── colab/    # Google Colab notebooks (.ipynb) capturing data preprocessing and PyTorch model training cycles

🚀 How to Run the Project Locally

1. Backend Setup (FastAPI & PyTorch)
First, make sure you have Python installed on your machine. Navigate into the backend directory:

Bash
cd breednode-backend
Create a virtual environment and activate it:

Bash
# On Windows
python -m venv venv
venv\Scripts\activate

# On Mac/Linux
python3 -m venv venv
source venv/bin/activate
Install the required dependencies:

Bash
pip install fastapi uvicorn torch torchvision numpy pillow
Start your Uvicorn local server:

Bash
uvicorn main:app --host 0.0.0.0 --port 8000 --reload
Note: Using --host 0.0.0.0 allows your physical Android device to connect to your laptop's backend server as long as both devices are connected to the same Wi-Fi network.

2. Frontend Setup (Android Studio)
Launch Android Studio.

Click Open and select your local breednode-android folder.

Let the Gradle sync complete automatically.

Open your networking configurations or client code (e.g., MainActivity.kt) and replace the placeholder base URL IP address with your laptop's local IPv4 network address (e.g., http://ip_address:8000).

Connect your physical Android smartphone via USB debugging or start an emulator, then click the Run ('app') button.

🚀 Future Roadmap & Production Implementation

To elevate BreedNode from a robust academic prototype to a high-resilience, government-grade utility, the production roadmap focuses on three core pillars:

Offline-First Layer (Android Room Database): To handle unstable internet connectivity in rural "shadow zones," future iterations will integrate an on-device Room Database. When a farmer scans an animal offline, the Breed Details, GPS Coordinates, and Timestamps will be securely locked into local phone storage. A background synchronization service will automatically push these records to the Bharat Pashudhan cloud once network coverage is restored, ensuring 100% data preservation.

CameraX API Integration: Upgrading the application's image capture module from standard gallery selection to an optimized, real-time CameraX lifecycle view directly inside the Jetpack Compose user interface.

On-Device PDF Generation: Emitting localized JSON backend payloads into standardized, offline-generated "Breed Identification Certificates" that farmers can download instantly to use for insurance claims, government subsidies, or livestock trade validation.

🎯 Conclusion

BreedNode successfully bridges the gap between deep learning complexity and practical, field-level accessibility. By combining a high-throughput FastAPI backend with a robust, location-aware Android client application, this platform provides an actionable, end-to-end engineering solution tailored directly for national livestock management and genetic traceability.
