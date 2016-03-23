package com.babeeta.hudee.test.parallel;

public interface CredentialProvider {
	  public abstract String getId(Session paramSession);

	  public abstract String getKey(Session paramSession);
}
