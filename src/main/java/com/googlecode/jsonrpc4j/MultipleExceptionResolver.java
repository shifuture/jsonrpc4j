package com.googlecode.jsonrpc4j;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * {@link com.googlecode.jsonrpc4j.ExceptionResolver} that supports the use
 * of multiple {@link com.googlecode.jsonrpc4j.ExceptionResolver} used one
 * after another until one is able to resolve
 * the Exception.
 *
 */
@SuppressWarnings({ "unused", "WeakerAccess" })
public class MultipleExceptionResolver implements ExceptionResolver {

	private final List<ExceptionResolver> resolvers;

	/**
	 * Creates with the given {@link com.googlecode.jsonrpc4j.ExceptionResolver}s,
	 * {@link #addExceptionResolver(com.googlecode.jsonrpc4j.ExceptionResolver)} can be called to
	 * add additional {@link com.googlecode.jsonrpc4j.ExceptionResolver}s.
	 * @param resolvers the {@link com.googlecode.jsonrpc4j.ExceptionResolver}s
	 */
	public MultipleExceptionResolver(ExceptionResolver... resolvers) {
		this.resolvers = new LinkedList<>();
		Collections.addAll(this.resolvers, resolvers);
	}

	/**
	 * Adds an {@link com.googlecode.jsonrpc4j.ExceptionResolver} to the end of the
	 * resolver chain.
	 * @param ExceptionResolver the {@link com.googlecode.jsonrpc4j.ExceptionResolver} to add
	 */
	public void addExceptionResolver(ExceptionResolver ExceptionResolver) {
		this.resolvers.add(ExceptionResolver);
	}

	/**
	 * {@inheritDoc}
	 */
	public Throwable resolveException(ObjectNode response) {
		for (ExceptionResolver resolver : resolvers) {
			Throwable resolvedException = resolver.resolveException(response);
			if (resolvedException != null) { return resolvedException; }
		}
		return null;
	}

}
