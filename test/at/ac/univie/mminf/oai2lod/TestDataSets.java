package at.ac.univie.mminf.oai2lod;

import java.util.HashMap;
import java.util.Map;

public class TestDataSets {

	// INFOMOTION DATASET
	
	public static Map<String, String> INFOMOTIONS_LISTRECORD_ALL = new HashMap<String, String>();

	static {
		INFOMOTIONS_LISTRECORD_ALL.put("NO_TOKEN", "./testData/infomotion/sample_response.xml");
		INFOMOTIONS_LISTRECORD_ALL.put("!!!oai_dc!200", "./testData/infomotion/sample_response_r1.xml");
		INFOMOTIONS_LISTRECORD_ALL.put("!!!oai_dc!400", "./testData/infomotion/sample_response_r2.xml");
		INFOMOTIONS_LISTRECORD_ALL.put("!!!oai_dc!600", "./testData/infomotion/sample_response_r3.xml");
		INFOMOTIONS_LISTRECORD_ALL.put("!!!oai_dc!800", "./testData/infomotion/sample_response_r4.xml");
		INFOMOTIONS_LISTRECORD_ALL.put("!!!oai_dc!1000", "./testData/infomotion/sample_response_r5.xml");
		INFOMOTIONS_LISTRECORD_ALL.put("!!!oai_dc!1200", "./testData/infomotion/sample_response_r6.xml");
		INFOMOTIONS_LISTRECORD_ALL.put("!!!oai_dc!1400", "./testData/infomotion/sample_response_r7.xml");
		INFOMOTIONS_LISTRECORD_ALL.put("!!!oai_dc!1600", "./testData/infomotion/sample_response_r8.xml");
		INFOMOTIONS_LISTRECORD_ALL.put("!!!oai_dc!1800", "./testData/infomotion/sample_response_r9.xml");

	}

	public static Map<String,String> INFOMOTIONS_LISTRECORD_SINGLE = new HashMap<String, String>();
	
	static{
		INFOMOTIONS_LISTRECORD_SINGLE.put("NO_TOKEN", "./testData/infomotion/sample_response.xml");
	}
	
	public static Map<String,String> INFOMOTIONS_LISTRECORD_ERROR = new HashMap<String, String>();
	
	static{
		INFOMOTIONS_LISTRECORD_ERROR.put("NO_TOKEN", "./testData/infomotion/sample_error_response.xml");
	}
	
	
	// LIBRARY OF CONGRESS DATA SET
	
	public static String LOC_LISTSETS = "./testData/loc/LOC_ListSetsResponse.xml";
	
	public static Map<String, String> LOC_LISTRECORD_ALL = new HashMap<String, String>();
	
	static{
		LOC_LISTRECORD_ALL.put("NO_TOKEN", "./testData/loc/LOC_MODS_sample_response1.xml");
		LOC_LISTRECORD_ALL.put("xGF0", "./testData/loc/LOC_MODS_sample_response2.xml");
		LOC_LISTRECORD_ALL.put("rFlO", "./testData/loc/LOC_MODS_sample_response3.xml");
		LOC_LISTRECORD_ALL.put("I77P", "./testData/loc/LOC_MODS_sample_response4.xml");
		LOC_LISTRECORD_ALL.put("Pwx9", "./testData/loc/LOC_MODS_sample_response5.xml");
		
	}
		

	// AUSTRIAN NATIONAL LIBRARY DATA SET
	
	public static String ONB_LISTSETS = "./testData/onb/ONB_ListSetsResponse.xml";
	
	public static Map<String, String> ONB_LISTRECORD_ALL = new HashMap<String, String>();

	static{
		ONB_LISTRECORD_ALL.put("NO_TOKEN", "./testData/onb/ONB_ListRecordsResponse1.xml");
		ONB_LISTRECORD_ALL.put("343835", "./testData/onb/ONB_ListRecordsResponse2.xml");
		ONB_LISTRECORD_ALL.put("343836", "./testData/onb/ONB_ListRecordsResponse3.xml");
		ONB_LISTRECORD_ALL.put("343837", "./testData/onb/ONB_ListRecordsResponse4.xml");
		ONB_LISTRECORD_ALL.put("343838", "./testData/onb/ONB_ListRecordsResponse5.xml");
		ONB_LISTRECORD_ALL.put("343839", "./testData/onb/ONB_ListRecordsResponse6.xml");
	}
	
	public static Map<String, String> ONB_DEMO_ALL = new HashMap<String, String>();

	static{
		ONB_DEMO_ALL.put("NO_TOKEN", "./testData/onb/ONB_DEMO_response1.xml");
		ONB_DEMO_ALL.put("345887", "./testData/onb/ONB_DEMO_response2.xml");
		ONB_DEMO_ALL.put("345888", "./testData/onb/ONB_DEMO_response3.xml");
		ONB_DEMO_ALL.put("345889", "./testData/onb/ONB_DEMO_response4.xml");
		ONB_DEMO_ALL.put("3458810", "./testData/onb/ONB_DEMO_response5.xml");
	}
	
}
