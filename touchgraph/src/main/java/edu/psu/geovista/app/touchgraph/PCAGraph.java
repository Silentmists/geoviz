/*
 * Frank Hardisty
 */
package edu.psu.geovista.app.touchgraph;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.EventListenerList;

import edu.psu.geovista.app.touchgraph.graphelements.VisibleLocality;
import edu.psu.geovista.common.data.DataSetForApps;
import edu.psu.geovista.common.event.ColorArrayEvent;
import edu.psu.geovista.common.event.IndicationEvent;
import edu.psu.geovista.common.event.SelectionEvent;
import edu.psu.geovista.common.event.SubspaceEvent;
import edu.psu.geovista.common.event.SubspaceListener;
import edu.psu.geovista.data.DescriptiveStatistics;
import edu.psu.geovista.data.sample.GeoData48States;
import edu.psu.geovista.symbolization.ColorInterpolator;


/** GLPanel contains code for adding scrollbars and interfaces to the TGPanel
  * The "GL" prefix indicates that this class is GraphLayout specific, and
  * will probably need to be rewritten for other applications.
  *
  * @author   Alexander Shapiro
  * @version  1.20
  */
public class PCAGraph extends LinkGraph implements ActionListener{
    JButton sendButton = new JButton("Send Factors");

    /** Default constructor.
      */
    public PCAGraph() {
        super();
        
        this.hideVC();

        JPanel modePanel = this.getModeSelectPanel();
        modePanel.add(sendButton);
        sendButton.addActionListener(this);
        this.tgPanel.setFireIndications(false);
        this.tgPanel.setFireSelections(false);
    }

    public void actionPerformed(ActionEvent e) {
        //button was pressed
        //int[] sel = this.tgPanel.findSelection();


        //let's be clever and reorder this by distance from the currently
        //selected variable
        VisibleLocality loc = (VisibleLocality) this.tgPanel.getGES();
        Edge[] eds = loc.getAllEdges();
        Arrays.sort(eds);
        Vector nodes = this.findNodeOrder(eds,this.tgPanel.getSelect());
        int[] sel = new int[nodes.size()];
        for (int i = 0; i < nodes.size(); i++){
          Node no = (Node)nodes.get(i);
          String id = no.getID();
          Integer valInt = new Integer(id);
          int nodeNum = valInt.intValue();
          sel[i] = nodeNum;
        }
        this.fireSubspaceChanged(sel);
    }

    private Vector findNodeOrder(Edge[] eds, Node selNode) {
        Vector returnVec = new Vector();

        Vector sortedEds = new Vector(eds.length);

        for (int i = 0; i < eds.length; i++) {
            sortedEds.add(eds[i]);
        }

        returnVec.add(selNode);

        Vector nodesToFind = new Vector();
        nodesToFind.add(selNode);

        Node currNode = selNode;

        while (returnVec.size() < eds.length+1) {//mst always has one more node than edge


            Edge ed = this.findAndRemoveFirstEdge(sortedEds, currNode);

            if (ed == null) {
                nodesToFind.remove(currNode);
                currNode = (Node) nodesToFind.get(nodesToFind.size() - 1); //last element
            } else { //found an edge containing that node

                Node nextNode = null;

                if (ed.getTo() == currNode) {
                    nextNode = ed.getFrom();
                } else {
                    nextNode = ed.getTo();
                }
                nodesToFind.add(nextNode);
                returnVec.add(nextNode);
                currNode = (Node) nodesToFind.get(nodesToFind.size() - 1); //last element
            }
        }//loop

        return returnVec;
    }

    private Edge findAndRemoveFirstEdge(Vector v, Node n) {
        for (Enumeration e = v.elements(); e.hasMoreElements();) {
            Edge ed = (Edge) e.nextElement();
            Node to = ed.getTo();
            Node from = ed.getFrom();

            if ((n == to) || (n == from)) {
                v.remove(ed);

                return ed;
            }
        }

        return null;
    }

    public void processAllVariables(int nFactors) {
        for (int i = 0; i < gvEdges.length; i++) {
            tgPanel.deleteEdge(gvEdges[i]);
        }
        int nOriginalVars = data.getNumberNumericAttributes() -nFactors;
        double[] correlations = new double[nFactors * nOriginalVars];
        int[] fromCorr = new int[correlations.length];
        int[] toCorr = new int[fromCorr.length];
       
        
        int nEdges = nFactors * nOriginalVars;
        gvEdges = new Edge[nEdges];
        
        float criterion = 0.7f;
        int counter = 0;
        for (int i = 0; i < nOriginalVars; i++){

        	for(int j = nOriginalVars; j < data.getNumberNumericAttributes(); j++){

        		Node from = gvNodes[i];
        		Node to = gvNodes[j];
        		
        		double[] datafrom = data.getNumericDataAsDouble(i);
        		double[] datato = data.getNumericDataAsDouble(j);
                double length = DescriptiveStatistics.correlationCoefficient(datafrom, datato, false);
                length = 1-Math.abs(length);
                correlations[counter] = length;
                fromCorr[counter] = i;
                toCorr[counter] = j;

                int dist = (int) (length * length * 1000d);

                if (dist < 10) {
                    dist = 10; //otherwise component might blow up.
                }
                Edge e = new Edge(from, to, dist);
        		
        		
                e.setColor(Color.cyan);

                //Edge e = new Edge(from,to);
                gvEdges[counter] = e;
                if (length >= criterion){
                	tgPanel.addEdgeToEltSet(e);
                }
                counter++;
        	}
        }
        


        //for (int i = 0; i < nEdges; i++){

        Node selNode = gvNodes[1];
        tgPanel.setLocale(selNode, localityScroll.getRadius());
        tgPanel.setSelect(selNode);
        //}  
    }

