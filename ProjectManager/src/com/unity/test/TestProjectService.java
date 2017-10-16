package com.unity.test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.WriterAppender;
import org.json.simple.parser.ParseException;
import org.junit.BeforeClass;
import org.junit.Test;

import com.unity.model.Key;
import com.unity.model.Project;
import com.unity.services.ProjectService;

import io.restassured.RestAssured;


public class TestProjectService {

	private static List<String> countries;
	private static Key[] keyArr;
	
	
	@BeforeClass
	public static void init()
	{
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = 8080;
		countries = new ArrayList<>();
		countries.add("USA");
		countries.add("INDIA");
		countries.add("HONGKONG");
		countries.add("RUSSIA");
		Key key1 = new Key();
		key1.setNumber(27);
		key1.setKeyword("news");
		Key key2 = new Key();
		key2.setNumber(30);
		key2.setKeyword("sports");
		keyArr = new Key[2];
		keyArr[0] = key1;
		keyArr[1] = key2;
		
	}
	//first run the create project test to create multiple valid projects, commenting other tests, then comment this test and execute rest of the test cases
	@Test
	public void createProjectTest()
	{
		//test to create valid project
		Project project1 = getProjectObj(1, "test project number 1", "06112017 00:00:00", "06202018 00:00:00", true, "http://www.unity3d.com", 7.6, countries, keyArr);
		Project project2 = getProjectObj(2, "test project number 2", "06112017 00:00:00", "06202018 00:00:00", true, null, 6.6, countries, keyArr);
		Project project3 = getProjectObj(3, "test project number 3", "06112017 00:00:00", "06202018 00:00:00", true, "http://www.unity3d.com", 7.6, countries, keyArr);
		Project project4 = getProjectObj(4, "test project number 4", "06112017 00:00:00", "06202018 00:00:00", true, "http://www.unity3d.com", 7.9, countries, keyArr);
		Project project5 = getProjectObj(5, "test project number 5", "06112017 00:00:00", "06202018 00:00:00", true, "http://www.unity3d.com", 8.1, countries, keyArr);
		
		List<Project> projList = new ArrayList<>();
		projList.add(project1);
		projList.add(project2);
		projList.add(project3);
		projList.add(project4);
		projList.add(project5);
		
		for(Project project : projList)
		{
			given()
			.contentType("application/json")
			.body(project)
			.when().post("/ProjectManager/restapi/createProject").then()
			.body(containsString("campaign is successfully created"))
			.statusCode(200);
		}
	}
	
	
	/**
	 * test to create project with invalid projectId like 0 or less
	 */
	@Test
	public void createProjectWithInvalidIdTest()
	{
		Project project = getProjectObj(-1, "test project number 1", "06112017 00:00:00", "06202018 00:00:00", true, "http://www.unity3d.com", 7.6, countries, keyArr);
		given()
		.contentType("application/json")
		.body(project)
		.when().post("/ProjectManager/restapi/createProject").then()
		.body(containsString("Invalid id"))
		.statusCode(400);
	}
	
	
	/**
	 * test to create project with null date test
	 */
	@Test
	public void createProjectWithNullDateTest()
	{
		Project project = getProjectObj(1, "test project number 1", null, "06202018 00:00:00", true, "http://www.unity3d.com", 7.6, countries, keyArr);
		given()
		.contentType("application/json")
		.body(project)
		.when().post("/ProjectManager/restapi/createProject").then()
		.body(containsString("Invalid date format"))
		.statusCode(400);
	}
	
	
	/**
	 * test to create project with invalid date test
	 */
	@Test
	public void createProjectWithInvalidDateTest()
	{
		Project project = getProjectObj(1, "test project number 1", "XXX", "06202018 00:00:00", true, "http://www.unity3d.com", 7.6, countries, keyArr);
		given()
		.contentType("application/json")
		.body(project)
		.when().post("/ProjectManager/restapi/createProject").then()
		.body(containsString("Date can not be null"))
		.statusCode(400);
	}
	
	/**
	 * test to create project with creation date after expiry date
	 */
	@Test
	public void createProjectWithImproperDateTest()
	{
		Project project = getProjectObj(1, "test project number 1", "06202019 00:00:00", "06202018 00:00:00", true, "http://www.unity3d.com", 7.6, countries, keyArr);
		given()
		.contentType("application/json")
		.body(project)
		.when().post("/ProjectManager/restapi/createProject").then()
		.body(containsString("Creation date can not be later than expiry date"))
		.statusCode(400);
	}
	
