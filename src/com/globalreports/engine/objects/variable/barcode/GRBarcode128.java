package com.globalreports.engine.objects.variable.barcode;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.globalreports.engine.err.GRBarcodeException;
import com.globalreports.engine.err.GRBarcodeValueIncorrectException;
import com.globalreports.engine.err.GRValidateException;
import com.globalreports.engine.objects.GRObject;
import com.globalreports.engine.objects.variable.GRBarcode;
import com.globalreports.engine.objects.variable.barcode.code128.GRMap;
import com.globalreports.engine.structure.GRMeasures;
import com.globalreports.engine.structure.grpdf.GRContext;

public class GRBarcode128 extends GRBarcode {
	
	private static final GRMap mapencoding = new GRMap();
	
	private final String START_CODE_A		= "211412";
	private final String START_CODE_B		= "211214";
	private final String START_CODE_C		= "211232";
	private final String STOP				= "2331112";
	
	public GRBarcode128() {
		super(GRBarcode.TYPEBARCODE_CODE_128);
		
		mapencoding.put(' ',0,"212222");
		mapencoding.put('!',1,"222122");
		mapencoding.put('"',2,"222221");
		mapencoding.put('#',3,"121223");
		mapencoding.put('$',4,"121322");
		mapencoding.put('%',5,"131222");
		mapencoding.put('&',6,"212222");
		mapencoding.put('\'',7,"122312");
		mapencoding.put('(',8,"132212");
		mapencoding.put(')',9,"221213");
		mapencoding.put('*',10,"221312");
		mapencoding.put('+',11,"231212");
		mapencoding.put(',',12,"112232");
		mapencoding.put('-',13,"122132");
		mapencoding.put('.',14,"122231");
		mapencoding.put('/',15,"113222");
		mapencoding.put('0',16,"123122");
		mapencoding.put('1',17,"123221");
		mapencoding.put('2',18,"223211");
		mapencoding.put('3',19,"221132");
		mapencoding.put('4',20,"221231");
		mapencoding.put('5',21,"213212");
		mapencoding.put('6',22,"223112");
		mapencoding.put('7',23,"312131");
		mapencoding.put('8',24,"311222");
		mapencoding.put('9',25,"321122");
		mapencoding.put(':',26,"321221");
		mapencoding.put(';',27,"312212");
		mapencoding.put('<',28,"322112");
		mapencoding.put('=',29,"322211");
		mapencoding.put('>',30,"212123");
		mapencoding.put('?',31,"212321");
		mapencoding.put('@',32,"232121");
		mapencoding.put('A',33,"111323");
		mapencoding.put('B',34,"131123");
		mapencoding.put('C',35,"131321");
		mapencoding.put('D',36,"112313");
		mapencoding.put('E',37,"132113");
		mapencoding.put('F',38,"132311");
		mapencoding.put('G',39,"211313");
		mapencoding.put('H',40,"231113");
		mapencoding.put('I',41,"231311");
		mapencoding.put('J',42,"112133");
		mapencoding.put('K',43,"112331");
		mapencoding.put('L',44,"132131");
		mapencoding.put('M',45,"113123");
		mapencoding.put('N',46,"113321");
		mapencoding.put('O',47,"133121");
		mapencoding.put('P',48,"313121");
		mapencoding.put('Q',49,"211331");
		mapencoding.put('R',50,"231131");
		mapencoding.put('S',51,"213113");
		mapencoding.put('T',52,"213311");
		mapencoding.put('U',53,"213131");
		mapencoding.put('V',54,"311123");
		mapencoding.put('W',55,"311321");
		mapencoding.put('X',56,"331121");
		mapencoding.put('Y',57,"312113");
		mapencoding.put('Z',58,"312311");		
		mapencoding.put('[',59,"332111");
		mapencoding.put('\\',60,"314111");
		mapencoding.put(']',61,"221411");
		mapencoding.put('^',62,"431111");
		mapencoding.put('_',63,"111224");
		mapencoding.put(((char)96),64,"111422");
		mapencoding.put('a',65,"121124");
		mapencoding.put('b',66,"121421");
		mapencoding.put('c',67,"141122");
		mapencoding.put('d',68,"141221");
		mapencoding.put('e',69,"112214");
		mapencoding.put('f',70,"112412");
		mapencoding.put('g',71,"122114");
		mapencoding.put('h',72,"122411");
		mapencoding.put('i',73,"142112");
		mapencoding.put('j',74,"142211");
		mapencoding.put('k',75,"241211");
		mapencoding.put('l',76,"221114");
		mapencoding.put('m',77,"413111");
		mapencoding.put('n',78,"241112");
		mapencoding.put('o',79,"134111");
		mapencoding.put('p',80,"111242");
		mapencoding.put('q',81,"121142");
		mapencoding.put('r',82,"121241");
		mapencoding.put('s',83,"114212");
		mapencoding.put('t',84,"124112");
		mapencoding.put('u',85,"124211");
		mapencoding.put('v',86,"411212");
		mapencoding.put('w',87,"421112");
		mapencoding.put('x',88,"421211");
		mapencoding.put('y',89,"212141");
		mapencoding.put('z',90,"214121");
		
		mapencoding.put('{',91,"412121");
		mapencoding.put('|',92,"111143");
		mapencoding.put('}',93,"111341");
		mapencoding.put(((char)126),94,"131141");
		mapencoding.put(((char)200),95,"114113");
		mapencoding.put(((char)201),96,"114311");
		mapencoding.put(((char)202),97,"411113");
		mapencoding.put(((char)203),98,"411311");
		mapencoding.put(((char)204),99,"113141");
		mapencoding.put(((char)205),100,"114131");
		mapencoding.put(((char)206),101,"311141");
		mapencoding.put(((char)207),102,"411131");
		mapencoding.put(((char)208),103,"211412");
		

	}