    public void processSelectedVariables() {
      //I can't think of any reason for subspace linkgraph to do this, so i'm
      //making this a no-op for now fah 17 may 03

      //what we should do here:
      //make the first variable selected
      //fire the set as a subspace
      int[] attList = this.plottedAttributes;


      super.setSelectedNode(attList[0]);
      this.fireSubspaceChanged(attList);

    }

    public void colorArrayChanged(ColorArrayEvent e) {
        Color[] colors = e.getColors();
        if (colors == null){
          return;
        }
        if (colors.length == 0) {
            return;
        }

        //this.setColors(colors);


    }

    // let's set colors
    public void setColors(Color[] colors) {
        if (data == null) {
            return;
        }

        for (int i = 0; i < data.getNumObservations(); i++) {
            gvNodes[i].setBackColor(colors[i]);
            Node.setNodeBackDefaultColor(colors[i]);
            gvNodes[i].setNodeBackFixedColor(colors[i]);
        }

        //calling all edges
        Edge[] allEdges = this.tgPanel.getAllEdges();

        for (int i = 0; i < allEdges.length; i++) {
            Edge e = allEdges[i];
            Color colOne = e.getFrom().getBackColor();
            Color colTwo = e.getTo().getBackColor();

            allEdges[i].setColor(ColorInterpolator.mixColorsRGB(colOne, colTwo));
        }

        this.repaint();
    }



    //this method for graphing attributes
    public void setDataSet(DataSetForApps data, int nFactors) {
        //first blast old graph
        this.tgPanel.clearAllFull();

        this.data = data;
        attributesDisplay = data.getAttributeNamesNumeric();
        attList = new JList(this.attributesDisplay);

        gvNodes = new Node[data.getNumberNumericAttributes()];

        for (int i = 0; i < data.getNumberNumericAttributes(); i++) {
            String id = String.valueOf(i);

            //Node n = new Node(id,Node.TYPE_RECTANGLE,Color.white,obsNames[i]);
            Node n = new Node(id, attributesDisplay[i]);
            if (i < data.getNumberNumericAttributes()-nFactors){
            	n.setBackColor(Color.cyan);
            } else{
            	n.setBackColor(Color.pink);
            }
            
            gvNodes[i] = n;
        }

        try {
            for (int i = 0; i < data.getNumberNumericAttributes(); i++) {
                Node n = gvNodes[i];
                tgPanel.addNodeToEltSet(n);
                gvNodes[i] = n;
            }
        } catch (TGException tge) {
            System.err.println(tge.getMessage());
            tge.printStackTrace(System.err);
        }

        int numAtts = data.getNumberNumericAttributes();

        if (numAtts >= 3) {
            int[] plottedAtt = { 1, 2 };
            this.plottedAttributes = plottedAtt;
        } else if (numAtts == 2) {
            int[] plottedAtt = { 1, 2 };
            this.plottedAttributes = plottedAtt;
        } else if (numAtts == 1) {
            int[] plottedAtt = { 1 };
            this.plottedAttributes = plottedAtt;
        } else if (numAtts == 1) {
            plottedAttributes = new int[0];
        }

        gvEdges = new Edge[0];
        processAllVariables(nFactors);

        //int[][] mst = nDMST.getMSTIndexes();
        //tgPanel.setLocale(tgPanel.getGES().getFirstNode(),localityScroll.getRadius());
        //tgPanel.setSelect(tgPanel.getGES().getFirstNode()); //Select first node, so hiding works
    }

    public void selectionChanged(SelectionEvent e) {
        //int[] sel = e.getSelection();
    }

    public void indicationChanged(IndicationEvent e) {
    }

    /**
     * adds an SubspaceListener
     */
    public void addSubspaceListener(SubspaceListener l) {
        listenerList.add(SubspaceListener.class, l);

    }

    /**
     * removes an SubspaceListener from the component
     */
    public void removeSubspaceListener(SubspaceListener l) {
        listenerList.remove(SubspaceListener.class, l);

    }

    /**
     * Notify all listeners that have registered interest for
     * notification on this event type. The event instance
     * is lazily created using the parameters passed into
     * the fire method.
     * @see EventListenerList
     */
    public void fireSubspaceChanged(int[] selection) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        SubspaceEvent e = null;

        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == SubspaceListener.class) {
                // Lazily create the event:
                if (e == null) {
                    e = new SubspaceEvent(this, selection);
                }

                ((SubspaceListener) listeners[i + 1]).subspaceChanged(e);
            }
        }
         //next i
    }

    public static void main(String[] args) {
        JFrame frame;
        frame = new JFrame("Graph Layout");

        PCAGraph glPanel = new PCAGraph();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.getContentPane().add("Center", glPanel);
        frame.setSize(500, 500);
        frame.setVisible(true);

        GeoData48States data = new GeoData48States();
        glPanel.setDataSet(data.getDataForApps());
    }
}
 // end com.touchgraph.graphlayout.GLPanel
