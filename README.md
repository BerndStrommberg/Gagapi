# Summary

In diesem Projekt haben wir eine Anwendung entwickelt, die die Bewegungsdaten eines herkömmlichen Smartphones klassifiziert und somit die durschnittliche Bewegung mehrerer Clients auswerten kann. 

Diese Informa

In this project we ghave build a software, that is able to classify the movement data from a normal smartphone. The classification works for multiple clients and this data will be used by a Rasperry Pi to adjust the light color of a Philips Hue to represent the mood at an event.

# Documentation

The Repository consists of three main folders.

## The Data and the Classifier
The Data was created throug the same App we use to send the data for classification. It contains around 14000 entries wich were splitted in train and test data to train the Neural Network we build with keras.

## App
This folder contains the code for the native Android-App.

I had nothing to do with that...

## RasperryPi
This folder contains the code for the Rasperry Pi.

### run()
Runs an endless while-loop (yeah I know...), calls 
get_current_state(), uses the response to change the light with change_light() every 10 seconds.

### LightsHandlerTools.get_current_state()
Sends a Post-Request to the server and recieves the current state of the server.

### LightsHandlerTools.change_light()
Sends a Request to the Philips Hue Gateway attached to the Rasperry and sends the needed data for changing the lights based on the given state.


## Server 
This folder contains the code for the main classification server.

The FlaskKerasApi.py module is responsible for handling incoming Http-Post-Requests.

### predict()
The incoming Post request sends the formatted data of the smartphone, which is then parsed and given to the AI-Classifier. The Classifiert creates a prediction which is added to the update_activity_stack() function.

### present_data()
Creates a return with the current status of the Server based on the movement data that already has been classified.

### update_activity_stack()
Ads a ne prediction of the classifier to the activity stack (an array with length of 10) and sets the status to average of the activity stack.

