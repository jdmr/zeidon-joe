package com.hfi.cheetah;

//import java.io.File;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.CharBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.mutable.MutableInt;

import com.quinsoft.zeidon.View;
import com.quinsoft.zeidon.vml.VmlOperation;
import com.quinsoft.zeidon.vml.zVIEW;
// import com.quinsoft.zeidon.vml.VmlOperation.DateTimeRecord;
import com.quinsoft.zeidon.zeidonoperations.KZOEP1AA;
import com.quinsoft.zeidon.zeidonoperations.ZDRVROPR;

/**
* @author QuinSoft
*
*/

public class ZGLOBAL1_Operation extends VmlOperation
{
private final ZDRVROPR m_ZDRVROPR;
private final KZOEP1AA m_KZOEP1AA;
//public ZGLOBAL1_Operation( TaskQualification taskQual )
public ZGLOBAL1_Operation( View view )
{
   super( view );
   m_ZDRVROPR = new ZDRVROPR( view );
   m_KZOEP1AA = new KZOEP1AA( view );
   //private final KZOEP1AA m_KZOEP1AA;
}
protected static final int Minute =       1;
protected static final int Hour =         (Minute * 60);
protected static final int Day =          (Hour * 24);
protected static final int Week =         (Day * 7);
protected static final int Year =         (Day * 365);
protected static final int LeapYear =     (Day * 366);
protected static final int Century =      ((Year * 76) + (LeapYear * 24));
protected static final int LeapCentury =  ((Year * 75) + (LeapYear * 25));
protected static final int Year1900 =     ((Century * 14) + (LeapCentury * 5));
protected static final int Year2000 =     ((Century * 15) + (LeapCentury * 5));
protected static final int lNullInteger = -2147483647 - 1;

protected static final                     // J  F  M  A  M  J  J  A  S  O  N  D
                       char cMonth_Val[ ] = { 6, 2, 2, 5, 0, 3, 5, 1, 4, 6, 2, 4 };

protected static final int usDayTable[ ] = { 0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334 };
protected static final int usLeapDayTable[ ] = { 0, 31, 60, 91, 121, 152, 182, 213, 244, 274, 305, 335 };

// Internal DateTime structure
public static class DateTimeRecord
{
   private int  ulDateMinutes;    // Minutes since year zero
   private int  usTSeconds;       // Thousandths of seconds
};

/////////////////////////////////////////////////////////////////////////////////////////////////////
//
// .Function Name: >>>SetAttributeFromCurrentDateTime
//
// .Description: Set an Attribute to Current Date Time
//
// .Return Value: int
//    (Same as SetAttributeFromString() )
//
// .Parameter:
//    Data type       Name        (I/O/U) Description
//    View            View          (I)   View for Attribute
//    String          entityName    (I)   Name of Entity
//    String          attributeName (I)   Name of Attribute
//
/////////////////////////////////////////////////////////////////////////////////////////////////////
// .Detail description
//
/////////////////////////////////////////////////////////////////////////////////////////////////////
public int
SetAttributeFromCurrentDateTime( View   View,
                                 String entityName,
                                 String attributeName )
{
  String  stringTimeStamp = null;
  int rc;

  stringTimeStamp = m_KZOEP1AA.SysGetDateTime( stringTimeStamp );
  rc = SetAttributeFromString( View, entityName, attributeName, stringTimeStamp );
  return rc;
}

public int HFI_GetDateTime( StringBuilder szDate )
{
   //yyyyMMddHHmmssSSS
   DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
   Date date = new Date();

   szDate.insert(0, dateFormat.format(date));
   return 0;
}
public int HFI_StatusCodeToProgram( String szCode, String szProgram ){
	List<String> result = new ArrayList<String>();
	Map<String, List<String>> codesToPrograms = new HashMap<String, List<String>>();
	
	// Mass Lists
	List<String> Basic = new ArrayList<String>();				Basic.add("Basic");
	List<String> BuyIn = new ArrayList<String>(); 				BuyIn.add("Buy-In");
	List<String> CommonHealth = new ArrayList<String>();		CommonHealth.add("Common Health");
	List<String> CommonWealthCare = new ArrayList<String>();	CommonWealthCare.add("Commonwealth Care");
	List<String> ChildMedSecPlan = new ArrayList<String>(); 	ChildMedSecPlan.add("Childrens Medicaid Security Plan");
	List<String> Essential = new ArrayList<String>(); 			Essential.add("Essential");
	List<String> EmergencyMed = new ArrayList<String>(); 		EmergencyMed.add("Emergency Medical");
	List<String> FamilyAssistance = new ArrayList<String>(); 	FamilyAssistance.add("Family Assistance");
	List<String> FullHSN = new ArrayList<String>(); 			FullHSN.add("Full HSN");FullHSN.add("Prenatal");FullHSN.add("EAEDC");
	List<String> MedHardship = new ArrayList<String>(); 		MedHardship.add("Medical Hardship");
	List<String> PartialHSN = new ArrayList<String>(); 			PartialHSN.add("Partial HSN");
	List<String> HuskyA = new ArrayList<String>();				HuskyA.add("HuskyA");
	List<String> HuskyB = new ArrayList<String>();				HuskyB.add("HuskyB");
	List<String> HuskyC = new ArrayList<String>();				HuskyC.add("HuskyC - Disabled"); HuskyC.add("HuskyC - Over 65");HuskyC.add("HuskyC and Long Term Care");
	List<String> HuskyD = new ArrayList<String>();				HuskyD.add("HuskyD");
	List<String> HuskyCLTC = new ArrayList<String>();			HuskyCLTC.add("HuskyC - Disabled"); HuskyC.add("HuskyC - Over 65"); HuskyCLTC.add("HuskyC and Long Term Care"); 
	List<String> Limited = new ArrayList<String>();				Limited.add("Limited");Limited.add("MA over 65 Limited");Limited.add("Family Limited");Limited.add("Disability Limited");
	List<String> LimitedEssential = new ArrayList<String>();	LimitedEssential.add("Limited and Essential");
	List<String> LongTermCare = new ArrayList<String>();		LongTermCare.add("Long Term Care");
	List<String> MCOE = new ArrayList<String>();				MCOE.add("MCO Enrollment");
	List<String> OutOfStateMed = new ArrayList<String>();		OutOfStateMed.add("Out of State Medicaid");
	List<String> PremiumAssistance = new ArrayList<String>();	PremiumAssistance.add("Premium Assistance");
	List<String> Standard = new ArrayList<String>();			Standard.add("Standard"); Standard.add("Disability Standard");Standard.add("Family Standard");Standard.add("MA over 65 Standard");
	List<String> StandardLTC = new ArrayList<String>();			StandardLTC.add("Standard and Long Term Care");
	List<String> AT = new ArrayList<String>();					AT.add("Long Term Care"); AT.add("");			
	
	// ACA
	List<String> CarePlus = new ArrayList<String>();			CarePlus.add("CarePlus");
	List<String> QHealthPlan = new ArrayList<String>();			QHealthPlan.add("Qualified Health Plan");
	List<String> ConnectorCare = new ArrayList<String>();		ConnectorCare.add("Connector Care");
	
	// SSI Lists
	List<String> ASSDH = new ArrayList<String>();				ASSDH.add("SSDI");
	List<String> ASSDI = new ArrayList<String>();				ASSDI.add("SSDI");
	List<String> ASSDR = new ArrayList<String>();				ASSDR.add("SSDI");
	List<String> ASSH = new ArrayList<String>();				ASSH.add("SSI");
	List<String> ASSI = new ArrayList<String>();				ASSI.add("SSI");
	List<String> ASSR = new ArrayList<String>();				ASSR.add("SSI");
	
	// depreciated 
	List<String> AMU  = new ArrayList<String>();				AMU.add("HuskyC - Disabled");
	
	codesToPrograms.put("AB", Basic);
	codesToPrograms.put("ABI", BuyIn);
	codesToPrograms.put("AC", CommonHealth);
	codesToPrograms.put("ACC", CommonWealthCare);
	codesToPrograms.put("ACMS", ChildMedSecPlan);
	codesToPrograms.put("ACON", ConnectorCare);
	codesToPrograms.put("ACP", CarePlus);
	codesToPrograms.put("AE", Essential);
	codesToPrograms.put("AEMD", EmergencyMed);
	codesToPrograms.put("AFA", FamilyAssistance);
	codesToPrograms.put("AFCC", FullHSN);
	codesToPrograms.put("AFCmed", MedHardship);
	codesToPrograms.put("AFCP", PartialHSN);
	codesToPrograms.put("AHA", HuskyA);
	codesToPrograms.put("AHB", HuskyB);
	codesToPrograms.put("AHC", HuskyC);
	codesToPrograms.put("AHLTC", HuskyCLTC);
	codesToPrograms.put("AHD", HuskyD);
	codesToPrograms.put("AL", Limited);
	codesToPrograms.put("ALE", LimitedEssential);
	codesToPrograms.put("ALTC", LongTermCare);
	codesToPrograms.put("AMCO", MCOE);
	codesToPrograms.put("AOSM", OutOfStateMed);
	codesToPrograms.put("APAS", PremiumAssistance);
	codesToPrograms.put("AS", Standard);
	codesToPrograms.put("ASLTC", StandardLTC);
	codesToPrograms.put("AT", AT);
	codesToPrograms.put("ASSDH", ASSDH);
	codesToPrograms.put("ASSDI", ASSDI);
	codesToPrograms.put("ASSDR", ASSDR);
	codesToPrograms.put("ASSH", ASSH);
	codesToPrograms.put("ASSI", ASSI);
	codesToPrograms.put("ASSR", ASSR);
	codesToPrograms.put("AMU", AMU);
	codesToPrograms.put("AQHP", QHealthPlan);
	
	result = codesToPrograms.get(szCode);
	
	for(Iterator<String> index = result.iterator(); index.hasNext();){
		String item = index.next();
		if (item.equals(szProgram))
			return 0;
	}
	
	return -1;
}
/*
 * This function will clean up the files first before creating or duplicating records.
 * We first look at the which operating system that is being used. If Windows is being used, it 
 * is assumed that we are debugging. If not, we assume we are on the ubuntu server. 
 */
public void HFI_CleanExceptions() throws IOException{
	File szPath;
	
	if (System.getProperty("os.name").contains("Wind")){
		szPath = new File("C:\\Exceptions\\");
	}
	else {
 		szPath = new File("//mnt//hfistorage//lplr//misc");
	}
	
	File files[] = szPath.listFiles();
	if(files!=null){
		for(File f: files){
			f.delete();
		}
	}
}

public int HFI_SendEmail(String szTo, String szFrom, String szSubject, String szMessage, String szSMTPServer){
	int nRC = 0;
	Properties p = new Properties();
	String szHost = "hfimail1.hfimass.com";
	
	p.setProperty("mail.smtp.host", szHost);
	
	Session s = Session.getDefaultInstance(p,null);
	
	Message msg = new MimeMessage(s);
	
	try{
		msg.setFrom(new InternetAddress(szFrom));
		msg.setRecipient(Message.RecipientType.TO, new InternetAddress(szTo));
		msg.setRecipient(Message.RecipientType.BCC, new InternetAddress(szFrom));
		msg.setSubject(szSubject);
		msg.setText(szMessage);
		Transport.send(msg);
	}catch(Exception e){
		nRC = -1;
	}	
	
	return nRC;
}

public void HFI_GenerateExceptions(String szReferenceID, 
								   String szFirstName,
								   String szLastName,								 
								   String szAction,
								   String szReason,
								   String szFileName
								   ) throws IOException{
	String szPath = "";	
	
	if (System.getProperty("os.name").contains("Wind")){
		szPath = "C:\\Exceptions\\" + szFileName;
	}
	else {
 		szPath = "//mnt//hfistorage//lplr//Exceptions//" + szFileName;
	}

	
	BufferedWriter ExceptionReport;
	ExceptionReport = new BufferedWriter(new FileWriter(szPath, true));
	ExceptionReport.write(szReferenceID + szFirstName + szLastName + szAction + szReason + "\r\n");
	ExceptionReport.flush();
	ExceptionReport.close();	
}



protected int
fnValidateDayLocal( int usMonth, int usDay, int ulYear )
{
   int   usMax;

   if ( usDay >= 1 && usDay <= 28 )
      return( usDay );

   if ( usDay < 1 )
      return( 1 );

   switch ( usMonth )
   {
      case 2:
         usMax = 28;
         if ( (ulYear % 4) == 0 &&
              ((ulYear % 100) != 0 || (ulYear % 400) == 0) )
         {
            usMax = 29;
         }

         break;

      case 4:
      case 6:
      case 9:
      case 11:
         usMax = 30;
         break;

      default:
         usMax = 31;
   }

   if ( usDay > usMax )
      return( usMax );
   else
      return( usDay );

} /*** END fnValidateDay ***/
public int HFI_GetDate( StringBuilder szDate )
{
   DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
   Date date = new Date();

   szDate.insert(0, dateFormat.format(date));
   return 0;
}

public int HFI_GetDayNumber( )
{
   Calendar cal = Calendar.getInstance();
   // You cannot use Date class to extract individual Date fields
   int day = cal.get(Calendar.DAY_OF_WEEK);
   
   return day;
}

public String
HFI_GlobalArrays(int onThisArray, int getValueFromArray, StringBuilder checkValue){
	String valueFromArray = null;	
	String[] AStatusCodes = {"A", "AB", "ABI" ,"ABM", "ABU", "AC", "ACC", "ACCU","ACMS", "AE", "AEA", "AEMD",
			 				 "AEU", "AFA", "AFAM", "AFCC", "AHA", "AHB", "AL", "ALE", "ALJ", "ALTC", "AMC", "AMCO", "AMD", "AMO", "AMU", "ANC",
			 				 "AO", "AOSM", "AP", "APA", "APAS", "APCC", "AS", "ASLTC", "ASM", "ASMU", "ASSDH", "ASSDI", 
			 				 "ASSDR", "ASSH", "ASSI", "ASSR", "ASU", "AT", "AU", "AVS"};	
	String[] BStatusCodes = {"B", "BAD", "BAD/ALJ", "BAF", "BAF/ALJ", "BASD", "BCC", "BCCP", "BCF", "BDD", "BDI",
			 	             "BEWL", "BGI", "BISD", "BMC", "BMCO", "BSD", "BSPD", "BSSI", "BV", "BVDD", "BZZ"};
	String[] CStatusCodes = {"C", "CDS", "CMC", "CMH"};
	String[] DStatusCodes = {"D", "DCC", "DI", "DNC", "DSSAD", "DSSAI"};
	String[] EStatusCodes = {"E"};
	String[] FStatusCodes = {"F", "F1", "F2"};
	String[] GStatusCodes = {"G", "GSSH", "GSSI", "GSSR", "Gx"};
	String[] HStatusCodes = {"H"};
	String[] IStatusCodes = {"I", "IP"};
	String[] KStatusCodes = {"KCC", "KE", "KMH", "KSSA"};
	String[] LStatusCodes = {"LBAF", "LBCF", "LBSSI"};
	String[] NStatusCodes = {"NRN"};
	String[] OStatusCodes = {"O"};
	String[] SStatusCodes = {"S", "SD"};
	String[] WStatusCodes = {"W", "W1", "W2", "W30", "WCC", "WDSH", "WP", "WPCF", "WR"};
	String[] XStatusCodes = {"X", "XDSSAI", "XO", "XS"};
	
	String[] BillCodeTypes = {"", "", "", "Percentage", "", "",
			                  "", "", "", "", "Standard New", "App Taken",
			                  "Placement", "BMC MH", "BMC FC", "Historical"};
	
	try{
		switch (onThisArray) {
			case 0:
				valueFromArray = AStatusCodes[getValueFromArray]; break;
			case 1:
				valueFromArray = BStatusCodes[getValueFromArray]; break;
			case 2:
				valueFromArray = CStatusCodes[getValueFromArray]; break;
			case 3:
				valueFromArray = DStatusCodes[getValueFromArray]; break;
			case 4:
				valueFromArray = EStatusCodes[getValueFromArray]; break;
			case 5:
				valueFromArray = FStatusCodes[getValueFromArray]; break;
			case 6:
				valueFromArray = GStatusCodes[getValueFromArray]; break;
			case 7:
				valueFromArray = HStatusCodes[getValueFromArray]; break;
			case 8:
				valueFromArray = IStatusCodes[getValueFromArray]; break;
			case 9:
				valueFromArray = KStatusCodes[getValueFromArray]; break;
			case 10:
				valueFromArray = LStatusCodes[getValueFromArray]; break;
			case 11:
				valueFromArray = NStatusCodes[getValueFromArray]; break;
			case 12:
				valueFromArray = OStatusCodes[getValueFromArray]; break;
			case 13:
				valueFromArray = SStatusCodes[getValueFromArray]; break;
			case 14:
				valueFromArray = WStatusCodes[getValueFromArray]; break;
			case 15:
				valueFromArray = XStatusCodes[getValueFromArray]; break;
			case 16:
				valueFromArray = BillCodeTypes[getValueFromArray]; break;
			default:
				break;
			}			
		}catch(Exception e){
			return "E";
		}finally{}
	
	
	checkValue.insert(0, valueFromArray);
	
	return valueFromArray;
}
public int
HFI_CheckAnyCodeType(String checkValue, int codeType){
	StringBuilder tmp = new StringBuilder();
	int i = 1;
	while(HFI_GlobalArrays(codeType, i, tmp) != "E")
	{
		if (checkValue.equals(HFI_GlobalArrays(16, i, tmp)))
			return i;
		
		i++;
	}
	
	return -1;
}
public int
HFI_CheckStatusCode(View vResultSet, String RootEntityName, String AttributeName, int codeType){
	int nRC = -1;
	StringBuilder tmp = new StringBuilder();
	// Code Types: ID : Length/Size
	// A:0:48	  B:1:22    C:2:4    D:3:6    E:4:1    F:5:3    G:6:5    H:7:1    I:8:2    K:9:4   L:10:3   N:11:1   O:12:1   S:13:2   W:14:9   X:15:4 
	//String[] statusCodeRootTypes = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "K", "L", "N", "O", "S", "W", "X"};
							  // A   B
	// Code Type Sizes
	int[] statusCodeArraySize = {48, 22, 4, 6, 1, 3, 5, 1, 2, 4, 3, 1, 1, 2, 9, 4};
	
	// sets length to the size of the arrayType that we want to use
	// so if codeType = 1 then we'd use BStatusCode array with a size of 22
	int length = statusCodeArraySize[codeType];
	
	for (int i = 0; i < length && nRC == -1; i++){
		if (HFI_GlobalArrays(codeType, i, tmp).equals(GetStringFromAttribute(vResultSet, RootEntityName, AttributeName))){
			nRC = 0;
		}			
	}
	
	return nRC;
}

public int 
HFI_PreviousMonth(StringBuilder startDate, StringBuilder endDate, int offset){
	Calendar prevMonth = Calendar.getInstance();
	prevMonth.add(Calendar.MONTH, offset);
	prevMonth.set(Calendar.DATE, 1);
	SimpleDateFormat dt = new SimpleDateFormat("MM/dd/yyyy");
	Date firstDayOfPreviousMonth = prevMonth.getTime();
	
	startDate.insert(0, dt.format(firstDayOfPreviousMonth));
	
	prevMonth.set(Calendar.DATE, prevMonth.getActualMaximum(Calendar.DATE));
	
	Date lastDayOfPreviousMonth = prevMonth.getTime();	
	endDate.insert(0, dt.format(lastDayOfPreviousMonth));	
	return 0;
}
public int
HFI_GetEntityCount(View view, String rootEntity){
	int count = 0;
	int nRC = 0;
	
	nRC = SetCursorFirstEntity(view, rootEntity, "");
	while(nRC > zCURSOR_UNCHANGED)
	{
		nRC = SetCursorNextEntity(view, rootEntity, "");
		count++;
	}
	
	return count;	
}

public int
HFI_InsertOI_TemplateFile(View view, View workView, String fromFile, String toFile, String stringRootEntityName) throws IOException{
	BufferedWriter bw;
	StringBuilder sbInsertTemplate  = new StringBuilder();
	StringBuilder sbRawTemplate = new StringBuilder();
	StringBuilder sbEntityBuffer;
	StringBuilder sbAttributeBuffer;
	String swapString = null;
	String stringStart  = "{";
	String stringEnd    = "}";
	int nRC = 0;
	int lSelectedCount = 0;
	int lTemplateLth = 0;

	nRC = SetCursorFirstEntity(workView, stringRootEntityName, "");
	while(nRC > zCURSOR_UNCHANGED){
		lSelectedCount++;
		nRC = SetCursorNextEntity(workView, stringRootEntityName, "");
	}

	if (lSelectedCount <= 0)
		return 0;

	lTemplateLth = ReadFileDataIntoMemory(workView, fromFile, lTemplateLth, sbRawTemplate);

	if (lTemplateLth > Integer.MAX_VALUE)
		return 0;

	swapString = sbRawTemplate.substring(1, (lTemplateLth - 1));
	lTemplateLth = swapString.length();
	sbRawTemplate = new StringBuilder();

	nRC = SetCursorFirstEntity(workView, stringRootEntityName, "");
	while(nRC > zCURSOR_UNCHANGED)
	{
		sbRawTemplate.insert(0, swapString);

		for(int i = 0; i < sbRawTemplate.length(); i++)
		{
			sbEntityBuffer = new StringBuilder();
			sbAttributeBuffer = new StringBuilder();
			if (sbRawTemplate.charAt(i) == '[' && sbRawTemplate.charAt(i + 1) == 'Z')
			{
				int j = i;
				i += 2;
				while(sbRawTemplate.charAt(++i) != '.')
					sbEntityBuffer.append(sbRawTemplate.charAt(i));

				while(sbRawTemplate.charAt(++i) != ']')
					sbAttributeBuffer.append(sbRawTemplate.charAt(i));

				i++;
				sbRawTemplate.replace(j, i, GetStringFromAttribute(workView,
																   sbEntityBuffer.toString(),
																   sbAttributeBuffer.toString()));

			}
		}

		sbInsertTemplate.append(sbRawTemplate);
		sbRawTemplate = new StringBuilder();
		nRC = SetCursorNextEntity(workView, stringRootEntityName, "");
	}

	sbInsertTemplate.insert(0, stringStart);
	sbInsertTemplate.append(stringEnd);

	bw = new BufferedWriter(new FileWriter(toFile));
	bw.write(sbInsertTemplate.toString());
	bw.flush();
	bw.close();

	return 0;
}
/*
/////////////////////////////////////////////////////////////////////////////////////////////////////
//
// .Function Name: >>>DecimalSumOf
//
// .Description: Compute the sum of a double attribute over all
//               instances of an entity
//
// .Return Value: BigDecimal - sum
//
// .Parameter:
//    Datatype        Name            (I/O/U) Description
//    View            View              (I)   View for Entity
//    String          entityName        (I)   Name of Entity
//    String          attributeName     (I)   Name of Attribute
//    String          stringParentName  (I)   Name of Parent Entity
//
/////////////////////////////////////////////////////////////////////////////////////////////////////
// .Detail description
//
/////////////////////////////////////////////////////////////////////////////////////////////////////
*/
public Double
DecimalSumOf( View   vSum,
              String entityName,
              String attributeName,
              String stringParentName )
{
   Double decimalSum;
   Double decimalValue = null;
   int RESULT;

   decimalSum = 0.0;

   RESULT = SetCursorFirstEntity( vSum, entityName, stringParentName );
   while ( RESULT > zCURSOR_UNCHANGED )
   {
     decimalSum += GetDecimalFromAttribute( decimalValue, vSum, entityName, attributeName );
      RESULT = SetCursorNextEntity( vSum, entityName, stringParentName );
   }

   return decimalSum;
}

// Sets the cursor to the latest entity based on a date/time stamp
// attribute.  The attribute passed should be a date/time stamp, but
// could be any attribute with ascending collating sequence.
public int
SetCursorLatestEntity( View   view,
                       String entityName,
                       String attributeName )
{
   OrderEntityForView( view, entityName, attributeName );
   SetCursorLastEntity( view, entityName, "" );
   return 0;
}

public int
GetIntFromAttrByContext( MutableInt lValue,
                         View   view,
                         String stringEntity,
                         String stringAttribute,
                         String stringContext )
{
	  int lValueInt = 0;

	  lValueInt = GetVariableFromAttribute( lValueInt, 0, zTYPE_INTEGER, 0,
                                         view, stringEntity, stringAttribute, stringContext, 0 );

	  lValue.setValue(lValueInt);
   return lValue.intValue();
}

public int
GetStrFromAttrByContext( StringBuilder sbValue,
                         int    lOrigLth,
                         View   view,
                         String stringEntity,
                         String stringAttribute,
                         String stringContext )
{
   MutableInt k = new MutableInt( 0 );
   int     lLth;
// String  string;

   // If the Context value is null, use the default Context.
   if ( lOrigLth < 10000 )
      lLth = lOrigLth;
   else
      lLth = 10000;

   if ( stringContext == null )
      GetVariableFromAttribute( sbValue, k, zTYPE_STRING, lLth,
                                view, stringEntity, stringAttribute, "", zUSE_DEFAULT_CONTEXT );
   else
      GetVariableFromAttribute( sbValue, k, zTYPE_STRING, lLth,
                                view, stringEntity, stringAttribute, stringContext, 0 );

   return k.intValue( );
}

public int
SetAttrFromIntByContext( View   view,
                         String stringEntity,
                         String stringAttribute,
                         int    lValue,
                         String stringContext )
{
  int rc;

  rc = SetAttributeFromVariable( view, stringEntity, stringAttribute,
                                 lValue, zTYPE_INTEGER, 0, stringContext, 0);
  return rc;
}

public int
AddToAttrFromIntByContext( View   view,
                           String stringEntity,
                           String stringAttribute,
                           int    lValue,
                           String stringContext )
{
  int rc;

  rc = AddToAttributeFromVariable( view, stringEntity, stringAttribute,
                                   lValue, zTYPE_INTEGER, 0, stringContext );
  return rc;
}

/*
/////////////////////////////////////////////////////////////////////////////////////////////////////
//
// .Function name: >>>GetEnvVariable
//
// .Description: Get an environment variable
//
// .Return value: int
//    0 - OK
//    else Error
//
// .Parameter:
//    Data type,      Name             (I/O/U) Description
//    String          stringReturnWert   (O)   value of the env var (returned)
//    String          stringVariableName (I)   name of the env var
//    int             nMaxReturnLth      (I)   max. length of stringReturnWert
//
/////////////////////////////////////////////////////////////////////////////////////////////////////
*/
public String
GetEnvVariable( String stringReturnValue,
                String stringVariableName,
                int    nMaxReturnLth )
{
   stringReturnValue = m_KZOEP1AA.SysGetEnvVar( stringReturnValue, stringVariableName, nMaxReturnLth );
   return stringReturnValue;
}

/*
/////////////////////////////////////////////////////////////////////////////////////////////////////
//
// .Function Name: >>>StrToInt
//
// .Description: Convert an String to an Integer
//
// .Return Value: int
//    (Integer Value of String )
//
// .Parameter:
//    Data type,      Name,       (I/O/U) Description
//    String          stringStr         (I)   String to convert
//
/////////////////////////////////////////////////////////////////////////////////////////////////////
// .Detail description
//
/////////////////////////////////////////////////////////////////////////////////////////////////////
*/
public int
StrToInt( String string )
{
   return Integer.parseInt( string );
}

/*
/////////////////////////////////////////////////////////////////////////////////////////////////////
//
// .Function Name: >>>StrToDecimal
//
// .Description: Convert an String to a Decimal
//
// .Return Value: BigDecimal
//             0 - OK
//             else: invalid string
//
// .Parameter:
//    Data type,      Name,       (I/O/U) Description
//    String          stringStr         (I)   String to convert
//
/////////////////////////////////////////////////////////////////////////////////////////////////////
// .Detail description
//
/////////////////////////////////////////////////////////////////////////////////////////////////////
*/
public Double
StrToDecimal( String stringStr )
{
	   if ( stringStr == null )
		   return 0.0;

	   if ( stringStr.equals("") )
		   return 0.0;

   return Double.valueOf( stringStr );
}

/*
/////////////////////////////////////////////////////////////////////////////////////////////////////
//
// .Function Name: >>>CodeToChar
//
// .Description: Returns the Char for the given code
//
// .Return Value: zVOID
//
// .Parameter:
//    Data type,      Name,       (I/O/U) Description
//    String          stringStr     (O)   String, which contains the char
//    int             sCode         (I)   Code for Char
//
/////////////////////////////////////////////////////////////////////////////////////////////////////
// .Detail description
//
/////////////////////////////////////////////////////////////////////////////////////////////////////
*/
public String
CodeToChar( String stringStr, int sCode )
{
   char[] arrayChar = new char[ 2 ];

    arrayChar[ 0 ] = (char) (sCode & 0x00ff);
    arrayChar[ 1 ] = (char) 0;
    arrayChar[ 2 ] = 'x';  // testing to ensure array bounds checking occurs at run time
    return stringStr = arrayChar.toString( );
}

/////////////////////////////////////////////////////////////////////////////////////////////////////
//
// .Function Name: >>>CharToCode
//
// .Description: Returns the code of the first char in string
//
// .Return Value: int - code for char
//
// .Parameter:
//    Data type,      Name,       (I/O/U) Description
//    String          stringStr         (I)   String with char on first pos
//
/////////////////////////////////////////////////////////////////////////////////////////////////////
// .Detail description
//
/////////////////////////////////////////////////////////////////////////////////////////////////////
public int
CharToCode( String stringStr )
{
   int nCode = 0;

   nCode = (int) stringStr.charAt( 0 );
   return nCode;
}
//./ ADD NAME=UfDateTimeDiff
// Source Module=kzoeufaa.c
/////////////////////////////////////////////////////////////////////////////
//
// ENTRY:      UfDateTimeDiff
//
// PARAMETERS:
//             plDiff      - pointer to variable returning difference
//             lpDateTime1 - First Timestamp
//             lpDateTime2 - Second Timestamp
//             nDiffType   - unit for difference (zDT_SECOND, ...)
//
// RETURNS:     0 - Success
//             -1 - overflow
//    zCALL_ERROR - error during call (invalid AmountType)
//
/////////////////////////////////////////////////////////////////////////////
//./ END + 4
public int
UfDateTimeDiffLocal( int  lDiff,
                     DateTimeRecord   lpDateTime1,
                     DateTimeRecord   lpDateTime2,
                     int  nDiffType )
{
	if (nDiffType == zDT_DAY)
	{
		   lDiff = lpDateTime1.ulDateMinutes - lpDateTime2.ulDateMinutes;
		   lDiff = lDiff / 60 / 24;
	}
   return( lDiff );
} // UfDateTimeDiff

public MutableInt
GetDateAttributeDifferenceInDays( MutableInt    lDays,
                                  View   srcView,
                                  String srcEntityName,
                                  String srcAttributeName,
                                  View   tgtView,
                                  String tgtEntityName,
                                  String tgtAttributeName )
{
   DateTimeRecord  SourceDate = new DateTimeRecord();
   DateTimeRecord  TargetDate = new DateTimeRecord();
   String          stringSourceDate = null;
   String          stringTargetDate = null;
   int             lDaysTmp;

   // read the attributes
   stringSourceDate = GetStringFromAttribute( stringSourceDate, srcView, srcEntityName, srcAttributeName );
   stringTargetDate = GetStringFromAttribute( stringTargetDate, tgtView, tgtEntityName, tgtAttributeName );

   UfStringToDateTimeLocal( stringSourceDate, SourceDate );
   UfStringToDateTimeLocal( stringTargetDate, TargetDate );

   // subtract the values
   lDaysTmp = lDays.intValue();
   lDaysTmp = UfDateTimeDiffLocal( lDaysTmp, TargetDate, SourceDate, zDT_DAY );

   lDays.setValue(lDaysTmp);

   return lDays;
}

public int
UfStringToDateTimeLocal( String cpcDateTimeString, DateTimeRecord lpDateTime )
{
   int           usStringLth;
   int           usMonth;
   int           usDay;
   int           usDayOrg;
   int           usSeconds;
   int           usTSeconds;
   int           ulYear;
   int           ulHours;
   int           usMinutes;
   int           ulDateMinutes;
   int           ulDays;
   int           ulWorkYear;
   StringBuilder sbWorkString = new StringBuilder( 20 );
   DateTimeRecord lpDTInternal;
   boolean       bDateSet;
   int           nRC = 0;

   lpDTInternal = lpDateTime;

   // Null string will set the DateTime to 'NULL'
   if ( StringUtils.isBlank( cpcDateTimeString ) )
   {
      lpDTInternal.ulDateMinutes = lNullInteger;
      lpDTInternal.usTSeconds = 0;
      return( 0 );
   }

   usMonth = 0;
   usDay = 0;
   ulYear = 0;
   ulHours = 0;
   usMinutes = 0;
   usSeconds = 0;
   usTSeconds = 0;

   usStringLth = zstrlen( cpcDateTimeString );
   switch ( usStringLth )
   {
      case 17:   // YYYYMMDDHHmmSSTht
      case 16:   // YYYYMMDDHHmmSSTh
      case 15:   // YYYYMMDDHHmmSST
         // Get Thousandths of seconds Value
         zstrcpy( sbWorkString, cpcDateTimeString.substring( 14 ) );
         usTSeconds = zatol( sbWorkString.toString( ) );
         if ( usStringLth < 17 )
            usTSeconds *= (usStringLth == 16) ? 10 : 100 ;

      case 14:   // YYYYMMDDHHmmSS
         // Get Seconds Value
         sbWorkString.insert( 0, cpcDateTimeString.charAt( 12 ) );
         sbWorkString.insert( 1, cpcDateTimeString.charAt( 13 ) );
         sbWorkString.insert( 2, '\0' );
         usSeconds = zatol( sbWorkString.toString( ) );
         if ( usSeconds > 59 )
         {
            usSeconds = 59;
            nRC = zCALL_ERROR;
         }

      case 12:   // YYYYMMDDHHmm
         // Get Minutes Value
         sbWorkString.insert( 0, cpcDateTimeString.charAt( 10 ) );
         sbWorkString.insert( 1, cpcDateTimeString.charAt( 11 ) );
         sbWorkString.insert( 2, '\0' );
         usMinutes = zatol( sbWorkString.toString( ) );
         if ( usMinutes > 59 )
         {
            usMinutes = 59;
            nRC = zCALL_ERROR;
         }

         // Get Hours Value
         sbWorkString.insert( 0, cpcDateTimeString.charAt( 8 ) );
         sbWorkString.insert( 1, cpcDateTimeString.charAt( 9 ) );
         sbWorkString.insert( 2, '\0' );
         ulHours = zatol( sbWorkString.toString( ) );
         if ( ulHours > 23 )
         {
            ulHours = 23;
            nRC = zCALL_ERROR;
         }

      case 8:    // YYYYMMDD
         // Get Day Value
         //sbWorkString.insert( 0, cpcDateTimeString.charAt( 6 ) );
         //sbWorkString.insert( 1, cpcDateTimeString.charAt( 7 ) );
         //sbWorkString.insert( 2, '\0' );
         //usDay = zatol( sbWorkString.toString( ) );
         String str = new String(cpcDateTimeString.substring(6, 8));
         usDay = zatol( str );

         // Get Month Value
         //sbWorkString.insert( 0, cpcDateTimeString.charAt( 4 ) );
         //sbWorkString.insert( 1, cpcDateTimeString.charAt( 5 ) );
         //sbWorkString.insert( 2, '\0' );
         //usMonth = zatol( sbWorkString.toString( ) );
         str = cpcDateTimeString.substring(4, 6);
         usMonth = zatol( str );

         // Get Year Value
         //zstrncpy( sbWorkString, cpcDateTimeString, 4 );
         //sbWorkString.insert( 4, '\0' );
         //ulYear = zatol( sbWorkString.toString( ) );
         str = cpcDateTimeString.substring(0, 4);
         ulYear = zatol( str );

         // Check to see if we have date/datetime or only time without date.
         bDateSet = (ulYear != 0 || usMonth != 0 || usDay != 0);

         if ( ulYear == 0 )
         {
           ulYear = 1900;
           if ( bDateSet )
             nRC = zCALL_ERROR;
         }
         // the year will be multiplied with minutes/year and then stored as
         // unsigned long. These means, max. can be not much more than 8000
         if ( ulYear > 8000 )
         {
           ulYear = 8000;
           nRC = zCALL_ERROR;
         }

         // if month out of range, make it January
         if ( usMonth < 1 || usMonth > 12 )
         {
            usMonth = 1;
            if ( bDateSet )
               nRC = zCALL_ERROR;
         }

         // Get valid day for the month
         usDayOrg = usDay;
         usDay = fnValidateDayLocal( usMonth, usDayOrg, ulYear );
         if ( usDay != usDayOrg )
         {
           if ( bDateSet )
             nRC = zCALL_ERROR;
         }
         break;

      default:
         return( zCALL_ERROR );
   }


   /* Calculate Year in Minutes */
   ulWorkYear = 0;
   ulDateMinutes = 0;

   // Fast path for dates starting Jan 1, 1900.
   // Start point set to beginning of century.
   if ( ulYear >= 1900L )
   {
      ulWorkYear = 1900;
      ulDateMinutes = Year1900;
      if ( ulYear >= 2000L )
      {
         ulWorkYear = 2000;
         ulDateMinutes += Century;
      }
   }

   // This will get us to Jan 1, of the desired year.
   // This will take a bit longer when the year is less than 1900.
   while ( ulWorkYear < ulYear )
   {
      if ( (ulWorkYear % 4) == 0 &&
           ((ulWorkYear % 100) != 0 || (ulWorkYear % 400) == 0) )
      {
         ulDateMinutes += LeapYear;
      }
      else
      {
         ulDateMinutes += Year;
      }

      ulWorkYear++;
   }

   // This will get the number of days from the Jan 1,
   // to the beginning of the desired month.
   if ( (ulWorkYear % 4) == 0 &&
        ((ulWorkYear % 100) == 0 || (ulWorkYear % 400) == 0) )
   {
      ulDays = usLeapDayTable[ usMonth - 1 ];
   }
   else
   {
      ulDays = usDayTable[ usMonth - 1 ];
   }

   ulDays += usDay - 1;                 // add day of the month, for days
                                        // this year
   ulDateMinutes += ulDays * Day;       // add days_minutes to total minutes
   ulDateMinutes += ulHours * Hour;     // add hours_minutes to total minutes
   // add minutes to total minutes
   ulDateMinutes += (int)(usMinutes * Minute);

   // Now save this, before we forget...
   lpDTInternal.ulDateMinutes = ulDateMinutes;

   // Convert seconds to thousandths, and save it too.
   lpDTInternal.usTSeconds = usTSeconds + (usSeconds * 1000);

   return( nRC );

}  /* END of StringToDateTime */

public int GetEntityNameFromStructure( String stringInternalEntityStructure, StringBuilder returnEntityName )
{
    returnEntityName.setLength( 0 );
    returnEntityName.append( stringInternalEntityStructure );
    return 0;
}

public String GetEntityNameFromStructure( String stringInternalEntityStructure, String returnEntityName )
{
 // returnEntityName = stringInternalEntityStructure;
 // return returnEntityName;
   return stringInternalEntityStructure;
}

public int
AddWorkingDaysToDate( View   tgtView,
                      String tgtEntityName,
                      String tgtAttributeName,
                      int    lWorkingDays )
{
   int  lRegularDays;
   int  lRemainder;
   int  nRC;

   // Convert Working Days to Regular Days and add them to a Date Attribute.
   // To determine regular days, we will take working daYS and
   // simply multiply by 7/5, using remainder and not fraction.
   // Thus 8 working days is divided by 5 to get 1 with remainder 3.
   // We multiply the 1 by 7 and add the 3 to get 10.
   lRegularDays = lWorkingDays / 5;
   lRemainder   = lWorkingDays - (lRegularDays * 5);
   lRegularDays = (lRegularDays * 7) + lRemainder;

   nRC = AddToAttributeFromVariable( tgtView, tgtEntityName, tgtAttributeName,
                                     lRegularDays, zTYPE_INTEGER, 0, "Day" );
   return nRC;
}

public int
CompareAttributeByShortString( View   view,
                               String entityName,
                               String attributeName,
                               String compareValue )

{
    String  stringTempString = null;
    int nLth = 0;
    int nRC;

    stringTempString = GetVariableFromAttribute( stringTempString, nLth, zTYPE_STRING, 254,
                                                 view, entityName, attributeName, "", 0 );

// nLth = stringStringValue.length( );
   nRC = stringTempString.compareTo( compareValue );
   return nRC;
}

////////////////////////////////////////////////////////////////////////////////////////////////////
//
//  Method Name: ActivateWorkObjectFromFile
//
// Activate a work object from a file
//
public int
ActivateWorkObjectFromFile( zVIEW  vWorkView,
                            String stringFileName,
                            String stringLOD_Name,
                            View   ViewToWindow ) throws IOException
{
   int  file;
   int  nRC;

   // First make sure the file exists. If not, return an error code.
   file = m_KZOEP1AA.SysOpenFile( ViewToWindow, stringFileName, COREFILE_READ );
   if ( file == -1 )
      return -1;

   m_KZOEP1AA.SysCloseFile( ViewToWindow, file, 0 );

   // Next Activate the OI from the file just created.
   nRC = ActivateOI_FromFile( vWorkView, stringLOD_Name, ViewToWindow,
                              stringFileName, zSINGLE );
   return nRC;

} // ActivateWorkObjectFromFile

////////////////////////////////////////////////////////////////////////////////////////////////////
//
//  Method Name: AddDaysToDate
//
// Add Days To a Date Attribute
//
public int  // TODO
AddDaysToDate( View view, String entityName, String attributeName, int days )
{
  int nRC;

  nRC = AddToAttributeFromVariable( view, entityName,
                                    attributeName, days,
                                    zTYPE_INTEGER, 0, "Day" );
  return nRC;
}

////////////////////////////////////////////////////////////////////////////////////////////////////
//
//  Method Name: AddMonthsToDate
//
// Add Months To a Date Attribute
//
public int  // TODO
AddMonthsToDate( View view,
               String entityName,
               String attributeName,
               int months )  // Days or Months?
{
   int nRC;

   nRC = AddToAttributeFromVariable( view, entityName,
                                     attributeName, months,
                                     zTYPE_INTEGER, 0, "Month" );
   return nRC;
} // AddDaysToDate


////////////////////////////////////////////////////////////////////////////////////////////////////
//
//  Method Name: SeparateName
//
// Separate a name into first and last names
//
public int
SeparateName( String        fullName,
              StringBuilder firstName,
              StringBuilder lastName )
{
// String lpNext;
   int k = 0;
   int nLth = fullName.length( );

   String s = fullName.trim( );  // remove whitespace from beginning and end of FullName

   // Eliminate any "Mr." or "Ms." characters in front of the name and then point to first non-blank.
   if ( s.charAt( k ) == 'M' && (s.charAt( k + 1 ) == 'r' || s.charAt( k + 1 ) == 's') && s.charAt( k + 2 ) == '.' )
   {
      k = 3;
       while ( s.charAt( k ) == ' ' )
          k++;
   }

   // put original string into an array of chars
   //
// char[] charArray = new char[ nLth + 1 ];  // we are eliminating characters, so this should be plenty of room
   int j = 0;
   int nLastBegin = 0;
// int nFirstEnd = 0;
   while ( k <= nLth )
   {
      if ( nLastBegin == 0 )
      {
         if ( s.charAt( k ) == ' ' || s.charAt( k ) == '\t' )
         {
         // nFirstEnd = k;
            firstName.insert( j++, '\0' );  // terminate first name
            nLastBegin = k + 1;

            // Skip to next non-blank or end of string.
            while ( s.charAt( nLastBegin ) == ' ' || s.charAt( nLastBegin ) == '\t' )
               nLastBegin++;

            j = 0;  // process last name
            while ( nLastBegin <= nLth )
               lastName.insert( j++, s.charAt( nLastBegin++ ) );

            break;  // out of outer while loop
            }
         else
         {
            firstName.insert( j++, s.charAt( k++ ) );  // process the First Name (all chars to the first blank)
         }
      }
   }

   return 0;
} // SeparateName


////////////////////////////////////////////////////////////////////////////////////////////////////
//
//  Method Name: AddressFormatToMultiLine
//
// can be used for display or labels
//
public int
AddressFormatToMultiLine( String stringMultiLineAddress,
                          String stringLine1,
                          String stringLine2,
                          String stringLine3,
                          String stringCity,
                          String stringState,
                          String stringPostalCode )
{
   String stringFirstPart = null;

   if ( stringLine1.isEmpty( ) == false )
      stringFirstPart = zsprintf( stringFirstPart, "%s\n", stringLine1 );

   if ( stringLine2.isEmpty( ) == false )
      stringFirstPart = zsprintf( stringFirstPart, "%s%s\n", stringFirstPart, stringLine2 );

   if ( stringLine3.isEmpty( ) == false )
      stringFirstPart = zsprintf( stringFirstPart, "%s%s\n", stringFirstPart, stringLine3 );

   stringMultiLineAddress = zsprintf( stringMultiLineAddress, "%s\n%s, %s %s", stringFirstPart,
                                      stringCity, stringState, stringPostalCode );
   return 0;
} // AddressFormatToMultiLine

////////////////////////////////////////////////////////////////////////////////////////////////////
//
//  Method Name: dAdressLabel
//
// multiline address (without name)
//
public String
fnAdressLabelText( View  vAnyObject,  // BASED ON LOD "any object with entity that has address attributes"
                   String stringInternalEntityStructure,
                   String stringInternalAttribStructure,
                   String stringReturnText )
{
   String entityName = null;
   String stringAttribName = null;
   String stringMultiLineAddress = null;
   StringBuilder sb = new StringBuilder( "" );
   String stringAttn = null;
   String stringCity = null;
   String stringState = null;
   String stringZipCode = null;
   String stringZipCodeFormatted = null;
   String stringCountry = null;
   StringBuilder stringSep = null;      // set to /r/n or "; "

   zstrcpy( entityName, stringInternalEntityStructure );
   zstrcpy( stringAttribName, stringInternalAttribStructure );
   if ( ZeidonStringCompare( stringAttribName, 1, 5, "dLine", 1, 5, 33 ) == 0 )
      zstrcpy( stringSep, "; " );
   else
      zstrcpy( stringSep, "\r\n" );

   stringMultiLineAddress = "";
   stringAttn = "";
   sb.setCharAt( 0, '\0' );

   if ( IsValidAttribute ( "AttentionLine1", stringInternalEntityStructure ) == 0 )
      GetStringFromAttribute( sb, vAnyObject, entityName, "AttentionLine1" );

   if ( sb.length( ) != 0 )
   {
      stringAttn = zsprintf( stringAttn, "Attn:  %s%s", sb, stringSep );
      sb.setCharAt( 0, '\0' );
   }

   if ( IsValidAttribute ( "AttentionLine2", stringInternalEntityStructure ) == 0 )
      GetStringFromAttribute( sb, vAnyObject, entityName, "AttentionLine2" );

   if ( sb.length( ) != 0 )
   {
     stringAttn = zsprintf( stringAttn, "%s         %s%s", stringAttn, sb, stringSep );
      sb.setCharAt( 0, '\0' );
   }

   if ( stringAttn.length( ) != 0 )
      ZeidonStringCopy( stringMultiLineAddress, 1, 0, stringAttn, 1, 0, 2000 );

   if ( IsValidAttribute ( "Line1", stringInternalEntityStructure ) == 0 )
      GetStringFromAttribute( sb, vAnyObject, entityName, "Line1" );

   if ( sb.length( ) != 0 )
      zsprintf( stringMultiLineAddress, "%s%s%s", stringMultiLineAddress, sb, stringSep );

   if ( IsValidAttribute ( "Line2", stringInternalEntityStructure ) == 0 )
      GetStringFromAttribute( sb, vAnyObject, entityName, "Line2" );
   else
      sb.setCharAt( 0, '\0' );

   if ( sb.length( ) != 0 )
      stringMultiLineAddress = zsprintf( stringMultiLineAddress, "%s%s%s", stringMultiLineAddress, sb, stringSep );

   if ( IsValidAttribute ( "Line3", stringInternalEntityStructure ) == 0 )
      GetStringFromAttribute( sb, vAnyObject, entityName, "Line3" );
   else
      sb.setCharAt( 0, '\0' );

   if ( sb.length( ) != 0 )
      stringMultiLineAddress = zsprintf( stringMultiLineAddress, "%s%s%s", stringMultiLineAddress, sb, stringSep );

   stringCity = GetStringFromAttribute( stringCity, vAnyObject, entityName, "City" );
   //GetStringFromAttribute( stringState, vAnyObject, entityName, "State" );
   stringState = GetVariableFromAttribute( stringState, 0, zTYPE_STRING, 120,
                                       vAnyObject, entityName,
                                       "StateProvince", "State", 0 );
   if ( stringState.length( ) == 0 )
   {
      if ( IsValidAttribute ( "InternationalRegion", stringInternalEntityStructure ) == 0 )
        stringState = GetVariableFromAttribute( stringState, 0, zTYPE_STRING, 120,
                                              vAnyObject, entityName,
                                              "InternationalRegion", "", 0 );
   }

   // For ZipCodes larger than five characters, we want to format them with a
   // dash, if they don't already have a dash.
   stringZipCode = GetVariableFromAttribute( stringZipCode, 0, zTYPE_STRING, 11,
                                        vAnyObject, entityName, "PostalCode", "", 0 );
   if ( stringZipCode.length( ) > 5 && stringZipCode.charAt( 5 ) != '-' )
        stringZipCodeFormatted = stringZipCode.substring( 0, 4 ) + "-" + stringZipCode.substring( 5, -1 );
   else
     stringZipCodeFormatted = stringZipCode;

   stringCountry = "";
   if ( IsValidAttribute ( "Country", stringInternalEntityStructure ) == 0 )
      GetStrFromAttrByContext( sb, 50, vAnyObject, entityName, "Country", "Country" );

   if ( sb.equals( "US" ) == true )
      stringCountry = "";
   else
      stringCountry = sb.toString( );

   if ( stringCity.length( ) != 0 )
      stringMultiLineAddress = zsprintf( stringMultiLineAddress, "%s%s, %s %s %s", stringMultiLineAddress,
                                         stringCity, stringState, stringZipCodeFormatted, stringCountry );
   else
      stringMultiLineAddress = zsprintf( stringMultiLineAddress, "%s%s %s %s", stringMultiLineAddress,
                                         stringState, stringZipCodeFormatted, stringCountry );

   stringReturnText = ZeidonStringCopy( stringReturnText, 1, 0, stringMultiLineAddress, 1, 0, 255 );

   return stringReturnText;
} // fnAdressLabelText

////////////////////////////////////////////////////////////////////////////////////////////////////
//
//  Method Name: dAdressLabel
//
// multiline address (without name)
//
public int /* DERIVED ATTRIBUTE */
dAdressLabel( View  vAnyObject,  // BASED ON LOD "any object with entity that has address attributes"
              String stringInternalEntityStructure,
              String stringInternalAttribStructure,
              int nGetOrSetFlag )
{
   String stringMultiLineAddress = null;
   String entityName = null;

   entityName = zstrcpy( entityName, stringInternalEntityStructure );
   if ( CheckExistenceOfEntity( vAnyObject, entityName ) != 0)
   {
      return 0;
   }

   stringMultiLineAddress = fnAdressLabelText( vAnyObject, stringInternalEntityStructure,
                                             stringInternalAttribStructure, stringMultiLineAddress );

   StoreStringInRecord( vAnyObject, stringInternalEntityStructure,
                        stringInternalAttribStructure, stringMultiLineAddress );

   return 0;
} // dAdressLabel

////////////////////////////////////////////////////////////////////////////////////////////////////
//
//  Method Name: dAdressLabel
//
// multiline address (with name)
//
public int /* DERIVED ATTRIBUTE */
dAdressLabelFull( View  vAnyObject,  // BASED ON LOD "any object with entity that has address attributes"
                  String stringInternalEntityStructure,
                  String stringInternalAttribStructure,
                  int nGetOrSetFlag )
{
   String entityName = null;
   String stringMultiLineAddress;
   String string = null;
   String stringCompanyName;

   entityName = zstrcpy( entityName, stringInternalEntityStructure );
   if ( CheckExistenceOfEntity( vAnyObject, entityName ) != 0 )
   {
      return 0;
   }

   stringMultiLineAddress = "";
   stringCompanyName = "";
   if ( IsValidAttribute ( "CompanyName", stringInternalEntityStructure ) == 0 )
      stringCompanyName = GetStringFromAttribute( stringCompanyName, vAnyObject, entityName, "CompanyName" );

   if ( stringCompanyName.length( ) != 0 )
      stringMultiLineAddress = zsprintf( stringMultiLineAddress, "%s\r\n", stringCompanyName );

   string = fnAdressLabelText( vAnyObject, stringInternalEntityStructure, stringInternalAttribStructure, string );

   stringMultiLineAddress = ZeidonStringConcat( stringMultiLineAddress, 1, 0, string, 1, 0, 2000 );

   StoreStringInRecord( vAnyObject, stringInternalEntityStructure,
                        stringInternalAttribStructure, stringMultiLineAddress );

   return 0;
} // dAdressLabelFull

////////////////////////////////////////////////////////////////////////////////////////////////////
//
//  Method Name: PersonName_LastFirstMiddle
//
// Person's name formatted Last Name first
//
public int /* DERIVED ATTRIBUTE */
PersonName_LastFirstMiddle( View  vAnyObject,  // BASED ON LOD "any object with entity that has "NAME" attributes"
                            String stringInternalEntityStructure,
                            String stringInternalAttribStructure,
                            int nGetOrSetFlag )
{
   String stringLastFirstMiddle;
   String entityName = null;
   String string;

   entityName = zstrcpy( entityName, stringInternalEntityStructure );
   stringLastFirstMiddle = "";
   string = "";

   // Last Name
   if ( IsValidAttribute( "LastName", stringInternalEntityStructure ) == 0 )
      stringLastFirstMiddle = GetStringFromAttribute( stringLastFirstMiddle, vAnyObject, entityName, "LastName" );

   // First Name
   if ( IsValidAttribute( "FirstName", stringInternalEntityStructure ) == 0 )
      string = GetStringFromAttribute( string, vAnyObject, entityName, "FirstName" );

   if ( string.length( ) != 0 )
   {
      stringLastFirstMiddle = ZeidonStringConcat( stringLastFirstMiddle, 1, 0, ", ", 1, 0, 101 );
      stringLastFirstMiddle = ZeidonStringConcat( stringLastFirstMiddle, 1, 0, string, 1, 0, 101 );
      string = "";
   }

   // Middle Name
   if ( IsValidAttribute ( "MiddleName", stringInternalEntityStructure ) == 0 )
      string = GetStringFromAttribute( string, vAnyObject, entityName, "MiddleName" );

   if ( string.length( ) != 0 )
   {
      stringLastFirstMiddle = ZeidonStringConcat( stringLastFirstMiddle, 1, 0, " ", 1, 0, 101 );
      stringLastFirstMiddle = ZeidonStringConcat( stringLastFirstMiddle, 1, 0, string, 1, 0, 101 );
      if ( string.length( ) == 1 )
         stringLastFirstMiddle = ZeidonStringConcat( stringLastFirstMiddle, 1, 0, ".", 1, 0, 101 );

      string = "";
   }

   // Suffix
   if ( IsValidAttribute ( "Suffix", stringInternalEntityStructure ) == 0 )
      string = GetStringFromAttribute( string, vAnyObject, entityName, "Suffix" );

   if ( zstrlen( string ) != 0 )
   {
      stringLastFirstMiddle = ZeidonStringConcat( stringLastFirstMiddle, 1, 0, " ", 1, 0, 101 );
      stringLastFirstMiddle = ZeidonStringConcat( stringLastFirstMiddle, 1, 0, string, 1, 0, 101 );
      string = "";
   }

   StoreStringInRecord( vAnyObject, stringInternalEntityStructure,
                        stringInternalAttribStructure, stringLastFirstMiddle );

   return 0;
} // dAdressLabel

////////////////////////////////////////////////////////////////////////////////////////////////////
//
//  Method Name: PersonName_FirstMiddleLast
//
// Person's name formatted Last Name last
//
public int /* DERIVED ATTRIBUTE */
PersonName_FirstMiddleLast( View  vAnyObject,  // BASED ON LOD "any object with entity that has "NAME" attributes"
                            String stringInternalEntityStructure,
                            String stringInternalAttribStructure,
                            int nGetOrSetFlag )
{
   StringBuilder stringReturnName = new StringBuilder( );
   String entityName = null;
   StringBuilder string = new StringBuilder( );

   entityName = zstrcpy( entityName, stringInternalEntityStructure );
   stringReturnName.append("");
   string.append("");

   // Last Name
   if ( IsValidAttribute ( "FirstName", stringInternalEntityStructure ) == 0 )
      GetStringFromAttribute( stringReturnName, vAnyObject, entityName, "FirstName" );

   // Middle Name
   if ( IsValidAttribute ( "MiddleName", stringInternalEntityStructure ) == 0 )
      GetStringFromAttribute( string, vAnyObject, entityName, "MiddleName" );
   if ( zstrlen( string ) != 0 )
   {
      ZeidonStringConcat( stringReturnName, 1, 0, " ", 1, 0, 101 );
      ZeidonStringConcat( stringReturnName, 1, 0, string, 1, 0, 101 );
      if ( zstrlen( string ) == 1 )
         ZeidonStringConcat( stringReturnName, 1, 0, ".", 1, 0, 101 );

      string.append("");
   }

   // Last Name
   if ( IsValidAttribute ( "LastName", stringInternalEntityStructure ) == 0 )
      GetStringFromAttribute( string, vAnyObject, entityName, "LastName" );

   if ( zstrlen( string ) != 0 )
   {
      ZeidonStringConcat( stringReturnName, 1, 0, " ", 1, 0, 101 );
      ZeidonStringConcat( stringReturnName, 1, 0, string, 1, 0, 101 );
      string.append("");
   }

   // Suffix
   if ( IsValidAttribute ( "Suffix", stringInternalEntityStructure ) == 0 )
      GetStringFromAttribute( string, vAnyObject, entityName, "Suffix" );

   if ( zstrlen( string ) != 0 )
   {
      ZeidonStringConcat( stringReturnName, 1, 0, " ", 1, 0, 101 );
      ZeidonStringConcat( stringReturnName, 1, 0, string, 1, 0, 101 );
      string.append("");
   }

   StoreStringInRecord( vAnyObject, stringInternalEntityStructure,
                        stringInternalAttribStructure, stringReturnName.toString() );

   return 0;
} // dAdressLabel

////////////////////////////////////////////////////////////////////////////////////////////////////
//
//  Method Name: zTrim
//
// trim whitespace from front and back
//
// Remove whitespace at the beginning and end of a string.
//
public int
zTrim( StringBuilder stringStringInOut )
{
    String s = StringUtils.trim( stringStringInOut.toString() );
    stringStringInOut.replace( 0, stringStringInOut.length(), s );
    return 0;
} // zTrim

public String
zTrim( String stringStringInOut )
{
   return stringStringInOut.trim( );
} // zTrim

////////////////////////////////////////////////////////////////////////////////////////////////////
//
//  Method Name: SetDecimalPrecisionRounded
//
public Double
SetDecimalPrecisionRounded( Double pdDecimalValue,
                            int ulNumberOfDecimals )
{
   StringBuilder sb = new StringBuilder( ulNumberOfDecimals > 0 ? ulNumberOfDecimals + 5 : 25 );

   SysConvertDecimalToString( pdDecimalValue, sb, (int) ulNumberOfDecimals );
   MutableDouble d = new MutableDouble( pdDecimalValue );
   SysConvertStringToDecimal( sb.toString( ), d );
   return d.toDouble( );

}  // SetDecimalPrecisionRounded

////////////////////////////////////////////////////////////////////////////////////////////////////
//
//  Method Name: RemoveLeadingBlanksFromAttrib
//
public int
RemoveLeadingBlanksFromAttrib( View   view,
                               String stringEntity,
                               String stringAttribute )
{
   StringBuilder sb = new StringBuilder( 256 );
   MutableInt i = new MutableInt( 0 );
   int k;

   // Remove any leading blanks from the attribute.

   GetVariableFromAttribute( sb, i, zTYPE_STRING, 253,
                             view, stringEntity, stringAttribute, "", 0 );
   if ( sb.charAt( 0 ) == ' ' )
   {
      k = 1;

      while ( sb.charAt( k ) == ' ' )
         k++;

      SetAttributeFromString( view, stringEntity, stringAttribute, sb.substring( k ) );
   }

   return 0;

} // RemoveLeadingBlanksFromAttrib

////////////////////////////////////////////////////////////////////////////////////////////////////
//
//  Method Name: RemoveLeadingZerosFromAttrib
//
public int
RemoveLeadingZerosFromAttrib( View   view,
                              String stringEntity,
                              String stringAttribute )
{
   StringBuilder sb = new StringBuilder( 256 );
   MutableInt i = new MutableInt( 0 );
   int k;

   // Remove any leading zeros from the attribute.

   GetVariableFromAttribute( sb, i, zTYPE_STRING, 253,
                             view, stringEntity, stringAttribute, "", 0 );
   if ( sb.charAt( 0 ) == '0' )
   {
      k = 1;

      while ( sb.charAt( k ) == '0' )
         k++;

      SetAttributeFromString( view, stringEntity, stringAttribute, sb.substring( k ) );
   }

   return 0;

} // RemoveLeadingZerosFromAttrib

////////////////////////////////////////////////////////////////////////////////////////////////////
//
//  Method Name: FindStringInAttribute
//
//    Find a string within a string attribute. Not case sensitive.
//
////////////////////////////////////////////////////////////////////////////////////////////////////
public int
FindStringInAttribute( String stringSearchString,
                       View   view,
                       String entityName,
                       String attributeName )
{
   String stringAttributeValue = null;
   String stringFoundValue;

   // Look for a match on the string stringSearchString within the attribute.
   // Return 0 if found.
   // Return -1 if not found.
   stringAttributeValue = GetStringFromAttribute( stringAttributeValue, view, entityName, attributeName );
   stringFoundValue = zstrstr( stringAttributeValue, stringSearchString );
   if ( stringFoundValue == null )
      return -1;  // the string was not found
   else
      return 0;   // the string was found

} // FindStringInAttribute

public int
ConvertExternalValueOfAttribute( StringBuilder lpReturnedString,
                                 String srcString,
                                 View   lpView,
                                 String entityName,
                                 String attributeName )
{
   // TODO - Convert code from "C"
   return( 0 );
}

////////////////////////////////////////////////////////////////////////////////////////////////////
//
//  Method Name: AddSpacesToString
//
//    Insert spaces within a Zeidon string name where capital letters exist.
//
////////////////////////////////////////////////////////////////////////////////////////////////////
public String
AddSpacesToString( String stringZeidonName  )
{
   StringBuilder sb = new StringBuilder( stringZeidonName );
   int k;

   for ( k = 1; k < sb.length( ); k++ )
   {
      if ( sb.charAt( k ) >= 'A' && sb.charAt( k ) <= 'Z' )
      {
         sb.insert( k, ' ' );
         k++;
      }
   }

   return sb.toString( );

} // AddSpacesToString

//
int
ParseOutEntityAttribute( String entityDotAttribute,
                         StringBuilder entityName,
                         StringBuilder attributeName )
{
   int k;
   int lSkipLth;

   // Initialize entityName and attributeName.
   entityName.replace( 0, -1, entityDotAttribute );
   attributeName.delete( 0, -1 );
         // entityDotAttribute is pointing to the first character of the entity name on entry to this routine.
   // Parse out Entity Name

   for ( k = 0; k < entityName.length( ); k++ )
   {
      char ch = entityName.charAt( k );
      if ( ch == '.' || ch == ']' || ch == '}' )
      {
         entityName.setCharAt( k, '\0' );
         if ( ch == '}' )
           return -2;

         if ( ch != ']' )  // there is an attribute, so keep going
         {
            int j = 0;
            k++;

               // Parse out Attribute Name
            ch = entityDotAttribute.charAt( k );
            while ( ch != ']' && ch != '}' )
            {
               if ( ch == '}' )
                  return -2;

               attributeName.setCharAt( j, ch );
               j++;
               k++;
               ch = entityDotAttribute.charAt( k );
            }

            attributeName.setCharAt( k, '\0' );
         }
      }
   }

   lSkipLth = k + 1;  // TODO not sure this translation to java is exactly right for SkipLth
   return lSkipLth;
}

int
ConvertCharacterString( StringBuilder sbTarget,
                  StringBuilder sbSource,
                  StringBuilder sbOrigMemory,
                        int  nFileType )  // 1-Text   2-HTML
{
   char   ch;
   int    lTabCount;
   int    i;  // index to sbTarget
   int    j;  // index to sbSource
   int    k;

   // This code checks for "carriage return/line feed" combinations in the
   // text and inserts the correct \par and \tab strings in the target text.
// pchTarget = *sbTarget;

   // First, determine if the start of the text is preceded by tab characters and if so, count them.
   lTabCount = 0;

   /** TODO - figure this out and implement java version
   String pchBack = sbOrigMemory - 5;
   while ( zstrncmp( sbOrigMemory, "\\tab", 4 ) == 0 )
   {
      lTabCount++;
      pchBack = pchBack - 5;
   }
    **/

   // Copy the characters, inserting \par and \tab strings as necessary for new lines.
   for ( i = 0, j = 0; (ch = sbSource.charAt( j )) != '\0'; j++ )
   {
      // Search for carriage return/line feed and insert \par and \tab strings.
      if ( ch == 13 && sbSource.charAt( j + 1 ) == 10 )
      {
         // Copy carriage control and line feed characters.
        sbTarget.setCharAt( i++ , sbSource.charAt( j++ ) );
        sbTarget.setCharAt( i++ , sbSource.charAt( j++ ) );

         // Insert \par and \tab characters.
         if ( nFileType == 1 )
         {
            i = zstrcpy( sbTarget, i, "\\par " );
         }
         else
         {
            i = zstrcpy( sbTarget, i, "<br />" );
         }

         for ( k = 0; k < lTabCount; k++ )
         {
            i = zstrcpy( sbTarget, i, "\\tab " );
         }
      }
      else
      {
       sbTarget.setCharAt( i++ , sbSource.charAt( j++ ) );
      }
   }

   sbTarget.setCharAt( i++ , '\0' );

   return( 0 );
}

int
ReadFileDataIntoMemory( View    vResultSet,
                        String  stringDocumentFile,
                        long    hDocumentMemory,
                        StringBuilder sbDocumentData ) throws IOException
{
   int  hDocumentFile;
   int  lDocumentLth;

   hDocumentMemory = 0;
   sbDocumentData.setLength( 0 );
   lDocumentLth = 0;

   hDocumentFile = m_KZOEP1AA.SysOpenFile(vResultSet, stringDocumentFile, COREFILE_READ);

   if ( hDocumentFile < 0 )
   {
   // IssueError( vResultSet, 0, 0, "Can't open Document file." );
      return -1;
   }

   lDocumentLth = m_KZOEP1AA.SysGetFileSize( vResultSet, hDocumentFile );

   // Exit if the document file is empty.
   if ( lDocumentLth == 0 )
   {
	   m_KZOEP1AA.SysCloseFile( vResultSet, hDocumentFile, 0 );
      return 0;
   }

   m_KZOEP1AA.SysReadFile( vResultSet, hDocumentFile, sbDocumentData, lDocumentLth );
   m_KZOEP1AA.SysCloseFile( vResultSet, hDocumentFile, 0 );


  /* if ( sbDocumentData == null )
   {
      sbDocumentData = null;
      lDocumentLth = 0;
      IssueError( vResultSet, 0, 0, "Read Error on Document file" );
      return -1;
   }*/

   return lDocumentLth;
}

public int
ParseBooleanExpression( View zqFrame )
{
	   //zPCHAR pchValue;
	   //zPCHAR pchNext;
	   String  szBooleanExpression=null;
	   StringBuilder sbBooleanExpression=null;
	   String  szConditionValue=null;

	   // Parse the Boolean Expression and create each component value as an entity Component.

	   GetStringFromAttributeByContext( sbBooleanExpression,
	                                    zqFrame, "BooleanExpression",
	                                    "TextValue", "", 254 );
	   return 0;
}

////////////////////////////////////////////////////////////////////////////////////////////////////
//
//  Method Name: ReadLine5000
//
//    Read a line into a 5000 character string
//
////////////////////////////////////////////////////////////////////////////////////////////////////
public int
ReadLine5000( View   ViewToWindow,
              StringBuilder sbLineBuffer,
              int    FileHandle ) throws IOException
{
   int nRC = 0;

   nRC = m_KZOEP1AA.SysReadLine( ViewToWindow, sbLineBuffer, FileHandle );
   if ( sbLineBuffer.length( ) == 0 )
      return 0;

   if ( sbLineBuffer.length( ) > 5000 )
   {
      TraceLineS( "////////////* > 5000", "//////////*" );
      sbLineBuffer.setCharAt( 5000, '\0' );
   }

   return nRC;

} // ReadLine5000

////////////////////////////////////////////////////////////////////////////////////////////////////
//
//  Method Name: ConvertLineToEntity
//
//    Convert data in a comma or tab delimited record to attribute values in an entity.
//
////////////////////////////////////////////////////////////////////////////////////////////////////
public int
ConvertLineToEntity( View   vTarget,
                     View   vXOD,
                     String stringRecord,
                     String stringDelimiterType,
                     int    lMaxRecordLth )
{
   return 0;
} // ConvertLineToEntity

public int SetAttrFromStrByContext( View view, String entityName, String attributeName, String value, String context )
{
   int      RESULT = 0;
   view.cursor( entityName ).setAttribute( attributeName, value, context );
   RESULT = 0;
   return RESULT;
}

////////////////////////////////////////////////////////////////////////////////////////////////////
//
//  Method Name: GetCurrentApplicationName
//
////////////////////////////////////////////////////////////////////////////////////////////////////
public int
GetCurrentApplicationName( StringBuilder stringReturnedString,
                           int    lMaxLength,
                           View   ViewToWindow )
{
   // LPAPP  stringApp;

   return SfGetApplicationForSubtask( stringReturnedString, ViewToWindow );
}

// GetCurrentApplicationName

////////////////////////////////////////////////////////////////////////////////////////////////////
//
//  Method Name: DBQualEntityByString
//
////////////////////////////////////////////////////////////////////////////////////////////////////
public int
DBQualEntityByString( View   vQualObject,
                      String entityName,
                      String attributeName,
                      String operationName,
                      String value,
                      int  bExists )
{
  if ( entityName.length() == 0 )
     entityName = null;

  if ( attributeName.length() == 0 )
     attributeName = null;

  if ( operationName.length() == 0 )
     operationName = null;

  if ( value.length() == 0 )
     value = null;

   // add qualification
   CreateEntity( vQualObject, "QualAttrib", zPOS_AFTER );
   SetAttributeFromString( vQualObject, "QualAttrib", "EntityName", entityName );
   if ( bExists == TRUE )
   {
      SetAttributeFromString( vQualObject, "QualAttrib", "Oper", "EXISTS" );
      CreateEntity( vQualObject, "SubQualAttrib", zPOS_AFTER );
      SetAttributeFromString( vQualObject, "SubQualAttrib", "EntityName", entityName );
      SetAttributeFromString( vQualObject, "SubQualAttrib", "AttributeName", attributeName );
      SetAttributeFromString( vQualObject, "SubQualAttrib", "Value", value );
      SetAttributeFromString( vQualObject, "SubQualAttrib", "Oper", operationName );
   }
   else
   {
      SetAttributeFromString( vQualObject, "QualAttrib", "AttributeName", attributeName );
      SetAttributeFromString( vQualObject, "QualAttrib", "Value", value );
      SetAttributeFromString( vQualObject, "QualAttrib", "Oper", operationName );
   }

   return 0;
} // DBQualEntityByString

////////////////////////////////////////////////////////////////////////////////////////////////////
//
//  Method Name: ReturnSuffixOfFileName
//
////////////////////////////////////////////////////////////////////////////////////////////////////
public String
ReturnSuffixOfFileName( String stringReturnedSuffix,
                        String stringFileName )
{
   int nPosition;

   nPosition = zstrrchr( stringFileName, '.' );  // find last period
   if ( nPosition >= 0 )  // if we found the last period ...
   {
      stringReturnedSuffix = stringFileName.substring( nPosition + 1 );  // ... we have our ext!
      stringReturnedSuffix = stringReturnedSuffix.toLowerCase( );
   }
   else
      stringReturnedSuffix = "";  // initialize to empty extension

   return stringReturnedSuffix;

} // ReturnSuffixOfFileName

/////////////////////////////////////////////////////////////////////////////
//
//  Method Name: SubSectionShowHideDisableTabs
//
/////////////////////////////////////////////////////////////////////////////
public int
SubSectionShowHideDisableTabs( View    mUser,
                               View    vSubtask,
                               String  stringTabTag )
{
   return 0;  // TODO fnSubSectionShowHideDisableTabs( mUser, vSubtask, stringTabTag );
}

////////////////////////////////////////////////////////////////////////////////////////////////////
//
//  Method Name: WL_QC
//
//  PURPOSE:    This routine Converts an instance of a special character in
//              a buffer and then writes out the buffer. The character to
//              be translated is stringTransChar and any instance of it is
//              converted to a double quote.
//
//  PARAMETERS: lFile - File handle
//              stringBuffer - the string to be converted.
//              stringTransChar - The character to be converted to a quote.
//              nAddBlankLineCnt - Number of blank lines to append.
//
////////////////////////////////////////////////////////////////////////////////////////////////////
public int
WL_QC( int    lFile,
       String stringInput,
       String stringTransChar,
       int    nBlankLineCnt ) throws IOException
{
   View   taskView = null; // TODO ??? = GetDefaultViewForActiveTask( );

   stringInput = stringInput.replaceAll( stringTransChar, "\"" );
   m_KZOEP1AA.SysWriteLine( taskView, lFile, stringInput );
   while ( nBlankLineCnt-- > 0 )
	   m_KZOEP1AA.SysWriteLine( taskView, lFile, "" );

   return 0;
}


int
AddAttributeToCSV( CharBuffer cb, int nLth, View  lLibPers,
                   String entityName, String attributeName, boolean bNumeric )
{
    String s = null;

   cb.put( 0, '"' );  // opening quote

// if ( bNumeric )
// {
      s = GetStringFromAttribute( s, lLibPers, entityName, attributeName );
      nLth = zstrcpy( cb, 1, s );
// }
// else
// {
//    String stringAttrib;
//
//    GetAddrForAttribute( &stringAttrib, lLibPers, entityName, stringAttribute );
//    zstrcpy( stringBuffer, stringAttrib );
// }


   s = cb.toString( );
   if ( s.indexOf( '"', 1 ) > 0 )
   {
      s = s.replace( "\"", "\"\" " );  // double any quotes ...
      s = s.substring( 1 );            // except the first one
   }

   s += "\",";   // terminating quote plus comma
   nLth = zstrcpy( cb, 0, s );
   return nLth;
}

int
WriteCSV_RecordFromEntity( View  lLibPers, String entityName, int lFile ) throws IOException
{
   CharBuffer charBuffer = CharBuffer.allocate( 32000 );
   int nLth;

   charBuffer.put( 0, '"' );
   charBuffer.put( 1, entityName.charAt( 0 ) );  // S E P (Student Employee Prospect)
   charBuffer.put( 2, '"' );
   charBuffer.put( 3, ',' );
   nLth = 4;

   nLth += AddAttributeToCSV( charBuffer, nLth, lLibPers, entityName,
                              "Status", false );
   nLth += AddAttributeToCSV( charBuffer, nLth, lLibPers, "Person",
                              "CampusID", false );
   nLth += AddAttributeToCSV( charBuffer, nLth, lLibPers, "Person",
                              "ID", true );
   nLth += AddAttributeToCSV( charBuffer, nLth, lLibPers, "Person",
                              "LastName", false );
   nLth += AddAttributeToCSV( charBuffer, nLth, lLibPers, "Person",
                              "FirstName", false );
   nLth += AddAttributeToCSV( charBuffer, nLth, lLibPers, "Person",
                              "MiddleName", false );
   nLth += AddAttributeToCSV( charBuffer, nLth, lLibPers, "Person",
                              "Suffix", false );
   nLth += AddAttributeToCSV( charBuffer, nLth, lLibPers, "Person",
                              "PreferedFirstName", false );
   nLth += AddAttributeToCSV( charBuffer, nLth, lLibPers, "Person",
                              "Gender", false );
   nLth += AddAttributeToCSV( charBuffer, nLth, lLibPers, "Person",
                              "MaritalStatus", false );
   nLth += AddAttributeToCSV( charBuffer, nLth, lLibPers, "Person",
                              "HomePhone", false );
   nLth += AddAttributeToCSV( charBuffer, nLth, lLibPers, "Person",
                              "WorkPhone", false );
   nLth += AddAttributeToCSV( charBuffer, nLth, lLibPers, "Person",
                              "Extension", false );
   nLth += AddAttributeToCSV( charBuffer, nLth, lLibPers, "Person",
                              "eMailAddress", false );
   nLth += AddAttributeToCSV( charBuffer, nLth, lLibPers, "Person",
                              "DateOfBirth", false );
   if ( CheckExistenceOfEntity( lLibPers, "Address" ) == 0 )
   {
      nLth += AddAttributeToCSV( charBuffer, nLth, lLibPers, "Address",
                                 "Line1", false );
      nLth += AddAttributeToCSV( charBuffer, nLth, lLibPers, "Address",
                                 "City", false );
      nLth += AddAttributeToCSV( charBuffer, nLth, lLibPers, "Address",
                                 "StateProvince", false );
      nLth += AddAttributeToCSV( charBuffer, nLth, lLibPers, "Address",
                                 "PostalCode", false );
      nLth += AddAttributeToCSV( charBuffer, nLth, lLibPers, "Address",
                                 "Country", false );
   }
   else
   {
     charBuffer.put(  nLth++, ',' );
     charBuffer.put(  nLth++, ',' );
     charBuffer.put(  nLth++, ',' );
     charBuffer.put(  nLth++, ',' );
     charBuffer.put(  nLth++, ',' );
   }

   nLth += AddAttributeToCSV( charBuffer, nLth, lLibPers, entityName,
                              "ID", false );
   nLth += AddAttributeToCSV( charBuffer, nLth, lLibPers, entityName,
                              "eMailAddress", false );
   if ( entityName.charAt( 0 ) == 'S' )
   {
      nLth += AddAttributeToCSV( charBuffer, nLth, lLibPers, entityName,
                                 "CurrentLevel", false );
      nLth += AddAttributeToCSV( charBuffer, nLth, lLibPers, "AdministrativeDivision",
                                 "Name", false );
      nLth += AddAttributeToCSV( charBuffer, nLth, lLibPers, entityName,
                                 "ClearingHouseGradDate", false );
   }
   else
   if ( entityName.charAt( 0 ) == 'P' )
   {
      nLth += AddAttributeToCSV( charBuffer, nLth, lLibPers, entityName,
                                 "ExpectedEntryTerm", false );
      nLth += AddAttributeToCSV( charBuffer, nLth, lLibPers, entityName,
                                 "ExpectedEntryYear", false );
   }

   if ( nLth > 0 && charBuffer.get( nLth - 1 ) == ',' )
     charBuffer.put(  nLth - 1, '\0' );  // drop terminating ',' and null terminate
   else
     charBuffer.put(  nLth++, '\0' );    // ensure null termination

   m_KZOEP1AA.SysWriteLine( lLibPers, lFile, charBuffer.toString( ) );
   return nLth;
}

////////////////////////////////////////////////////////////////////////////////////////////////////
//
//  Method Name: SetEntityCursorByInteger
//    Does a SetEntityCursor for an Integer, since the regular SetEntityCursor doesn't work for
//    an integeri in VML.
//
////////////////////////////////////////////////////////////////////////////////////////////////////
public int
SetEntityCursorByInteger( View   view,
                          String entityName,
                          String attributeName,
                          int    cursorPosition,
                          int    lIntegerSearchValue,
                          String stringScopingEntityName )
{
   int nRC;

   nRC = SetEntityCursor( view, entityName, attributeName, cursorPosition, lIntegerSearchValue,
                          "", "", 0, stringScopingEntityName, "" );
   return nRC;
} // SetEntityCursorByInteger

public int
PositionOnEntityByZID( View   vLDD,
                       String pchZID )
{
   String pchDot;
   int    nDot1;
   int    nDot2;
   int    nRC;

   if ( pchZID == null || pchZID.isEmpty( ) )
      return 0;

   nDot1 = zstrchr( pchZID, '.' );
   if ( nDot1 >= 0 )
      pchDot = pchZID.substring( 0, nDot1 - 1 );
   else
      pchDot = pchZID;

   nRC = SetCursorFirstEntityByString( vLDD, "MasterLabelContent", "ID", pchDot, "" );
   if ( nRC == zCURSOR_SET )
   {
      if ( nDot1 < 0 )
         return 1;  // found MasterLabelContent

      nDot1++;
      nDot2 = zstrchr( pchZID, nDot1, '.' );
      if ( nDot2 >= 0 )
         pchDot = pchZID.substring( nDot1, nDot2 - 1 );
      else
         pchDot = pchZID.substring( nDot1 );

      nRC = SetCursorFirstEntityByString( vLDD, "MasterLabelSection", "ID", pchDot, "" );
      if ( nRC == zCURSOR_SET )
      {
         if ( nDot2 < 0 )
            return 2;  // found MasterLabelSection

         pchDot = pchZID.substring( nDot2 + 1 );
         nRC = SetCursorFirstEntityByString( vLDD, "MasterLabelParagraph", "ID", pchDot, "" );
         if ( nRC == zCURSOR_SET )
            return 3;  // found MasterLabelParagraph
      }
   }

   return -1;  // did not find proper entity
}

/////////////////////////////////////////////////////////////////////////////
//
// OPERATION: DetermineNextVersion
//
// Determine the next available version.  If the current version is 1.2 and
// version 1.3 exists, the next available version is 1.2.1, otherwise, the
// next available version is 1.3.
//
// Algorithm:  Match dots and sub-versions.  If second runs out of dots or
//      has a greater corresponding sub-version, simply increment the final
//      sub-version of the first version as the new version.  Otherwise, keep
//      adding the second's sub-version on to the end if the first and, if
//      necessary, add a new sub-version.
//
/////////////////////////////////////////////////////////////////////////////
public String
DetermineNextVersion( String  pchVersionNew,
                      View    vListVersionIn,
                      String  cpcListVersionEntity,
                      String  cpcListVersionAttribute )
{
   // "SubregLabelContent", "Version", mSubProd, "SubregLabelContent", "Version" )
   zVIEW  vListVersion = null;
   String pch;
   String pchDot1;
   String pchDot2;
   String pchDotNext1;
   String pchDotNext2;
   String szVersion = null;
   String szVersionNext = null;
   int    lVersion = 0;
   int    lVersionNext = 0;
   int    nDot1 = 0;
   int    nDot2 = 0;
   int    nDotNext2 = 0;
   int    nDotCnt = 0;
   int    nDotCntNext = 0;
   int    nRC;

   CreateViewFromView( vListVersion, vListVersionIn );
   szVersion = GetStringFromAttribute( szVersion, vListVersion, cpcListVersionEntity, cpcListVersionAttribute );
   nRC = SetCursorNextEntity( vListVersion, cpcListVersionEntity, "" );
   if ( nRC == zCURSOR_SET )
      szVersionNext = GetStringFromAttribute( szVersionNext, vListVersion, cpcListVersionEntity, cpcListVersionAttribute );
   else
      szVersionNext = "";

   /* test stuff
   zstrcpy( szVersion, "1.2" );
   zstrcpy( szVersionNext, "1.3" );
   zstrcpy( szVersion, "1.2" );
   zstrcpy( szVersionNext, "1.2.1" );
   zstrcpy( szVersion, "1.2" );
   zstrcpy( szVersionNext, "2" );
   zstrcpy( szVersion, "1" );
   zstrcpy( szVersionNext, "" );
   end of test stuff */

   // Count the number of Dots and compare the sub-versions in the two versions and determine
   // where the versions quit matching up.
   pchDot1 = szVersion;
   pchDotNext1 = szVersionNext;
   do
   {
      nDot2 = zstrchr( pchDot1, '.' );
      nDotNext2 = zstrchr( pchDotNext1, '.' );
      if ( nDot2 >= 0 ) // still more versions for first
      {
         nDotCnt++;
         pch = pchDot1.substring( 0, nDot2 - 1 );
         lVersion = zatol( pch );
         pchDot1 = pchDot1.substring( 0, nDot2 + 1 );
      }
      else
      {
         lVersion = zatol( pchDot1 );
         pchDot1 = null;
      }

      if ( nDotNext2 >= 0 ) // still more versions for second
      {
         nDotCntNext++;
         pch = pchDotNext1.substring( 0, nDotNext2 - 1 );
         lVersionNext = zatol( pch );
         pchDotNext1 = pchDotNext1.substring( 0, nDotNext2 + 1 );
      }
      else
      {
         lVersionNext = zatol( pchDotNext1 );
         pchDotNext1 = null;
      }

      if ( (lVersionNext > lVersion + 1) ||
           (lVersionNext > lVersion && pchDot1 != null && pchDotNext1 == null) ||
           (lVersionNext == 0) )
      {
         // Simply increment final sub-version of the first version.
         pchVersionNew = zstrcpy( pchVersionNew, szVersion );
         nDot1 = zstrrchr( pchVersionNew, '.' );
         if ( nDot1 >= 0 )
            pchDot1 = pchVersionNew.substring( nDot1 + 1 );
         else
            pchDot1 = pchVersionNew;

         lVersion = zatol( pchDot1 );
         lVersion++;
         pchDot1 = zltoa( lVersion, pchDot1 );  // now pchVersionNew contains the next version
         break;  // we are done ... get out of loop
      }

      if ( pchDot1 == null || pchDotNext1 == null )
      {
         pchVersionNew = zstrcpy( pchVersionNew, szVersion );
         if ( pchDotNext1 != null )  // something in second version
         {
            lVersionNext = zatol( pchDotNext1 );
            pchVersionNew += '.';
            pchVersionNew += zltoa( lVersionNext - 1, pchVersionNew );  // now pchVersionNew contains the next version
         }
         else
         {
            // On the last version for both ... just extend the next version
            pchVersionNew = zstrcat( pchVersionNew, ".5" );
         }

         break;  // we are done ... get out of loop
      }

   }  while ( pchDot1 != null );

   DropView( vListVersion );
   return pchVersionNew;
}

////////////////////////////////////////////////////////////////////////////////////////////////////
//
//  Method Name: CheckForTableAttribute
//    Check if an attribute has a table domain
//
////////////////////////////////////////////////////////////////////////////////////////////////////
public int
CheckForTableAttribute( View  lpView,
                        String entityName,
                        String attributeName,
                        String stringObjectName )
{
   zVIEW   vXOD = null;
   zVIEW   vXDM = null;
   StringBuilder sbFileName = new StringBuilder( 0 );
   String  stringDomainName = null;
   String  stringDomainType = null;
   int     cursorPosition;
   int  nRC;

   // Check if the attribute passed has a table domain.
   // A 0 indicates it does.
   // A 1 indicates it does not.

   // If necessary, activate the XOD for the requested object.
   nRC = GetViewByName( vXOD, "FindTableXOD", lpView, zLEVEL_TASK );
   if ( nRC < 0 )
   {
      GetApplDirectoryFromView( sbFileName, lpView, zAPPL_DIR_OBJECT, 300 );
      zstrcat( sbFileName, stringObjectName );
      zstrcat( sbFileName, ".XOD" );
      // 536870912 is ACTIVATE_SYSTEM in the following activate statement.
      ActivateOI_FromFile( vXOD, "TZZOXODO", lpView, sbFileName.toString( ), zACTIVATE_ROOTONLY_MULTIPLE );  // zACTIVATE_SYSTEM 536870912 );
      SetNameForView( vXOD, "FindTableXOD", lpView, zLEVEL_TASK );
   }

   // Position on correct entity and attribute (using recursive set cursor on entity) and
   // retrieve Domain Name.
   ResetView( vXOD );
   cursorPosition = zPOS_FIRST;  // TODO to get by an error zQUAL_STRING + zPOS_FIRST + zRECURS;
   SetEntityCursor( vXOD, "ENTITY", "NAME", cursorPosition, entityName, "", "", 0, "OBJECT", "" );

   SetCursorFirstEntityByString( vXOD, "ATTRIB", "NAME", attributeName, "" );
   GetStringFromAttribute( stringDomainName, vXOD, "ATTRIB", "DOMAIN" );
   //TraceLineS( "Domain Name: ", stringDomainName );

   // If necessary, activate the XDM containing all Domain data.
   nRC = GetViewByName( vXDM, "FindTableXDM", lpView, zLEVEL_TASK );
   if ( nRC < 0 )
   {
      GetApplDirectoryFromView( sbFileName, lpView, zAPPL_DIR_OBJECT, 300 );
      zstrcat( sbFileName, "zeidon.xdm" );
      ActivateOI_FromFile( vXDM, "TZDMXGPO", lpView, sbFileName.toString( ), zACTIVATE_ROOTONLY_MULTIPLE );  // zACTIVATE_SYSTEM 536870912 );
      SetNameForView( vXDM, "FindTableXDM", lpView, zLEVEL_TASK );
   }

   // Position on the correct Domain and retrieve the Domain Type, which is "T" for Table Domains.
   SetCursorFirstEntityByString( vXDM, "Domain", "Name", stringDomainName, "" );
   stringDomainType = GetStringFromAttribute( stringDomainType, vXDM, "Domain", "DomainType" );

   // Set return code base on whether or not the Domain is a Table.
   if ( stringDomainType.charAt( 0 ) == 'T' )
      return 0;
   else
      return 1;

} // CheckForTableAttribute

////////////////////////////////////////////////////////////////////////////////////////////////////
//
//  Method Name: AD_AuthenticateUserPassword
//    Authenticate User Name and Password against Active Directory.
//
////////////////////////////////////////////////////////////////////////////////////////////////////
public int
AD_AuthenticateUserPassword( String stringAD_Pathname,
                             String stringAD_UserName,
                             String stringAD_Password )
{

	//Could also use ldaps over port 636 to protect the communication to the
	//Active Directory domain controller. Would also need to add
	//env.put(Context.SECURITY_PROTOCOL,"ssl") to the "server" code
	//String ldapurl = "ldap://mydc.antipodes.com:389"; // this is example
	//String ldapurl = "LDAP://DC=ENC-AD,DC=ENC,DC=EDU";
	String ldapurl = "ldap://10.150.0.10";
	boolean IsAuthenticated = false;
	try
	{
//	ldapfastbind ctx = new ldapfastbind(ldapurl);
//	IsAuthenticated = ctx.Authenticate("enc-ad\\testz","Temp1234");
//	IsAuthenticated = ctx.Authenticate("enc-ad\\testz","Temp4444");
//	ctx.finito();
	}
	catch (Exception e)
	{
           //highly unlikely since we are using a standard DataFlavor
           System.out.println(e);
           e.printStackTrace();
 	}

	return 0;

	//return ActiveDirectoryLoginAuthentication( stringAD_Pathname, stringAD_UserName, stringAD_Password );

} // AD_AuthenticateUserPassword

////////////////////////////////////////////////////////////////////////////////////////////////////
//
//  Method Name: AD_AddUserPassword
//    Add Active Directory User and Password
//
////////////////////////////////////////////////////////////////////////////////////////////////////
public int
AD_AddUserPassword( String stringServerName,
                    String stringServerPort,
                    String stringOrganization,
                    String stringUserName,
                    String stringUserPassword )
{
   return 0;// ActiveDirectoryAddUser( stringServerName, stringServerPort, stringOrganization, stringUserName, stringUserPassword );

} // AD_AddUserPassword

////////////////////////////////////////////////////////////////////////////////////////////////////
//
//  Method Name: AD_RemoveUserPassword
//    Remove Active Directory User with Password
//
////////////////////////////////////////////////////////////////////////////////////////////////////
public int
AD_RemoveUserPassword( String stringServerName,
                       String stringServerPort,
                       String stringOrganization,
                       String stringUserName )
{
   return 0; //ActiveDirectoryRemoveUser( stringServerName, stringServerPort, stringOrganization, stringUserName );

} // AD_RemoveUserPassword

////////////////////////////////////////////////////////////////////////////////////////////////////
//
//  Method Name: AD_ChangeUserPassword
//    Change the user password through Active Directory
//
////////////////////////////////////////////////////////////////////////////////////////////////////
public int
AD_ChangePassword( String stringAD_Pathname,
                       String stringAD_LoginUserName,
                       String stringAD_LoginPassword,
                       String stringAD_UserName,
                       String stringAD_OldPassword,
                       String stringAD_NewPassword )
{
   return 0;//ActiveDirectoryChangePassword( stringAD_Pathname, stringAD_LoginUserName, stringAD_LoginPassword, stringAD_UserName, stringAD_OldPassword, stringAD_NewPassword );

} // AD_ChangeUserPassword

////////////////////////////////////////////////////////////////////////////////////////////////////
//
//  Method Name: AD_SetPassword
//    Set password in  Active Directory
//
////////////////////////////////////////////////////////////////////////////////////////////////////
public int
AD_SetPassword( String stringAD_Pathname,
                String stringAD_LoginUserName,
                String stringAD_LoginPassword,
                String stringAD_UserName,
                String stringAD_Password )
{
   return 0;//ActiveDirectorySetPassword( stringAD_Pathname, stringAD_LoginUserName, stringAD_LoginPassword, stringAD_UserName, stringAD_Password );
}

// AD_SetPassword

////////////////////////////////////////////////////////////////////////////////////////////////////
//
//  Method Name: AD_AddNewUser
//    Add a new user to Active Directory
//
////////////////////////////////////////////////////////////////////////////////////////////////////
public int
AD_AddNewUser( String stringAD_Pathname,
               String stringAD_LoginUserName,
               String stringAD_LoginPassword,
               String stringAD_NewUserName,
               String stringAD_NewUserPassword )
{
   return 0;//ActiveDirectoryAddUser( stringAD_Pathname, stringAD_LoginUserName, stringAD_LoginPassword, stringAD_NewUserName, stringAD_NewUserPassword );

}  // AD_AddNewUser

////////////////////////////////////////////////////////////////////////////////////////////////////
//
//  Method Name: AD_GetUserProperty
//    Get the value of an Active Directory property for the user.
//
////////////////////////////////////////////////////////////////////////////////////////////////////
public int
AD_GetUserProperty( String stringAD_Pathname,
                    String stringAD_UserName,
                    String stringAD_Password,
                    String stringAD_Property,
                    StringBuilder stringReturnProperty )
{
   return 0;//ActiveDirectoryGetProperty( stringAD_Pathname, stringAD_UserName, stringAD_Password, stringAD_Property, stringReturnProperty );

} // AD_GetUserProperty

////////////////////////////////////////////////////////////////////////////////////////////////////
//
//  Method Name: ValidateAndSetState
//
////////////////////////////////////////////////////////////////////////////////////////////////////
public int
ValidateAndSetState( View   view,
                     String entityName,
                     String attributeName,
                     String stringStateString )
{
   StringBuilder  sbTableValue = new StringBuilder( );
   MutableInt Pointer = null;
   int  lFoundFlag = 0;
   int  nRC;

   nRC = GetFirstTableEntryForAttribute( sbTableValue, view, entityName, attributeName, "", Pointer );
   while ( lFoundFlag == 0 && nRC >= 0 )
   {
      TraceLineS( "//* Domain Value: ", sbTableValue.toString( ) );
      if ( zstrcmp( stringStateString, sbTableValue.toString( ) ) == 0 )
         lFoundFlag = 1;

      nRC = GetNextTableEntryForAttribute( sbTableValue, view, entityName, attributeName, "", Pointer );
   }

   if ( lFoundFlag == 0 )
      return -1;  // String was NOT found.
   else
   {
      // String WAS found.
      SetAttributeFromString( view, entityName, attributeName, stringStateString );
      return 0;
   }

} // ValidateAndSetState

////////////////////////////////////////////////////////////////////////////////////////////////////
//
//  Method Name: SetStringUpperLowerCase
//    Set characters in a string to a combination of upper and lower case, with the 1st character
//    of each "word" in the string to be in upper case and the remainder in lower case.  Eliminate
//    leading and trailing whitespace and multiple consecutive whitespace characters.  Also check
//    for Roman Numeral suffix and upper case as necessary.
//
////////////////////////////////////////////////////////////////////////////////////////////////////
public int
SetStringUpperLowerCase( StringBuilder sbName )
{
   String   string;
   boolean  bSuffix = false;
   int nBlankFound = 0;
   @SuppressWarnings("unused") int nLth;
   int k;
   int j;

   // Eliminate leading and trailing blanks.
   string = sbName.toString( );
   string = string.trim( );

   // Force first character to be upper case
   StringBuffer sb = new StringBuffer( string.toLowerCase( ) );  // force string to lower case
   sb.setCharAt( 0, Character.toUpperCase( sb.charAt( 0 ) ) );

   for ( k = 1; k < sb.length( ); k++ )
   {
      // Eliminate multiple consecutive blanks.
      if ( sb.charAt( k ) == ' ' )
      {
         j = k;
         while ( sb.charAt( j + 1 ) == ' ' )
            j++;

         if ( j > k )
          sb.delete( k + 1, j );
      }
      else
   // if ( sb.charAt( k ) != ' ' )
      {
         if ( sb.charAt( k - 1 ) == ' ' )
         {
            nBlankFound++;
            char ch = Character.toUpperCase( sb.charAt( k ) );
            sb.setCharAt( k, ch );

            if ( ch == 'I' || ch == 'V' )
               bSuffix = true;
            else
               bSuffix = false;
         }
         else
         {
            if ( nBlankFound > 1 && bSuffix )  // checking for II or III or IV or V or VI or VII or VIII
            {
               if ( (sb.charAt( k ) == 'i' || sb.charAt( k ) == 'v') &&
                    (sb.charAt( k + 1 ) == '\0' || sb.charAt( k + 1 ) == 'i' || sb.charAt( k + 1 ) == 'v') )
               {
                  sb.setCharAt( k, Character.toUpperCase( sb.charAt( k ) ) );
               }
               else
                  bSuffix = false;
            }
            else
            {
               bSuffix = false;  // can't start with suffix
            }
         }
      }
   }

   zstrcpy( sbName, sb.toString( ) );
   return sbName.length( );

} // SetStringUpperLowerCase

public int HFI_CleanLangauge( StringBuilder sbSource ){
	
	String chkSource[] = {"ASL", "ARA", "CAN", "ENG", "GRE", "HAI", "KHM","LAO","CHI","POR","RUS","SPA","VIE", "UND","OTH", ""};
	String sTarget = sbSource.toString().toUpperCase();
	//sbSource = new StringBuilder(3);
	sbSource.setLength(0);
	
	for (int i = 0; i < 15; i++){
		if (sTarget.equals(chkSource[i])){
			sbSource.insert(0, sTarget);
			return 0;
		}
	}
	
	sbSource.insert(0, "OTH");
	
	return 0;
}
public int HFI_CleanStateProvince(StringBuilder sbSource){
	if (sbSource == null){
		return 0;		
	}
	
	String chkSource[] = {"AK","AL","AR","AZ","CA","CO","CT","DC","DE","FL","GA","HI","IA","ID","IL","IN","KS","KY","LA",
			              "MA","MO","MD","ME","MI","MN","MT","NC","NH","NJ","NM","NV","NY","OH","OK","OR","PA","RI","SC",
			              "SD","TN","TX","UT","VA","VT","WA","WI","WV"};
	String sTarget = sbSource.toString().toUpperCase();
	sbSource.setLength(0);
	int index = 0;
	
	while (index < 47){
		if (sTarget.equals(chkSource[index])){
			sbSource.insert(0, sTarget);
			break;
		}
		index++;
	}
	
	return 0;
}

public int HFI_CleanDate(StringBuilder sbTarget)
{
	String pattern = "^000";
	
	Pattern regexp = Pattern.compile(pattern);
	
	Matcher m = regexp.matcher(sbTarget.toString());
	
	if (m.find()){
		sbTarget.setLength(0);		
	}
	return 0;
}

public int HFI_CleanPhone( StringBuilder sbTarget,
                            StringBuilder sbSource )
   {
	  StringBuilder sbTarget1;
	  int nRC;
      int targetIdx;
      int sourceIdx;
      int nbrToCopy;
      int lth;
      
      if ( sbTarget == null || sbSource == null )
      {
         SysMessageBox( null, "JOE System Error",
                        "HFI_CleanPhone: Invalid parameter.", 1 );
         return( qINVALIDPARAMETER );
      }
      sbTarget1 = new StringBuilder( "" );
      targetIdx = 0;
      sourceIdx = 0;
      nbrToCopy = 10;
 
      while ( sourceIdx < sbSource.length( ) && sbSource.charAt( sourceIdx ) != '\0' ) 
      {
         if ( sbSource.charAt( sourceIdx ) != '(' && sbSource.charAt( sourceIdx ) != ')' && 
    		  sbSource.charAt( sourceIdx ) != ' ' && sbSource.charAt( sourceIdx ) != '-' && 
    		  sbSource.charAt( sourceIdx ) != '.' )  
            sbTarget1.insert( targetIdx++, sbSource.charAt( sourceIdx++ ) );
         else  
        	sourceIdx++ ;
      }
      sbTarget.insert(0, sbTarget1.toString());
      return( sbTarget.length( ) );
 }
public int HFI_CleanSSN( StringBuilder sbTarget,
                         String sbSource )
{
	StringBuilder sbTarget1;
	int nRC;
	int targetIdx;
	int sourceIdx;
	int nbrToCopy;
	int lth;

	if ( sbTarget == null || sbSource == null )
	{
		SysMessageBox( null, "JOE System Error",
				"HFI_CleanPhone: Invalid parameter.", 1 );
		return( qINVALIDPARAMETER );
	}
	sbTarget1 = new StringBuilder( "" );
	targetIdx = 0;
	sourceIdx = 0;
	nbrToCopy = 10;
	
	while ( sourceIdx < sbSource.length( ) && sbSource.charAt( sourceIdx ) != '\0' ) 
	{
		if ( sbSource.charAt( sourceIdx ) != '(' && sbSource.charAt( sourceIdx ) != ')' && 
				sbSource.charAt( sourceIdx ) != ' ' && sbSource.charAt( sourceIdx ) != '-' && 
				sbSource.charAt( sourceIdx ) != '.' )  
			sbTarget1.insert( targetIdx++, sbSource.charAt( sourceIdx++ ) );
		else  
			sourceIdx++ ;
	}
	sbTarget.insert(0, sbTarget1.toString());
	return( sbTarget.length( ) );
}

public int HFI_CheckFileForDataload(String sbSource, StringBuilder sbTarget, int max){
	int index;
	String szTmp;
	if ( sbSource == null )
	{
		SysMessageBox( null, "JOE System Error",
				"HFI_CheckFileForDataload: Invalid parameter.", 1 );
		return( qINVALIDPARAMETER );
	}
	
	index = sbSource.length()-3;
	
	szTmp = sbSource.substring(index);
	sbTarget.insert(0, szTmp);
	
	return 0;
}

////////////////////////////////////////////////////////////////////////////////////////////////////
//
//  Method Name: SetAttributeFromUC_String
//    Set Attribute from String, converting string to upper case.
//
////////////////////////////////////////////////////////////////////////////////////////////////////
public int
SetAttributeFromUC_String( View   tgtView,
                           String tgtEntityName,
                           String tgtAttributeName,
                           String srcString )
{

   // Convert srcString to upper case and store it in the attribute.
   SetAttributeFromString( tgtView, tgtEntityName, tgtAttributeName, srcString.toUpperCase( ) );

   return 0;
} // SetAttributeFromUC_String

////////////////////////////////////////////////////////////////////////////////////////////////////
//
//  Method Name: SetAttributeFromUC_Attribute
//    Set Attribute from Attribute, converting the source string to upper case in the process.
//
////////////////////////////////////////////////////////////////////////////////////////////////////
public int
SetAttributeFromUC_Attribute( View   tgtView,
                              String tgtEntityName,
                              String tgtAttributeName,
                              View   srcView,
                              String srcEntityName,
                              String srcAttributeName )
{
   String  srcString = null;

   // Convert source attribute value to upper case and store it in the target attribute.
   // zToUpper prototype is in TZVMLIP.H
   srcString = GetStringFromAttribute( srcString, srcView, srcEntityName, srcAttributeName );
   SetAttributeFromString( tgtView, tgtEntityName, tgtAttributeName, srcString.toUpperCase( ) );

   return 0;
} // SetAttributeFromUC_Attribute

////////////////////////////////////////////////////////////////////////////////////////////////////
//
//  Method Name: AD_SetUserProperty
//    Set an AD property for a user
//
////////////////////////////////////////////////////////////////////////////////////////////////////
public int
AD_SetUserProperty( String stringAD_Pathname,
                    String stringAD_AdminName,
                    String stringAD_AdminPassword,
                    String stringAD_UserName,
                    String stringAD_PropertyName,
                    String stringAD_PropertyValue )
{
   int nRC;

   nRC = 0;//ActiveDirectorySetProperty( stringAD_Pathname, stringAD_AdminName, stringAD_AdminPassword,
                                   //  stringAD_UserName, stringAD_PropertyName, stringAD_PropertyValue );
   return nRC;

} // AD_SetUserProperty


/*************************************************************************************************
**
**    OPERATION: CopyFileToPDF
**    Copy a file to a pdf file.
**
*************************************************************************************************/
public int CopyFileToPDF( View vMapObject,
		                     String szFileToCopy,
		                     String szPDFName )
{
   zVIEW   vKZXMLPGO = null;
   String  szCommandLine = null;
   String  szPathFileName = null;
   String  szFileName = null;

   zstrcpy( szCommandLine, "copypdf.bat \"" );
   zstrcat( szCommandLine, szFileToCopy );
   zstrcat( szCommandLine, "\"" );

   // KJS 01/05/11 - When we move to java I don't think we will be able to do this anymore.
   // Need to keep JODConverter in mind:http://stackoverflow.com/questions/586411/is-there-a-free-way-to-convert-rtf-to-pdf

   //system( szCommandLine );
   try {
		Runtime.getRuntime().exec( szCommandLine );
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}


   // KJS 02/20/2009 - We would like our pdfs to be created in a separate directory.
   // Use PDF_PathFileName to get this.  Currently I have this set as /zencas/pdf/ but
   // I know Aadit would like to put these in a totally different directory not under
   // zencas.  Might need to change something because I had a hard time getting a file
   // to open when PDF_PathFileName was something like "C:\Program Files...".
   StringBuilder sb_szPathName;
   sb_szPathName = new StringBuilder( 200 );
   //SysReadZeidonIni( -1, "[App.Zencas]", "WebDirectory", sb_szDirectoryName );
   m_KZOEP1AA.SysReadZeidonIni( -1, "Workstation", "PDF_PathFileName", sb_szPathName );
   szPathFileName = sb_szPathName.toString( );

   /*
   nZRetCode = GetWorkstationApplicationValues( vMapObject, "PDF_PathFileName",
      szPathFileName, 32, &lFontSize, &lWork, &lWork, &lWork, &lWork, &lWork,
      &lWork, &lWork, &lWork, &lWork );
   */

   zstrcpy( szFileName, szPathFileName );
   zstrcat( szFileName, szPDFName );
   //szFileName += szPathFileName;
   //szFileName += ;

   // We set the report name in KZXMLPGO so that
   // we can retrieve this name in FindOpenFile (kzoejava.c) when trying to
   // open the file in the jsp files.
   GetViewByName( vKZXMLPGO, "KZXMLPGO", vMapObject, zLEVEL_TASK );
   SetAttributeFromString( vKZXMLPGO, "Session",
                           "PrintFileName",
                           szFileName );
                           //pchReportName );
   SetAttributeFromString( vKZXMLPGO, "Session",
                           "PrintFileType",
                           "pdf" );
   return( 0 );
} // CopyFileToPDF


public int HFI_LoadAgencyZipIntoWork(View vTmpObj,
                                     String szFileToImport)
{
	// Load in a two column csv file. The first column is the agency (location) and the
	// second column is the postal code.
	// We will use this to update the ssaoffice attribute in the PostalCode table.
    try
    {
      // The following line is a test because I was having issues finding the csv file.
      System.out.println(System.getProperty("user.dir"));  
      //File fr = JoeUtils.getFile( szFileToImport );
      //BufferedReader br = new BufferedReader( new FileReader( fr ) );
      FileReader fr = new FileReader(szFileToImport);
      BufferedReader br = new BufferedReader(fr);
      String stringRead = br.readLine();

      while( stringRead != null )
      {
        StringTokenizer st = new StringTokenizer(stringRead, ",");
        String location = st.nextToken( );
        String postalcode = st.nextToken( );  
        
        if ( location != null && postalcode != null)
        {
           vTmpObj.cursor("AgencyZip").createEntity();
           vTmpObj.cursor("AgencyZip").setAttribute("Location", location);
           vTmpObj.cursor("AgencyZip").setAttribute("PostalCode", postalcode);
        }
        // read the next line
        stringRead = br.readLine();
      }
      br.close( );
    }
    catch(IOException ioe){
    	return -1;
    }
    return (0);
}
}
