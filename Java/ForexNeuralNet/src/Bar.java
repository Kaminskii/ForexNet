import java.util.Date;

public class Bar {

    private int Index; //Index from file
    private String Date; //The date of this bar
    private double[] IndicatorValues;
    //private double Price; // Closing price of this bar
    //private double RSI; // RSI value of this bar
    //private double MA; // Moving average of this bar


    public Bar(int index, String date, double[] indicatorValues) {
        this.Index = index;
        this.Date = date;
        this.IndicatorValues = indicatorValues;
    }

    public int getIndex() {
        return Index;
    }

    public String getDate() {
        return Date;
    }

    public double[] getIndicatorValues() {
        return IndicatorValues;
    }
    //public double getPrice() {
    //    return Price;}
//
    //public double getRSI() {
    //    return RSI; }
//
    //public double getMA() {
    //    return MA;
    //}
}
