/**
 * 
 */
package com.unity.services;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.codehaus.groovy.runtime.dgmimpl.arrays.LongArrayGetAtMetaMethod;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.unity.model.Key;
import com.unity.model.Project;


@Path("/")
public class ProjectService {
	
	private JSONObject noProjFoundJSONObj;
	private Logger logger;
	
	@SuppressWarnings("unchecked")
	public ProjectService() {
		this.noProjFoundJSONObj = new JSONObject();
		this.noProjFoundJSONObj.put("message", "no project found");
		this.logger = LoggerFactory.getLogger(ProjectService.class);
	}
	

	/**
	 * @param project
	 * @return
	 * @throws IOException 
	 * @throws ParseException 
	 * @throws java.text.ParseException 
	 */
	@SuppressWarnings("unchecked")
	@POST
	@Path("/createProject")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_HTML)
	public String consumeJSON( Project project ) throws IOException, ParseException, java.text.ParseException {
		
		logger.info("createProject service invoked.. with request payload object "+ project );
		
		//check whether the request payload has valid data to create the project
		checkValidRequestPayload(project);	
		
		
		String filename = "/Users/Abhishek/Documents/workspaces/SpringWorkspace/ProjectManager/src/Projects.txt";
		File file = new File(filename);
		
		boolean projectPresent = false;
		
		if(file.exists())
		{
			List<JSONObject> projList = loadProjectFromFile(filename);
			projectPresent = checkProjectAlreadyPresent(projList, project);
			if(projectPresent)
				return "Project already present";
		}
		else
		{
			// if file doesn't exist, then create it
			file.createNewFile();
		}
		JSONObject obj = new JSONObject();
		obj.put("id", project.getId());
		obj.put("projectName", project.getProjectName());
		obj.put("creationDate", project.getCreationDate());
		obj.put("expiryDate", project.getExpiryDate());
		obj.put("enabled", project.isEnabled());
		obj.put("projectCost", project.getProjectCost());
		obj.put("projectUrl", project.getProjectUrl());
 
		JSONArray targetCountriesArr = new JSONArray();
		
		for(String targetCountry : project.getTargetCountries())
		{
			targetCountriesArr.add(targetCountry);
		}
		
		obj.put("targetCountries", targetCountriesArr);
		
		JSONArray targetKeysArr = new JSONArray();
		
		for(Key key : project.getTargetKeys())
		{
			JSONObject keyObj = new JSONObject();
			keyObj.put("number", key.getNumber());
			keyObj.put("keyword", key.getKeyword());
			targetKeysArr.add(keyObj);
		}
		
		obj.put("targetKeys", targetKeysArr);
		
		//saving the object in the file
		FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(obj.toJSONString()+"\n");
		bw.close();
		logger.info("request json saved on file...");
		logger.info("project successfully created...");
		return "campaign is successfully created";
	}
	
	
	/**
	 * @param id
	 * @param country
	 * @param number
	 * @param keyword
	 * @return JSONObject
	 * 
	 * This method will handle the requestProject request and return the matching projects as JSON objects
	 * @throws java.text.ParseException 
	 * @throws IOException 
	 */
	@SuppressWarnings("unchecked")
	@GET
	@Path("/requestProject")
	@Produces(MediaType.APPLICATION_JSON)
	public Object getProject(@QueryParam("projectid") int id,
								@QueryParam("country") List<String> country,
								@QueryParam("number") int number,
								@QueryParam("keyword") String keyword, @Context UriInfo uriInfo) throws java.text.ParseException, IOException, ParseException
	{

		logger.info("requestProject service invoked..");
		// check whether the request query id is valid or not to fetch the project
		if(id<=0 && uriInfo!=null)
		{
			Response invalidIdRes = checkInvalidIdRequest(uriInfo);
			if(invalidIdRes!=null)
				return invalidIdRes;
		}
		// check whether the request query params are valid or not to fetch the project
		if(uriInfo!=null)
		{
			Response validParamResponse = checkValidGetRequestParams(uriInfo);
			if(validParamResponse!=null)
				return validParamResponse;
			
		}
		
		//Reading the json objects saved in the file Projects.txt
		JSONObject jsonResObj = new JSONObject();
		List<JSONObject> enabledActivePrjList = new ArrayList<>();

		List<JSONObject> jsonObjList = new ArrayList<>();

		String filename = "/Users/Abhishek/Documents/workspaces/SpringWorkspace/ProjectManager/src/Projects.txt";

		jsonObjList = loadProjectFromFile(filename);

		if(!jsonObjList.isEmpty())
		{
			enabledActivePrjList = checkEnableActiveValidURLProject(jsonObjList);
			//if no request parameters are present the project with the highest cost is returned
			if(enabledActivePrjList.size()!=0)
			{
				if(id==0 && country.size()==0 && number==0 && keyword==null)
				{
					Object projObj = findProjectWithHighestCost(enabledActivePrjList);
					logger.info("response object sent.. "+ projObj);
					return projObj;
				}
				//if project id is present in the request parameter then find project only based on id
				else if(id!=0)
				{
					jsonResObj = findProjectById(enabledActivePrjList, id, jsonResObj);
					logger.info("response object sent.. "+ jsonResObj);
					return jsonResObj;
				}
				//if only country is passed as request parameter
				else if(country.size()!=0 && id==0 && number==0 && keyword==null)
				{
					//String trgetCountry = country.get(0).replaceAll("\"",	"");
					Object projObj = findProjectByTargetCountry(country.get(0), enabledActivePrjList);
					logger.info("response object sent.. "+ projObj);
					return projObj;
				}
				//if country and number is passed as request parameter
				else if(country.size()!=0 && number!=0 && id==0 && keyword==null)
				{
					//String trgetCountry = country.get(0).replaceAll("\"",	"");
					Object projObj = findProjByCountryAndNum(country.get(0), number, enabledActivePrjList);
					logger.info("response object sent.. "+ projObj);
					return projObj;
				}
				//if country and target keyword is passed as request parameter
				else if(country.size()!=0 && keyword!=null && number==0 && id==0)
				{
					//String trgetCountry = country.get(0).replaceAll("\"",	"");
					Object projObj = findProjectByCountryAndKeyword(enabledActivePrjList, country.get(0) , keyword);
					logger.info("response object sent.. "+ projObj);
					return projObj;
				}
				//if country, number and keyword is passed as request parameter
				else if(country.size()!=0 && number!=0 && keyword !=null && id==0)
				{
					//String targetCountry = country.get(0).replaceAll("\"", "");
					Object projObj = findProjectByCountryNumKeyword(enabledActivePrjList,country.get(0), number,keyword);
					logger.info("response object sent.. "+ projObj);
					return projObj;
				}
				//if target number and keyword is passed as request parameter 
				else if(number!=0 && keyword!=null && country.size()==0 && id==0)
				{
					Object projObj = findProjectByNumKeyword(enabledActivePrjList, keyword, number);
					logger.info("response object sent.. "+ projObj);
					return projObj;
				}
				//if only target number is passed as request parameter
				else if(number!=0 && keyword==null && country.size()==0 && id==0)
				{
					Object projObj = findProjectByTargetNum(enabledActivePrjList, number);
					logger.info("response object sent.. "+ projObj);
					return projObj;
				}
				//if only target keyword is passed as request parameter
				else if(keyword!=null && country.size()==0 && id==0 && number==0)
				{
					Object projObj = findProjectByTargetKeyword(enabledActivePrjList, keyword);
					logger.info("response object sent.. "+ projObj);
					return projObj;
				}
				else
				{
					logger.info("response object sent having no project..");
					return noProjFoundJSONObj;
				}
			}
			else
			{
				logger.info("response object sent having no project..");
				return noProjFoundJSONObj;
			}
		}

		logger.info("response object sent having no project..");
		return noProjFoundJSONObj;
	}
	
	/**
	 * @param projObj
	 * @return the final json response object with 3 fields and values
	 */
	@SuppressWarnings("unchecked")
	public JSONObject createResponseObj(JSONObject projObj)
	{
		logger.info("creating the rsponse json object...");
		JSONObject jsonResObj = new JSONObject();
		jsonResObj.put("projectUrl", projObj.get("projectUrl"));
		jsonResObj.put("projectCost", projObj.get("projectCost"));
		jsonResObj.put("projectName", projObj.get("projectName"));
		return jsonResObj;
		
	}

	/**
	 * @param jsonObjList
	 * @param id
	 * @param jsonResObj
	 * @return JSONObject
	 */
	@SuppressWarnings("unchecked")
	public JSONObject findProjectById(List<JSONObject> jsonObjList, int id , JSONObject jsonResObj)
	{
		logger.info("finding project with valid id..");
		for(JSONObject jsonObj : jsonObjList)
		{
			if(Long.valueOf(id)==jsonObj.get("id"))
			{
				return createResponseObj(jsonObj);
			}
		}
		return noProjFoundJSONObj;
	}
	
	/**
	 * @param objList
	 * @param keyword
	 * @param num
	 * @return project with matching target keys and number 
	 */
	@SuppressWarnings("unchecked")
	public Object findProjectByNumKeyword(List<JSONObject> objList, String keyword, int num)
	{
		logger.info("finding project by number and keyword");
		List<JSONObject> projectList = new ArrayList<>();
		boolean targetKeyNumPresent = false;
		boolean checkKeywordPresent = false;
		
		for(JSONObject projObj : objList)
		{
			//check for each project whether it matches with the required target key number
			targetKeyNumPresent = checkTargetKeyNumPresent(projObj, num);
			
			//check for each project whether it matches with the required keyword
			checkKeywordPresent	= checkaTargetKeywordPresent(projObj, keyword);
			
			if(checkKeywordPresent && targetKeyNumPresent)
			{
				projectList.add(projObj);
			}
		}
		return findProjectWithHighestCost(projectList);
	}
	
	/**
	 * @param objList
	 * @param num
	 * @return project with the matched target key number
	 */
	public Object findProjectByTargetNum(List<JSONObject> objList, int num)
	{
		logger.info("finding project only by number...");
		List<JSONObject> projectList = new ArrayList<>();
		boolean targetKeyNumPresent = false;
		
		for(JSONObject projObj : objList)
		{
			//check for each project whether it matches with the required target key number
			targetKeyNumPresent = checkTargetKeyNumPresent(projObj, num);
			
			if(targetKeyNumPresent)
			{
				projectList.add(projObj);
			}
		}
		return findProjectWithHighestCost(projectList);
	}
	
	/**
	 * @param objList
	 * @param keyword
	 * @return project with matched target keyword
	 */
	public Object findProjectByTargetKeyword(List<JSONObject> objList, String keyword)
	{
		logger.info("finding project only by keyword...");
		List<JSONObject> projectList = new ArrayList<>();
		boolean targetKeywordPresent = false;
		
		for(JSONObject projObj : objList)
		{
			//check for each project whether it matches with the required target key number
			targetKeywordPresent = checkaTargetKeywordPresent(projObj, keyword);
			
			if(targetKeywordPresent)
			{
				projectList.add(projObj);
			}
		}
		return findProjectWithHighestCost(projectList);
	}
	
	
	
	/**
	 * @param targetCountry
	 * @param jsonObjList 
	 * @return list of projects with the target country as parameter and then with highest cost among them
	 * @throws java.text.ParseException 
	 */
	@SuppressWarnings("unchecked")
	public Object findProjectByTargetCountry(String targetCountry, List<JSONObject> jsonObjList) throws java.text.ParseException
	{
		logger.info("finding project by the country passed in request...");
		List<JSONObject> projectList = new ArrayList<>();
		
		for(JSONObject obj : jsonObjList)
		{
			if(checkCountryPresent(obj, targetCountry))
				projectList.add(obj);
		}
		return findProjectWithHighestCost(projectList);
		
	}
	
	/**
	 * @param targetCountry
	 * @param num
	 * @param jsonObjList
	 * @return Project Object
	 */
	@SuppressWarnings("unchecked")
	public Object findProjByCountryAndNum(String targetCountry, int num, List<JSONObject> jsonObjList)
	{
		logger.info("finding project by country and key number both...");
		List<JSONObject> projectList = new ArrayList<>();
		
		for(JSONObject obj : jsonObjList)
		{
			//check if the current project contains both the targetCountry and targetKeyNum as required
			if(checkCountryPresent(obj, targetCountry) && checkTargetKeyNumPresent(obj, num))
			{
				projectList.add(obj);
			}
		}
		return findProjectWithHighestCost(projectList);
	}
	
	/**
	 * @param obj
	 * @param targetCountry
	 * @param num
	 * @return boolean value based on country and num both present
	 */
	
	/**
	 * @param jsonObjList
	 * @param targetCountry
	 * @param number
	 * @param keyword
	 * @return Project Object that matches the country and target key number
	 */
	@SuppressWarnings("unchecked")
	public Object findProjectByCountryNumKeyword(List<JSONObject> jsonObjList, String targetCountry, int number, String keyword)
	{
		logger.info("finding project by country, key number amd key keyword...");
		List<JSONObject> projectList = new ArrayList<>();
		for(JSONObject obj : jsonObjList)
		{
			//check for each project whether it matches the required country, target number and keyword
			if(checkCountryPresent(obj, targetCountry) && checkTargetKeyNumPresent(obj, number) && checkaTargetKeywordPresent(obj, keyword))
			{
				projectList.add(obj);
			}
		}
		return findProjectWithHighestCost(projectList);
	}
	
	/**
	 * @param jsonObjList
	 * @param targetCountry
	 * @param keyword
	 * @return Project Object that matches the country and target keyword
	 */
	public Object findProjectByCountryAndKeyword(List<JSONObject> jsonObjList, String targetCountry, String keyword)
	{
		logger.info("finding project by country and keyword...");
		List<JSONObject> projectList = new ArrayList<>();
		for(JSONObject obj : jsonObjList)
		{
			//check for each project whether it matches the required country and target keyword
			if(checkCountryPresent(obj, targetCountry) && checkaTargetKeywordPresent(obj, keyword))
			{
				projectList.add(obj);
			}
		}
		return findProjectWithHighestCost(projectList);
	}
	
	
	/**
	 * @param projObjList
	 * @return List of projects which are enabled and not expired
	 * @throws java.text.ParseException
	 * method to check project is enabled and not expired and having not null projectURL
	 */
	public List<JSONObject> checkEnableActiveValidURLProject(List<JSONObject> projObjList) throws java.text.ParseException
	{
		logger.info("checking for enabled, active projects with not null project url...");
		List<JSONObject> enabledActivePrjList = new ArrayList<>();
		SimpleDateFormat sdf = new SimpleDateFormat("MMddyyyy HH:mm:ss");
		Date currentDate = new Date();
		
		for(JSONObject projObj : projObjList)
		{
			Date projExpiryDate = sdf.parse((String)projObj.get("expiryDate"));
			
			if((Boolean)projObj.get("enabled") && (currentDate.compareTo(projExpiryDate)<=0) && (projObj.get("projectUrl")!=null))
				enabledActivePrjList.add(projObj);
		}
		
		return enabledActivePrjList;
	}
	
	/**
	 * @param projObj
	 * @param targetCountry
	 * @return boolean 
	 */
	@SuppressWarnings("unchecked")
	public boolean checkCountryPresent(JSONObject projObj, String targetCountry)
	{
		boolean targetCountryPresent = false;
		for(String country : (List<String>)projObj.get("targetCountries"))
		{
			if(targetCountry.equalsIgnoreCase(country))
			{
				targetCountryPresent = true;
				break;
			}
		}
		return targetCountryPresent;
	}
	
	/**
	 * @param projObj
	 * @param number
	 * @return boolean
	 */
	@SuppressWarnings("unchecked")
	public boolean checkTargetKeyNumPresent(JSONObject projObj, int number)
	{
		boolean targetKeyNumPresent = false;
		//check if the targetKeyNumber of this project is >= to the required project number
		for(JSONObject keyObj : (List<JSONObject>)projObj.get("targetKeys"))
		{
			if((Long)keyObj.get("number")>=Long.valueOf(number))
			{
				targetKeyNumPresent = true;
				break;
			}
		}
		return targetKeyNumPresent;
	}
	
	/**
	 * @param projObj
	 * @param keyword
	 * @return boolean
	 */
	@SuppressWarnings("unchecked")
	public boolean checkaTargetKeywordPresent(JSONObject projObj, String keyword)
	{
		boolean checkKeywordPresent = false;
		//check for each project whether it matches with the required keyword
		for(JSONObject keyObj : (List<JSONObject>)projObj.get("targetKeys"))
		{
			if(keyword.equalsIgnoreCase((String)keyObj.get("keyword")))
			{
				checkKeywordPresent = true;
				break;
			}
		}
		return checkKeywordPresent;
	}
	
	/**
	 * @param jsonObjList
	 * @return JSONArray
	 * finding projects with the maximum costs
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object findProjectWithHighestCost(List<JSONObject> jsonObjList)
	{
		logger.info("retreiving project with the highest cost...");
		JSONArray projObjArr = new JSONArray();
		
		//HashMap to store the project object and its corresponding cost in key, value pair
		HashMap<JSONObject, Double> projectMap = new HashMap<>();
		
		for(JSONObject jsonObj : jsonObjList)
		{
			projectMap.put(jsonObj, (Double)jsonObj.get("projectCost"));
		}
		
		//the list may contain no projects as all could be expired or not enabled or empty based on other rules, then return no project found
		if(jsonObjList.size()==0)
		{
			return noProjFoundJSONObj;
		}
		//if there is only active project it is the only project with maximum cost
		else if(jsonObjList.size()==1)
		{
			return createResponseObj(jsonObjList.get(0));
		}
		else
		{
			double maxCost = Double.MIN_VALUE;
			
			//iterating over the map entry and comparing each project's cost with the previous found cost
			for(Map.Entry<JSONObject, Double> projEntryObj : projectMap.entrySet())
			{
				double eachProductCost = projEntryObj.getValue();
				if(eachProductCost>maxCost)
					maxCost=eachProductCost;
			}
			//adding the projects with maximum costs in the json Array 
			for(Map.Entry<JSONObject, Double> projEntryObj : projectMap.entrySet())
			{
				if(projEntryObj.getValue()==maxCost)
					projObjArr.add(createResponseObj(projEntryObj.getKey()));
			}
		}
		
		return projObjArr;
		
	}
	
	//this method loads all the projects list json objects from the txt file 
	public List<JSONObject> loadProjectFromFile(String filename) throws IOException, ParseException
	{
		logger.info("loading project list from the txt file...");
		JSONParser jsonParser = new JSONParser();
		List<JSONObject> jsonObjList = new ArrayList<>();
		String line = null;

		// FileReader reads text file in the default encoding.
		FileReader fileReader = new FileReader(filename);

		BufferedReader bufferedReader = new BufferedReader(fileReader);

		//iterating over each json object in the text file 
		while((line=bufferedReader.readLine()) !=null)
		{
			JSONObject jsonObj =  (JSONObject)jsonParser.parse(line);
			jsonObjList.add(jsonObj);
		}
		bufferedReader.close();
		fileReader.close();
		
		return jsonObjList;

	}
	
	/**
	 * @param projList
	 * @return boolean
	 */
	public boolean checkProjectAlreadyPresent(List<JSONObject> projList, Project project)
	{
		boolean projectPresent = false;
		if(projList.size()!=0)
		{
			//for each proj in the project list check whether there is any project present with the same requested project id
			for(JSONObject proj : projList)
			{
				if(Long.valueOf(project.getId())==proj.get("id"))
				{
					projectPresent=true;
					break;
				}
			}
		}
		return projectPresent;
	}
	
	/**
	 * @param project
	 * @return string message based on data validation
	 * @throws java.text.ParseException 
	 * @throws com.fasterxml.jackson.core.JsonParseException 
	 * @throws JsonMappingException 
	 */
	public void checkValidRequestPayload(Project project) throws java.text.ParseException, com.fasterxml.jackson.core.JsonParseException, JsonMappingException
	{
		logger.info("checking for valid request payload...");
		if(project.getId()<=0)
		{
			throw new JsonMappingException("Invalid id");
		}
		
		if(project.getCreationDate()!=null)
			checkValidDateFormat(project.getCreationDate());
		
		if(project.getExpiryDate()!=null)
			checkValidDateFormat(project.getExpiryDate());
		
		if(project.getId()!=0 && (project.getCreationDate()==null || project.getExpiryDate()==null))
			throw new JsonMappingException("Date can not be null");
		
		if(checkCreationExpiryDateOrder(project.getCreationDate(), project.getExpiryDate()))
			throw new JsonMappingException("Creation date can not be later than expiry date");
		
	}
	
	public boolean checkCreationExpiryDateOrder(String creationDate, String expiryDate) throws JsonMappingException
	{
		SimpleDateFormat sdf = new SimpleDateFormat("MMddyyyy HH:mm:ss");
		Date projExpiryDate = new Date();
		Date projCreationDate = new Date();
		boolean improperDateOrder = false;
		try {
			projExpiryDate = sdf.parse(expiryDate);
			projCreationDate =sdf.parse(creationDate);
		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			throw new JsonMappingException("Invalid date format");
			
		}
		
		if(projCreationDate.compareTo(projExpiryDate)>0)
		{
			improperDateOrder=true;
			return improperDateOrder;
		}
		
		return improperDateOrder;
	}
	
	public boolean checkValidDateFormat(String date) throws JsonMappingException
	{
		SimpleDateFormat sdf = new SimpleDateFormat("MMddyyyy HH:mm:ss");
		try {
			if(sdf.parse(date) instanceof Date)
			{
				return false;
			}
		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			throw new JsonMappingException("Invalid date format"); 
		}
		return true;
			
	}
	
	public Response checkValidGetRequestParams(UriInfo uriInfo)
	{
		logger.info("checking for the params..");
		Map<String, List<String>> paramMap = uriInfo.getQueryParameters();
		
		String[] validParams = {"projectid","country", "number","keyword"};
		
		boolean invalidParamPresent = false;
		
		for(Map.Entry<String, List<String>> entry : paramMap.entrySet())
		{
			if(!Arrays.asList(validParams).contains(entry.getKey()))
			{
				invalidParamPresent = true;
				break;
			}
		}
		
		if(invalidParamPresent)
			return Response.status(400).entity("Invalid GET request with new params..").build();
		
		return null;
		
	}
	
	public Response checkInvalidIdRequest(UriInfo uriInfo)
	{
		logger.info("chceking for invalid id in get request..");
		if(Integer.valueOf(uriInfo.getQueryParameters().get("projectid").get(0))<=0)
			return Response.status(400).entity("Invalid GET request by id").build();
		return null;
		
	}
}
