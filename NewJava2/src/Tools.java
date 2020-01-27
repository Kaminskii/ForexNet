public class Tools {

    public static double randomValue(double lower_bound, double upper_bound){
        return Math.random() * (upper_bound - lower_bound) + lower_bound;
    }
}
