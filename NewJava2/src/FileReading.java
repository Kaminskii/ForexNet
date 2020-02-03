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


    int x = 5;// Define the amount of bars in average comparison.
    // of the previous x bars.


    double[] slope_BarValues;
    double[][] indicator_Table;
    double[] BarValues;

    public FileReading(String filename){

        String line;
        int barCount = 0;
        double error = 0;
        Network net = new Network(7,50,100,50,1);
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
            for (int iteration = 0; iteration < 999999999; iteration++) {
                random = rand.nextInt(Bars.size());
                Bar bar = Bars.get(random);
                while ( (bar.getIndex() < (x+1)) || ((bar.getIndex() % 3) == 0) ) {
                    random = rand.nextInt(Bars.size());
                    bar = Bars.get(random);
                    continue;
                }
                    slope_BarValues = new double[bar.getIndicatorValues().length];
                    indicator_Table = new double[bar.getIndicatorValues().length][x];

                    for (int i = random - x ; i < random; i++) { // For the last x bars
                        for (int j = 0; j < slope_BarValues.length; j++) {
                                indicator_Table[j][i - (random-x)] = Bars.get(i).getIndicatorValues()[j];
                        }
                    }

                    //Calculating mean of x and mean of each indicator
                    y_average = new double[slope_BarValues.length];
                    x_average = 0;// Is there a simpler way to sum all integers upto x?
                    for (int i = 0; i < x; i++) {
                        x_average = x_average + i;
                    }
                    x_average = x_average / x;

                    for (int i = 0; i < slope_BarValues.length; i++) {
                        for (int j = 0; j < x; j++) {
                            y_average[i] = y_average[i] + indicator_Table[i][j];
                        }
                    }
                    for (int i = 0; i < y_average.length; i++) {
                        y_average[i] = y_average[i] / x;
                    }
                    //Now calculating the slope of each indicator
                    int rows = bar.getIndicatorValues().length;
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


                    //difference = new double[bar.getIndicatorValues().length];
                    //price_diff = new double[bar.getIndicatorValues().length];

                    //for (int i = 0; i < bar.getIndicatorValues().length; i++) {
                    //    difference[i] = ( bar.getIndicatorValues()[i] / avg_BarValues[i] ) - 0.5;
                    //    price_diff[i] = difference[i];
                    //}

                    float[] input = new float[rows + 1];
                    input[0] = (float) Bars.get(random-1).getIndicatorValues()[0]; // also giving the price of the last bar before
                    for (int i = 1; i < rows; i++) {
                        input[i] = (float) indicator_gradients[i];
                    }
                    float[] output = new float[1];

                    output[0] = (float) bar.getIndicatorValues()[0];
                    net.train(input,output);


                    random = rand.nextInt(Bars.size());
                    bar = Bars.get(random);
                    while ( (bar.getIndex() < (x+1)) || ((bar.getIndex() % 3) != 0) ){
                            random = rand.nextInt(Bars.size());
                            bar = Bars.get(random);
                            continue;
                        }

                input[0] = (float) bar.getIndicatorValues()[0]; // also giving the price of the last bar before
                for (int i = 1; i < rows; i++) {
                    input[i] = (float) indicator_gradients[i];
                }
                output[0] = (float) bar.getIndicatorValues()[0];

                    float[] predicted = net.calculate(input);

                    if (output.length != predicted.length){ // Difference in size,
                        return;
                    }

                    for (int i = 0; i < output.length; i++) {
                        error = error + (output[i] - predicted[i])*(output[i] - predicted[i]);
                    }
                    error = error / output.length;

                        if ((iteration % 25000) == 0 && iteration != 0){
                            System.out.println("Average Error Per Output Neuron over 25000: " + (error / 2500) * 100000); //Making reading error more pleasant
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
