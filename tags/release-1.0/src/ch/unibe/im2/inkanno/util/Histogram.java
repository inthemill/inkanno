package ch.unibe.im2.inkanno.util;

import ch.unibe.eindermu.utils.NumberList;

public class Histogram extends NumberList.Double{
    
    private static final long serialVersionUID = 7558031209449875270L;
   
    public Histogram(int size){
        growTo(size);
    }
    
    public Histogram(){
        
    }
    
    private void growTo(int size){
        while(this.size() < size){
            add(0.0);
        }
    }
    
    public void inc(int place){
        if(this.size() <= place){
            growTo(place+1);
        }
        set(place,get(place)+1);
    }
    
    /**
     * returns the mean index of all bins in the histogram
     * @return
     */
    public double getMean() {
        return getMoment(1);
    }
    
    
    public double get2Moment() {
        return getMoment(2);
    }
    
    public double getMoment(int order) {
        double mean = 0;
        double sum = 0;
        for(int i=0;i<size();i++){
            mean += Math.pow(i, order) * get(i).doubleValue();
            sum += get(i).doubleValue();
        }
        return (sum==0)?0:mean/sum;
    }
    
    /**
     * returns the variance of all values in the Histogram
     * @return
     */
    public java.lang.Double getVariance() {
        double mean = getMean();
        double sum = 0;
        double var = 0;
        for(int i=0;i<size();i++){
            var += get(i).doubleValue()*Math.pow(mean-i,2);
            sum += get(i).doubleValue();
        }
        return  (sum == 0)?0:var/sum;
    }
    
    /**
     * compresses the histogram to contain <code>bins</code> number of bins.
     * the first bin contains the histogram values which are <=2.
     * The second bin contains the histogram values which are 
     * @param bins
     * @return
     */
    public double[] compress(int bins) {
        int m = 2;//length max (length must be smaller than m) starting with 2 ending with 2^(#bins)
        int m_max = (int) Math.pow(2, bins); //max of m == 2^(#bins)
        
        int j = 0;// bin index starting with 0 ending with #bins-1
        int i = 0;// histogram index starting with 0 ending with 2^(#bins)-1 
        double[] result = new double[bins];
        for(;i<size();i++){
            result[j] += (i<size())?get(i):0;
            if(i+1>=m){
                j++;
                m = 2*m;
                if(m > m_max){
                    j--;
                    m = size();
                }
            }
        }
        return result;
    }
}
