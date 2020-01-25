import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

public class Main {


    public static void main(String[] args) {
       //Network n = new Network(3,10,1000,500,1);
       //n.PARAM_LearnRate = (float) 0.01;
       //float[] i = new float[3];
       //float[] o = new float[1];
       //i[0] = (float) 0;
       //i[1] = (float) 1;
       //i[2] = (float) 1;
       //o[0] = (float) 1;
       //System.out.println(Arrays.toString(n.calculate(i)));
       //for (int j = 0; j < 5000; j++) {
       //    n.train(i,o);
       //    System.out.println(Arrays.toString(n.calculate(i)));
       //}

        String filename = "C:\\Users\\Michal\\AppData\\Roaming\\MetaQuotes\\Tester\\D0E8209F77C8CF37AD8BF550E51FF075\\Agent-127.0.0.1-3000\\MQL5\\Files\\Research\\dataFile.txt";
        FileReading r = new FileReading(filename);


    }
}
