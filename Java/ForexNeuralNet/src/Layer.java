

//=================================================================================

// This class is used to store each layers neurons.
// It helps to understand what is happening in the network algorithms

//=================================================================================

public class Layer {

    private final Neuron[] neurons; // Array of neurons in the layer.
    private int index; // The index of the layer

    //Default constructor called when network is initialized
    public Layer(int neuronAmount, int index){
        this.index = index;
        this.neurons = new Neuron[neuronAmount];
        for (int i = 0; i < neuronAmount; i++)
            neurons[i] = new Neuron(); // Generating neuron objects and adding them to array
    }

    //Getters for variables

    public Neuron getNeuron(int i){
        return neurons[i];
    }

    public int getIndex(){ return index;}

}
