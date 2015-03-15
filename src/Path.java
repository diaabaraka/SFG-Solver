
public class Path {
	
	private double gain;
	private String path;
	
	public Path(double gain, String path){
		this.gain = gain;
		this.path = path;
	}
	
	public double getGain() {
		return gain;
	}
	
	public String getPath() {
		return path;
	}
	
	public void setGain(long gain) {
		this.gain = gain;
	}
	
	public void setPath(String path) {
		this.path = path;
	}
}
