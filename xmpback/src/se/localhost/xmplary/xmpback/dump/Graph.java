package se.localhost.xmplary.xmpback.dump;

import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.RefineryUtilities;

import se.lolcalhost.xmplary.common.models.XMPDataPoint;
import se.lolcalhost.xmplary.common.models.XMPDataPoint.DataPointField;

public abstract class Graph {
	private GraphType type;
	private JFreeChart chart;
	private String title;
	
	public enum GraphType {
		TIMESERIES
	}
	
	public Graph(GraphType type, String title) {
		this.type = type;
		this.title = title;
	}
	

	
	abstract String getXLabel();
	
	private JFreeChart createPlot(XYDataset dataset) {
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                title,  // title
                "Date",             // x-axis label
                getXLabel(),   // y-axis label
                dataset,            // data
                true,               // create legend?
                true,               // generate tooltips?
                false               // generate URLs?
            );

            chart.setBackgroundPaint(Color.white);

            XYPlot plot = (XYPlot) chart.getPlot();
            plot.setBackgroundPaint(Color.lightGray);
            plot.setDomainGridlinePaint(Color.white);
            plot.setRangeGridlinePaint(Color.white);
            plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
            plot.setDomainCrosshairVisible(true);
            plot.setRangeCrosshairVisible(true);

            XYItemRenderer r = plot.getRenderer();
            if (r instanceof XYLineAndShapeRenderer) {
                XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
                renderer.setBaseShapesVisible(true);
                renderer.setBaseShapesFilled(true);
                renderer.setDrawSeriesLineAsPath(true);
            }

            DateAxis axis = (DateAxis) plot.getDomainAxis();
            axis.setDateFormatOverride(new SimpleDateFormat("MMM-yyyy"));
            return chart;
	}
	
	protected abstract TimeSeriesCollection populate();
	
	public void generate() {
		TimeSeriesCollection set = populate();

		
		chart = createPlot(set);
	}
	
	public void save(File f) throws IOException {
		ChartUtilities.saveChartAsPNG(f, chart, 600, 400);
	}
	
	public void show() {
		JFrame frame = new JFrame(" ~~ YOLO ~~ ");
		ChartPanel cp = new ChartPanel(chart);
		cp.setFillZoomRectangle(true);
		cp.setMouseWheelEnabled(true);
		cp.setPreferredSize(new Dimension(600, 400));
		frame.setContentPane(cp);
		frame.pack();
		RefineryUtilities.centerFrameOnScreen(frame);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setVisible(true);
	}
}
