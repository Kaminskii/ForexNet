public class Neuron {

    private float output;
    private float error;
    private float output_derivative;

    public float getOutput_derivative() {
        return output_derivative;
    }

    public void setOutput_derivative(float output_derivative) {
        this.output_derivative = output_derivative;
    }

    public float getError() {
        return error;
    }

    public void setError(float error) {
        this.error = error;
    }

    public float getOutput() {
        return output;
    }

    public void setOutput(float output) {
        this.output = output;
    }
}
