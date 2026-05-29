import requests
from bs4 import BeautifulSoup
import json

def generate_breed_json():
    url = "https://www.nddb.coop/services/animalbreeding/geneticimprovement/breeds"
    print("Connecting to Government Database...")
    
    try:
        response = requests.get(url, timeout=10)
        soup = BeautifulSoup(response.text, 'html.parser')
        
        table = soup.find('table') 
        rows = table.find_all('tr')
        
        breed_data = {}
        
        for row in rows[1:]: 
            cols = row.find_all('td')
            if len(cols) >= 3:
                name = cols[1].text.strip()
                origin = cols[2].text.strip()
                usage = cols[3].text.strip()
                
                
                breed_data[name] = {
                    "origin": origin,
                    "milk_yield": "Check ICAR standards", # NDDB table doesn't have yield
                    "description": f"Commonly used for {usage}. Adapted to {origin} climate."
                }
        
        # Save to JSON file
        with open("breeds_info.json", "w") as f:
            json.dump(breed_data, f, indent=4)
            
        print(f"✅ Success! Generated data for {len(breed_data)} breeds.")
        
    except Exception as e:
        print(f"❌ Error fetching online: {e}")
        print("Falling back to internal expert list...")

if __name__ == "__main__":
    generate_breed_json()