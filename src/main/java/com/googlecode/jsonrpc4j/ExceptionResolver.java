package com.googlecode.jsonrpc4j;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Resolves client {@link Throwable}s from server generated {@link com.fasterxml.jackson.databind.node.ObjectNode}.
 *
 */
public interface ExceptionResolver {

	/**
	 * Resolves the exception from the given json-rpc
	 * response {@link com.fasterxml.jackson.databind.node.ObjectNode}.
	 * @param response the response
	 * @return the exception
	 */
	Throwable resolveException(ObjectNode response);

}
