import io
import json
import torch
import torch.nn as nn
from torchvision import models, transforms
from PIL import Image
from fastapi import FastAPI, File, UploadFile
from fastapi.middleware.cors import CORSMiddleware

app = FastAPI()

#  phone can connect to laptop
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

#  Loading Breed Info 
with open("breeds_info.json", "r") as f:
    breed_db = json.load(f)

#  Load Categories 
with open("classes.txt", "r") as f:
    categories = [line.strip() for line in f.readlines()]


device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
model = models.mobilenet_v3_small()
num_ftrs = model.classifier[3].in_features
model.classifier[3] = nn.Linear(num_ftrs, len(categories))


model.load_state_dict(torch.load("cattle_breed_model_v2.pth", map_location=device))
model.to(device)
model.eval()

#  Image Preprocessing
preprocess = transforms.Compose([
    transforms.Resize(256),
    transforms.CenterCrop(224),
    transforms.ToTensor(),
    transforms.Normalize([0.485, 0.456, 0.406], [0.229, 0.224, 0.225])
])

@app.get("/")
def home():
    return {"status": "Cattle Breed API is Running with Breed Wiki"}

@app.post("/predict")
async def predict(file: UploadFile = File(...)):
    try:
        # Reading the image
        image_bytes = await file.read()
        image = Image.open(io.BytesIO(image_bytes)).convert('RGB')
        
        # Process and Predict
        input_tensor = preprocess(image).unsqueeze(0).to(device)
        with torch.no_grad():
            outputs = model(input_tensor)
            # Convert raw scores to percentages
            probabilities = torch.nn.functional.softmax(outputs, dim=1)[0]
            # Get the top 3 results
            top_prob, top_indices = torch.topk(probabilities, 3)

        predictions = []
        for i in range(3):
            breed_name = categories[top_indices[i].item()]
            # Getting  details from JSON (matches the breed name)
            info = breed_db.get(breed_name, {
                "origin": "Unknown",
                "utility": "N/A",
                "milk_yield": "N/A",
                "traits": "N/A",
                "fact": "No additional info available."
            })
            
            predictions.append({
                "breed": breed_name,
                "confidence": round(top_prob[i].item() * 100, 2),
                "info": info
            })
        
        return {"predictions": predictions}
    
    except Exception as e:
        return {"error": str(e)}