package com.basistech.lsh;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageIO;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;

public class PRPlot extends ApplicationFrame{
    
    private static final long serialVersionUID = -8571832684556874448L;
    
    public static void main(String[] args){
        List<Boolean> ground = Arrays.asList(new Boolean[]{true,false,true,false});
        List<Double> pred = Arrays.asList(new Double[]{.6,.4,.8,.7});
        new PRPlot(ground,pred);
        //writeChart(ground,pred,"/u1/fsd/plot.png");
    }

    public PRPlot(List<Boolean> groundTruth, List<Double> discriminant) {
        super("Precision Recall Chart");
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
        int threshCount = 0;
        ArrayList<Double> threshs = new ArrayList<Double>();
        for(double thresh=1+1/(double)5000; thresh>=-1/(double)5000; thresh=thresh-1/(double)5000){
            threshCount++;
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
            threshs.add(thresh);
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

        XYLineAndShapeRenderer renderer
            = (XYLineAndShapeRenderer) ((XYPlot)chart.getPlot()).getRenderer();

        final ArrayList<Double> threshs2 = threshs;

        renderer.setBaseItemLabelGenerator(
                new StandardXYItemLabelGenerator(){
                    public String generateLabel(XYDataset dataset, int series, int item){
                        if(item%50==0){
                            return String.valueOf(Math.round(threshs2.get(item)*100));
                        }else{
                            return "";
                        }
                    }
            });
            
        renderer.setBaseItemLabelsVisible(true);
        return chart;
    }

    public static void writeChart(List<Boolean> groundTruth, List<Double> discriminant, String file){try{
        JFreeChart chart = getChart(groundTruth, discriminant);
        BufferedImage buf = chart.createBufferedImage(1680, 1050);
        ImageIO.write(buf, "png", new File(file));
    }catch(IOException e){throw new RuntimeException(e);}}
}
