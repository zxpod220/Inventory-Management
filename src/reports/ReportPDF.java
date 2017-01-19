package reports;

import java.awt.Color;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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

public class ReportPDF extends ReportMaster {
	/**
	 * PDF document variable
	 */
	private PDDocument doc;
	private final String DATETIME = "MM/dd/yyyy HH:mm:ss";
	
	private static Logger log = Logger.getLogger(ReportPDF.class);
	
	public ReportPDF(ReportGateway gw) {
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
			PropertyConfigurator.configure(new FileInputStream("log4j.properties"));
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
		
		// get current time
		DateFormat dateFormat = new SimpleDateFormat( DATETIME );
		Calendar cal = Calendar.getInstance();
		String current_time =  "Report created on: "+ (dateFormat.format(cal.getTime())); //2014/08/06 16:00:22
		
		//prep the report page 1
		doc = new PDDocument();
		PDPage page = new PDPage();
		PDRectangle rect = page.getMediaBox();
		
		doc.addPage(page);
		page.setRotation(90);
		//get content stream for page 1
		PDPageContentStream content = null;
		
		//prep the fonts
		PDFont fontPlain = PDType1Font.HELVETICA;
		PDFont fontBold = PDType1Font.HELVETICA_BOLD;
		PDFont fontItalic = PDType1Font.HELVETICA_OBLIQUE;
		PDFont fontMono = PDType1Font.COURIER;
		PDFont fontMonoBold = PDType1Font.COURIER_BOLD;
		
		int record_each_page = 21;
		
		float pageWidth  = rect.getWidth();
		float pageHeight = rect.getHeight();
		
		//same margin all around document
		float marginX = pageWidth - 550;
		float marginY = pageHeight - 250;
		
		int font_size = 17;

		int pageCount = 0;
		
		try {
			
			content = new PDPageContentStream(doc, page);
			
			// convert all characters to lanscape
			content.concatenate2CTM(0, 1, -1, 0, pageWidth, 0);
			//print header of page 1
			//draw 50 point height grey stripe across top of page
			content.setNonStrokingColor(Color.LIGHT_GRAY);
			content.setStrokingColor(Color.BLACK);
			
			// fill for rectangle
			content.addRect( marginX - 10, marginY - 10, pageWidth + 85, 50);
			content.fillAndStroke();

			//reset non-stroking color
			content.setNonStrokingColor(Color.BLACK);

			//print report title
			content.setFont(fontBold, 45);
			content.beginText();
			content.newLineAtOffset(marginX, marginY);
			content.showText("Warehouse Inventory Summary");
			content.endText();

			
			//get startingY for data (col header first then data rows)
			content.setFont(fontMonoBold, font_size);
			float dataY = marginY - 50;
			
			//sketch the layout of the columns in the report
			//Warehouse Name 		Part # 		Part Name 		Quantity		Unit
			float colX_0 = marginX + 5 ; //warehouse name
			float colX_1 = colX_0 + 170; //part #
			float colX_2 = colX_1 + 125; //part name
			float colX_3 = colX_2 + 150; //quantity of inventory
			float colX_4 = colX_3 + 130; //Unit
			
			//print column headings
			content.beginText();
			content.newLineAtOffset(colX_0, dataY);
			content.showText("Warehouse Name");
			content.endText();
			
			content.beginText();
			content.newLineAtOffset(colX_1, dataY);
			content.showText("Part #");
			content.endText();
			
			content.beginText();
			content.newLineAtOffset(colX_2, dataY);
			content.showText("Part Name");
			content.endText();
			
			content.beginText();
			content.newLineAtOffset(colX_3, dataY);
			content.showText("Quantity");
			content.endText();
			
			content.beginText();
			content.newLineAtOffset(colX_4, dataY);
			content.showText("Unit");
			content.endText();
			
			//print report rows
			content.setFont(fontMono, font_size);
			int counter = 1;
			
			int size = records.size();
			
			log.trace("Writing record "+ size +" to report");
			
			log.info("Record "+record_each_page+" will cause a page break");
			
			int m = 0;
			
			for(int i=0; i < size; i++) {
				
				// record to trace
				String record = records.get(i).get("warehouse_name")+" | "+ records.get(i).get("Part_Number")+" | "+ records.get(i).get("Part_name")+" | "+ records.get(i).get("quantity")+" | "+ records.get(i).get("Unit_of_Quantity");
				log.debug(record);
				//y offset for the current row
				int k = counter%record_each_page;
				
			
				float offset;// = dataY - (k * (fontMono.getHeight(12) + 20));
				
				if( counter > 10 && counter%record_each_page == 0){
					
					// print time at bottom of page
					
					content.beginText();
					content.newLineAtOffset(25,25);
					content.showText( current_time );
					content.endText();
					
					// print page number
					
					content.beginText();
					content.newLineAtOffset( pageWidth + 90,25);
					content.showText(  "Page " + ( ++pageCount) );
					content.endText();
					
					
					content.close();	// close previous page
					
					log.info("Page "+pageCount+" created");
					
					//get another page
					page = new PDPage();
					doc.addPage(page);
					page.setRotation(90);
					content = new PDPageContentStream(doc, page);
					
					// convert all characters to lanscape
					content.concatenate2CTM(0, 1, -1, 0, pageWidth, 0);
					content.setFont(fontMono, font_size);
					m = 65;
					
					offset = dataY + m - (k * (fontMono.getHeight(12) + 20) );
					
				}else{
					offset = dataY + m - (k * (fontMono.getHeight(12) + 20));
				}
				
					
				
				content.beginText();
				content.newLineAtOffset(colX_0, offset);
				content.showText( records.get(i).get("warehouse_name") );
				content.endText();
				
				content.beginText();
				content.newLineAtOffset(colX_1, offset);
				content.showText( records.get(i).get("Part_Number") );
				content.endText();
				
				content.beginText();
				content.newLineAtOffset(colX_2, offset);
				content.showText( records.get(i).get("Part_name") );
				content.endText();
				
				content.beginText();
				content.newLineAtOffset(colX_3, offset);
				content.showText( records.get(i).get("quantity") );
				content.endText();
				
				content.beginText();
				content.newLineAtOffset(colX_4, offset);				
				content.showText( records.get(i).get("Unit_of_Quantity") );
				content.endText();			
				
				//move to next row
				counter++;
				
			}
			
		} catch (IOException e) {
			throw new ReportException("Error in report generation: " + e.getMessage());
		} finally {
			//close last page
			try {
				
				content.beginText();
				content.newLineAtOffset(25,25);
				content.showText( current_time );
				content.endText();
				
				// print page
				// print page number
				
				content.beginText();
				content.newLineAtOffset( pageWidth + 90,25);
				content.showText(  "Page " + ( ++pageCount) );
				content.endText();				
				content.close();
				
				log.info("Page "+pageCount+" created");
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		
	}
	
	/**
	 * write PDF document to file
	 */
	@Override
	public void outputReportToFile(String fileName) throws ReportException {
		//Save the results and ensure that the document is properly closed:
		try {
			doc.save(fileName);
			log.info("Writing report to file");
		} catch (IOException e) {
			throw new ReportException("Error in report save to file: " + e.getMessage());
		}
	}

	@Override
	public void close() {
		super.close();
		try {
			doc.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
