/**
 * 
 */
package com.unity.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;

/**
 * @author Abhishek
 *
 */
@Provider
public class UnrecognizedPropertyExceptionMapper implements ExceptionMapper<UnrecognizedPropertyException> {

	@SuppressWarnings("deprecation")
	@Override
	public Response toResponse(UnrecognizedPropertyException exception) {
		// TODO Auto-generated method stub
		return Response
                .status(Response.Status.BAD_REQUEST)
                .entity("This is an invalid request. The field " + exception.getUnrecognizedPropertyName() + " is not recognized by the system.")
                .type( MediaType.TEXT_PLAIN)
                .build();
	}

}
