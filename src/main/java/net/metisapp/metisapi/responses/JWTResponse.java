package net.metisapp.metisapi.responses;

public class JWTResponse {
	private final long expiry;
	private final String token;
	public JWTResponse(String token, long expiry){
		this.expiry = expiry;
		this.token = token;
	}

	@Override
	public String toString(){
		return String.format("{\"token\": \"%s\",\"expiry\":%d}", token, expiry);
	}
}
