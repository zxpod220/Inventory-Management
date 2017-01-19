package reports;

import java.awt.Color;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import database.GatewayException;

/**
 * Output owner dog report as Excel spreadsheet using StringBuilder
 * @author Dominik Garcia xzb387
 *
 */
public class ReportExcel extends ReportMaster {
	/**
	 * Excel document variable
	 * 
	 */
	private StringBuilder doc;
	
	private static Logger log = Logger.getLogger(ReportExcel.class);
	
	public ReportExcel(ReportGateway gw) {
		super(gw);
		
		//pdfbox uses log4j so we need to run a configurator
		BasicConfigurator.configure();
		
		//init doc
		doc = null;
	}

	@Override
	public void generateReport() throws ReportException {
		//declare data variables
		
		try {
			PropertyConfigurator.configure(new FileInputStream("XLS.log4j.properties"));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		log.info("Report creation started for PDF");

		log.warn("No inventory records found for report yet");
		
		List< HashMap<String, String> > records = null;
		try {
			records = gateway.fetchInventory();
		} catch(GatewayException e) {
			throw new ReportException("Error in report generation: " + e.getMessage());
		}
		
		//prep the report page 1
		doc = new StringBuilder();
		
		doc.append("Warehouse Inventory Summary\n\n");
			
		doc.append("Warehouse Name\t");
		doc.append("Part #\t");
		doc.append("Part Name\t");
		doc.append("Quantity\t");
		doc.append("Unit\n");

		int size = records.size();
		
		log.trace("Writing record "+ size +" to report");
				
		for(int i=0; i < size; i++) {
			
			String record = records.get(i).get("warehouse_name")+" | "+ records.get(i).get("Part_Number")+" | "+ records.get(i).get("Part_name")+" | "+ records.get(i).get("quantity")+" | "+ records.get(i).get("Unit_of_Quantity");
			log.debug(record);
			
			doc.append( records.get(i).get("warehouse_name") + "\t");
			doc.append(records.get(i).get("Part_Number") + "\t");
			doc.append(records.get(i).get("Part_name") + "\t");
			doc.append(records.get(i).get("quantity") + "\t");			
			doc.append(records.get(i).get("Unit_of_Quantity") + "\n");
		}
	}
	
	/**
	 * write Excel report to file
	 */
	@Override
	public void outputReportToFile(String fileName) throws ReportException {
		//Save the results and ensure that the document is properly closed:
		try(PrintWriter out = new PrintWriter(fileName)){
			out.print(doc.toString());
			log.info("Writing report to file");
		} catch (IOException e) {
			throw new ReportException("Error in report save to file: " + e.getMessage());
		}
	}

	@Override
	public void close() {
		super.close();
	}
	
}