	/**
	 * test to create project that is already created
	 */
	@Test
	public void createDuplicateProjectTest()
	{
		
		Project project = getProjectObj(1, "test project number 1", "06112017 00:00:00", "06202018 00:00:00", true, "http://www.unity3d.com", 7.6, countries, keyArr);
		given()
		.contentType("application/json")
		.body(project)
		.when().post("/ProjectManager/restapi/createProject").then()
		.body(containsString("Project already present"))
		.statusCode(200);
	}
	
	/**
	 * test for checking the returned project details by projectid
	 */
	@Test
	public void requestProjectByOnlyIdTest()
	{
		given().when()
		.get("/ProjectManager/restapi/requestProject?projectid=1")
		.then()
		.body("projectName", equalTo("test project number 1"))
		.body("projectCost", equalTo(7.6f))
		.body("projectUrl", equalTo("http://www.unity3d.com"))
		.statusCode(200);
		
	}
	/**
	 * test for checking the returned project details by invalid projectid
	 */
	@Test
	public void requestProjectByInvalidIdTest()
	{
		given().when()
		.get("/ProjectManager/restapi/requestProject?projectid=-1")
		.then()
		.body(containsString("Invalid GET request by id"))
		.statusCode(400);
	}
	/**
	 * test for checking the returned project details by projectid whose prject is not created
	 */
	@Test
	public void noProjectFoundTest()
	{
		given().when()
		.get("/ProjectManager/restapi/requestProject?projectid=10")
		.then()
		.body("message", equalTo("no project found"))
		.statusCode(200);
	}
	/**
	 * test for checking the returned project details without any params in request url
	 */
	@Test
	public void requestProjectByNoParamsTest()
	{
		given().when()
		.get("/ProjectManager/restapi/requestProject")
		.then()
		.body("projectName", hasItem("test project number 5"))
		.body("projectCost", hasItem(8.1f))
		.body("projectUrl", hasItem("http://www.unity3d.com"))
		.statusCode(200);
	}
	/**
	 * test for checking the returned project details by passing all 4 required params
	 */
	@Test
	public void requestProjectByAllParamsTest()
	{
		given().when()
		.get("/ProjectManager/restapi/requestProject?projectid=1&country=USA&number=27&keyword=sports")
		.then()
		.body("projectName", equalTo("test project number 1"))
		.body("projectCost", equalTo(7.6f))
		.body("projectUrl", equalTo("http://www.unity3d.com"))
		.statusCode(200);
	}
	/**
	 * test for checking the returned project details by country and number
	 */
	@Test 
	public void requestProjectByCountryNumberTest()
	{
		given().when()
		.get("/ProjectManager/restapi/requestProject?country=USA&number=27")
		.then()
		.body("projectName", hasItem("test project number 5"))
		.body("projectCost", hasItem(8.1f))
		.body("projectUrl", hasItem("http://www.unity3d.com"))
		.statusCode(200);
	}
	/**
	 * test for checking the returned project details by country, number and keyword
	 */
	@Test
	public void requestProjectByCountryNumKeywordTest()
	{
		given().when()
		.get("/ProjectManager/restapi/requestProject?country=USA&number=27&keyword=sports")
		.then()
		.body("projectName", hasItem("test project number 5"))
		.body("projectCost", hasItem(8.1f))
		.body("projectUrl", hasItem("http://www.unity3d.com"))
		.statusCode(200);
	}
	
	/**
	 *  test for checking the returned project details by number and keyword
	 */
	@Test
	public void requestProjectByNUmKeywordTest()
	{
		given().when()
		.get("/ProjectManager/restapi/requestProject?number=27&keyword=sports")
		.then()
		.body("projectName", hasItem("test project number 5"))
		.body("projectCost", hasItem(8.1f))
		.body("projectUrl", hasItem("http://www.unity3d.com"))
		.statusCode(200);
	}
	
	/**
	 *  test for checking the returned project details by number
	 */
	@Test
	public void requestProjectByNumberTest()
	{
		given().when()
		.get("/ProjectManager/restapi/requestProject?number=27")
		.then()
		.body("projectName", hasItem("test project number 5"))
		.body("projectCost", hasItem(8.1f))
		.body("projectUrl", hasItem("http://www.unity3d.com"))
		.statusCode(200);
	}
	
