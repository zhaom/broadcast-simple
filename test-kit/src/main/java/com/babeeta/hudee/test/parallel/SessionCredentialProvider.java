package com.babeeta.hudee.test.parallel;

public class SessionCredentialProvider implements CredentialProvider {

	@Override
	public String getId(Session paramSession) {
		 return paramSession.did;
	}

	@Override
	public String getKey(Session paramSession) {
		 return paramSession.secureKey;
	}

}
