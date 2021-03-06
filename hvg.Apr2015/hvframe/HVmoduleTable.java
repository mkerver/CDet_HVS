// Last update: 10 Feb 2014
//***********************************************************************************
// Add&Modd: 08-Oct-01 : create PopUp menu, add editCell method for edit 
//                       channels parameters from popup menu
// 17-Jan-02 : add methods for highlight channel cell in red color that have set alarm
// 16-Sep-05 : add method 'setValueAtColumn()' set value in all rows for one selected column in TableModel class
// 13-Dec-2011 : replaced SwingWorker to hvtools.SwingWorker due to new java version has this class as standard.
// 10-Feb-2014 : add  NullPointerException in MyTableModel::setdata().
//
//             
package hvframe;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import javax.swing.DefaultCellEditor;

import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.awt.geom.*;
import java.awt.Dimension;
import java.util.*;
import java.awt.Color;
import java.text.*;
import javax.swing.text.*;
import javax.swing.*;
import java.awt.Toolkit;
import java.beans.*; //Property change stuff
import java.lang.Double;


import hvtools.*;

/* HVmoduleTable - implements table(JTable) and methods to present and edit channels data
 *
 * @version 1.2
 * Last update: 10-Feb-2014
 */
public class HVmoduleTable extends JPanel implements Printable, ActionListener {

   // private boolean true = false;
    private boolean DEBUG = true;
    public HVmodule m = null;
    public JTable hvtable = null;
    MyTableModel myModel = null;
    public JTextArea statArea = null;
    final int TFONTSIZE = 16;
    final int SFONTSIZE = 16;
    JPopupMenu popup;
    JMenuItem menuItem;
    static final Color mycolor = new Color(240,245,245);
    static final Color alarmBgColor = Color.red.brighter();
    static final Color defaultBgColor = Color.white;
    Color defaultBgSelectionColor = Color.white;
    
