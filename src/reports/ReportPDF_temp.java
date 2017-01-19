package reports;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import database.GatewayException;

public class ReportPDF_temp extends ReportMaster {
	/**
	 * PDF document variable
	 * @param gw
	 */
	private PDDocument doc;
	
	public ReportPDF_temp(ReportGateway gw) {
		super(gw);

		BasicConfigurator.configure();
		
		//init doc
		doc = null;
	}

	@Override
	public void generateReport() throws ReportException {
		//declare data variables
		List< HashMap<String, String> > records = null;
		try {
			records = gateway.fetchInventory();
		} catch(GatewayException e) {
			throw new ReportException("Error in report generation: " + e.getMessage());
		}
		
		//prep the report page 1
		doc = new PDDocument();
		PDPage page1 = new PDPage();
		PDRectangle rect = page1.getMediaBox();
		
		doc.addPage(page1);
		page1.setRotation(90);
		//get content stream for page 1
		PDPageContentStream content = null;
		
		//prep the fonts
		PDFont fontPlain = PDType1Font.HELVETICA;
		PDFont fontBold = PDType1Font.HELVETICA_BOLD;
		PDFont fontItalic = PDType1Font.HELVETICA_OBLIQUE;
		PDFont fontMono = PDType1Font.COURIER;
		PDFont fontMonoBold = PDType1Font.COURIER_BOLD;
		
	
		float pageWidth = rect.getWidth();
		//page1.setMediaBox(PDPage.PAGE_SIZE_A4);
		//same margin all around document
		float margin = 200;
		float startX = 20;
        float startY = 100;
        
        float fontSize = 10;
        //float stringWidth = fontPlain.getStringWidth( message )*fontSize/1000f;
		try {
			content = new PDPageContentStream(doc, page1);
			//content = new PDPageContentStream(doc, page1, false, false);
			
			content.setFont( fontPlain, fontSize );
			
			content.concatenate2CTM(0, 1, -1, 0, pageWidth, 0);
			//print header of page 1
			//draw 50 point height grey stripe across top of page
			content.setNonStrokingColor(Color.LIGHT_GRAY);
			content.setStrokingColor(Color.BLACK);
			float bottomY = rect.getHeight() - margin - 50;
			float headerEndX = rect.getWidth() - margin * 2;
			content.addRect(margin, bottomY, headerEndX, 50);
			content.fillAndStroke();

			//reset non-stroking color
			content.setNonStrokingColor(Color.BLACK);

			//print report title
			content.setFont(fontBold, 50);
			content.beginText();
			content.newLineAtOffset(margin + 15, bottomY + 15);
			content.showText("Warehouse Inventory Summary");
			content.endText();

			
			//get startingY for data (col header first then data rows)
			content.setFont(fontMonoBold, 12);
			float dataY = rect.getHeight() - margin - 50 - 25 - fontMonoBold.getHeight(12);
			
			//sketch the layout of the columns in the report
			//Warehouse Name 		Part # 		Part Name 		Quantity		Unit
			float colX_0 = margin + 15; //warehouse name
			float colX_1 = colX_0 + 150; //part #
			float colX_2 = colX_1 + 100; //part name
			float colX_3 = colX_2 + 130; //quantity of inventory
			float colX_4 = colX_3 + 90; //Unit
			
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
			content.setFont(fontMono, 12);
			int counter = 1;
			
			int size = records.size();
			
			for(int i=0; i < size; i++) {
				//y offset for the current row
				float offset = dataY - (counter * (fontMono.getHeight(12) + 15));
				
				content.beginText();
				content.newLineAtOffset(colX_0, offset);
				content.showText( records.get(i).get("warehouseName") );
				content.endText();
				
				content.beginText();
				content.newLineAtOffset(colX_1, offset);
				content.showText( records.get(i).get("partId") );
				content.endText();
				
				content.beginText();
				content.newLineAtOffset(colX_2, offset);
				content.showText( records.get(i).get("partName") );
				content.endText();
				
				content.beginText();
				content.newLineAtOffset(colX_3, offset);
				content.showText( records.get(i).get("quantity") );
				content.endText();
				
				content.beginText();
				content.newLineAtOffset(colX_4, offset);				
				content.showText( records.get(i).get("partUnit") );
				content.endText();			
				
				//move to next row
				counter++;
			}
			
		} catch (IOException e) {
			throw new ReportException("Error in report generation: " + e.getMessage());
		} finally {
			//close page 1
			try {
				content.close();
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
