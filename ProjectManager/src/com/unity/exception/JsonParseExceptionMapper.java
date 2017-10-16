/**
 * 
 */
package com.unity.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.core.JsonParseException;

/**
 * @author Abhishek
 *
 */
@Provider
public class JsonParseExceptionMapper implements ExceptionMapper<JsonParseException> {

	@Override
	public Response toResponse(JsonParseException exception) {
		// TODO Auto-generated method stub
		return Response
                .status(Response.Status.BAD_REQUEST)
                .entity("This is an invalid json. The request can not be parsed")
                .type( MediaType.APPLICATION_JSON)
                .build();
	}

}
