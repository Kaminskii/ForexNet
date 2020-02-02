import javax.naming.spi.DirObjectFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.IllegalFormatCodePointException;


public class Network {

    //DEFAULT PARAMETER VALUES
    public float PARAM_LearnRate = (float) 0.5;
    public float PARAM_WeightRange = (float) 1;
    public float PARAM_Gradient = (float) 1;

    private Layer[] layers; //Array of layers
    private float[][][] weight; // [Layer L][Neuron in L][Neuron in L-1]
    private final int[] NETWORK_LAYER_SIZE; // the array of layer sizes for XOR e.g {2,2,1}
    private int INPUT_SIZE; // The number of input neurons
    private int OUTPUT_SIZE;// The number of output neurons
    private int NETWORK_SIZE; // the number of layers in the network

    //Default constructor
    public Network(int... NETWORK_LAYER_SIZE) {

        //Setting default values


        this.NETWORK_LAYER_SIZE = NETWORK_LAYER_SIZE;
        this.INPUT_SIZE = NETWORK_LAYER_SIZE[0];
        this.NETWORK_SIZE = NETWORK_LAYER_SIZE.length;
        this.OUTPUT_SIZE = NETWORK_LAYER_SIZE[NETWORK_SIZE - 1];

        this.layers = new Layer[NETWORK_SIZE]; // Creating array of layers with specific size
        this.weight = new float[NETWORK_SIZE][][]; // Defining the length of the first dimension of the array
        for (int i = 0; i < layers.length; i++) {
            layers[i] = new Layer(NETWORK_LAYER_SIZE[i], i); // Creating a new layer, (Neuron amount, int index)
        }

        for (int size = 0; size < NETWORK_SIZE; size++) {//For each layer
            if (size > 0) {//Except the input layer
                // Defining the size of the array at layer = size
                this.weight[size] = new float[NETWORK_LAYER_SIZE[size - 1]][NETWORK_LAYER_SIZE[size]];
                //For each neuron in that layer
                for (int neuron = 0; neuron < NETWORK_LAYER_SIZE[size]; neuron++) {
                    //Cycle through the prev layers neurons
                    for (int prevneuron = 0; prevneuron < NETWORK_LAYER_SIZE[size - 1]; prevneuron++) {
                        //Assign a random value to it
                        weight[size][prevneuron][neuron] = (float) Tools.randomValue(-PARAM_WeightRange, PARAM_WeightRange);
                    }
                }

            }
        }

    }


    // This function takes some inputs, propagates them, and returns the outputs
    public float[] calculate(float[] input) { // float for inputs
        if (input.length != INPUT_SIZE) return null; // checking if inputs size match the amount of input neurons
        for (int i = 0; i < NETWORK_LAYER_SIZE[0]; i++) { // for each input neuron
            layers[0].getNeuron(i).setOutput(input[i]); // setting the neurons output to the input
        }

        for (int layer = 1; layer < NETWORK_SIZE; layer++) { // for each layer except the input
            for (int neuron = 0; neuron < NETWORK_LAYER_SIZE[layer]; neuron++) { // take each neuron in that layer
                float sum = 0;
                for (int prevNeuron = 0; prevNeuron < NETWORK_LAYER_SIZE[layer - 1]; prevNeuron++) { // for each neuron in the prev layer
                    sum += layers[layer - 1].getNeuron(prevNeuron).getOutput() * weight[layer][prevNeuron][neuron];
                }
                Neuron neuroonn = layers[layer].getNeuron(neuron);
                if (layer == NETWORK_SIZE-1){ // If it the last layer
                    if (sum < 0){ // Using aleks exp
                        neuroonn.setOutput(aleks_exp(sum));
                        neuroonn.setOutput_derivative(PARAM_Gradient * aleks_exp(sum)); // The derivative of the sigmoid function used for backpropErro
                    } else { //Using aleks linear
                        neuroonn.setOutput(aleks_linear(sum));
                        neuroonn.setOutput_derivative(PARAM_Gradient);
                    }
                } else {
                    neuroonn.setOutput(sigmoid(sum));// sigmoid after summing, the set the value for that neuron
                    neuroonn.setOutput_derivative(neuroonn.getOutput() * (1 - neuroonn.getOutput())); // The derivative of the sigmoid function used for backpropErro
                }
            }
        }

        float[] outputs = new float[NETWORK_LAYER_SIZE[NETWORK_SIZE - 1]]; //creating a array for output values
        for (int output = 0; output < outputs.length; output++) {
            outputs[output] = layers[NETWORK_SIZE - 1].getNeuron(output).getOutput();// setting each index of output to each output neurons value
        }
        return outputs;//returning outputs array containing output values
    }

    // This function takes a single float value and returns the sigmoid of that value
    public float sigmoid(float x) {
        return (float) (1d / (1 + Math.exp(-x))); // 1d is used so that a double value is returned
    }
    public float sigmoid_derivative(float x){
        return (sigmoid(x)*(1-sigmoid(x)));
    }
    public float aleks_linear(float x){
        return (PARAM_Gradient * x) + 1;
    }
    public float aleks_linear_der(float x){ return (PARAM_Gradient); }