	public Vector<String> draw(GRContext grcontext) throws GRValidateException, GRBarcodeException {
		Vector<String> stream = new Vector<String>();
		StringBuffer content = new StringBuffer();
		double left,top,height;
		
		left = grcontext.getLeft();
		if(this.getHPosition() == GRObject.HPOSITION_ABSOLUTE) {
			top = grcontext.getTop();
		} else {
			top = grcontext.getHPosition();
		}
		
		height = top;
		
		content.append("q\n");
		content.append("0.0 w\n");
		content.append("1.0 1.0 1.0 RG\n");
		
		left = GRMeasures.arrotonda(left+this.getLeft());
		top = GRMeasures.arrotonda(top - this.getTop() - this.getHeight());
		
		String valueBarcode = value;
		long checksum = 0;
		
		double widthChar = width / valueBarcode.length();
		double singleToken = widthChar / 11;
		
		double widthRect = 0;
		
		if(grdata != null) 
			valueBarcode = grdata.addVariables(valueBarcode);
		
		// Start
		for(int indexCode = 0;indexCode < START_CODE_B.length();indexCode++) {
			if(indexCode % 2 == 0)
				content.append("0.0 0.0 0.0 rg\n");
			else
				content.append("1.0 1.0 1.0 rg\n");
			
			widthRect = singleToken * Integer.parseInt(START_CODE_B.substring(indexCode,indexCode+1));
			
			content.append(left+" "+top+" "+widthRect+" "+this.getHeight()+" re\n");
			content.append("B\n");
			
			left += widthRect;
		}
		
		for(int i = 0;i < valueBarcode.length();i++) {
			String code = mapencoding.getWeightsFromCharacter(valueBarcode.charAt(i));

			if(code == null)
				throw new GRBarcodeValueIncorrectException();
			
			for(int indexCode = 0;indexCode < code.length();indexCode++) {
				if(indexCode % 2 == 0)
					content.append("0.0 0.0 0.0 rg\n");
				else
					content.append("1.0 1.0 1.0 rg\n");
				
				widthRect = singleToken * Integer.parseInt(code.substring(indexCode,indexCode+1));
				
				
				content.append(left+" "+top+" "+widthRect+" "+this.getHeight()+" re\n");
				content.append("B\n");
				
				left += widthRect;
				
			}
			
			// Aggiorna il checksum
			checksum += (i+1) * mapencoding.getPositionFromCharacter(valueBarcode.charAt(i));

			
		}
		
		// Aggiunge al checksum il valore dello start
		checksum += 104;	// START CODE B
		
		if(checksum > 103)
			checksum = checksum % 103;
		else
			checksum = 103 % checksum;
		System.out.println("CHECKSUM: "+((char)125));
		// Estrare il codice relativo al checksum
		String code = mapencoding.getWeightsFromPosition(checksum);
		if(code == null) 
			throw new GRBarcodeValueIncorrectException();
		
		for(int indexCode = 0;indexCode < code.length();indexCode++) {
			if(indexCode % 2 == 0)
				content.append("0.0 0.0 0.0 rg\n");
			else
				content.append("1.0 1.0 1.0 rg\n");
			
			widthRect = singleToken * Integer.parseInt(code.substring(indexCode,indexCode+1));
			
			content.append(left+" "+top+" "+widthRect+" "+this.getHeight()+" re\n");
			content.append("B\n");
			
			left += widthRect;
		}
			
		// Stop
		for(int indexCode = 0;indexCode < STOP.length();indexCode++) {
			if(indexCode % 2 == 0)
				content.append("0.0 0.0 0.0 rg\n");
			else
				content.append("1.0 1.0 1.0 rg\n");
			
			widthRect = singleToken * Integer.parseInt(STOP.substring(indexCode,indexCode+1));
			
			content.append(left+" "+top+" "+widthRect+" "+this.getHeight()+" re\n");
			content.append("B\n");
			
			left += widthRect;
		}
		
		//content.append(left+" "+top+" "+this.getWidth()+" "+this.getHeight()+" re\n");
		
		/*
		content.append("0.0 0.0 0.0 rg\n");
		content.append("B\n");
		*/
		//content.append("S\n");
		
		grcontext.setHPosition(top);
		grcontext.setMaxHeight(height+this.getHeight());
		
		content.append("Q\n");
		
		stream.add(content.toString());		
		return stream;
	}
	
	
	public short getTypeBarcode() {
		return GRBarcode.TYPEBARCODE_CODE_128;
	}
}
