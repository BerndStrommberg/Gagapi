import requests


def get_current_state(ip, port):
    r = request("http://" + ip + ":" + port +"/?action=present&data=none")
    
    return r.text


def change_light(activity):
    red = 5463738
    green = 82891289
    blue = 637623
    color = 0
    url = "pups"
    if activity == 1:
        # sitzen
    	color = blue
    elif activity == 2:
        # stehen
        color = red
    elif activity == 3:
        # laufen
        color = green

    data = {
        "on": True,
        "bri": 180,
        "hue": color,
        "sat": 255,
        "transitiontime": 10
    }
    r = request(url, data = data)