    public float aleks_exp(float x){ return (float) (Math.exp(PARAM_Gradient*x)); }
    public float aleks_exp_der(float x){ return   (float) (PARAM_Gradient*(Math.exp(PARAM_Gradient*x))); }

    // This function connects everything together by calling other necessary methods
    public void train(float[] input, float[] target) {
        if (INPUT_SIZE != input.length || OUTPUT_SIZE != target.length) return;
        calculate(input);
        backpropError(target);
        updateWeights();
    }

    // This function generates the error for  each neuron by taking the error from the outputs first, propagating backwards.
    public void backpropError(float[] target) {
        for (int neuron = 0; neuron < NETWORK_LAYER_SIZE[NETWORK_SIZE - 1]; neuron++) { //Calculating Error for output neurons
            Neuron neuroon = layers[NETWORK_SIZE - 1].getNeuron(neuron);
            neuroon.setError((target[neuron] - neuroon.getOutput())* neuroon.getOutput_derivative());

        }
        for (int layer = NETWORK_SIZE - 2; layer > 0; layer--) { // Starting at the last hidden layer
            for (int neuron = 0; neuron < NETWORK_LAYER_SIZE[layer]; neuron++) {// For each of its neurons
                float sum = 0;
                for (int nextNeuron = 0; nextNeuron < NETWORK_LAYER_SIZE[layer + 1]; nextNeuron++) {// Grab the next neuron in layer + 1
                    sum += weight[layer + 1][neuron][nextNeuron] * layers[layer + 1].getNeuron(nextNeuron).getError();
                }
                layers[layer].getNeuron(neuron).setError(sum * layers[layer].getNeuron(neuron).getOutput_derivative());
            }

        }

    }

    public void updateWeights() {
        for (int layer = 1; layer < NETWORK_SIZE; layer++) {
            for (int neuron = 0; neuron < NETWORK_LAYER_SIZE[layer]; neuron++) {
                for (int prevNeuron = 0; prevNeuron < NETWORK_LAYER_SIZE[layer - 1]; prevNeuron++) {
                    //weight[Layer][prevNeuron][neuron]
                    Neuron neuroon = layers[layer].getNeuron(neuron);
                    Neuron prevneuron = layers[layer - 1].getNeuron(prevNeuron);

                    //if (layer == NETWORK_SIZE - 1){

                    //}

                    double delta = PARAM_LearnRate * prevneuron.getOutput() * neuroon.getError();
                    weight[layer][prevNeuron][neuron] += delta;
                }
            }

        }
    }

    public void NetSave(String filename) {
        if (!filename.contains(".txt")) {
            filename = filename + ".txt";
        }
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
            for (int i = 0; i <NETWORK_LAYER_SIZE.length; i++) {
                writer.write( NETWORK_LAYER_SIZE[i] +",");
            }
            writer.write("\n");
            int layer = NETWORK_SIZE - 1;
            while (layer >= 1) {
                for (int neuron = 0; neuron < NETWORK_LAYER_SIZE[layer]; neuron++) {
                    for (int prevNeuron = 0; prevNeuron < NETWORK_LAYER_SIZE[layer - 1]; prevNeuron++) {
                        writer.write(String.format("%12.8f \n", weight[layer][prevNeuron][neuron]));
                    }
                }
                layer--;
            }


            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error saving weights");
        }


    }

    public ArrayList<Float> getWeights() {
        ArrayList<Float> weightsArray = new ArrayList<Float>();
        int layer = NETWORK_SIZE - 1;

        while (layer >= 1) {
            for (int neuron = 0; neuron < NETWORK_LAYER_SIZE[layer]; neuron++) {
                for (int prevNeuron = 0; prevNeuron < NETWORK_LAYER_SIZE[layer - 1]; prevNeuron++) {
                    weightsArray.add(weight[layer][prevNeuron][neuron]);
                }
            }
            layer--;
        }

        return weightsArray;
    }

    public void NetLoad(String filename) {
        if (!filename.contains(".txt")) {
            filename = filename + ".txt";
        }

        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line = br.readLine();
            if (line.contains(",")) {
                String[] Netsize = line.split(",");
                for (int i = 0; i < Netsize.length; i++) {
                    NETWORK_LAYER_SIZE[i] = Integer.parseInt(Netsize[i]);
                }
            }
            line = br.readLine();
            int layer = NETWORK_SIZE - 1;
            while (line != null) {
                while (layer >= 1) {
                    for (int neuron = 0; neuron < NETWORK_LAYER_SIZE[layer]; neuron++) {
                        for (int prevNeuron = 0; prevNeuron < NETWORK_LAYER_SIZE[layer - 1]; prevNeuron++) {
                            line = line.replaceAll("\\s", "");
                            weight[layer][prevNeuron][neuron] = Float.parseFloat(line);
                            line = br.readLine();
                        }
                    }
                    layer--;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


