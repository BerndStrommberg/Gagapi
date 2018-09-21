import requests
import json

def get_current_state(ip, port):
    url = "http://"+ip+":"+port+"/?action=present&data=none"
    print(url)
    r = requests.get(url)
    
    return r.text


def change_light(activity):
    red = 60872
    green = 25600
    blue = 47104
    color = 0
    url = "http://192.168.0.135:80/api/5686F1931E/lights/1/state"
    if activity == 1:
        # sitzen
    	color = blue
    elif activity == 2:
        # stehen
        color = red
    elif activity == 3:
        # laufen
        color = green
    
    print(color)
    data = {
        "on": True,
        "hue": color,
        "transitiontime": 50
    }

    
    r = requests.put(url, data=data)
    print(r.status_code)
