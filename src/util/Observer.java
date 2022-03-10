package util;

public interface Observer {

	public NullObserver nullobserver = new NullObserver();
	
	public void update(String msg);

	static class NullObserver implements Observer {

		@Override
		public void update(String msg) {
			
		}
	}
	
}
