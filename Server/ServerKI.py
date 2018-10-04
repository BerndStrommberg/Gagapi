import keras
import tensorflow as tf
from keras.models import Sequential
from keras.layers import Dense, Activation, Dropout
from keras.models import load_model
import numpy as np
#import pandas as pd

from http.server import BaseHTTPRequestHandler, HTTPServer
import logging
import json




mos_voice = "[MORITZ-COOL-VOICE]: " 
print(mos_voice + "Imported all dependencies")
print(mos_voice + "keras version: " + str(keras.__version__))

model = load_model("myData.h5")
print(mos_voice + "Model Loaded")
labels = {
    1 : "Sitzen",
    2 : "Stehen",
    3 : "Laufen",
    
}


activity_stack = [3 for _ in range(10)]
status = 3



def write_data_to_file(path, data):
    file = open(path, "a")
    file.write(data)
    file.write("\n")
    file.close()
    
def average(activity_list):
    return sum(activity_list)/len(activity_list)

def update_activity_stack(data):
    activity_stack.insert(0, data)
    activity_stack.pop()
    global status
    status = average(activity_stack)




class S(BaseHTTPRequestHandler):

    def _set_response(self):
        self.send_response(200)
        self.send_header('Content-type', 'application/json')
        self.end_headers()

    def do_GET(self):
        
        path = self.path
        arguments = path.split("&")
        try:
            action = arguments[0].split("=")[1]

            if (action == "evaluate"):
                data = arguments[1].split("=")[1]
                input = np.fromstring(data, dtype = float, sep=",")
                realInput = input.reshape((1,20))
                output = model.predict(realInput)
                activity = labels[np.argmax(output) + 1]
                update_activity_stack(np.argmax(output) + 1)
                print(activity)
                print("STATUS: ", status)
                #write_data_to_file("stayinghosentasche2.csv", data)
                
                self._set_response()
                self.wfile.write(json.dumps('{"predicted":true}').encode())
            
            elif (action == "present"):
                print("Action: Present")
                print("Status for sending ", status)

                send_status = int(status)
                json_res = {
                    "activity_num" : status,
                    "activity_label" : labels[send_status]   
                }

                print(json_res)
                
                res = json.dumps(json_res)
                self._set_response()
                self.wfile.write(res.encode())
                
        except IndexError:
            print("There was a fault with the response")            

 


def run(server_class=HTTPServer, handler_class=S, port=8080):
    #logging.basicConfig(level=logging.INFO)
    server_address = ('', port)
    httpd = server_class(server_address, handler_class)
    #logging.info('Starting httpd...\n')
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        pass
    httpd.server_close()
    #logging.info('Stopping httpd...\n')

if __name__ == '__main__':
    from sys import argv

    if len(argv) == 2:
        run(port=int(argv[1]))
    else:
        run()