    public HVmoduleTable(HVmodule m) {
        super(false);
	this.m = m;
	
        myModel = new MyTableModel();
        hvtable = new JTable(myModel);
	myModel.addTableModelListener(hvtable);
	hvtable.setFont(new Font("Hevletica", Font.PLAIN, 16));
        hvtable.setPreferredScrollableViewportSize(hvtable.getSize());
	//hvtable.setColumnSelectionAllowed(true);
	hvtable.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
	hvtable.setCellSelectionEnabled(true);
	defaultBgSelectionColor = hvtable.getSelectionBackground(); // store default color for selection
	//hvtable.getColumnModel.setColumnSelectionAllowed(true);
	//hvtable.setRowSelectionAllowed(false);

        //DefaultTableCellRenderer tableRenderer = new DefaultTableCellRenderer();
	//tableRenderer.setHorizontalAlignment(JLabel.RIGHT);

	//hvtable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN
	// hvtable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
	//hvtable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
	hvtable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	//	  hvtable.sizeColumnsToFit(-1);

      //Create the scroll pane and add the table to it. 
        JScrollPane scrollPane = new JScrollPane(hvtable);

	//Set the amount of empty space between cells
        //Set up real input validation(editor) for the edited columns
	// and setup renderers(color,size) for edited columns;
	setColumnEditors(hvtable);
	//hvtable.setRowMargin(3+hvtable.getRowMargin());

        //Create the popup menu.
        popup = new JPopupMenu();
        menuItem = new JMenuItem("Edit");
        menuItem.setActionCommand("edit_C");
        menuItem.addActionListener(this);
        popup.add(menuItem);
        menuItem = new JMenuItem("Cancel");
        menuItem.setActionCommand("cancel_C");
        menuItem.addActionListener(this);
        popup.add(menuItem);

        //Add listener to components that can bring up popup menus.
        MouseListener popupListener = new PopupListener();
        hvtable.addMouseListener(popupListener);
    

	setLayout(new GridLayout(1, 1)); 
        //Add the scroll pane to this window.
	//	add(scrollPane, BorderLayout.CENTER);


	JTextArea statArea = new JTextArea("End initialization");
	statArea.setFont(new Font("Serif", Font.PLAIN, SFONTSIZE));
        statArea.setLineWrap(true);
        statArea.setWrapStyleWord(true);
	JScrollPane areaScrollPane = new JScrollPane(statArea);
	areaScrollPane.setVerticalScrollBarPolicy(
		       JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        areaScrollPane.setPreferredSize(new Dimension(800, 60));
        areaScrollPane.setBorder(
	    BorderFactory.createCompoundBorder(
		  BorderFactory.createCompoundBorder(
			        BorderFactory.createTitledBorder("Status"),
                                BorderFactory.createEmptyBorder(2,2,2,2)),
                areaScrollPane.getBorder()));
	
	//	add(areaScrollPane, BorderLayout.CENTER);

	BoxLayout tBox = new BoxLayout(this, BoxLayout.Y_AXIS);
        this.setLayout(tBox);
        this.add(scrollPane);
	//        this.add(areaScrollPane);

	//	setPreferredSize(hvtable.getSize());
	//	setVisible(true);
    }

    // methods

    private void setEcolorCell(JTable table) {
	int column = table.getColumnCount();
	Color mycolor = new Color(240,245,24);
 	for (int i=0;i<column;i++) {
	    if (table.isCellEditable(0,i)) {
		TableColumn tableColumn = table.getColumn(table.getColumnName(i));
	       // Set background and tooltip for the Color column renderer.
		//DefaultTableCellRenderer tableColumnRenderer = new DefaultTableCellRenderer();
		//tableColumnRenderer.setBackground(mycolor);
		//  tableColumnRenderer.setToolTipText("Input Voltage value");

		//tableColumn.setCellRenderer(tableColumnRenderer);

        // Set a tooltip for the header of the colors column.
	//        TableCellRenderer headerRenderer = tableColumn.getHeaderRenderer();
        //if (headerRenderer instanceof DefaultTableCellRenderer)
        //    ((DefaultTableCellRenderer)headerRenderer).setToolTipText("Demand Voltage")
;
	    }
	}
    }
    
    private void setColumnEditors(JTable table) {
	NumberFormat valueFormat;
	valueFormat = NumberFormat.getNumberInstance(Locale.US) ;
	valueFormat.setMaximumFractionDigits(1);
	//valueFormat.setMinimumFractionDigits(0);
	//valueFormat.setParseIntegerOnly(false);	
	//((DecimalFormat)valueFormat).setPositiveSuffix(" ");
	
   	for(int i=1; i<table.getColumnCount();i++) {
	    if(table.isCellEditable(0,i)) {		
		if(!m.pattr[i-1].getName().equals("CE")) {
		    double maxval = m.pattr[i-1].getMaxRange();
		    double minval = m.pattr[i-1].getMinRange();
		 final DecimalField doubleField = new DecimalField(minval,maxval,10,valueFormat);
		    doubleField.setHorizontalAlignment(DecimalField.RIGHT);
		    doubleField.setFont(new Font("Serif", Font.PLAIN, TFONTSIZE));		    
		    DefaultCellEditor doubleEditor = 
			new DefaultCellEditor(doubleField) {
			    //Override DefaultCellEditor's getCellEditorValue method
			    //to return an Double, not a String:
			    public Object getCellEditorValue() {
				double ret = doubleField.getValue();
				return new String().valueOf(ret);
			    }
			};

		    TableColumn tColumn = table.getColumnModel().getColumn(i);
		    tColumn.setCellEditor(doubleEditor);
		    DefaultTableCellRenderer tColumnRenderer = new DefaultTableCellRenderer();
		    tColumnRenderer.setBackground(mycolor);
		    tColumnRenderer.setForeground(Color.blue);
		    tColumnRenderer.setHorizontalAlignment(JLabel.RIGHT);
		    //		    tColumnRenderer.setFont(new Font("Serif", Font.PLAIN, TFONTSIZE));

		    tColumn.setCellRenderer(tColumnRenderer);

		}
	    }
	}
	
    }

    public synchronized  void setCellBackground(int row, int col, Color c) {
	TableColumn column = hvtable.getColumnModel().getColumn(col);	
	Component comp = hvtable.getCellRenderer(row, col).
	    getTableCellRendererComponent(
					  hvtable, null ,
					  false, false, row, col);
	comp.setBackground(c);
	//comp.revalidate();
	myModel.fireTableCellUpdated(row, col);	
	}
    
    
    public synchronized Color getCellBackground(int row, int col) {	
	Component comp = hvtable.getCellRenderer(row,col).
	    getTableCellRendererComponent(
					      hvtable, null ,
					      false, false, row, col);
	return comp.getBackground();
    }
    

    public synchronized  void restoreSelectionBackground() {
 	hvtable.setSelectionBackground(defaultBgSelectionColor);
    }


    public synchronized  void changeCellSelection(int row, int col) {
	hvtable.changeSelection(row,col,false,false);
    }

    public synchronized  void setCellSelected(int row, int col, Color selcol) {
	Color newc;

	TableColumn column = hvtable.getColumnModel().getColumn(col);	
	Component comp = hvtable.getCellRenderer(row, col).
	    getTableCellRendererComponent(
					  hvtable, null ,
					  true, false, row, col);
	
	newc = alarmBgColor;
	
	//set alarm color
	hvtable.setSelectionBackground(selcol);
        //If the specified cell is selected, deselect it. If it is not selected, select it. 
	if(!hvtable.isCellSelected(row,col)) {
	    hvtable.changeSelection(row,col,false,false);
	} else {
	    hvtable.changeSelection(row,col,true,false);
	}

	//myModel.fireTableCellUpdated(row, col);
    }
    

    public boolean getBit(int val, int bit) {
	return (((val >> bit)&1) == 1) ? true : false ;
    }

    public int print(Graphics g, PageFormat pageFormat, int pageIndex) throws PrinterException {
	
	System.out.println("Page:"+pageIndex);
     	Graphics2D  g2 = (Graphics2D) g;
        g2.setColor(Color.black);
        int fontHeight=g2.getFontMetrics().getHeight();
        int fontDesent=g2.getFontMetrics().getDescent();
     	double pageHeight = pageFormat.getImageableHeight() - fontHeight;
     	double pageWidth = pageFormat.getImageableWidth();
      	double tableWidth = (double) hvtable.getColumnModel().getTotalColumnWidth();
        double paneWidth = (double) this.getWidth();
        double scale = 1; 
        if (tableWidth >= pageWidth) {
                scale =  pageWidth / tableWidth;
        }

        double headerHeightOnPage=
                      hvtable.getTableHeader().getHeight()*scale;
        double tableWidthOnPage=tableWidth*scale;

        double oneRowHeight=(hvtable.getRowHeight()+ hvtable.getRowMargin())*scale;

        int numRowsOnAPage=(int)(( pageHeight-headerHeightOnPage)/oneRowHeight);
        double pageHeightForTable=oneRowHeight*numRowsOnAPage;
        int totalNumPages= (int)Math.ceil((
                      (double)hvtable.getRowCount())/numRowsOnAPage);

        //if(pageIndex>=totalNumPages) {
	    //                      return NO_SUCH_PAGE;
	// }

        g2.translate(pageFormat.getImageableX(), 
                       pageFormat.getImageableY());

        DateFormat df = DateFormat.getDateInstance();
	String dateString = df.getDateTimeInstance().format(new Date());
	String outstr = new String("Page. " +(pageIndex+1) +". "+m.name +" settings.  " + dateString);
	int strshift = g2.getFontMetrics().stringWidth(outstr);
        g2.drawString(outstr, (int)pageWidth/2 - strshift/2,
                      (int)(pageHeight+fontHeight-fontDesent));//bottom center



	if(false) {
	g2.translate(0f,headerHeightOnPage);
	g2.translate(0f,-pageIndex*pageHeightForTable);
        //If this piece of the table is smaller than the size available,
        //clip to the appropriate bounds.
	if(false) {
	//        if (pageIndex + 1 == totalNumPages) {
                     int lastRowPrinted = numRowsOnAPage * pageIndex;
                     int numRowsLeft = hvtable.getRowCount() - lastRowPrinted;
                     g2.setClip(0, (int)(pageHeightForTable * pageIndex),
                       (int) Math.ceil(tableWidthOnPage),
                       (int) Math.ceil(oneRowHeight * numRowsLeft));
        }
        //else clip to the entire area available.
        else{    
        
            g2.setClip(0, (int)(pageHeightForTable*pageIndex), 
                     (int) Math.ceil(tableWidthOnPage),
                     (int) Math.ceil(pageHeightForTable));        
        }
	}

        g2.scale(scale,scale);
	
	hvtable.paint(g2);
	g2.scale(1/scale,1/scale);
	g2.translate(0f,pageIndex*pageHeightForTable);
	g2.translate(0f, -headerHeightOnPage);
	g2.setClip(0, 0,(int) Math.ceil(tableWidthOnPage), 
		   (int)Math.ceil(headerHeightOnPage));
    	g2.scale(scale,scale);
	hvtable.getTableHeader().paint(g2);//paint header at top
	
        return Printable.PAGE_EXISTS;
    }

    public void actionPerformed(ActionEvent e) {
    
        if (e.getActionCommand().equals("edit_C")) {
	    editCell();
	    if(false) {
		final hvtools.SwingWorker worker = new hvtools.SwingWorker() {
			public Object construct() {
			    editCell();
			    return "All done"; //return value not used by this program
			}
			//Runs on the event-dispatching thread.
			public void finished() {
			    hvtable.clearSelection();
			}
		    };
		worker.start();  //required for SwingWorker 3
	    }
	
	 } 
	if (e.getActionCommand().equals("cancel_C")) {
	    hvtable.clearSelection();
	}
    }
    
    /**
     * Shows dialog window to edit selected channels parameters from popup menu.
     *
     */
    public void editCell() {
	
	final int nscol =  hvtable.getSelectedColumnCount();
	final int nsrow =  hvtable.getSelectedRowCount();

	if (nscol==1) {

                        
	   final int[] col = hvtable.getSelectedColumns();
	   final int[]  rows = hvtable.getSelectedRows();

	    if(hvtable.isCellEditable(rows[0],col[0])) {		
		int nchn = m.nchn; // number of channels in module
		System.out.println("CHN:"+nchn+" Rows:"+nsrow);
		int icol = col[0];

		if(!m.pattr[icol-1].getName().equals("CE")) {
		    double maxval = m.pattr[icol-1].getMaxRange();
		    double minval = m.pattr[icol-1].getMinRange();

		    NumberFormat valueFormat;	
		    valueFormat = NumberFormat.getNumberInstance(Locale.US) ;
		    valueFormat.setMaximumFractionDigits(1);
		    final DecimalField doubleField = new DecimalField(minval,maxval,10,valueFormat);
		    doubleField.setHorizontalAlignment(DecimalField.RIGHT);
		    doubleField.setFont(new Font("Serif", Font.PLAIN, TFONTSIZE));
		    doubleField.setValue( Double.parseDouble((String)hvtable.getValueAt(rows[0],col[0])) );
		    final String s;
		    Object[] inpField = {"input value", doubleField};

		    JFrame frame = new JFrame();
		    final JOptionPane optionPane = new JOptionPane(inpField,
						       JOptionPane.QUESTION_MESSAGE,
						       JOptionPane.OK_CANCEL_OPTION);
	
		    final JDialog dialog = optionPane.createDialog(hvtable,"Input Dialog"); 
		    // final JDialog dialog = new JDialog(frame, 
		    //				       "Input Dialog",
		    //				       true);
		    dialog.setContentPane(optionPane);
		    //		    dialog.setDefaultCloseOperation(
		    //			JDialog.DO_NOTHING_ON_CLOSE);
		    //dialog.addWindowListener(new WindowAdapter() {
		    //		    public void windowClosing(WindowEvent we) {
		    //			optionPane.setValue(new Integer(
		    //				    JOptionPane.CLOSED_OPTION));	
		     //     setLabel("Thwarted user attempt to close window.");
		    //	    }
		    //		});
		    //optionPane.addPropertyChangeListener(
		    //	         new PropertyChangeListener() {
		    //			 public void propertyChange(PropertyChangeEvent e) {
		    //			     String prop = e.getPropertyName();
		    //			     
		    //			     if (dialog.isVisible() 
		    //				 && (e.getSource() == optionPane)
		    //				 && (prop.equals(JOptionPane.VALUE_PROPERTY) ||
		    //				     prop.equals(JOptionPane.INPUT_VALUE_PROPERTY)))
		    //				 {
						     //If you were going to check something
						     //before closing the window, you'd do
						     //it here.
		    //				     dialog.setVisible(false);
		    //				 }
		    //			 }
		    //		     });
 			  	    
		    dialog.pack();
		    dialog.setVisible(true);
		    int value = ((Integer)optionPane.getValue()).intValue();
		    if (value == JOptionPane.OK_OPTION) {
			double d = doubleField.getValue();
			if((d<minval)||(d>maxval)) {
			    Toolkit.getDefaultToolkit().beep(); 
			    return;
			}
			s = new String().valueOf(d);	
			if (nchn==nsrow) {
			    myModel.setValueAtColumn(s , col[0]); // set value in column  
			} else {
			    for(int i = 0; i<nsrow;i++) {
				System.out.println("col:"+col[0]+" row:"+rows[i] + " input:"+ s);
				hvtable.setValueAt(s, rows[i], col[0]);
			    }
			}
			
			hvtable.clearSelection();
		    } else {
			hvtable.clearSelection();
		    }
		} else {
		    Object[] options = {"Enable",
					"Disable",
					"Cancel"};
		    int n = JOptionPane.showOptionDialog(hvtable,
							 "Select operation",
							 "Select Dialog",
							 JOptionPane.YES_NO_CANCEL_OPTION,
							 JOptionPane.QUESTION_MESSAGE,
							 null,
							 options,
							 options[2]);

		    if (n == JOptionPane.YES_OPTION) {
			if (nchn==nsrow) {
			    myModel.setValueAtColumn(new Boolean(true) , col[0]); // set value in column  
			} else {
			
			    for(int i = 0; i<nsrow;i++) {
				System.out.println("col:"+col[0]+" row:"+rows[i] + " input: Yes" );
				Boolean bval = (Boolean)hvtable.getValueAt( rows[i], col[0]);
				if(!bval.booleanValue()) 
				    hvtable.setValueAt(new Boolean(true) , rows[i], col[0]);
			    }
			}
		    }

		    if (n == JOptionPane.NO_OPTION) {
			if (nchn==nsrow) {
			    myModel.setValueAtColumn(new Boolean(false) , col[0]); // set value in column  
			} else {

			    for(int i = 0; i<nsrow;i++) {
				System.out.println("col:"+col[0]+" row:"+rows[i] + " input: NO");
				Boolean bval = (Boolean)hvtable.getValueAt( rows[i], col[0]);
				if(bval.booleanValue()) 
				    hvtable.setValueAt(new Boolean(false), rows[i], col[0]);
			    }
			}
		    }		    hvtable.clearSelection();
    
		}
		hvtable.clearSelection();
	    } 

    	} else { 
	    Toolkit.getDefaultToolkit().beep();
	}
    }
    
//---------------------------------------------------------------------------

    /**********************************************************
     *
     *   inner class table model
     *
     **********************************************************************/
    class MyTableModel extends AbstractTableModel {
	String[] columnNames = setcolumnNames();
	
	public String[] setcolumnNames() {
	    int np = m.numparam +1;	    
	    String[] s = new String[np];
	    System.out.println("NUMPARAM:"+np);

		s[0] = "Ch_name";		
	    for(int i=1;i<np;i++) {
		s[i] = m.pattr[i-1].getAttr("label");		
		System.out.println("Label:"+i+":"+s[i]);
	    }
	    return s;
	}
	
	Object[][] data = setdata();

	public Object[][] setdata() {
	    int nch = m.nchn ;
	    int np = m.numparam +1;	    
	    Object[][] ob = new Object[nch][np];
	    for(int i=0; i<nch; i++) {
		ob[i][0] = new Object();
		ob[i][0] = m.ch[i].id;
		for(int k=1;k<np; k++) {
		    ob[i][k] = new Object();
		    try { // *** 10 Feb 2014
			String p = m.pattr[k-1].name;
			String b = m.ch[i].getValue(p);

			if (p.equals("CE")) {
			    if(b.equals("1")) { 
				ob[i][k] = new Boolean(true);
			    } else {
				ob[i][k] = new Boolean(false);
			    }
			} else {  
			    ob[i][k] =(String)b;
			}
		    } catch (NullPointerException en) {
			System.out.println("Error : NullPointerException: in  MyTableModel::setdata() : "+m.name);	       
		    }
		}
	    }
	    return ob;
	}

	public Vector editableProperties = setEditableProperties();

	public Vector setEditableProperties() {
	    int np = m.numparam;
	    Vector v = new Vector();
	    for(int i=0; i< np ;i++) {
		String b = m.pattr[i].getAttr("protection");
		System.out.println("Prot:"+b);
		if (!b.equalsIgnoreCase("M")) {
		    v.addElement((String)m.pattr[i].name);
		    System.out.println("EditProp:"+m.pattr[i].name);
		}
	    }
	    return v;
	}

	public synchronized void updateColumn(int ind) {
	    int nch = m.nchn ;
	    System.out.println("UpdateColumn:"+ind);
	    for(int i=0; i<nch; i++) {
		String p = (String)m.par.get(ind-1);
		String b = m.ch[i].getValue(p);
		if (p.equals("CE")) {
		    if(b.equals("1")) { 
			data[i][ind] = new Boolean(true);
		    } else {
			data[i][ind] = new Boolean(false);
		    }
		} else {  
		    data[i][ind] =(String)b;
		}
		fireTableCellUpdated(i, ind);
	    }
	    //fireTableDataChanged();			
	}


        public synchronized int getColumnCount() {
            return columnNames.length;
        }
        
        public synchronized int getRowCount() {
            return data.length;
        }

        public synchronized String getColumnName(int col) {
            return columnNames[col];
        }

        public synchronized Object getValueAt(int row, int col) {
            return data[row][col];
        }

        /*
         * JTable uses this method to determine the default renderer/
         * editor for each cell.  If we didn't implement this method,
         * then the last column would contain text ("true"/"false"),
         * rather than a check box.
         */
/*
       public synchronized Class getColumnClass(int c) {
           System.out.println("column: " +c );
           return getValueAt(0, c).getClass();
       }
*/

        public synchronized Class getColumnClass(int c) {
		Object o = getValueAt(0, c);
		if (o != null) {
		    return o.getClass();
		}
		return Object.class;
        }

        /*
         * Don't need to implement this method unless your table's
         * editable.
         */

        public synchronized boolean isCellEditable(int row, int col) {
	    if (col > 0) {
		String b = m.pattr[col-1].name;
		if (editableProperties.contains(b)) {
		    return true;
		    //Note that the data/cell address is constant,
		    //no matter where the cell appears onscreen.
		} else {
		    return false;
		}
	    } else {
		return false;
	    }
	}
	/** 
	 *  sets value in table column. Swingworker is used for send command to client.
	 */
        public synchronized void setValueAtColumn(final Object value, final int col) {
	    if (true) {
		System.out.println("Setting value at " +col
				   + " to " + value
				   + " (an instance of " 
				   + value.getClass() + ")");
	    }
	    
	    int row_cnt=getRowCount();
	    Object[] oldval = new Object[row_cnt];
	    Component comp;
	    Component comp2;
	    // stop monitor

	    final Color[] oldbg = new Color[row_cnt];
		for(int row=0;row<row_cnt;row++) { 

		oldval[row] = getValueAt(row, col);
		
		comp = hvtable.getCellRenderer(row,col).
		    getTableCellRendererComponent(
					      hvtable, oldval[row] ,
					      true, true, row, col);
	    
		comp2 = hvtable.getCellEditor(row,col).
		    getTableCellEditorComponent(
						hvtable, oldval[row] ,
						true, row, col);
	    
		oldbg[row] = comp2.getBackground();
	    	    
		comp.setBackground(Color.yellow);
		//		comp2.setBackground(Color.yellow);
		
	    }

	    final hvtools.SwingWorker worker = new hvtools.SwingWorker() {
		    public Object construct() {
			String command = null;
			String id = m.lu;
			String prop = (String)m.par.get(col-1);
			int nchn=m.nchn;
			String mname = m.name;

			if (prop.equals("CE")) {
			    Boolean val = (Boolean)value;
			    if(val.booleanValue()) {
				command="";
				for(int i=0;i<nchn;i++) command=command+m.bl+"1";
			        command = "LD "+id+m.bl+prop +command;
				//value = new Boolean(true);
			    } else {
				command="";
				for(int i=0;i<nchn;i++) command=command+m.bl+"0";
			        command = "LD "+id+m.bl+prop +command;
				//value = new Boolean(false);
			    }
			} else {
				command="";
				for(int i=0;i<nchn;i++) command=command+m.bl+(String)value;
			    
			    command = "LD "+id+m.bl+prop +command;
			}
			
			if(m.hvcom.exec(command, mname)) {
			    // get response
			    String r=null;
			    while(!m.hvcom.isEmpty()) {
				r = m.hvcom.getResponse();
				// System.out.println(" resp:"+r);
			    }
			    // imitation only delay
			    			    
			    return new Object() ;
			} else return null;
			
		    }
		    
		    public void finished() {
			Object[] oldval = new Object[getRowCount()];
			Component comp;			
			Component comp2;
			for(int row=0;row<m.nchn;row++) {
			    data[row][col] = value;
			    fireTableCellUpdated(row, col);
			    
			    oldval[row] = getValueAt(row, col);

			     comp = hvtable.getCellRenderer(row,col).
				getTableCellRendererComponent(
							      hvtable, oldval[row] ,
							      true, true, row, col);
			     comp2 = hvtable.getCellEditor(row,col).
				 getTableCellEditorComponent(
							     hvtable, oldval[row] ,
							     true, row, col);
			    
			     comp.setBackground(mycolor);	    
			     //comp2.setBackground(oldbg[row]);
			     comp2.repaint();
			}	
			
			// resume monitor
		    }
		};
	    
	    worker.start();
	    
	    
	    if (true) {
		System.out.println("New value of data:");
		printDebugData();
	    }
        }



	/** sets value in table cell. Swingworker is used for send command to client.
	 */
        public synchronized void setValueAt(final Object value, final int row, final int col) {
	    if (true) {
		System.out.println("Setting value at " + row + "," + col
				   + " to " + value
				   + " (an instance of " 
				   + value.getClass() + ")");
	    }
	    
	    Object oldval = getValueAt(row, col);
	    final Component comp = hvtable.getCellRenderer(row,col).
		getTableCellRendererComponent(
					      hvtable, oldval ,
						  true, true, row, col);
	    
	    final Component comp2 = hvtable.getCellEditor(row,col).
		getTableCellEditorComponent(
					    hvtable, oldval ,
					    true, row, col);
	    
	    final Color oldbg = comp.getBackground();
	    
	    comp.setBackground(Color.yellow);

	    final hvtools.SwingWorker worker = new hvtools.SwingWorker() {
		    public Object construct() {
			String command = null;
			String id = m.ch[row].id;
			String mname = m.name;
			String prop = (String)m.par.get(col-1);
			if (prop.equals("CE")) {
			    Boolean val = (Boolean)value;
			    if(val.booleanValue()) {
			        command = "LD "+id+m.bl+prop +m.bl+"1";
				//value = new Boolean(true);
			    } else {
				command = "LD "+id+m.bl+prop +m.bl+"0";
				//value = new Boolean(false);
			    }
			} else {
			    command = "LD "+id+m.bl+prop +m.bl+(String)value;
			}
			
			if(m.hvcom.exec(command, mname)) {
			    // get response
			    String r=null;
			    while(!m.hvcom.isEmpty()) {
				r = m.hvcom.getResponse();
				// System.out.println(" resp:"+r);
			    }
			    // imitation only delay
			    			    
			    return new Object() ;
			} else return null;
			
		    }
		    
		    public void finished() {
			data[row][col] = value;
			comp.setBackground(mycolor);
			fireTableCellUpdated(row, col);	
			
		    }
		};
	    
	    worker.start();
	    
	    
	    // Runnable doSetValue = new Runnable() {
	    //    public void run() {
	    //	String command = null;
	    //	String id = m.ch[row].id;
	    //	String prop = (String)m.par.get(col-1);
	    //	if (prop.equals("CE")) {
	    //	    Boolean val = (Boolean)value;
	    //	    if(val.booleanValue()) {
	    //	        command = "LD "+id+m.bl+prop +m.bl+"1";
	    //		//value = new Boolean(true);
	    //	    } else {
	    //		command = "LD "+id+m.bl+prop +m.bl+"0";
	    //		//value = new Boolean(false);
	    //	    }
	    //	} else {
	    //	    command = "LD "+id+m.bl+prop +m.bl+(String)value;
	    //	}
	    //	
	    //	if(m.hvcom.exec(command,id)) {
	    //	    // get response
	    //	    String r=null;
	    //	    while(!m.hvcom.isEmpty()) {
	    //		r = m.hvcom.getResponse();
	    //		// System.out.println(" resp:"+r);
	    //	    }
	    //	//try {
	    //	//   Thread.sleep(1000);
	    //	//} catch (InterruptedException ei) {}
	    //	
	    //	    data[row][col] = value;
	    //	    comp.setBackground(null);
	    //	    fireTableCellUpdated(row, col);	
	    //	}
	    //    }
	    //};
	    //
	    //SwingUtilities.invokeLater(doSetValue);
	    
	    
	    if (true) {
		System.out.println("New value of data:");
		printDebugData();
	    }
        }

        private void printDebugData() {
            int numRows = getRowCount();
            int numCols = getColumnCount();

            for (int i=0; i < numRows; i++) {
                System.out.print("    row " + i + ":");
                for (int j=0; j < numCols; j++) {
                    System.out.print("  " + data[i][j]);
                }
                System.out.println();
            }
            System.out.println("--------------------------");
        }
    }


    class PopupListener extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                popup.show(e.getComponent(),
                           e.getX(), e.getY());
            }
        }
    }


    //    public static void main(String[] args) {
    //     TableEditDemo frame = new TableEditDemo();
    //    frame.pack();
    //    frame.setVisible(true);
    //    }
}





