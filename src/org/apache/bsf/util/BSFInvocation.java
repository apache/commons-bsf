package org.apache.bsf.util;

import java.lang.reflect.Proxy;

import javax.script.Invocable;



/**
 * @author Sanka Samaranayake <ssanka@gmail.com>
 *
 */
public class BSFInvocation {
	private Invocable engine = null;
	
	public BSFInvocation(Invocable engine) {
		this.engine = engine;
	}
	
	public Object getInterface(Class clasz) {
		return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
				new Class[]{clasz}, new BSFInvocationHandler(engine));		
	}
	
}


			