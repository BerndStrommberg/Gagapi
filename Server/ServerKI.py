import keras
import tensorflow as tf
from keras.models import Sequential
from keras.layers import Dense, Activation, Dropout
from keras.models import load_model
import numpy as np
#import pandas as pd

from http.server import BaseHTTPRequestHandler, HTTPServer
import logging




mos_voice = "[MORITZ-COOL-VOICE]: " 
print(mos_voice + "Imported all dependencies")
print(mos_voice + "keras version: " + str(keras.__version__))

model = load_model("without_fouier_try.h5")
print(mos_voice + "Model Loaded")
labels = {
    1 : "WALKING",
    2 : "WALKING_UPSTAIRS",
    3 : "WALKING_DOWNSTAIRS",
    4 : "SITTING",
    5 : "STANDING",
    6 : "LAYING" 
}

class S(BaseHTTPRequestHandler):
    def _set_response(self):
        self.send_response(200)
        self.send_header('Content-type', 'text/html')
        self.end_headers()

    def do_GET(self):
        #logging.info("GET request,\nPath: %s\nHeaders:\n%s\n", str(self.path), str(self.headers))
        path = self.path
        index = path.find('=') + 1
        data = path[index:]


        """ print("---This is the recieved data---")
        print(data)
 """
        
 
        print("---This is the type of the data---")
        print(type(data))

        print("---splitting the data by commas---")
        input = np.fromstring(data, dtype = float, sep=",")
        print(input.shape)
        print(input)
        realInput = input.reshape((1,20))
        if(any(any(x < -1 or x > 1 for x in array) for array in realInput)):
        	print("DATA outside [-1,1] !")
        else:
        	print("DATA ALRIGHT")


        
        print("Reshaped Input")
        print(realInput.shape)
        print(realInput)
        output = model.predict(realInput)
        print("---Generatet output---")
        print(output)
        print(labels[np.argmax(output)+1])
        
        file = open("sitting_data.csv", "a")
        file.write("\n")
        file.write(data)
        print("wrote data")
        file.close()


  



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

