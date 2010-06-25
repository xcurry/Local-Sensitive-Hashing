package com.basistech.lsh;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageIO;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;

public class PRPlot extends ApplicationFrame{
    
    private static final long serialVersionUID = -8571832684556874448L;
    
    public static void main(String[] args){
        new PRPlot(Arrays.asList(new Boolean[]{true,false,true,false}),
                             Arrays.asList(new Double[]{.6,.4,.8,.7})); 
    }

    public PRPlot(List<Boolean> groundTruth, List<Double> discriminant) {
        super("Precision Recall Chart");
        // TODO Auto-generated constructor stub
        JFreeChart chart=getChart( groundTruth, discriminant);
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));
        setContentPane(chartPanel);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setVisible(true);
    }

    public static JFreeChart getChart(List<Boolean> groundTruth, List<Double> discriminant){
        XYSeries curve = new XYSeries("");
        for(double thresh=1+1/(double)5000; thresh>=-1/(double)5000; thresh=thresh-1/(double)5000){
            int truePositives=0;
            int groundPositives=0;
            int trueNegatives=0;
            int groundNegatives=0;
            for(int i=0; i<groundTruth.size(); i++){
                if(groundTruth.get(i)){
                    groundPositives++;
                    if(discriminant.get(i)>=thresh){
                        truePositives++;
                    }
                }else{
                    groundNegatives++;
                    if(discriminant.get(i)<=thresh){
                        trueNegatives++;
                    }
                }
            }
            double falsePositiveRate=(groundNegatives-trueNegatives)/(double)groundNegatives;
            double detectionRate=(truePositives)/(double)groundPositives;
            curve.add(falsePositiveRate,detectionRate);
        }
        
        
        
        
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(curve);
        
        final JFreeChart chart = ChartFactory.createXYLineChart(
                "Precision-Recall Curve",      // chart title
                "False Positive Rate",                      // x axis label
                "Detection Rate",                      // y axis label
                dataset,                  // data
                PlotOrientation.VERTICAL,
                false,                     // include legend
                true,                     // tooltips
                false                     // urls
            );
        return chart;
    }

    public static void writeChart(List<Boolean> groundTruth, List<Double> discriminant, String file){try{
        JFreeChart chart = getChart(groundTruth, discriminant);
        BufferedImage buf = chart.createBufferedImage(1680, 1050);
        ImageIO.write(buf, "png", new File(file));
    }catch(IOException e){throw new RuntimeException(e);}}
}
