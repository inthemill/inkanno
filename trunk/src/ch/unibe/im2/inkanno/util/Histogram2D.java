/**
 * 
 */
package ch.unibe.im2.inkanno.util;

/**
 * @author emanuel
 *
 */
public class Histogram2D{
    private double[][] matrix;
    
    public Histogram2D(int d1,int d2){
        matrix = new double[d1][d2];
    }
    
    public void inc(int d1, int d2){
        matrix[d1][d2] += 1;
    }
    
    public double[] compress(int bins){
        double result[] = new double[bins*bins];
     
        int bin_border1 = 2;//length max (length must be smaller than m) starting with 2 ending with 2^(#bins)
        int bin_border2 = 2;//length max (length must be smaller than m) starting with 2 ending with 2^(#bins)
        int m_max = (int) Math.pow(2, bins); //max of m == 2^(#bins)
        
        int j1 = 0;
        int j2 = 0;
        
        for(int i1 = 0;i1<matrix.length;i1++){
            for(int i2 = 0;i2<matrix[0].length;i2++){
                result[j1*bins+j2] += matrix[i1][i2];
                if(i2+1>=bin_border2){
                    j2++;
                    bin_border2 = 2*bin_border2;
                    if(bin_border2 > m_max){
                        bin_border2 = matrix[0].length;
                        j2--;
                    }
                }   
            }
            if(i1>=bin_border1){
                j1++;
                bin_border1 = 2*bin_border1;
                if(bin_border1 > m_max){
                    bin_border1 = matrix.length;
                    j1--;
                }
            }
        }
        return result;
    }
}
