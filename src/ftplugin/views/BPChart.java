package ftplugin.views;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jfree.chart.*;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.*;
import org.jfree.ui.RectangleEdge;

import com.itextpdf.awt.DefaultFontMapper;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

import ftplugin.model.FTModel;

public class BPChart extends JFrame {
	static JFreeChart chart;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public BPChart() {
		super("Belief/Plausibility Chart");

		JPanel chartPanel = createChartPanel();
		add(chartPanel, BorderLayout.CENTER);

		setSize(640, 480);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
	}

	private JPanel createChartPanel() {
		String chartTitle = "Belief/Plausibility Chart";
		String xAxisLabel = "";
		String yAxisLabel = "";

		final XYDataset dataset = createDataset();

		chart = ChartFactory.createXYLineChart(chartTitle, xAxisLabel,
				yAxisLabel, dataset, PlotOrientation.VERTICAL, true, false,false);
		XYPlot plot = (XYPlot) chart.getPlot();
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer() {
			Stroke soild = new BasicStroke(3.0f);
			Stroke dashed = new BasicStroke(1.0f);

			@Override
			public Stroke getItemStroke(int row, int column) {
				if (row == 1) {

					return dashed;
				} else {
					return soild;
				}

			}
			
		};
		renderer.setDrawSeriesLineAsPath(true);
		renderer.setDrawOutlines(false);
		renderer.setBaseShapesVisible(false);
		// renderer.setBaseShapesFilled(true);
		renderer.setDrawSeriesLineAsPath(true);
		renderer.setSeriesPaint(0, Color.BLACK);

		
		renderer.setSeriesPaint(1, Color.BLACK);
		plot.setRenderer(renderer);
		plot.setBackgroundPaint(Color.WHITE);
		chart.getLegend().setPosition(RectangleEdge.RIGHT);
		return new ChartPanel(chart);
	}

	private XYDataset createDataset() {
		XYSeriesCollection dataset = new XYSeriesCollection();
		XYSeries beliefSeries = new XYSeries("Belief");

		XYSeries plausabilitySeries = new XYSeries("Plausability");
		dataset.addSeries(beliefSeries);
		dataset.addSeries(plausabilitySeries);
		FTModel model = FTModel.getCurrentModel();
		for (double th = 0.0; th <= 1.; th += 0.0001) {
			beliefSeries.add(th, model.belief(th));
			plausabilitySeries.add(th, model.plausability(th));
		}
		
		return dataset;
	}

	public static void displayChart() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new BPChart().setVisible(true);
				double[] nonspec = FTModel.getCurrentModel().nonspecificty();
				for(double d:nonspec)
					System.out.println(d);
			}
		});
	}

	public static void writeChartToPDF() {
		PdfWriter writer = null;
		int width = 500, height = 500;
		Document document = new Document();

		try {
			writer = PdfWriter.getInstance(document, new FileOutputStream(
					"F:\\ftplugin\\chart.pdf"));
			document.open();
			PdfContentByte contentByte = writer.getDirectContent();
			PdfTemplate template = contentByte.createTemplate(width, height);
			@SuppressWarnings("deprecation")
			Graphics2D graphics2d = template.createGraphics(width, height,
					new DefaultFontMapper());
			Rectangle2D rectangle2d = new Rectangle2D.Double(0, 0, width,
					height);

			chart.draw(graphics2d, rectangle2d);

			graphics2d.dispose();
			contentByte.addTemplate(template, 0, 0);

		} catch (Exception e) {
			e.printStackTrace();
		}
		document.close();
	}
}