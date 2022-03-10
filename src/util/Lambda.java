package util;

public interface Lambda {
	
	@FunctionalInterface
	public interface Function<Y, X> { 
		Y apply(X x);
	}
	
}

