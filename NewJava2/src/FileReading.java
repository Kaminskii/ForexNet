import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class FileReading {

    public ArrayList<Bar> Bars = new ArrayList<Bar>();


    String date;
    double price;
    double rsi;
    double ma;

    double[] BarValues;
    double[] avg_BarValues;
    public FileReading(String filename){

        String line;
        int barCount = 0;


          double avr_price = 0;   double price_diff[];
          double avr_rsi = 0;     double rsi_diff;
          double avr_ma = 0;      double ma_diff;

          double difference[];

        double error = 0;
        double[] avr_error = new double[100];
        Network net = new Network(6,100,100,100,6);



        int x = 8;// Define the amount of bars in average comparison.
        // of the previous x bars.


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
            for (int iteration = 0; iteration < 100000; iteration++) {
                random = rand.nextInt(Bars.size());
                Bar bar = Bars.get(random);
                if (bar.getIndex()< (x+1)){
                    continue;
                }
                    avg_BarValues = new double[bar.getIndicatorValues().length];
                    for (int i = random - x ; i < random; i++) { // For the last x bars
                        for (int j = 0; j < avg_BarValues.length; j++) {
                            avg_BarValues[j] = avg_BarValues[j] + Bars.get(i).getIndicatorValues()[j];
                        }
                    }
                    for (int i = 0; i < avg_BarValues.length; i++) {
                        avg_BarValues[i] = avg_BarValues[i] / x;
                    }


                    difference = new double[bar.getIndicatorValues().length];
                    price_diff = new double[bar.getIndicatorValues().length];

                    for (int i = 0; i < bar.getIndicatorValues().length; i++) {
                        difference[i] = ( bar.getIndicatorValues()[i] / avg_BarValues[i] ) - 0.5;
                        price_diff[i] = difference[i];
                    }

                    float[] input = new float[bar.getIndicatorValues().length];
                    float[] output = new float[bar.getIndicatorValues().length];

                    for (int i = 0; i < input.length; i++) {
                        input[i] = (float) avg_BarValues[i];
                        output[i] = (float) price_diff[i];
                    }

                    float[] predicted = net.calculate(input);

                    if (output.length != predicted.length){ // Difference in size,
                        return;
                    }

                    for (int i = 0; i < output.length; i++) {
                        error = error + (output[i] - predicted[i])*(output[i] - predicted[i]);
                    }
                    error = error / output.length;
                    if ((barCount % 1000) == 0 && barCount != 0){
                        System.out.println("Average Error Per Output Neuron: " + (error / 1000) * 1000000); //Making reading error more pleasant
                        System.out.println("Actual");
                        if ((barCount % 10000) == 0 && barCount != 0){
                            System.err.println("Average Error Per Output Neuron: " + (error / 10000) * 1000000); //Making reading error more pleasant
                        }
                    }
                    error = 0;

                    net.train(input,output);
                    barCount++;
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
