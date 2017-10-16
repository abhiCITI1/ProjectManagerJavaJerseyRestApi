/**
 * 
 */
package com.unity.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * @author Abhishek
 *
 */
@Provider
public class JsonMappingExceptionMapper implements ExceptionMapper<JsonMappingException> {

	@Override
	public Response toResponse(JsonMappingException exception) {
		// TODO Auto-generated method stub
		return Response
                .status(Response.Status.BAD_REQUEST)
                .entity("This is an invalid request. At least one field format or field rule is not validated by the system. " + exception.getMessage())
                .type( MediaType.TEXT_PLAIN)
                .build();
	}

}
