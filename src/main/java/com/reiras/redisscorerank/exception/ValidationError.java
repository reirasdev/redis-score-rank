package com.reiras.redisscorerank.exception;

import java.util.ArrayList;
import java.util.List;

public class ValidationError extends StandardError {
	private static final long serialVersionUID = 1L;
	
	private List<FieldMessage> fieldMessageList = new ArrayList<FieldMessage>();

	public ValidationError() {
		// TODO Auto-generated constructor stub
	}

	public ValidationError(Long timestamp, Integer status, String error, String message, String path) {
		super(timestamp, status, error, message, path);
		// TODO Auto-generated constructor stub
	}

	//Renamed from <getFieldMessageList> to <getErrors> for better looking.
	//Exhibited as "Errors" in Json response.
	public List<FieldMessage> getErrors() {
		return fieldMessageList;
	}

	public void setFieldMessageList(List<FieldMessage> fieldMessageList) {
		this.fieldMessageList = fieldMessageList;
	}

	public void AddError(String fieldName, String message) {
		this.fieldMessageList.add(new FieldMessage(fieldName, message));
	}

}
