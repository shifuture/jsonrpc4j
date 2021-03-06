package com.googlecode.jsonrpc4j;

import static com.googlecode.jsonrpc4j.ErrorResolver.JsonError.ERROR_NOT_HANDLED;

import com.fasterxml.jackson.databind.JsonNode;

import java.lang.reflect.Method;
import java.util.List;

/**
 * An {@link com.googlecode.jsonrpc4j.ErrorResolver} that puts type information into the
 * data portion of the error.  This {@link com.googlecode.jsonrpc4j.ErrorResolver} always
 * returns a {@link com.googlecode.jsonrpc4j.ErrorResolver.JsonError JsonError}.
 *
 */
@SuppressWarnings("WeakerAccess")
public enum DefaultErrorResolver implements ErrorResolver {
	INSTANCE;

	/**
	 * {@inheritDoc}
	 */
	public JsonError resolveError(Throwable t, Method method, List<JsonNode> arguments) {
		return new JsonError(ERROR_NOT_HANDLED.code, t.getMessage(), new ErrorData(t.getClass().getName(), t.getMessage()));
	}

}
