package sn.intouch.gu.lonaciapi.ejb.jndiutils;

import javax.naming.InitialContext;
import javax.naming.NamingException;

public class JNDIUtils {
	
	

private JNDIUtils() {
		super();
		// TODO Auto-generated constructor stub
	}

public static Object  lookUpEJB(EJBRegistry ejbRegistry)
{
	try {
		InitialContext initialContext = new InitialContext();
		return initialContext.lookup("java:app/LonaciAPI-ejb/"+ejbRegistry.name());
	} catch (NamingException e) {
		e.printStackTrace();
	}
	return null;
	}
}
