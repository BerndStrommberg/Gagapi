import keras
import tensorflow as tf
from keras.models import Sequential
from keras.layers import Dense, Activation, Dropout
from keras.models import load_model
import numpy as np

import flask
from flask import request, jsonify
import json


app = flask.Flask(__name__)

global model
model = load_model("moreData.h5")
global graph
graph = tf.get_default_graph()

labels = {
    1 : "Sitzen",
    2 : "Stehen",
    3 : "Laufen",
    
}
activity_stack = [3 for _ in range(10)]
status = 3

def average(activity_list):
    return sum(activity_list)/len(activity_list)

def update_activity_stack(data):
    activity_stack.insert(0, data)
    activity_stack.pop()
    global status
    status = average(activity_stack)




@app.route("/action", methods=['POST'])
def predict():
    if request.method == 'POST':
        data = request.data
        string_data = data.decode("utf8").replace("'", '"')
        data_json = json.loads(string_data)
        data_values = data_json['data']

        
        input = np.fromstring(data_values, dtype = float, sep=",")
        realInput = input.reshape((1,20))
        with graph.as_default():
            output = model.predict(realInput)
            activity = labels[np.argmax(output) + 1]
            print(activity)
            update_activity_stack(np.argmax(output) + 1)
                    

    return jsonify({"recieved" : True})


@app.route("/evaluate", methods=['POST'])
def present_data():
    send_status = int(status)
    json_res = {
        "activity_num" : status,
        "activity_label" : labels[send_status]   
    }

    return jsonify(json_res)

app.run()
