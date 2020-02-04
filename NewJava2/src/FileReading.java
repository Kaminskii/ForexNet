import java.awt.font.FontRenderContext;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class FileReading<x> {

    public ArrayList<Bar> Bars = new ArrayList<Bar>();


    String date;
    double price;
    double rsi;
    double ma;


    int x = 10; // Define the number of bars in backtrack calculations.
    int y = 5; // Define the number of bars used in training in forecast calculations.
    //int priceBack = 10; // number of previous prices added to the inputs

    double[] BarValues;

    public double[] getIndicatorGradients(Bar bar,double[][] indicator_Table){
        double[] y_average;
        double x_average;
        int rows = bar.getIndicatorValues().length;
        y_average = new double[rows];
        x_average = 0;// Is there a simpler way to sum all integers upto x?
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
        double[] x_temp = new double[x];
        double[][] y_temp = new double[rows][x];
        double[] indicator_gradients = new double[rows];

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
        return indicator_gradients;
    }

    public double[][] getIndicatorTable(Bar bar){
        double[][] indicator_Table = new double[bar.getIndicatorValues().length][x];
        for (int i = bar.getIndex() - x ; i < bar.getIndex(); i++) { // For the last x bars
            for (int j = 0; j < bar.getIndicatorValues().length; j++) {
                try{
                    indicator_Table[j][i - (bar.getIndex()-x)] = Bars.get(i).getIndicatorValues()[j];
                } catch (IndexOutOfBoundsException e){
                    System.out.println("Error : " + e);
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
        Network net = new Network(6,50,100,50,1);
        net.PARAM_LearnRate = (float) 0.01;
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
            double[] y_average;
            double x_average;
            double[][] indicator_Table;
            double[] indicator_gradients;

            for (int iteration = 0; iteration < 999999999; iteration++) {
                random = rand.nextInt(Bars.size());
                Bar bar = Bars.get(random);
                while (((bar.getIndex() - x - bar.getIndicatorValues().length) < 0) || (bar.getIndex() > (Bars.size() - y - 1)) || ((bar.getIndex() % 3) == 0) ) {
                    random = rand.nextInt(Bars.size());
                    bar = Bars.get(random);
                    continue;
                }


         int rows = bar.getIndicatorValues().length;
         float[] input = new float[rows];
         indicator_Table = getIndicatorTable(bar);
         indicator_gradients = getIndicatorGradients(bar,indicator_Table);
         for (int i = 0; i < rows; i++) { // First adding the indicator gradient values
             input[i] = (float) indicator_gradients[i];
         }
         float[] output = new float[y]; //Loading next y prices into the output neurons

                output[0] = (float) Bars.get(bar.getIndex()).getIndicatorValues()[0];
         for (int i = 1; i < y; i++) {
             output[i] = (float) (Bars.get(bar.getIndex() + i).getIndicatorValues()[0] - output[i-1]);
         }

         net.train(input,output);

         //!!!!!!!!!! NOW TESTING AFTER TRAINING !!!!!!!!!!!!!!!!!!!

         random = rand.nextInt(Bars.size());
         bar = Bars.get(random);
         while ( (bar.getIndex() < (rows + x + 1)) || ((bar.getIndex() % 3) != 0) || (bar.getIndex() > (Bars.size() - y - 1)) ){
                 random = rand.nextInt(Bars.size());
                 bar = Bars.get(random);
                 continue;
             }

         rows = bar.getIndicatorValues().length;
         indicator_Table = getIndicatorTable(bar);
         indicator_gradients = getIndicatorGradients(bar,indicator_Table);
         for (int i = 0; i < rows; i++) { // First adding the indicator gradient values
             input[i] = (float) indicator_gradients[i];
         }
                output[0] = (float) Bars.get(bar.getIndex()).getIndicatorValues()[0];
                for (int i = 1; i < y; i++) {
                    output[i] = (float) (Bars.get(bar.getIndex() + i).getIndicatorValues()[0] - output[i-1]);
                }

         float[] predicted = net.calculate(input);
         if (output.length != predicted.length){ // Difference in size,
             return;
         }

         for (int i = 0; i < output.length; i++) {
             temp = temp + (output[i] - predicted[i])*(output[i] - predicted[i]);
         }
         error = error + (temp / output.length);
         if ((iteration % 5000) == 0 && iteration != 0){
             System.out.println("Average Error Per Output Neuron over 5000: " + (error / 5000) * 100000); //Making reading error more pleasant
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
