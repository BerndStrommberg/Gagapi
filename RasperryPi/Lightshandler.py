import LightshandlerTools as lgt
import time
import json
def run():
    while (True):
        response = str(lgt.get_current_state("192.168.0.97","8080"))
        print(response)
        act_json = json.loads(response)
        print(act_json['activity_num'])
        lgt.change_light(act_json['activity_num'])
        time.sleep(10)
        # Erst State vom Server Holen
        # Dann Hue anpassen

if __name__ == "__main__":
    run()
