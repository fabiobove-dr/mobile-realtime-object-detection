# Model: ssd_mobilenet_v1_1_metadata_1.tflite

https://www.tensorflow.org/lite/examples/object_detection/overview?hl=it#example_applications_and_guides
https://github.com/tensorflow/examples/tree/master/lite/examples/object_detection/android
The ssd_mobilenet_v1_1_metadata_1.tflite file's input takes normalized 300x300x3 shape image. And
the output is composed of 4 different outputs. The 1st output contains the bounding box locations,
2nd output contains the label number of the predicted class, 3rd output contains the probabilty of
the image belongs to the class, 4th output contains number of detected objects(maximum 10).
Repository for the model: https://github.com/joonb14/TFLiteDetection