	/**
	 *  test for checking the returned project details by Keyword
	 */
	@Test
	public void requestProjectByKeywordTest()
	{
		given().when()
		.get("/ProjectManager/restapi/requestProject?keyword=sports")
		.then()
		.body("projectName", hasItem("test project number 5"))
		.body("projectCost", hasItem(8.1f))
		.body("projectUrl", hasItem("http://www.unity3d.com"))
		.statusCode(200);
	}
	
	/**
	 * test for checking the returned project details by country
	 */
	@Test
	public void requestProjectByCountryTest()
	{
		given().when()
		.get("/ProjectManager/restapi/requestProject?country=USA")
		.then()
		.body("projectName", hasItem("test project number 5"))
		.body("projectCost", hasItem(8.1f))
		.body("projectUrl", hasItem("http://www.unity3d.com"))
		.statusCode(200);
	}
	
	/**
	 * test function for testing invalid request parameters in the request url
	 */
	@Test
	public void requestWithWrongURLParamsTest()
	{
		given().when()
		.get("/ProjectManager/restapi/requestProject?enabled=true")
		.then()
		.body(containsString("Invalid GET request"))
		.statusCode(400);
	}
	
	/**
	 * test for a wrong url with bad rest method api , return status code as 405 - Bad method
	 */
	@Test
	public void requestWithWrongURLRestMethodTest()
	{
		given().when()
		.get("/ProjectManager/restapi/")
		.then()
		.statusCode(405);
	}
	
	/**
	 * test for a wrong url return status code as 404 - Bad request
	 */
	@Test
	public void requestWithWrongURLTest()
	{
		given().when()
		.get("/ProjectManager/restapi/requestProjec")
		.then()
		.statusCode(404);
	}
	
	/**
	 * @param id
	 * @param projectName
	 * @param creationDate
	 * @param expiryDate
	 * @param enabled
	 * @param projectUrl
	 * @param projectCost
	 * @param countries
	 * @param keyArr
	 * @return the project object after setting all the fields
	 */
	public Project getProjectObj(int id, String projectName, String creationDate, String expiryDate,
			boolean enabled, String projectUrl, double projectCost, List<String> countries, Key[] keyArr)
	{
		Project project = new Project();

		project.setId(id);
		project.setProjectName(projectName);
		project.setCreationDate(creationDate);
		project.setExpiryDate(expiryDate);
		project.setEnabled(enabled);
		project.setProjectUrl(projectUrl);
		project.setProjectCost(projectCost);
		project.setTargetCountries(countries);
		project.setTargetKeys(keyArr);
		
		return project;
	}
	

	/**
	 * @throws IOException
	 * @throws ParseException
	 * @throws java.text.ParseException
	 * test function to test whether the logs are properly executed in the createProject and requestProject service apis 
	 */
	@Test
	public void testLogMessageIsSent() throws IOException, ParseException, java.text.ParseException {
	    Logger logger = Logger.getLogger(ProjectService.class);
	 
	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    Layout layout = new SimpleLayout();
	    Appender appender = new WriterAppender(layout, out);
	    logger.addAppender(appender);
	 
	    try {
	    	
	    	ProjectService productService = new ProjectService();
	    	Project project = getProjectObj(1, "test project number 1", "06112017 00:00:00", "06202018 00:00:00", true, "http://www.unity3d.com", 7.6, countries, keyArr);
	    	productService.consumeJSON(project);
	 
	        String logMsg = out.toString();
	 
	        //test for checking whether createProject is being logged properly or not
	        assertNotNull(logMsg);
	        assertEquals(true,logMsg.startsWith("INFO - createProject service invoked.."));
	        
	        countries.add("USA");
	        HashMap<String, List<String>> map = new HashMap<>();
	        List<String> urlList = new ArrayList<>();
	        urlList.add("1");
	        map.put("projectid", urlList);
	        
	        productService.getProject(1, countries, 27, "Sports",  null);
	        String logMsgforGet = out.toString();
	        
	        //test for checking whether requestPorject is being logged properly or not
	        assertNotNull(logMsgforGet);
	        assertEquals(true,logMsgforGet.contains("INFO - requestProject service invoked.."));
	 
	    } finally {
	        logger.removeAppender(appender);
	    }
	}

}
