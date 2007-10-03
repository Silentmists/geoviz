package edu.psu.geovista.app.scatterplot;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.BitSet;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.event.EventListenerList;

import edu.psu.geovista.app.matrix.MatrixElement;
import edu.psu.geovista.common.data.DataSetForApps;
import edu.psu.geovista.common.data.DescriptiveStatistics;
import edu.psu.geovista.common.event.IndicationEvent;
import edu.psu.geovista.common.event.IndicationListener;
import edu.psu.geovista.common.ui.ExcentricLabelClient;
import edu.psu.geovista.common.ui.ExcentricLabels;
import edu.psu.geovista.symbolization.BivariateColorSymbolClassification;
import edu.psu.geovista.symbolization.BivariateColorSymbolClassificationSimple;

public class ScatterPlotBasic extends JPanel implements ComponentListener,
		MouseListener, MouseMotionListener, MatrixElement,
		ExcentricLabelClient, IndicationListener {

	public static final double AXISSPACEPORTION = 1.0 / 6.0;
	public static final String COMMAND_POINT_SELECTED = "cmdSel";
	public static final String COMMAND_DATARANGE_SET = "cmdset";
	protected final static int RADIUS = 3; // Glyph size
	 protected int pointSize = RADIUS;
	transient protected int plotOriginX;
	transient protected int plotOriginY;
	transient protected int plotEndX;
	transient protected int plotEndY;
	transient protected Object[] dataObject;
	transient protected DataSetForApps dataSet;
	transient protected double[][] doubleDataArrays;
	transient protected int[] dataIndices;
	transient protected double[] dataX;
	transient protected double[] dataY;
	transient protected int[] exsint;
	transient protected int[] whyint;
	 protected String[] attributeArrays;
	 protected String[] observNames;
	transient protected String attributeX;
	transient protected String attributeY;
	transient protected boolean axisOn;
	transient protected Dimension size;
	transient protected Color background;
	 protected Color foreground;
	 protected Color selectionColor = Color.blue;
	 protected Color indicationColor = Color.RED;
	protected  boolean selOriginalColorMode = true;
	transient protected Color[] multipleSelectionColors;

	transient protected DataArray dataArrayX;
	transient protected DataArray dataArrayY;
	 protected double[] xAxisExtents = new double[2];
	 protected double[] yAxisExtents = new double[2];
	transient protected int selectX = 0;
	transient protected int selectY = 0;
	transient protected int selectWidth = 0;
	transient protected int selectHeight = 0;
	transient protected BasicStroke strokeDashed;
	transient protected int[] selections;
	transient protected Vector selRecords;
	transient protected double[] selectedDataX;
	transient protected double[] selectedDataY;
	transient boolean pointSelected = false;
	transient protected int[] conditionArray;
	transient protected int mouseX1, mouseX2, mouseY1, mouseY2;
	protected transient JPopupMenu popup;
	 protected JTextField xAxisMinField = new JTextField(16);
	 protected JTextField xAxisMaxField = new JTextField(16);
	 protected JTextField yAxisMinField = new JTextField(16);
	 protected JTextField yAxisMaxField = new JTextField(16);
	 protected EventListenerList listenerListAction = new EventListenerList();

	// stuff added for colors
	transient protected Color[] pointColors;
	transient protected BivariateColorSymbolClassification bivarColorClasser = new BivariateColorSymbolClassificationSimple();
	 protected Histogram histogram = new Histogram();
	transient protected ExcentricLabels exLabels; // For paint label of
	// observation data while
	// indication rectangle is
	// moving
	transient int count = 0;

	transient int firstBar;
	transient int lastBar;
	transient double yBarDistance;
	transient double xBarDistance;

	transient protected ScatterPlotBasic detailSP;
	transient protected JFrame dlgSP;

	// added by ywkim for moransI regression line
	protected boolean plotLine = true;
	transient protected double slope;
	transient protected double intercept;
	transient protected double correlation;
	transient protected double rSquare;
	transient protected double yStartPosition;
	transient protected double yEndPosition;
	// regression for selected observations
	protected boolean plotLineForSelections = true;
	transient protected double slopeForSelections;
	transient protected double interceptForSelections;
	transient protected double correlationForSelections;
	transient protected double rSquareForSelections;
	transient protected double yStartPositionSelections;
	transient protected double yEndPositionSelections;
	private BufferedImage indicationStamp;
	private int stampSize = 80;
	private transient Image drawingBuff;

	static final Logger logger = Logger.getLogger(ScatterPlotBasic.class
			.getName());
	// indication
	protected int indiationId = -1;

	/**
	 * put your documentation comment here
	 */
	public ScatterPlotBasic() {
		super();
		this.isDoubleBuffered();
		this.indicationStamp = GradientStamp.makeGradientStamp(this.stampSize);
		this.setPreferredSize(new Dimension(300, 300));
		// ...where the GUI is constructed:

		this.addComponentListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	/**
	 * put your documentation comment here
	 * 
	 * @param String
	 *            attributeX
	 * @param String
	 *            attributeY
	 * @param double[]
	 *            dataX
	 * @param double[]
	 *            dataY
	 * @param boolean
	 *            axisOn
	 * @param boolean
	 *            plotLine
	 * @param double
	 *            slope
	 * @param double
	 *            intercept
	 */
	public ScatterPlotBasic(Object[] dataObject, int[] dataIndices,
			boolean axisOn, Color c) {
		this.dataObject = dataObject;
		this.attributeArrays = (String[]) dataObject[0];
		int len = attributeArrays.length;
		if (dataObject[len + 1] == null) {
			this.observNames = null;
		} else {
			this.observNames = (String[]) dataObject[len + 1];
		}
		this.dataIndices = (int[]) dataIndices;
		// convert Object array to double arrays.
		axisDataSetup();

		this.axisOn = axisOn;

		this.background = c;
		if (c == Color.black)
			this.foreground = Color.white;
		else
			this.foreground = Color.black;
		initialize();
	}

	/**
	 * @param data
	 * 
	 * This method is deprecated becuase it wants to create its very own pet
	 * DataSetForApps. This is no longer allowed, to allow for a mutable, common
	 * data set. Use of this method may lead to unexpected program behavoir.
	 * Please use setDataSet instead.
	 */
	@Deprecated
	public void setData(Object[] data) {
		this.setDataSet(new DataSetForApps(data));

	}

	public void setDataSet(DataSetForApps data) {
		if (data == null) {
			logger.finest("data null!");
			return;
		}
		this.dataSet = data;
		// XXX need to change this???
		this.dataObject = data.getDataSetNumericAndSpatial();
		this.attributeArrays = (String[]) dataObject[0];

		int len = attributeArrays.length;
		if (dataObject[len + 1] == null) {
			this.observNames = null;
		} else {
			this.observNames = (String[]) dataObject[len + 1];
		}
		this.initExcentricLabels();
	}

	/**
	 * put your documentation comment here
	 * 
	 * @param doubleDataArrays
	 */
	public void setDoubleDataArrays(double[][] doubleDataArrays) {
		this.doubleDataArrays = doubleDataArrays;
	}

	/**
	 * Not used in dataObject version.
	 * 
	 * @param dataIndices
	 */
	public void setDataIndices(int[] dataIndices) {
		this.dataIndices = (int[]) dataIndices.clone();
		this.dataX = doubleDataArrays[dataIndices[0]];
		this.dataY = doubleDataArrays[dataIndices[1]];
		this.attributeX = attributeArrays[dataIndices[0]];
		this.attributeY = attributeArrays[dataIndices[1]];
		initialize();
	}

	/**
	 * Set up x and y axises in each element by setting the attributes
	 * displayed.
	 * 
	 * @param indices
	 */
	public void setElementPosition(int[] indices) {
		this.dataIndices = (int[]) indices;
		axisDataSetup();
		initialize();
	}

	protected void axisDataSetup() {
		boolean[] dataBoolean;
		int[] dataInt;
		int len = 0;
		if (dataObject[dataIndices[0]] instanceof double[]) {
			this.dataX = (double[]) (dataObject[dataIndices[0]]);
			len = dataX.length;
		} else if (dataObject[dataIndices[0]] instanceof int[]) {
			dataInt = (int[]) dataObject[dataIndices[0]];
			len = dataInt.length;
			dataX = new double[len];
			for (int i = 0; i < len; i++) {
				dataX[i] = (double) dataInt[i];
			}
		} else if (dataObject[dataIndices[0]] instanceof boolean[]) {
			dataBoolean = (boolean[]) dataObject[dataIndices[0]];
			len = dataBoolean.length;
			dataX = new double[len];
			for (int i = 0; i < len; i++) {
				if (dataBoolean[i] == true) {
					dataX[i] = 1;
				} else
					dataX[i] = 0;
			}
		}
		if (dataObject[dataIndices[1]] instanceof double[]) {
			this.dataY = (double[]) dataObject[dataIndices[1]];
			len = this.dataY.length;
		} else if (dataObject[dataIndices[1]] instanceof int[]) {
			dataInt = (int[]) dataObject[dataIndices[1]];
			len = dataInt.length;
			dataY = new double[len];
			for (int i = 0; i < len; i++) {
				dataY[i] = (double) dataInt[i];
			}
		} else if (dataObject[dataIndices[1]] instanceof boolean[]) {
			dataBoolean = (boolean[]) dataObject[dataIndices[1]];
			len = dataBoolean.length;
			dataY = new double[len];
			for (int i = 0; i < len; i++) {
				if (dataBoolean[i] == true) {
					dataY[i] = 1;
				} else
					dataY[i] = 0;
			}
		}
		this.attributeX = attributeArrays[dataIndices[0] - 1]; // Minus 1
		// because
		// dataObject[0]
		// is attribute
		// names.
		this.attributeY = attributeArrays[dataIndices[1] - 1]; // and real data
		// begin from
		// dataObject[1].
		this.selections = new int[len];
		this.exsint = new int[len];
		this.whyint = new int[len];
	}

	/**
	 * put your documentation comment here
	 * 
	 * @return
	 */
	public int[] getElementPosition() {
		return this.dataIndices;
	}

	/**
	 * put your documentation comment here
	 * 
	 * @param attributeArrays
	 */
	public void setAttributeArrays(String[] attributeArrays) {
		this.attributeArrays = attributeArrays;
	}

	/**
	 * put your documentation comment here
	 * 
	 * @param dataX
	 */
	public void setX(double[] dataX) {
		this.dataX = dataX;
	}

	/**
	 * put your documentation comment here
	 * 
	 * @param dataY
	 */
	public void setY(double[] dataY) {
		this.dataY = dataY;
	}

	public void setAxisOn(boolean axisOn) {
		this.axisOn = axisOn;
	}

	public void setAttributeX(String attributeX) {
		this.attributeX = attributeX;
	}

	public void setAttributeY(String attributeY) {
		this.attributeY = attributeY;
	}

	public void setData(double[] dataX, double[] dataY) {
		this.dataX = dataX;
		this.dataY = dataY;
		int len = dataX.length;
		this.selections = new int[len];
		this.exsint = new int[len];
		this.whyint = new int[len];
		if ((this.drawingBuff == null) && (this.getWidth() > 0)
				&& (this.getHeight() > 0)) {
			this.drawingBuff = this.createImage(this.getWidth(), this
					.getHeight());
		}
		initialize();
	}

	/**
	 * Minimum and maximum values for xAxis. xAxisExtents[0] = min,
	 * xAxisExtents[1] = max.
	 * 
	 * @param xAxisExtents
	 */
	public void setXAxisExtents(double[] xAxisExtents) {
		this.xAxisExtents = (double[]) xAxisExtents.clone();
		this.setupDataforDisplay();
		this.paintDrawingBuff();
		repaint();
	}

	/**
	 * Minimum and maximum values for yAxis. yAxisExtents[0] = min,
	 * yAxisExtents[1] = max.
	 * 
	 * @param yAxisExtents
	 */
	public void setYAxisExtents(double[] yAxisExtents) {
		this.yAxisExtents = (double[]) yAxisExtents.clone();
		this.setupDataforDisplay();
		this.paintDrawingBuff();
		repaint();
	}

	/**
	 * return Minimum and maximum values for xAxis.
	 * 
	 * @return xAxisExtents
	 */
	public double[] getXAxisExtents() {
		return this.xAxisExtents;
	}

	/**
	 * return Minimum and maximum values for yAxis.
	 * 
	 * @return yAxisExtents
	 */
	public double[] getYAxisExtents() {
		return this.yAxisExtents;
	}

	public void setBivarColorClasser(
			BivariateColorSymbolClassification bivarColorClasser) {
	}

	/**
	 * pass conditioning into scatterplot. Observations which are shown are
	 * marked as 1.
	 * 
	 * @param conditionArray
	 */
	public void setConditionArray(int[] conditionArray) {
		this.conditionArray = conditionArray;
	}

	public void setColorArrayForObs(Color[] colorArray) {
	}

	/**
	 * defind background color for graph.
	 * 
	 * @param c
	 */
	public void setBackground(Color c) {
		if (c == null)
			return;
		this.background = c;
		int colorTotal = c.getRed() + c.getGreen() + c.getBlue();
		int greyColor = 128 * 3;
		if (colorTotal < greyColor)
			this.foreground = Color.white;
		else
			this.foreground = Color.black;
		this.repaint();
	}

	/**
	 * defind selection color for graph.
	 * 
	 * @param c
	 */
	public void setSelectionColor(Color c) {
		this.selectionColor = c;
		this.paintDrawingBuff();
		this.repaint();
	}

	public Color getSelectionColor() {
		return this.selectionColor;
	}

	public void setPointSelected(boolean pointSelected) {
		this.pointSelected = pointSelected;
	}

	public void setMultipleSelectionColors(Color[] c) {
		this.multipleSelectionColors = c;
	}

	public void setSelOriginalColorMode(boolean selOriginalColorMode) {
		this.selOriginalColorMode = selOriginalColorMode;
		this.paintDrawingBuff();
		repaint();
	}

	/**
	 * Return itself.
	 * 
	 * @return
	 */
	public MatrixElement getThis() {
		return this;
	}

	public String getShortDiscription() {
		return "XYP";
	}

	public JToolBar getTools() {
		return null;
	}

	/**
	 * Set up data and axis for drawing the scatter plot.
	 */
	protected void initialize() {

		this.dataArrayX = new DataArray(dataX);
		this.dataArrayY = new DataArray(dataY);
		this.conditionArray = new int[dataX.length];
		this.setBorder(BorderFactory.createLineBorder(Color.gray));
		if (axisOn) {
			xAxisExtents = (double[]) this.dataArrayX.getMaxMinCoorValue()
					.clone();
			yAxisExtents = (double[]) this.dataArrayY.getMaxMinCoorValue()
					.clone();
		} else {
			xAxisExtents[0] = dataArrayX.getExtent()[0];
			xAxisExtents[1] = dataArrayX.getExtent()[1];
			yAxisExtents[0] = dataArrayY.getExtent()[0];
			yAxisExtents[1] = dataArrayY.getExtent()[1];
		}

		size = this.getSize();

		this.setBackground(background);
		// ywkim added for regression line
		this.setVisiblePlotLine(dataX, dataY, true);

		this.setupDataforDisplay();
	}

	/**
	 * Set the size of plot area for both axis on and axis off situations.
	 * 
	 * @param axisOn
	 */
	protected void setVisibleAxis(boolean axisOn) {
		if (this.dataArrayX == null) {
			return;
		}
		if (axisOn) {
			plotOriginX = (int) (this.getWidth() * AXISSPACEPORTION);
			plotOriginY = (int) (this.getHeight() * (1 - AXISSPACEPORTION));
			plotEndX = (int) (this.getSize().getWidth())
					- (int) (this.getSize().getWidth() * AXISSPACEPORTION / 2);
			plotEndY = (int) (this.getSize().getHeight() / 12);
		} else {
			plotOriginX = 3;
			plotOriginY = (int) (this.getSize().getHeight() - 3);
			plotEndX = (int) (this.getSize().getWidth()) - 3;
			plotEndY = 3;
		}

		// set the location of bars and Strings.
		// firstBar = (int)(yAxisExtents[0]/(this.dataArrayY.getMajorTick()));
		// lastBar = (int)(yAxisExtents[1]/(this.dataArrayY.getMajorTick()));
		// double yBarNumber = (yAxisExtents[1] -
		// yAxisExtents[0])/(this.dataArrayY.getMajorTick());
		// yBarDistance = ((plotOriginY - plotEndY)/yBarNumber);
		// double xBarNumber = (xAxisExtents[1] -
		// xAxisExtents[0])/(this.dataArrayX.getMajorTick());
		// xBarDistance = ((plotEndX - plotOriginX)/xBarNumber);
	}

	private void paintDrawingBuff() {
		if (this.drawingBuff == null) {
			return;
		}

		Graphics g = this.drawingBuff.getGraphics();
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		g.setColor(background);
		g.fillRect(0, 0, getSize().width, getSize().height);
		g.setColor(foreground);
		this.paintBorder(g);

		if (axisOn) {
			drawAxis(g);
		}

		drawPlot(g);

		if (exLabels != null && this.axisOn == true) {
			this.setToolTipText("");
			exLabels.paint(g2, getBounds());
		}
		if (logger.isLoggable(Level.FINEST)) {
			logger.finest("in scatterplotbasic, plotLine = " + plotLine);
		}

		// draw regression line
		if (plotLine) {
			if (logger.isLoggable(Level.FINEST)) {
				logger.finest("in scatterplotbasic, going to paint line.");
			}

			if (this.dataIndices[0] != this.dataIndices[1]
					&& this.regressionClass != null) {
				if (this.pointSelected == true && this.plotLineForSelections) {
					g.setColor(Color.gray);
				} else {
					this
							.drawCorrelationValue(g, this.correlation,
									this.rSquare);
					g.setColor(Color.red);
				}
				drawPlotLine(g, this.yStartPosition, this.yEndPosition);
			}
		}

		if (this.plotLineForSelections) {
			if (this.dataIndices[0] != this.dataIndices[1]
					&& this.regressionClass != null) {
				if (this.pointSelected == true) {
					this.drawCorrelationValue(g, this.correlationForSelections,
							this.rSquareForSelections);
					g.setColor(Color.red);
					drawPlotLine(g, this.yStartPositionSelections,
							this.yEndPositionSelections);
				}
			}

		}
	}

	/**
	 * Draw the scatter plot.
	 * 
	 * @param g
	 */
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		if (this.dataIndices == null)
			return;

		if (this.drawingBuff == null) {
			this.drawingBuff = this.createImage(this.getWidth(), this
					.getHeight());
			paintDrawingBuff();
		}

		// Draw buff
		if (this.drawingBuff != null) {

			g.drawImage(this.drawingBuff, 0, 0, this);
		}

		if (indiationId >= 0 && this.dataIndices[0] != this.dataIndices[1]) {
			this.drawIndication(g2, indiationId);
		}
		Rectangle rec = new Rectangle(selectX, selectY, selectWidth,
				selectHeight);
		Graphics2D g2d = (Graphics2D) g;
		this.drawSelectRectangle(g2d, rec);
		if (exLabels != null && this.exLabels.isVisible()) {
			exLabels.paint(g2, getBounds());
		}

	}

	/**
	 * Draw pot (points) on the screen.
	 * 
	 * @param g
	 */
	protected void drawPlot(Graphics g) {
		int plotWidth, plotHeight;
		plotWidth = (int) this.getWidth();
		plotHeight = (int) this.getHeight();
		int size;
		size = (plotWidth < plotHeight) ? plotWidth : plotHeight;
		this.pointSize = (size < 360) ? size / 36 : 10;
		this.pointSize = (this.pointSize < 3) ? 4 : this.pointSize + 1;

		if (this.dataIndices[0] == this.dataIndices[1]) {
			// draw histogram. need to move to matrices.
			histogram.setAxisOn(false);
			histogram.setVariableName(this.attributeX);
			histogram.setData(this.dataX);
			histogram.setXAxisExtents(this.xAxisExtents);
			histogram.setBackground(background);
			histogram.setSize(this.getWidth(), this.getHeight());
			// XXX breaking for now, frank july 07
			// histogram.setSelections(this.selections);
			histogram.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			Color half = new Color(255, 255, 255, 100);
			g2.setColor(half);
			g.fillRect(0, 0, getSize().width, getSize().height);
			g2.setColor(foreground);
			Font font = new Font("", Font.PLAIN, (int) size / 8);
			g.setFont(font);
			/*
			 * if (attributeX.length()>12){ g.drawString(attributeX, 2,
			 * plotHeight/2); }else if (attributeX.length()<=7){
			 * g.drawString(attributeX, plotWidth/4, plotHeight/2); }else {
			 * g.drawString(attributeX, plotWidth/8, plotHeight/2); }
			 */

			// font.this.getSize() = (int)plotWidth/12;
			// font.this.getSize() = (int)plotWidth/12;
			Font font1 = new Font("", Font.PLAIN, (int) size / 12);
			g.setFont(font1);
			g.drawLine(0, 0, 5, 5);
			String maxString = Float.toString((float) (xAxisExtents[1]));
			g.drawString(maxString, 6,
					(int) (plotHeight * AXISSPACEPORTION / 2) + 2);
			g.drawLine(0, plotHeight, 5, plotHeight - 5);
			String minString = Float.toString((float) (xAxisExtents[0]));
			g.drawString(minString, 6, plotHeight - 5);
			g.drawLine(plotWidth, plotHeight, plotWidth - 5, plotHeight - 5);
			g.drawString(maxString, plotWidth
					- (int) (plotWidth * AXISSPACEPORTION + 5), plotHeight - 5);

		} else {
			if (dataArrayX != null) {
				int len = dataArrayX.length();
				// draw the points
				this.drawSlections(g, this.pointColors, len);

			}
		}
	}

	/**
	 * Add by jin Chen: Given a data id, draw the data in the plot
	 * given indication specific code by frank hardisty
	 */
	protected void drawIndication(Graphics2D g, int i) {
		Graphics2D g2 = (Graphics2D)g;

		// Stroke oldStroke = g.getStroke();
		//int x = exsint[i] - pointSize/2;
		//int y = whyint[i] - pointSize/2;
		int x = exsint[i];
		int y = whyint[i];
		
		int pointDrawX = x - (pointSize/2)+1;
		int pointDrawY = y - (pointSize/2)+1;
		
		Stroke tempStroke = g2.getStroke();
		
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		BasicStroke secondStroke = new BasicStroke(6f);
		
		BasicStroke underStroke = new BasicStroke(20f);
		g2.setColor(new Color(255,0,0,68));
		g2.setStroke(underStroke);
		g2.drawOval(pointDrawX,pointDrawY, pointSize, pointSize);
		
		g2.drawImage(this.indicationStamp, x-(this.stampSize/2), y-(this.stampSize/2), this);
		
		g2.setStroke(secondStroke);
		g2.setColor(Color.black);
		g2.drawOval(pointDrawX,pointDrawY, pointSize, pointSize);
		
		g2.setStroke(tempStroke);
		g2.setColor(this.pointColors[i]);
		g2.fillOval(pointDrawX,pointDrawY, pointSize, pointSize);
	}

	protected void drawSlections(Graphics g, Color[] colorNonSelected, int len) {

		g.setColor(this.foreground);
		for (int i = 0; i < len; i++) {
			if ((exsint[i] <= this.plotEndX) && (exsint[i] >= plotOriginX)
					&& (whyint[i] <= plotOriginY) && (whyint[i] >= plotEndY)
					&& (conditionArray[i] > -1)) {
				g.drawOval(exsint[i] - pointSize/2, whyint[i] - pointSize/2, pointSize, pointSize);
				// g.fillOval(exsint[i] - 2, whyint[i] - 2, pointSize,
				// pointSize);
			}
		}
		if (this.pointSelected != false) {
			for (int i = 0; i < len; i++) {
				g.setColor(this.selectionColor);
				if ((exsint[i] <= this.plotEndX) && (exsint[i] >= plotOriginX)
						&& (whyint[i] <= plotOriginY)
						&& (whyint[i] >= plotEndY) && (conditionArray[i] > -1)) {

					if (this.selections[i] == 1) {
						g.drawOval(exsint[i] - pointSize/2, whyint[i] - pointSize/2, pointSize,
								pointSize);
						// g.fillOval(exsint[i] - 2, whyint[i] - 2, pointSize,
						// pointSize);
					}
				}
			}
		}
	}

	/**
	 * Draw axises for scatterplot.
	 * 
	 * @param g
	 */
	protected void drawAxis(Graphics g) {
		int plotWidth, plotHeight;
		plotWidth = (int) this.getWidth();
		plotHeight = (int) this.getHeight();

		if (!(this.dataIndices[0] == this.dataIndices[1])) {

			if (logger.isLoggable(Level.FINEST)) {
				logger.finest("plotOriginX = " + plotOriginX);
			}
			// draw the lines
			g.setColor(foreground);
			g.drawLine(plotOriginX, plotEndY, plotOriginX, plotOriginY);
			g.drawLine(plotOriginX, plotOriginY, plotEndX, plotOriginY);
			// draw tick bars for scales on Y coordinate
			int fontSize;
			fontSize = (plotWidth < plotHeight) ? plotWidth : plotHeight;
			fontSize = ((int) (fontSize / 32) < 9) ? 9 : fontSize / 32;

			Font font = new Font("", Font.PLAIN, fontSize);
			g.setFont(font);
			String scaleStringY;
			int i;
			int realBarNum = 0;
			double barNumber = this.dataArrayY.getTickNumber();
			int firstBar = (int) (yAxisExtents[0] / (this.dataArrayY
					.getMajorTick()));
			int lastBar = (int) (yAxisExtents[1] / (this.dataArrayY
					.getMajorTick()));
			double yBarDistance = ((plotOriginY - plotEndY) / barNumber);
			for (i = firstBar; i <= lastBar + 0.00001; i++) {
				// for (i = 0; i <= barNumber; i++) {
				g.drawLine(plotOriginX - 3, plotEndY
						+ (int) (realBarNum * yBarDistance), plotOriginX,
						plotEndY + (int) (realBarNum * yBarDistance));
				if (Math.abs(this.dataArrayY.getMajorTick()) <= 1) {
					scaleStringY = Float
							.toString((float) (yAxisExtents[1] - realBarNum
									* this.dataArrayY.getMajorTick()));
				} else {
					scaleStringY = Integer
							.toString((int) (yAxisExtents[1] - realBarNum
									* this.dataArrayY.getMajorTick()));
				}
				g
						.drawString(
								scaleStringY,
								plotOriginX
										- (int) (plotWidth * AXISSPACEPORTION / 2),
								plotEndY
										+ (int) (realBarNum * yBarDistance + yBarDistance * 1 / 6));

				// draw background grid
				g.setColor(Color.lightGray);
				g.drawLine(plotOriginX, plotEndY
						+ (int) (realBarNum * yBarDistance), plotEndX, plotEndY
						+ (int) (realBarNum * yBarDistance));
				g.setColor(this.foreground);
				realBarNum++;
			}
			// draw tick bars for scales on X coordinate
			realBarNum = 0;
			barNumber = this.dataArrayX.getTickNumber();
			double xBarDistance = ((plotEndX - plotOriginX) / barNumber);
			String scaleStringX;
			for (i = (int) (xAxisExtents[0] / (this.dataArrayX.getMajorTick())); i <= (int) (xAxisExtents[1] / (this.dataArrayX
					.getMajorTick())) + 0.0001; i++) {
				g.drawLine(plotOriginX + (int) (realBarNum * xBarDistance),
						plotOriginY, plotOriginX
								+ (int) (realBarNum * xBarDistance),
						plotOriginY + 3);
				if (Math.abs(this.dataArrayX.getMajorTick()) <= 1) {
					scaleStringX = Float
							.toString((float) (xAxisExtents[0] + realBarNum
									* this.dataArrayX.getMajorTick()));
				} else {
					scaleStringX = Integer
							.toString((int) (xAxisExtents[0] + realBarNum
									* this.dataArrayX.getMajorTick()));
				}
				Graphics2D g2d = (Graphics2D) g;
				g2d.rotate(-Math.PI / 4, plotOriginX - 2
						+ (int) (realBarNum * xBarDistance), plotOriginY
						+ plotHeight * AXISSPACEPORTION * 2 / 3);
				g.drawString(scaleStringX, plotOriginX - 5
						+ (int) (realBarNum * xBarDistance), plotOriginY
						+ (int) (plotHeight * AXISSPACEPORTION * 2 / 3 - 1));
				g2d.rotate(Math.PI / 4, plotOriginX - 2
						+ (int) (realBarNum * xBarDistance), plotOriginY
						+ plotHeight * AXISSPACEPORTION * 2 / 3);
				// graw background grid
				g.setColor(Color.lightGray);
				g.drawLine(plotOriginX + (int) (realBarNum * xBarDistance),
						plotOriginY, plotOriginX
								+ (int) (realBarNum * xBarDistance), plotEndY);
				g.setColor(this.foreground);
				realBarNum++;
			}
			font = new Font("", Font.PLAIN, fontSize + 3);
			g.setFont(font);
			// draw X axis attribute string
			g.drawString(attributeX, plotOriginX + (plotEndX - plotOriginX) / 2
					- plotWidth / 12, plotOriginY + plotHeight / 6 - 5);
			// draw Y axis attribute string. Need rotation for drawing the
			// string vertically.
			Graphics2D g2d = (Graphics2D) g;
			g2d.rotate(-Math.PI / 2, plotOriginX - plotWidth / 9, plotOriginY
					- (plotOriginY - plotEndY) / 3);
			g2d.drawString(attributeY, plotOriginX - plotWidth / 9, plotOriginY
					- (plotOriginY - plotEndY) / 3);
			g2d.rotate(+Math.PI / 2, plotOriginX - plotWidth / 9, plotOriginY
					- (plotOriginY - plotEndY) / 3);
		}
	}

	/**
	 * Return selections from this scatterplot.
	 * 
	 * @return
	 */
	public Vector getSelectedObservations() {
		return selRecords;
	}

	/**
	 * Set up selections from other components.
	 * 
	 * @param selectedObservations
	 */
	public void setSelectedObservations(Vector selectedObservations) {
		selRecords = selectedObservations;
	}

	public void setSelections(int[] selectedObservations) {
		if (selections == null || selectedObservations == null) {
			return;
		}
		if (selections.length != selectedObservations.length) {
			for (int i = 0; i < selections.length; i++) {
				selections[i] = 0;
			}
			for (int i = 0; i < selectedObservations.length; i++) {
				selections[selectedObservations[i]] = -1;
			}
		} else {
			selections = selectedObservations;
		}
		int j = 0;
		for (int i = 0; i < selections.length; i++) {
			if (selections[i] == 1) {
				j++;
			}
		}
		this.pointSelected = false;
		if (j != 0) {
			this.selectedDataX = new double[j];
			this.selectedDataY = new double[j];
			int selIndex = 0;
			for (int i = 0; i < dataX.length; i++) {
				if (this.selections[i] == 1) {
					this.selectedDataX[selIndex] = this.dataX[i];
					this.selectedDataY[selIndex] = this.dataY[i];
					selIndex++;
				}
			}
			this.setVisiblePlotLine(this.selectedDataX, this.selectedDataY,
					false);
			this.setUpRegressionLine(this.slopeForSelections,
					this.interceptForSelections, false);
			this.pointSelected = true;
		} else {
			this.setVisiblePlotLine(dataX, dataY, true);
		}
		this.paintDrawingBuff();
		this.repaint();
	}

	/**
	 * Return selections from this scatterplot.
	 * 
	 * @return
	 */
	public int[] getSelections() {
		return this.selections;
	}

	/**
	 * @param indication
	 *            the id the data to be indicated
	 */
	public void setIndication(int indication) {
		logger.finest("indicate:"+indication);
		this.indiationId = indication;
		this.repaint();
		// noop

	}

	/**
	 * Calculate scale between real data and integer data for showing up on
	 * screen.
	 * 
	 * @param min
	 * @param max
	 * @param dataMin
	 * @param dataMax
	 * @return scale
	 */
	protected double getScale(int min, int max, double dataMin, double dataMax) {
		double scale;
		scale = (max - min) / (dataMax - dataMin);
		return scale;
	}

	/**
	 * Convert the single value to integer value worked on screen.
	 * 
	 * @param data
	 * @param scale
	 * @param min
	 * @param dataMin
	 * @return valueScreen
	 */
	protected int getValueScreen(double data, double scale, int min,
			double dataMin) {
		int valueScreen;
		if (Double.isNaN(data)) {
			valueScreen = Integer.MIN_VALUE;
		} else {
			valueScreen = (int) ((data - dataMin) * scale + min);
		}
		return valueScreen;
	}

	/**
	 * Convert the numeric values of observations to integer value worked on
	 * screen.
	 * 
	 * @param dataArray
	 * @param scale
	 * @param min
	 * @param dataMin
	 * @return valueScreen
	 */
	protected int[] getValueScreen(double[] dataArray, double scale, int min,
			double dataMin) {
		int[] valueScreen = new int[dataArray.length];
		for (int i = 0; i < dataArray.length; i++) {
			if (Double.isNaN(dataArray[i])) {
				valueScreen[i] = Integer.MIN_VALUE;
			} else {
				valueScreen[i] = (int) ((dataArray[i] - dataMin) * scale + min);
			}
		}
		return valueScreen;
	}

	protected void setupDataforDisplay() {

		this.setVisibleAxis(axisOn);
		if (dataArrayX == null)
			return;
		int len = dataArrayX.length();
		if (len != dataArrayY.length())
			return;
		// get positions on screen
		double xScale;
		double yScale;
		xScale = getScale(plotOriginX, plotEndX, xAxisExtents[0],
				xAxisExtents[1]);
		exsint = getValueScreen(dataX, xScale, plotOriginX, xAxisExtents[0]);
		yScale = getScale(plotOriginY, plotEndY, yAxisExtents[0],
				yAxisExtents[1]);
		whyint = getValueScreen(dataY, yScale, plotOriginY, yAxisExtents[0]);

		if (this.pointSelected && this.plotLineForSelections) {
			this.setUpRegressionLine(this.slope, this.intercept, true);
			this.setUpRegressionLine(this.slopeForSelections,
					this.interceptForSelections, false);
		} else {
			this.setUpRegressionLine(this.slope, this.intercept, true);
		}
		this.paintDrawingBuff();
		this.repaint();

	}

	// start excentric labeling stuff
	protected void initExcentricLabels() {
		this.exLabels = new ExcentricLabels();
		exLabels.setComponent(this);
		exLabels.setOpaque(true);
		Color halfWhite = new Color(255, 255, 255, 123);
		exLabels.setBackgroundColor(halfWhite);
		this.addMouseListener(exLabels);

	}

	public String getObservationLabel(int i) {
		String[] labels = this.observNames;
		if (labels != null && labels.length - 1 >= i) {
			return labels[i];
		} else
			return "";
	}

	public Shape getShapeAt(int i) {
		int x = this.exsint[i];
		int y = this.whyint[i];
		Ellipse2D circle = new Ellipse2D.Float(x, y, this.pointSize,
				this.pointSize);

		return circle;
	}

	public int[] pickAll(Rectangle2D hitBox) {
		Vector hits = new Vector();
		for (int i = 0; i < dataX.length; i++) {
			if (hitBox.contains(exsint[i], whyint[i])
					&& (conditionArray[i] > -1)) {
				Integer bigI = new Integer(i);
				hits.add(bigI);
			}
		}
		int[] intHits = new int[hits.size()];
		for (int i = 0; i < hits.size(); i++) {

			intHits[i] = ((Integer) hits.get(i)).intValue();
		}
		return intHits;
	}

	// end excentric labeling stuff

	public void componentHidden(ComponentEvent e) {

	}

	public void componentMoved(ComponentEvent e) {

	}

	public void componentResized(ComponentEvent e) {
		if (this.getWidth() > 0 && (this.getHeight() > 0)) {
			this.drawingBuff = this.createImage(this.getWidth(), this
					.getHeight());
		}
		this.setupDataforDisplay();
		this.paintDrawingBuff();
		this.repaint();
	}

	public void componentShown(ComponentEvent e) {
	}

	/**
	 * Begin the drawing of selection region (box).
	 * 
	 * @param e
	 */
	public void mousePressed(MouseEvent e) {
		if (dataIndices == null || dataIndices == null)
			return;
		if (dataIndices[0] == dataIndices[1])
			return;
		if (e.isPopupTrigger())
			maybeShowPopup(e);
		// selRecords.clear();
		mouseX1 = e.getX();
		mouseY1 = e.getY();
	}

	/**
	 * Work with mouseDragged to draw a selection region (box) for selection.
	 * 
	 * @param e
	 */
	public void mouseReleased(MouseEvent e) {
		if (dataIndices == null || dataIndices[0] == dataIndices[1])
			return;
		if (e.isPopupTrigger())
			maybeShowPopup(e);
		mouseX2 = e.getX();
		mouseY2 = e.getY();
		if ((Math.abs(mouseX1 - mouseX2) < 3)
				&& (Math.abs(mouseY1 - mouseY2) < 3)) {
			return;
		}
		// With shift pressed, it will continue to select.
		if (!(e.isShiftDown())) {
			// zero all selection indication to deselect them.
			for (int i = 0; i < this.selections.length; i++) {
				this.selections[i] = 0;
			}
		}
		if (mouseX1 <= mouseX2 && mouseY1 <= mouseY2) {
			selectX = mouseX1;
			selectY = mouseY1;
			selectWidth = mouseX2 - mouseX1;
			selectHeight = mouseY2 - mouseY1;
		}
		if (mouseX2 < mouseX1 && mouseY1 <= mouseY2) {
			selectX = mouseX2;
			selectY = mouseY1;
			selectWidth = mouseX1 - mouseX2;
			selectHeight = mouseY2 - mouseY1;
		}
		if (mouseX1 <= mouseX2 && mouseY2 < mouseY1) {
			selectX = mouseX1;
			selectY = mouseY2;
			selectWidth = mouseX2 - mouseX1;
			selectHeight = mouseY1 - mouseY2;
		}
		if (mouseX2 < mouseX1 && mouseY2 < mouseY1) {
			selectX = mouseX2;
			selectY = mouseY2;
			selectWidth = mouseX1 - mouseX2;
			selectHeight = mouseY1 - mouseY2;
		}
		Rectangle rec = new Rectangle(selectX, selectY, selectWidth,
				selectHeight);
		pointSelected = false;
		// int j = 0;
		for (int i = 0; i < dataX.length; i++) {
			if (rec.contains(exsint[i], whyint[i]) && (conditionArray[i] > -1)) {
				// Integer bigI = new Integer(i);
				// selRecords.add(bigI);
				this.selections[i] = 1;// new selection struction int[]
				pointSelected = true;
				// j++;
			}
		}
		int j = 0;
		for (int i = 0; i < selections.length; i++) {
			if (selections[i] == 1) {
				j++;
			}
		}

		if (j != 0) {
			this.selectedDataX = new double[j];
			this.selectedDataY = new double[j];
			int selIndex = 0;
			for (int i = 0; i < dataX.length; i++) {
				if (this.selections[i] == 1) {
					this.selectedDataX[selIndex] = this.dataX[i];
					this.selectedDataY[selIndex] = this.dataY[i];
					if (logger.isLoggable(Level.FINEST)) {
						logger.finest(i + " " + this.selectedDataX[selIndex]);
					}
					selIndex++;
				}
			}
			this.setVisiblePlotLine(this.selectedDataX, this.selectedDataY,
					false);
			this.setUpRegressionLine(this.slopeForSelections,
					this.interceptForSelections, false);
		} else
			this.setVisiblePlotLine(dataX, dataY, true);
		selectWidth = 0;
		selectHeight = 0;
		this.multipleSelectionColors = null;
		this.paintDrawingBuff();
		repaint();

		fireActionPerformed(COMMAND_POINT_SELECTED);
	}

	/**
	 * put your documentation comment here
	 * 
	 * @param e
	 */
	public void mouseExited(MouseEvent e) {
		this.setIndication(-1);
		this.fireIndicationChanged(-1);

	}

	/**
	 * Work with mouseReleased to draw a selection region (box) for selection.
	 * 
	 * @param e
	 */
	public void mouseDragged(MouseEvent e) {
		if (dataIndices == null || dataIndices[0] == dataIndices[1])
			return;

		mouseX2 = e.getX();
		mouseY2 = e.getY();
		if ((Math.abs(mouseX1 - mouseX2) < 3)
				&& (Math.abs(mouseY1 - mouseY2) < 3)) {
			return;
		}
		if (logger.isLoggable(Level.FINEST)) {
			logger.finest("mouse released: " + "mouseX2" + mouseX2
					+ "mouseY2" + mouseY2);
		}
		if (mouseX1 <= mouseX2 && mouseY1 <= mouseY2) {
			selectX = mouseX1;
			selectY = mouseY1;
			selectWidth = mouseX2 - mouseX1;
			selectHeight = mouseY2 - mouseY1;
		}
		if (mouseX2 < mouseX1 && mouseY1 <= mouseY2) {
			selectX = mouseX2;
			selectY = mouseY1;
			selectWidth = mouseX1 - mouseX2;
			selectHeight = mouseY2 - mouseY1;
		}
		if (mouseX1 <= mouseX2 && mouseY2 < mouseY1) {
			selectX = mouseX1;
			selectY = mouseY2;
			selectWidth = mouseX2 - mouseX1;
			selectHeight = mouseY1 - mouseY2;
		}
		if (mouseX2 < mouseX1 && mouseY2 < mouseY1) {
			selectX = mouseX2;
			selectY = mouseY2;
			selectWidth = mouseX1 - mouseX2;
			selectHeight = mouseY1 - mouseY2;
		}
		repaint();
	}

	/**
	 * Mouse over, it will show the values for current point by tool tip.
	 * 
	 * @param e
	 */
	public void mouseMoved(MouseEvent e) {
		if (e != null && this.axisOn == false) {
			this.makeToolTip(e.getX(), e.getY());
			e.consume();
		}
		int id = this.findCoveredDataId(e.getX(), e.getY());
		if (id >= 0) {
			this.setIndication(id);
			this.repaint();
			// fire indication event
			fireIndicationChanged(id);
		}

	}

	/**
	 * Add by Jin Chen. Given mouse position, find the id of the data covered by
	 * mouse
	 * 
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	protected int findCoveredDataId(int x, int y) {
		int arrayIndex = -1;
		// boolean pointMove = false;
		if (dataIndices == null) {
			return -1;
		}
		if (exsint == null) {
			return -1;
		}
		int tolerance = 3;
		if (this.pointSize > tolerance){
			tolerance = pointSize;
		}
		if ((dataIndices[0] != dataIndices[1]) && (exsint != null)
				&& (whyint != null)) {
			for (int i = 0; i < dataX.length; i++) {
				if ((exsint[i] - tolerance < x) && (x <= exsint[i] + tolerance)
						&& (whyint[i] - tolerance < y) && (y <= whyint[i] + tolerance)
						&& (conditionArray[i] > -1)) {
					// pointMove = true;
					arrayIndex = i;
					return arrayIndex;
				}
			}
		}
		return arrayIndex;
	}

	protected void selectAPoint(int[] mousePos) {
		if (dataIndices[0] != dataIndices[1]) {
			for (int i = 0; i < dataX.length; i++) {
				if ((exsint[i] - 5 < mousePos[0])
						&& (mousePos[0] < exsint[i] + 5)
						&& (whyint[i] - 5 < mousePos[1])
						&& (mousePos[1] < whyint[i] + 5)
						&& (conditionArray[i] > -1)) {
					this.selections[i] = 1;
				}
			}

			fireActionPerformed(COMMAND_POINT_SELECTED);
		}
	}

	protected void popupADetailedPlot(String className) {
		if (dataIndices[0] != dataIndices[1]) {
			MatrixElement detailSP;
			Class detailPlot;
			try {
				detailPlot = (className != null) ? Class.forName(className)
						: null;
				detailSP = (MatrixElement) detailPlot.newInstance();
				detailSP.setDataSet(this.dataSet);
				detailSP.setAxisOn(true);
				detailSP.setElementPosition(dataIndices);

				dlgSP = new JFrame("Detailed Scatter Plot");
				dlgSP.setLocation(300, 300);
				dlgSP.setSize(300, 300);
				dlgSP.getContentPane().setLayout(new BorderLayout());
				dlgSP.getContentPane().add((Component) detailSP,
						BorderLayout.CENTER);

				detailSP.setBackground(background);
				detailSP.setBivarColorClasser(this.bivarColorClasser, false);
				detailSP.setColorArrayForObs(this.pointColors);

				detailSP.setSelectionColor(this.selectionColor);
				detailSP.setSelOriginalColorMode(this.selOriginalColorMode);
				detailSP.setSelections(this.selections);
				detailSP.setXAxisExtents(this.xAxisExtents);// ?
				detailSP.setYAxisExtents(this.yAxisExtents);

				detailSP.addActionListener(new ActionListener() {

					/**
					 * put your documentation comment here
					 * 
					 * @param e
					 */
					public void actionPerformed(ActionEvent e) {
						if (logger.isLoggable(Level.FINEST)) {
							logger.finest("something came from detailed one.");
						}
						ScatterPlotBasic detailSP = (ScatterPlotBasic) e
								.getSource();
						String command = e.getActionCommand();
						if (command
								.compareTo(ScatterPlotBasic.COMMAND_POINT_SELECTED) == 0) {
							logger.finest("SPMC.plotUnitPanel.actionPerformed(), point selected");
							// Vector selRecords =
							// detailSP.getSelectedObservations();
							int[] selections = detailSP.getSelections();
							// Don't recall the scatterplot which generated the
							// original event
							ScatterPlotBasic.this.setSelections(selections);
							ScatterPlotBasic.this
									.fireActionPerformed(COMMAND_POINT_SELECTED);
						} else if (command
								.compareTo(ScatterPlotBasic.COMMAND_DATARANGE_SET) == 0) {
							double[] dataArrayX = detailSP.getXAxisExtents();
							double[] dataArrayY = detailSP.getYAxisExtents();
							ScatterPlotBasic.this.setXAxisExtents(dataArrayX);
							ScatterPlotBasic.this.setYAxisExtents(dataArrayY);
							fireActionPerformed(COMMAND_DATARANGE_SET);
						}
						// System.err.println("Unknown command! = " + command);
					}
				});
				dlgSP.setVisible(true);

			} catch (Exception e) {
				e.printStackTrace();
			}

		} else {
			Histogram histogram = new Histogram();
			histogram.setVariableName(this.attributeX);
			histogram.setData(this.dataX);
			// histogram.setSelections(this.selections);
			histogram.setBackground(background);
			JFrame dummyFrame = new JFrame();
			JDialog dlgSP = new JDialog(dummyFrame, "Histogram", true);
			dlgSP.setLocation(300, 300);
			dlgSP.setSize(300, 300);
			dlgSP.getContentPane().setLayout(new BorderLayout());
			dlgSP.getContentPane().add(histogram, BorderLayout.CENTER);
			histogram.addActionListener(new ActionListener() {

				/**
				 * put your documentation comment here
				 * 
				 * @param e
				 */
				public void actionPerformed(ActionEvent e) {
					Histogram histogram = (Histogram) e.getSource();
					BitSet bSet = histogram.getSelections();
					int[] selObs = new int[bSet.size()];
					
					for (int i = 0; i < bSet.size(); i++){
						if(bSet.get(i)){
						selObs[i] = 1;
						}else{
							selObs[i] = -1;
						}
					}
					ScatterPlotBasic.this.setSelections(selObs);
		
					ScatterPlotBasic.this
							.fireActionPerformed(COMMAND_POINT_SELECTED);
				}
			});
			dlgSP.setVisible(true);
		}

	}

	/**
	 * put your documentation comment here
	 * 
	 * @param e
	 */
	public void mouseEntered(MouseEvent e) {

	}

	/**
	 * Mouse click for selecting or brushing points (observations).
	 * 
	 * @param e
	 */
	public void mouseClicked(MouseEvent e) {
		int count = e.getClickCount();
		// moduleBody.selected(true);
		int[] mousePos = new int[2];
		mousePos[0] = e.getX();
		mousePos[1] = e.getY();
		// single click, select performed.
		if (dataIndices == null)
			return;
		if (count == 1) {
			this.selectAPoint(mousePos);
		}
		// double click, pop up a detail scatter plot.
		if (count == 2) // This is a double-click or triple...
		{
			String className = this.getClass().getName();
			this.popupADetailedPlot(className);
		}
	}

	protected void drawSelectRectangle(Graphics2D g2d, Rectangle rec) {

		Stroke tempStroke = g2d.getStroke();
		float[] dash = new float[3];
		dash[0] = (float) 5.0;
		dash[1] = (float) 7.0;
		dash[2] = (float) 5.0;
		BasicStroke dashStroke = new BasicStroke((float) 1.0,
				BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, (float) 10.0,
				dash, 0);
		g2d.setStroke(dashStroke);
		g2d.setPaintMode();
		g2d.setColor(foreground);
		g2d.drawRect(selectX, selectY, selectWidth, selectHeight);
		// Draw selected observations.
		// if (!selRecords.isEmpty()) {
		// g2d.setXORMode(background);
		g2d.drawRect(selectX, selectY, selectWidth, selectHeight);
		g2d.setStroke(tempStroke);
	}

	protected void makeToolTip(int x, int y) {
		int arrayIndex = -1;
		// boolean pointMove = false;
		if (dataIndices == null) {
			return;
		}
		if (exsint == null) {
			return;
		}
		if ((dataIndices[0] != dataIndices[1]) && (exsint != null)
				&& (whyint != null)) {
			for (int i = 0; i < dataX.length; i++) {
				if ((exsint[i] - 3 < x) && (x < exsint[i] + 3)
						&& (whyint[i] - 3 < y) && (y < whyint[i] + 3)
						&& (conditionArray[i] > -1)) {
					// pointMove = true;
					arrayIndex = i;
				}
			}
		}
		if (arrayIndex >= 0) {
			// setting multi-line tool tip
			// b.setToolTipText("<html>ToolTip : 1st Line<br>2nd Line<br> 3rd
			// Line </html>");
			String xVal = Double.toString(dataX[arrayIndex]);
			String yVal = Double.toString(dataY[arrayIndex]);
			String s = "<html> ";
			// s = s + "Idx: " + new Integer(arrayIndex).toString() + " ";
			if (this.observNames != null) {
				s = s + "Name = " + observNames[arrayIndex] + "<br>";
			}

			s = s + attributeX + " = " + xVal + "<br>" + attributeY + " = "
					+ yVal + "</html>";

			this.setToolTipText(s);
		} // end if
	}

	JFrame dummyFrame1;
	JDialog dialog;

	/**
	 * New data ranges setup dialog.
	 * 
	 * @param x
	 * @param y
	 */
	protected void showDialog(int x, int y) {
		dummyFrame1 = new JFrame();
		dialog = new JDialog(dummyFrame1, "Data Range Configuer", true);
		JButton actionButton;
		JButton resetButton;
		dialog.setLocation(x, y);
		dialog.setSize(300, 150);
		dialog.getContentPane().setLayout(new GridLayout(5, 2));

		// create buttons for action
		actionButton = new JButton("OK");
		actionButton.addActionListener(new java.awt.event.ActionListener() {

			/**
			 * Button to set up new data ranges shown up in scatter plot.
			 * 
			 * @param e
			 */
			public void actionPerformed(ActionEvent e) {
				try {
					actionButton_actionPerformed(e);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		resetButton = new JButton("Reset");
		resetButton.addActionListener(new java.awt.event.ActionListener() {

			/**
			 * put your documentation comment here
			 * 
			 * @param e
			 */
			public void actionPerformed(ActionEvent e) {
				resetButton_actionPerformed(e);
			}
		});
		// dialog.getContentPane().add(new JLabel("X Range Min:"));
		dialog.getContentPane().add(new JLabel((this.attributeX + " Min")));
		dialog.getContentPane().add(xAxisMinField);
		dialog.getContentPane().add(new JLabel((this.attributeX + " Max")));
		dialog.getContentPane().add(xAxisMaxField);
		dialog.getContentPane().add(new JLabel((this.attributeY + " Min")));
		dialog.getContentPane().add(yAxisMinField);
		dialog.getContentPane().add(new JLabel((this.attributeY + " Max")));
		dialog.getContentPane().add(yAxisMaxField);
		dialog.getContentPane().add(resetButton);
		dialog.getContentPane().add(actionButton);

		xAxisMinField.setText(Double.toString(xAxisExtents[0]));
		xAxisMaxField.setText(Double.toString(xAxisExtents[1]));
		yAxisMinField.setText(Double.toString(yAxisExtents[0]));
		yAxisMaxField.setText(Double.toString(yAxisExtents[1]));

		dialog.setVisible(true);
	}

	/**
	 * put your documentation comment here
	 * 
	 * @param e
	 */
	protected void maybeShowPopup(MouseEvent e) {
		{
			getPopup().show(e.getComponent(), e.getX(), e.getY());
		}
	}

	/**
	 * Set up new data ranges to show.
	 * 
	 * @param e
	 */
	protected void actionButton_actionPerformed(ActionEvent e) {
		// get the input data from text field
		xAxisExtents[0] = Double.parseDouble(xAxisMinField.getText());
		xAxisExtents[1] = Double.parseDouble(xAxisMaxField.getText());
		yAxisExtents[0] = Double.parseDouble(yAxisMinField.getText());
		yAxisExtents[1] = Double.parseDouble(yAxisMaxField.getText());
		this.dataArrayX.setExtent(xAxisExtents);
		this.dataArrayY.setExtent(yAxisExtents);
		this.setupDataforDisplay();

		fireActionPerformed(COMMAND_DATARANGE_SET);
		this.paintDrawingBuff();
		repaint();
		this.dialog.setVisible(false);
	}

	/**
	 * put your documentation comment here
	 * 
	 * @param e
	 */
	protected void resetButton_actionPerformed(ActionEvent e) {
		this.dataArrayX.setDataExtent();
		this.dataArrayY.setDataExtent();
		if (axisOn) {
			xAxisExtents = (double[]) this.dataArrayX.getMaxMinCoorValue()
					.clone();
			yAxisExtents = (double[]) this.dataArrayY.getMaxMinCoorValue()
					.clone();
		} else {
			xAxisExtents[0] = dataArrayX.getExtent()[0];
			xAxisExtents[1] = dataArrayX.getExtent()[1];
			yAxisExtents[0] = dataArrayY.getExtent()[0];
			yAxisExtents[1] = dataArrayY.getExtent()[1];
		}
		xAxisMinField.setText(Double.toString(xAxisExtents[0]));
		xAxisMaxField.setText(Double.toString(xAxisExtents[1]));
		yAxisMinField.setText(Double.toString(yAxisExtents[0]));
		yAxisMaxField.setText(Double.toString(yAxisExtents[1]));
		this.setupDataforDisplay();
		fireActionPerformed(COMMAND_DATARANGE_SET);
		this.paintDrawingBuff();
		repaint();
	}

	/**
	 * adds an IndicationListener to the button
	 */
	public void addIndicationListener(IndicationListener l) {
		listenerList.add(IndicationListener.class, l);
	}

	/**
	 * removes an IndicationListener from the button
	 */
	public void removeIndicationListener(IndicationListener l) {
		listenerList.remove(IndicationListener.class, l);
	}

	/**
	 * adds an ActionListener to the button
	 */
	public void addActionListener(ActionListener l) {
		listenerListAction.add(ActionListener.class, l);
	}

	/**
	 * removes an ActionListener from the button
	 */
	public void removeActionListener(ActionListener l) {
		listenerListAction.remove(ActionListener.class, l);
	}

	/**
	 * Notify all listeners that have registered interest for notification on
	 * this event type. The event instance is lazily created using the
	 * parameters passed into the fire method.
	 * 
	 * @see EventListenerList
	 */
	public void fireActionPerformed(String command) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerListAction.getListenerList();
		ActionEvent e = null;
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ActionListener.class) {
				// Lazily create the event:
				if (e == null) {
					e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED,
							command);
				}
				((ActionListener) listeners[i + 1]).actionPerformed(e);
			}
		}
	}

	/**
	 * If AxisOn is true, it will be a detailed version of scatterplot with
	 * axises.
	 * 
	 * @return
	 */
	public boolean isAxisOn() {
		return axisOn;
	}

	/**
	 * Sets colors for the current data.
	 */
	public void setBivarColorClasser(
			BivariateColorSymbolClassification bivarColorClasser,
			boolean reverseColor) {
	}

	public BivariateColorSymbolClassification getBivarColorClasser() {
		return this.bivarColorClasser;
	}

	/***************************************************************************
	 * add by jin chen for indication
	 **************************************************************************/
	public void indicationChanged(IndicationEvent e) {
		int id = e.getIndication();
		this.setIndication(id);
	}

	/**
	 * Notify all listeners that have registered interest for notification on
	 * this event type. The event instance is lazily created using the
	 * parameters passed into the fire method.
	 * 
	 * @see javax.swing.event.EventListenerList
	 */
	public void fireIndicationChanged(int indicator) {

		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		IndicationEvent e = null;
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == IndicationListener.class) {
				// Lazily create the event:
				if (e == null) {
					e = new IndicationEvent(this, indicator);
				}
				((IndicationListener) listeners[i + 1]).indicationChanged(e);
			}
		}// next i

	}

	/***************************************************************************
	 * * added by ywkim for regression line *
	 **************************************************************************/

	protected Class regressionClass;
	protected String regressionClassName = LinearRegression.class
			.getName();
	protected Regression regressionInterface;

	public void setRegressionClass(Object obj) {

		setRegressionClassName((obj != null) ? obj.getClass().getName() : null);

	}

	public void setRegressionClassName(String classname) {
		this.regressionClassName = classname;
	}

	public void setRegressionClass(Class clazz) {
		this.regressionClass = clazz;
	}

	public void setPlotLine(boolean plotLine) {
		this.plotLine = plotLine;
	}

	public void setVisiblePlotLine(double[] dataX, double[] dataY,
			boolean allData) {

		try {
			setRegressionClass((this.regressionClassName != null) ? Class
					.forName(this.regressionClassName)
					: null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			if (regressionClass != null) {
				plotLine = true;
				this.regressionInterface = (Regression) this.regressionClass
						.newInstance();
			}
			if (regressionClass == null) {

				plotLine = false;
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		double[] moransRegression = new double[2];
		if (dataX != null && dataY != null) {
			logger.finest("in regression");
			moransRegression = this.regressionInterface.getRegression(dataX,
					dataY);
		}
		if (allData == true) {
			this.slope = moransRegression[0];
			this.intercept = moransRegression[1];
			this.rSquare = this.regressionInterface.getRSquare(dataX, dataY);
			this.correlation = DescriptiveStatistics.correlationCoefficient(
					dataX, dataY, true);
			// this.setUpRegressionLine(this.slope, this.intercept, allData);
			logger.finest("scatterPlotBasic all: " + this.slope + "," +this.intercept);
		} else {
			slopeForSelections = moransRegression[0];
			interceptForSelections = moransRegression[1];
			correlationForSelections = DescriptiveStatistics
					.correlationCoefficient(dataX, dataY, true);
			rSquareForSelections = this.regressionInterface.getRSquare(dataX,
					dataY);
			// this.setUpRegressionLine(this.slopeForSelections,
			// this.interceptForSelections, allData);
			logger.finest("selected:" + this.slopeForSelections + "," +this.interceptForSelections);
		}
	}

	protected void setUpRegressionLine(double slope, double intercept,
			boolean allData) {
		if (this.dataX == null) {
			return;
		}
		double xStart, xEnd;
		xStart = xAxisExtents[0];
		xEnd = xAxisExtents[1];
		double yEstimateStart, yEstimateEnd;
		yEstimateStart = (xStart * slope) + intercept;
		yEstimateEnd = (xEnd * slope) + intercept;

		logger.finest("drawPlot: " + xStart + " " + xEnd + "," + yEstimateStart + " " + yEstimateEnd);
		int yEstStartInt, yEstEndInt;
		yEstStartInt = this.getValueScreen(yEstimateStart, getScale(
				plotOriginY, plotEndY, yAxisExtents[0], yAxisExtents[1]),
				plotOriginY, yAxisExtents[0]);
		yEstEndInt = this.getValueScreen(yEstimateEnd, getScale(plotOriginY,
				plotEndY, yAxisExtents[0], yAxisExtents[1]), plotOriginY,
				yAxisExtents[0]);
		if (allData == true) {
			yStartPosition = yEstStartInt - plotOriginY;
			yEndPosition = yEstEndInt - plotOriginY;
		} else {
			yStartPositionSelections = yEstStartInt - plotOriginY;
			yEndPositionSelections = yEstEndInt - plotOriginY;
		}

	}

	protected void drawPlotLine(Graphics g, double yStartPosition,
			double yEndPosition) {
		logger.finest("drawPlot: " + yStartPosition + yEndPosition);
		if (plotLine == false) {
			return;
		}
		int xRegStart, yRegStart, xRegEnd, yRegEnd;
		xRegStart = plotOriginX;
		yRegStart = plotOriginY + (int) (yStartPosition);
		xRegEnd = plotEndX;
		yRegEnd = plotOriginY + (int) (yEndPosition);
		logger.finest("drawPlot: " + xRegStart + " " + xRegEnd + "," +yRegStart + " " + yRegEnd);
		g.drawLine(xRegStart, yRegStart, xRegEnd, yRegEnd);
	}

	protected void drawCorrelationValue(Graphics g, double correlation,
			double rSquare) {
		int plotWidth, plotHeight;
		plotWidth = (int) this.getWidth();
		plotHeight = (int) this.getHeight();
		int fontSize;
		fontSize = (plotWidth < plotHeight) ? plotWidth : plotHeight;
		fontSize = ((int) (fontSize / 32) < 9) ? 9 : fontSize / 25;
		Font font = new Font("", Font.PLAIN, fontSize);
		g.setFont(font);
		g.setColor(Color.blue);
		String slopeTitle, rSquareTitle;
		if ((Double.toString(correlation)).length() > 7)
			slopeTitle = "Cor: "
					+ (Double.toString(correlation)).substring(0, 6);
		else
			slopeTitle = "Cor: " + Double.toString(correlation);

		if ((Double.toString(rSquare)).length() > 7)
			rSquareTitle = "rSquare: "
					+ (Double.toString(rSquare)).substring(0, 6);
		else
			rSquareTitle = "rSquare: " + Double.toString(rSquare);
		g.drawString(rSquareTitle, (int) (plotWidth / 2),
				(int) (plotHeight / 15));

		g.drawString(slopeTitle, 2, (int) (plotHeight / 15));

	}

	public JPopupMenu getPopup() {

		if (this.popup == null) {
			JPopupMenu popup = null;
			popup = new JPopupMenu();
			JMenuItem menuItem = new JMenuItem("Set Range");
			// menuItem.addActionListener(this);
			popup.add(menuItem);
			menuItem.addActionListener(new ActionListener() {

				/**
				 * put your documentation comment here
				 * 
				 * @param e
				 */
				public void actionPerformed(ActionEvent e) {
					showDialog(400, 400);
				}
			});
			menuItem = new JMenuItem("Edit");
			// menuItem.addActionListener(this);
			popup.add(menuItem);

			menuItem = new JMenuItem("Regression");
			popup.add(menuItem);
			menuItem.addActionListener(new ActionListener() {

				/**
				 * put your documentation comment here
				 * 
				 * @param e
				 */
				public void actionPerformed(ActionEvent e) {

					// toggle regression line
					plotLine = !plotLine;

					ScatterPlotBasic.this.paintDrawingBuff();
					repaint();
				}
			});

			menuItem = new JMenuItem("Regression For Selection");
			popup.add(menuItem);
			menuItem.addActionListener(new ActionListener() {

				/**
				 * put your documentation comment here
				 * 
				 * @param e
				 */
				public void actionPerformed(ActionEvent e) {
					plotLineForSelections = !plotLineForSelections;
					ScatterPlotBasic.this.paintDrawingBuff();
					repaint();
				}
			});

			this.popup = popup;
		}

		return popup;
	}

	public void setPopup(JPopupMenu popup) {
		this.popup = popup;
	}

}