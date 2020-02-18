import java.awt.font.FontRenderContext;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

class FileReading<x> {

    public ArrayList<Bar> Bars = new ArrayList<Bar>();


    String date;
    double price;
    double rsi;
    double ma;

    int inputSize = 300;
    int outputSize = 50;


    int x = 10; // Define the number of bars in backtrack calculations.
    int y = 10; // Define the number of bars used in training in forecast calculations.
    //int priceBack = 10; // number of previous prices added to the inputs

    double[] BarValues;

    public double[] getIndicatorGradients(Bar bar,double[][] indicator_Table,boolean forecasting){
        double[] y_average;
        double x_average;
        int rows = bar.getIndicatorValues().length;
        y_average = new double[rows];
        x_average = 0;
        double[] x_temp;
        double[][] y_temp;
        double[] indicator_gradients;

        if (forecasting){
            for (int i = 0; i < y; i++) {
                x_average = x_average + i;
            }
            x_average = x_average / y;

            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < y; j++) {
                    y_average[i] = y_average[i] + indicator_Table[i][j];
                }
            }
            for (int i = 0; i < rows; i++) {
                y_average[i] = y_average[i] / y;
            }

            x_temp = new double[y];
            y_temp = new double[rows][y];
            indicator_gradients = new double[rows];

