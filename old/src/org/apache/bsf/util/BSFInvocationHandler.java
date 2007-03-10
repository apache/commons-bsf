package org.apache.bsf.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import javax.script.Invocable;

/**
 * @author Sanka Samaranayake <ssanka@gmail.com>
 *
 */
public class BSFInvocationHandler implements InvocationHandler {
	private Invocable engine;
	private Object target;
	
	public BSFInvocationHandler(Invocable engine) {
		this.engine = engine;		
	}

	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		return engine.call(method.getName(), args);
	}

}
