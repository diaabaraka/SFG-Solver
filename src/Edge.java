
public class Edge {
	
	private int to;
	private double gain;
	public Edge(int to, double gain){
		this.to = to;
		this.gain = gain;
	}
	

	public int getTo() {
		return to;
	}
	
	public double getGain() {
		return gain;
	}
	

	public void setGain(int gain) {
		this.gain = gain;
	}
	
	public void setTo(int to) {
		this.to = to;
	}
}