            for (int j = 0; j < rows; j++) {
                for (int i = 0; i < y; i++) {
                    x_temp[i] = i - x_average;
                    y_temp[j][i] = indicator_Table[j][i] - y_average[j];
                }
            }
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < y; j++) {
                    indicator_gradients[i] = ( x_temp[j] * y_temp[i][j] ) / ( x_temp[j] * x_temp[j] );
                }
            }

        }
        else {
            for (int i = 0; i < x; i++) {
                x_average = x_average + i;
            }
            x_average = x_average / x;

            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < x; j++) {
                    y_average[i] = y_average[i] + indicator_Table[i][j];
                }
            }
            for (int i = 0; i < y_average.length; i++) {
                y_average[i] = y_average[i] / x;
            }
            //Now calculating the slope of each indicator

            x_temp = new double[x];
            y_temp = new double[rows][x];
            indicator_gradients = new double[rows];

            for (int j = 0; j < rows; j++) {
                for (int i = 0; i < x; i++) {
                    x_temp[i] = i - x_average;
                    y_temp[j][i] = indicator_Table[j][i] - y_average[j];
                }
            }

            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < x; j++) {
                    indicator_gradients[i] = ( x_temp[j] * y_temp[i][j] ) / ( x_temp[j] * x_temp[j] );
                }
            }
        }

        return indicator_gradients;
    }

    public double[][] getIndicatorTable(Bar bar,boolean forecasting){
        double[][] indicator_Table;

        if (forecasting){
            indicator_Table = new double[bar.getIndicatorValues().length][y];
            for (int i = bar.getIndex(); i < bar.getIndex() + y; i++) { // For the next y bars
                for (int j = 0; j < bar.getIndicatorValues().length; j++) {
                    try{
                        indicator_Table[j][i - (bar.getIndex())] = Bars.get(i).getIndicatorValues()[j];
                    } catch (IndexOutOfBoundsException e){
                        System.out.println("Error : " + e);
                    }
                }
            }
        } else {
            indicator_Table = new double[bar.getIndicatorValues().length][x];
            for (int i = bar.getIndex() - x ; i < bar.getIndex(); i++) { // For the last x bars
                for (int j = 0; j < bar.getIndicatorValues().length; j++) {
                    try{
                        indicator_Table[j][i - (bar.getIndex()-x)] = Bars.get(i).getIndicatorValues()[j];
                    } catch (IndexOutOfBoundsException e){
                        System.out.println("Error : " + e);
                    }
                }
            }
        }


        return indicator_Table;
    }

    public FileReading(String filename){

        String line;
        int barCount = 0;
        double error = 0;
        float temp = 0;
        Network net = new Network(inputSize,300,300,outputSize);
        net.PARAM_LearnRate = (float) 0.07;
        net.PARAM_Gradient = (float) 0.02;

        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            while ((line = br.readLine()) != null)   //returns a Boolean value
            {
                if (line.contains(".")){
                    String[] split = line.split(",");
                    BarValues = new double[split.length-1];
                    date = CleanByteArray(split[0].getBytes());
                    for (int i = 0; i < BarValues.length ; i++) {
                        BarValues[i] = Double.parseDouble(CleanByteArray(split[i+1].getBytes()));
                    }
                    Bars.add(new Bar(barCount,date,BarValues));
                    barCount++;
                }

            }
            Random rand = new Random();
            int random;
            double[][] indicator_Table;
            double[] indicator_gradients_inputs;
            double[] indicator_gradients_outputs;
            double average;
            for (int iteration = 0; iteration < 999999999; iteration++) {
                random = rand.nextInt(Bars.size());
                Bar bar = Bars.get(random);
                while ( bar.getIndex() < inputSize || bar.getIndex() > (Bars.size() - outputSize) || ((bar.getIndex() % 3) == 0) ) {
                    random = rand.nextInt(Bars.size());
                    bar = Bars.get(random);
                    continue;
                }


         int rows = bar.getIndicatorValues().length;

         //indicator_gradients_inputs = getIndicatorGradients(bar,getIndicatorTable(bar,false),false);
         //for (int i = 0; i < rows; i++) { // First adding the indicator gradient values to inputs
         //    input[i] = (float) indicator_gradients_inputs[i];
         //}
         //average = 0;
         //for (int i = 0; i < x; i++) {
         //    average = average + Bars.get(bar.getIndex() - i).getIndicatorValues()[0];
         //}
         //average = average / x;
         //input[input.length - 1] = (float) average; //setting the last input value as the average price over last x

            float[] input = new float[inputSize];
            float[] output = new float[outputSize]; // Just one output with the average of the next y price
                for (int i = 0; i < input.length; i++) {
                    input[i] = (float) Bars.get(bar.getIndex() - (i+1)).getIndicatorValues()[0];
                }
                for (int i = 0; i < output.length; i++) {
                    output[i] = (float) Bars.get(bar.getIndex()+i).getIndicatorValues()[0];
                }

         //indicator_gradients_outputs = getIndicatorGradients(bar,getIndicatorTable(bar,true),true);
         //for (int i = 0; i < rows; i++) { // First adding the indicator gradient values to inputs
         //    output[i] = (float) indicator_gradients_outputs[i];
         //    if (output[i] < 0){
         //        output[i] = net.aleks_exp(output[i]);
         //    } else {
         //        output[i] = net.aleks_linear(output[i]);
         //    }
         //}
         //average = 0;
         //for (int i = 0; i < y; i++) {
         //    average = average + Bars.get(bar.getIndex() + i).getIndicatorValues()[0];
         //}
         //average = average / y;
         //output[output.length - 1] = (float) average;

         net.train(input,output);

         //!!!!!!!!!! NOW TESTING AFTER TRAINING !!!!!!!!!!!!!!!!!!!

         random = rand.nextInt(Bars.size());
         bar = Bars.get(random);
         while ( bar.getIndex() < inputSize || bar.getIndex() > (Bars.size() - outputSize) || ((bar.getIndex() % 3) == 0) ){
                 random = rand.nextInt(Bars.size());
                 bar = Bars.get(random);
                 continue;
             }

         rows = bar.getIndicatorValues().length;
         //indicator_gradients_inputs = getIndicatorGradients(bar,getIndicatorTable(bar,false),false);
//
         //for (int i = 0; i < rows; i++) { // First adding the indicator gradient values
         //    input[i] = (float) indicator_gradients_inputs[i];
         //}
         //average = 0;
         //for (int i = 0; i < x; i++) {
         //    average = average + Bars.get(bar.getIndex() - i).getIndicatorValues()[0];
         //}
         //average = average / x;
         //input[input.length - 1] = (float) average; //setting the last input value as the average price over last x


                for (int i = 0; i < input.length; i++) {
                    input[i] = (float) Bars.get(bar.getIndex() - (i+1)).getIndicatorValues()[0];
                }

         float[] predicted = net.calculate(input);
         if (output.length != predicted.length){ // Difference in size,
             return;
         }
            temp = 0;
         for (int i = 0; i < output.length; i++) {
             temp = temp + (output[i] - predicted[i])*(output[i] - predicted[i]);
         }
         error = error + (temp / output.length);
         if ((iteration % 500) == 0 && iteration != 0){
             System.out.println("Average Error Per Output Neuron over 500: " + (error / 500) * 1000); //Making reading error more pleasant
             System.out.println("Iteration : "+ iteration +"    Predicted : " + predicted[0] + "    Target : " + output[0] + "\n");
             error = 0;
         }
                }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public String CleanByteArray(byte[] byteArray){
        int counter = 0;
        for (int i = 0; i < byteArray.length; i++) {
            if (byteArray[i] <= 0){
                counter++;
            }
        }
        byte[] cleaned = new byte[byteArray.length - counter];
        for (int i = 0; i < byteArray.length; i++) {
            if (byteArray[i] > 0){
                for (int j = 0; j < counter; j++) {
                    if (cleaned[j] == 0){
                        cleaned[j] = byteArray[i]; break;
                    }
                }
            }
        }
        String s = new String(cleaned);
        return s;
    }
}
