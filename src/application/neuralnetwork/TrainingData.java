package application.neuralnetwork;
public class TrainingData {
 
    float[] data;
    float[] expectedOutput;
   
    public TrainingData(float[] data, float[] expectedOutput) {
        this.data = data;
        this.expectedOutput = expectedOutput;
    }
    
    public float[] getData(){
        return data;
    }
    public float[] getExpectedOutput(){
        return expectedOutput;
    }
}