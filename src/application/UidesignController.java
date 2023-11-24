package application;

import application.neuralnetwork.Layer;
import application.neuralnetwork.NeuralNetwork;
import application.neuralnetwork.Neuron;
import application.neuralnetwork.TrainingData;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.*;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.*;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.*;
import javafx.stage.FileChooser;

public class UidesignController implements Initializable {
    @FXML
    private Label iterationValue, learningRateValue, MSEValue, currentIterLabel;
    @FXML
    private LineChart errorChart;
    @FXML
    private ComboBox learningRateChoice;
    @FXML
    private RadioButton testOutput0, testOutput1, testOutput2, testOutput3;
    @FXML
    private TextField iterationsChoice, testInput0, testInput1, testInput2, testInput3;
    @FXML
    private Button chooseFileButton, runButton, stopButton;
    @FXML
    private Pane testPane;
    @FXML
    private Tooltip tooltip10, tooltip11, tooltip21, tooltip20, tooltip30, tooltip31;

    ObservableList<String> learningRateList = FXCollections
            .observableArrayList("0.001", "0.003", "0.01", "0.03",  "0.05", "0.1", "0.3", "0.5", "1", "3", "5", "10");
    private final static NeuralNetwork DNN = new NeuralNetwork();
//    private final TrainThread t1 = new TrainThread();
    private final UpdateThread updateThread = new UpdateThread();
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        chartSetup();
        learningRateChoice.setItems(learningRateList);
        updateThread.start();
        System.out.println("Initialized");
    }
    
    private void chartSetup(){
        //Setting up MSE chart
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        
        xAxis.setLabel("Seconds");
        xAxis.setAnimated(false);
        yAxis.setLabel("Error Value");
        yAxis.setAnimated(false);
        
        errorChart.setAnimated(false);
        errorChart.setCreateSymbols(false);
    }
    
    public void ChooseData(ActionEvent e){
        //Choose .csv data file
        FileChooser fc = new FileChooser();
        fc.setTitle("Choose data file (.csv)");
        FileChooser.ExtensionFilter csvFilter = new FileChooser.ExtensionFilter("CSV files", "*.csv");
        fc.getExtensionFilters().add(csvFilter);
        File file = fc.showOpenDialog(null);
        Alert alert = new Alert(Alert.AlertType.NONE);
        if (file != null){
            System.out.println(file.getAbsolutePath());
            DNN.CreateTrainingData(file.getAbsolutePath());
            runButton.setDisable(false);
        }
        else {
            alert.setAlertType(Alert.AlertType.ERROR);
            alert.setContentText("Invalid file");
            alert.show();
        }   
    }
    
    public class TrainThread extends Thread{
        @Override
        public void run(){
            DNN.setIterations(500);
            DNN.setLearningRate(0.05f);
            Platform.runLater(() -> {
                iterationValue.setText(String.valueOf(DNN.getIterations()));
                learningRateValue.setText(String.valueOf(DNN.getLearningRate()));
            });
            DNN.run();
        }
    }
    
    public void startRunning(ActionEvent e){
        new Thread(() -> {
            float learningRate = 0.05f; //default learning rate value
            if (learningRateChoice.getValue() != null)
                learningRate = Float.parseFloat(learningRateChoice.getValue().toString());
            int iterations = 1000; //default iteration number
            if (iterationsChoice.getText() != null){
                iterations = Integer.parseInt(iterationsChoice.getText());
            }
            DNN.setLearningRate(learningRate);
            DNN.setIterations(iterations);
            Platform.runLater(() -> {
                iterationsChoice.setDisable(true);
                learningRateChoice.setDisable(true);
                chooseFileButton.setDisable(true);
                runButton.setDisable(true);
                stopButton.setDisable(false);
                if (!testPane.isDisabled()){
                    testPane.setDisable(true);
                }
            });
            DNN.run();
        }).start();
    }
    
    public void stopRunning(ActionEvent e){
        DNN.setStop(true);
        Platform.runLater(() -> {
            iterationsChoice.setDisable(false);
            learningRateChoice.setDisable(false);
            chooseFileButton.setDisable(false);
            runButton.setDisable(false);
            stopButton.setDisable(true);
            testPane.setDisable(false);
        });
    }
    
    public void calculateTest(ActionEvent e) {
        float[] testInput = new float[4];
        Arrays.fill(testInput, 0);
        float[] testOutput = new float[4];
        Arrays.fill(testOutput, 0);
        Platform.runLater(() -> {
            if (!testInput0.getText().equals(""))
                testInput[0] = Float.parseFloat(testInput0.getText());
            else testInput[0] = 0;
            if (!testInput1.getText().equals(""))
                testInput[1] = Float.parseFloat(testInput1.getText());
            else testInput[1] = 0;
            if (!testInput2.getText().equals(""))
                testInput[2] = Float.parseFloat(testInput2.getText());
            else testInput[2] = 0;
            if (!testInput3.getText().equals(""))
                testInput[3] = Float.parseFloat(testInput3.getText());
            else testInput[3] = 0;
            
            if (testOutput0.isSelected()) testOutput[0] = 1;
            if (testOutput1.isSelected()) testOutput[1] = 1;
            if (testOutput2.isSelected()) testOutput[2] = 1;
            if (testOutput3.isSelected()) testOutput[3] = 1;
            
            Layer[] result = DNN.calculate(testInput);
            
            Scene scene = errorChart.getScene();
            Label neuronValue;
            for (int j = 0; j < result[0].neurons.length; j++){
                neuronValue = (Label) scene.lookup("#inputValue"+String.valueOf(j));
                neuronValue.setText(String.valueOf(result[0].neurons[j].getValue()));
            }
            for (int i = 1; i < result.length-1; i++){
                for (int j = 0; j < result[i].neurons.length; j++){
                    neuronValue = (Label) scene.lookup("#layerValue"
                        + String.valueOf(i)
                        + String.valueOf(j));
                    double value = result[i].neurons[j].getValue();
                    String stringValue = String.valueOf((double) Math.round(value * 100.0) / 100);
                    neuronValue.setText(stringValue);
                }
            }
            for (int j = 0; j < result[result.length-1].neurons.length; j++){
                neuronValue = (Label) scene.lookup("#outputValue" + String.valueOf(j));
                double value = result[result.length-1].neurons[j].getValue();
                String stringValue = String.valueOf((double) Math.round(value * 100.0) / 100);
                neuronValue.setText(stringValue);
            }
            for (int j = 0; j < testOutput.length; j++){
                neuronValue = (Label) scene.lookup("#expectedValue"
                + String.valueOf(j));
                double value = testOutput[j];
                String stringValue = String.valueOf((double) Math.round(value * 100.0) / 100);
                neuronValue.setText(stringValue);
            }
        });
    }
    
    public class UpdateThread extends Thread{
        @Override
        public void run(){
            System.out.println("UPDATING GRAPHS...");
            XYChart.Series<String, Number> dataSeries = new XYChart.Series<>();
            errorChart.getData().add(dataSeries);
            
            final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
            
            // put dummy data onto graph per second
            scheduledExecutorService.scheduleAtFixedRate(() -> {
                if (DNN.isRunning()) {
                    Double error = DNN.getMSE();
                    Layer[] layers = DNN.getLayers();
                    float[] expectedOutput = DNN.getCurrentExpOut();
                    TrainingData[] tDataSet = DNN.getDataSet();
                    Platform.runLater(() -> {
                        currentIterLabel.setText(String.valueOf(DNN.getCurrentIter()));
                        Scene scene = errorChart.getScene();
                        Date now = new Date();
                        for (int j = 0; j < layers[0].neurons.length; j++){
                            Label neuronValue = (Label) scene.lookup("#inputValue"
                            + String.valueOf(j));
                            neuronValue.setText(String.valueOf(layers[0].neurons[j].getValue()));
                        }
                        
                        for (int i = 1; i < layers.length-1; i++){
                            for (int j = 0; j < layers[i].neurons.length; j++){
                                Label neuronValue = (Label) scene.lookup("#layerValue"
                                    + String.valueOf(i)
                                    + String.valueOf(j));
                                double value = layers[i].neurons[j].getValue();
                                String stringValue = String.valueOf((double) Math.round(value * 100.0) / 100);
                                if (!neuronValue.getText().equals(stringValue)){
                                    neuronValue.setStyle("-fx-font-weight: bold;");
                                    neuronValue.setText(stringValue);
                                }
                                else neuronValue.setStyle("-fx-font-weight: regular;");
                            }
                        }
                        
                        tooltip10.setText(tooltipText(layers[1].neurons[0]));
                        tooltip11.setText(tooltipText(layers[1].neurons[1]));
                        tooltip20.setText(tooltipText(layers[2].neurons[0]));
                        tooltip21.setText(tooltipText(layers[2].neurons[1]));
                        tooltip30.setText(tooltipText(layers[3].neurons[0]));
                        tooltip31.setText(tooltipText(layers[3].neurons[1]));
                        for (int j = 0; j < layers[layers.length-1].neurons.length; j++){
                            Label neuronValue = (Label) scene.lookup("#outputValue"
                            + String.valueOf(j));
                            
                            double value = layers[layers.length-1].neurons[j].getValue();
                            String stringValue = String.valueOf((double) Math.round(value * 100.0) / 100);
                            if (!neuronValue.getText().equals(stringValue)){
                                neuronValue.setStyle("-fx-font-weight: bold;");
                                neuronValue.setText(stringValue);
                            }
                        }
                        
                        for (int j = 0; j < expectedOutput.length; j++){
                            Label neuronValue = (Label) scene.lookup("#expectedValue"
                            + String.valueOf(j));
                            
                            double value = expectedOutput[j];
                            String stringValue = String.valueOf((double) Math.round(value * 100.0) / 100);
                            neuronValue.setText(stringValue);
                        }
                        
                        dataSeries.getData().add(
                            new XYChart.Data<>(simpleDateFormat.format(now),
                          error));
                        MSEValue.setText(String.valueOf(error));
                    });
                }
                else {
                    Platform.runLater(() -> {
                        dataSeries.getData().removeAll();
                    });
                }
            }, 0, 600, TimeUnit.MILLISECONDS);
        }

        private String tooltipText(Neuron neuron) {
            String text = new String();
            float[] weights = neuron.getWeights();
            for (int i = 0; i<weights.length; i++){
                text += "w" + String.valueOf(i)+" = "+String.valueOf(weights[i]) + "\n";
            }
            text += "bias = " + String.valueOf(neuron.getBias());
            return text;
        }
    }
}
