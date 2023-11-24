package application.neuralnetwork;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class NeuralNetwork {
    public static boolean stop;
    
    static public Layer[] layers;
    static public TrainingData[] tDataSet;
    static public float[] currentExpOut;
    static public long currentIter;
    
    static public int iterations = 1000;
    static public float learningRate = 0.05f;
    
    public void setIterations(int iterations){
        this.iterations = iterations;
    }
    public void setLearningRate(float learningRate){
        this.learningRate = learningRate;
    }
    public float[] getCurrentExpOut(){
        return currentExpOut;
    }
    public long getCurrentIter(){
        return currentIter;
    }
    
    public int getIterations(){
        return iterations;
    }
    public float getLearningRate(){
        return learningRate;
    }
    
    public void setStop(boolean value){
        this.stop = value;
    }
    public boolean isRunning(){
        return !stop;
    }
    
    public Layer[] getLayers(){
        return layers;
    }
    public TrainingData[] getDataSet(){
        return tDataSet;
    }
    
    public NeuralNetwork(){
        Neuron.setRangeWeight(-1,1);
    	stop = true;
        
    	layers = new Layer[5];
    	layers[0] = null;
    	layers[1] = new Layer(4,6);
    	layers[2] = new Layer(6,6);
        layers[3] = new Layer(6,6);
    	layers[4] = new Layer(6,4);
    }
    
    public double getMSE(){
        double sum = 0;
        for (int i = 0; i < tDataSet.length; i++){
            List<Float> listResult = Arrays.asList(layers[layers.length-1].getNeurons())
                .stream()
                .map(p -> p.getValue())
                .collect(Collectors.toList());
            float[] result = new float[listResult.size()];
            for (int l = 0; l < listResult.size(); l++){
                result[l] = listResult.get(l);
            }
            sum += StatUtil.meanSquaredError(tDataSet[i].expectedOutput, result);
        }
        
        return sum / tDataSet.length;
    }
    
    public static void run() {
        stop = false;
        System.out.println("====================================");
        System.out.println("The network, randomly initialized");
        System.out.println("====================================");
        for(int i = 0; i < tDataSet.length; i++) {
            forward(tDataSet[i].data);
            for (int j = 0; j < layers.length; j++) {
            	for (int k = 0; k < layers[j].neurons.length; k++) {
                    System.out.print("(" + layers[j].neurons[k].value + ")" + " ");
            	}
            	System.out.println();
            }
            List<Float> listResult = Arrays.asList(layers[layers.length-1].getNeurons())
                .stream()
                .map(p -> p.getValue())
                .collect(Collectors.toList());
            float[] result = new float[listResult.size()];
            for (int l = 0; l < listResult.size(); l++){
                result[l] = listResult.get(l);
            }
            System.out.println("SSE = "
                + StatUtil.meanSquaredError(tDataSet[i].expectedOutput, result));
            System.out.println("---------------");
        }
        
        int chosenIter = iterations;
        float chosenRate = learningRate;
        train(chosenIter, chosenRate);

        System.out.println("\n====================================");
        System.out.println("The network, after training");
        System.out.println("====================================");
        for(int i = 0; i < tDataSet.length; i++) {
            forward(tDataSet[i].data);
            for (int j = 0; j < layers.length; j++) {
            	for (int k = 0; k < layers[j].neurons.length; k++) {
            		System.out.print("(" + layers[j].neurons[k].value + ")" + " ");
            	}
            	System.out.println();
            }
            List<Float> listResult = Arrays.asList(layers[layers.length-1].getNeurons())
                    .stream()
                    .map(p -> p.getValue())
                    .collect(Collectors.toList());
            float[] result = new float[listResult.size()];
            for (int l = 0; l < listResult.size(); l++){
                result[l] = listResult.get(l);
            }
            System.out.println("SSE = "
                + StatUtil.meanSquaredError(tDataSet[i].expectedOutput, result));
            System.out.println("---------------");
        } 
    }
    //Read CSV data
    public static void CreateTrainingData(String path){
        List<TrainingData> listDataSet = new ArrayList<>();
        String file = path;
        BufferedReader reader = null;
        String line = "";
        float[] input = new float[4];
        float[] expectedOutput = new float[4];
        try {
            reader = new BufferedReader(new FileReader(file));
            line = reader.readLine();
            while ((line = reader.readLine()) != null){
                String[] row = line.split(",");
                
                for(int i = 0; i < 4; i++){
                    input[i] = Float.parseFloat(row[i]);
                }
                for(int i = 4; i < row.length; i++){
                    expectedOutput[i-4] = Float.parseFloat(row[i]);
                }
                listDataSet.add(new TrainingData(input.clone(), expectedOutput.clone()));
            }
            tDataSet = new TrainingData[listDataSet.size()];
            listDataSet.toArray(tDataSet);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    
    public static Layer[] calculate(float[] inputs) {
        Layer[] result = layers;
        for (int i=0; i<layers[0].neurons.length; i++){
            result[0].neurons[i].value = inputs[i];
        }
        for (int i = 1; i < result.length; i++) {
            for (int j = 0; j < result[i].neurons.length; j++) {
                float sum = 0;
                for (int k = 0; k < result[i-1].neurons.length; k++) {
                    sum += result[i-1].neurons[k].value * result[i].neurons[j].weights[k]
                            + result[i].neurons[j].bias;
                }
                result[i].neurons[j].value = StatUtil.Sigmoid(sum);
            }
        }
        return result;
    }
    
    public static void forward(float[] inputs) {
    	layers[0] = new Layer(inputs);
    	
        for(int i = 1; i < layers.length; i++) {
            for(int j = 0; j < layers[i].neurons.length; j++) {
                float sum = 0;
                for(int k = 0; k < layers[i-1].neurons.length; k++) {
                    sum += layers[i-1].neurons[k].value*layers[i].neurons[j].weights[k]
                            + layers[i].neurons[j].bias;
                }
                layers[i].neurons[j].value = StatUtil.Sigmoid(sum);
            }
            try {
                TimeUnit.MILLISECONDS.sleep(50);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    
    public static void backward(float learning_rate, TrainingData tData) {
    	int number_layers = layers.length;
    	int out_index = number_layers-1;
        currentExpOut = tData.expectedOutput;
    	
    	for(int i = 0; i < layers[out_index].neurons.length; i++) {
            
            float output = layers[out_index].neurons[i].value;
            float target = tData.expectedOutput[i];
            float derivative = output-target;
            float delta = derivative*(output*(1-output));
            
            layers[out_index].neurons[i].gradient = delta;
            
            for(int j = 0; j < layers[out_index].neurons[i].weights.length;j++) { 
                float previous_output = layers[out_index-1].neurons[j].value;
                float error = delta * previous_output;
                layers[out_index].neurons[i].cache_weights[j] = layers[out_index].neurons[i].weights[j] - learning_rate * error;
            }
    	}
    	
    	for(int i = out_index-1; i > 0; i--) {
            for(int j = 0; j < layers[i].neurons.length; j++) {
                float output = layers[i].neurons[j].value;
                float gradient_sum = sumGradient(j,i+1);
                float delta = (gradient_sum)*(output*(1-output));
                
                layers[i].neurons[j].gradient = delta;
                
                for(int k = 0; k < layers[i].neurons[j].weights.length; k++) {
                    float previous_output = layers[i-1].neurons[k].value;
                    float error = delta * previous_output;
                    layers[i].neurons[j].cache_weights[k] = layers[i].neurons[j].weights[k] - learning_rate * error;
                }
            }
    	}
    	
    	for(int i = 0; i< layers.length; i++) {
            for(int j = 0; j < layers[i].neurons.length;j++) {
                layers[i].neurons[j].update_weight();
            }
    	}
    }
    
    public static float sumGradient(int n_index,int l_index) {
    	float gradient_sum = 0;
    	Layer current_layer = layers[l_index];
    	for(int i = 0; i < current_layer.neurons.length; i++) {
            Neuron current_neuron = current_layer.neurons[i];
            gradient_sum += current_neuron.weights[n_index]*current_neuron.gradient;
    	}
    	return gradient_sum;
    }
    
    public static void train(int training_iterations, float learning_rate) {
    	for(int i = 0; i < training_iterations; i++) {
            currentIter = i + 1;
            System.out.print("--------Iter no."+i+"----------\n");
            if (stop){
                break;
            }
            for(int j = 0; j < tDataSet.length; j++) {
                forward(tDataSet[j].data);
                backward(learning_rate,tDataSet[j]);

                List<Float> listResult = Arrays.asList(layers[layers.length-1].getNeurons())
                    .stream()
                    .map(p -> p.getValue())
                    .collect(Collectors.toList());
                float[] result = new float[listResult.size()];
                for (int l = 0; l < listResult.size(); l++){
                    result[l] = listResult.get(l);
                }
                System.out.println("SSE data " + j 
                    + " = "
                    + StatUtil.meanSquaredError(tDataSet[j].expectedOutput, result));
                
                try {
                    TimeUnit.MILLISECONDS.sleep(50);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }            
    	}
    }
}