import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Scanner;

import javax.swing.JOptionPane;
import javax.swing.JTextField;

/*
Test #1:

4 6
1 2 1
2 3 2
3 4 3
1 3 1
4 3 -2
4 2 -3
4

4 6
A B 1
B C 2
C D 3
A C 1
D C -2
D B -3
4

Test #2:

6 9
1 2 1
2 3 2
3 4 3
4 5 4
5 6 5
2 4 10
3 2 -10
5 4 -5
5 2 -20
6

Test #3:

6 10
1 2 1
2 3 5
3 4 10
4 5 2
2 6 10
6 6 -1
6 5 2
4 3 -1
5 4 -2
5 2 -1
5

Test #4:

9 12
1 2 1
2 3 1
3 4 2
4 5 1
5 6 3
6 7 4
7 8 1
9 8 1
3 8 10
8 5 -10
6 3 -10
8 2 -1
8

Test #5:

8 13
1 2 1
2 3 1
3 4 1
4 5 1
5 6 1
6 7 1
7 8 1
4 7 1
6 8 1
8 6 -1
6 5 -1
8 2 -1
7 3 -1
8

Test #6:

6 9
1 2 1
2 3 2
3 4 3
4 5 4
5 6 5
2 4 10
3 2 -10
5 4 -5
5 2 -20
6
*/

public class Solver {
	
	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		int nodes=in.nextInt(),edges=in.nextInt();
		String from, to;
		double gain;
		double[][] arr=new double[nodes][nodes];
		Hashtable<String, Integer> map = new Hashtable<>();
		ArrayList<String> aList = new ArrayList<>();
		int next = 0;
		for (int i = 0; i < edges; i++) {
			from = in.next(); 
			if(!map.containsKey(from)){
				aList.add(from);
				map.put(from, next++);
			}
			to = in.next(); 
			if(!map.containsKey(to)){
				aList.add(to);
				map.put(to, next++);
			}
			gain = in.nextDouble(); 
			arr[map.get(from)][map.get(to)] = gain;
			
		}
		int dest = in.nextInt();
		
		Solver obMain = new Solver();
		obMain.constructGraph(arr, aList);
		System.out.println(obMain.printAllLoops());
		System.out.println(obMain.printForwardPaths(1, dest));
		System.out.println("Total Gain = "+obMain.computeGain(1, dest));
		
		double [] gains = obMain.getDeltas();
		for (int i = 0; i < gains.length; i++) {
			System.out.printf("Delta %d: %f\n",i,gains[i]);
		}
	}
	
	private SFG sfg;
	private double[][] graph;
	private int inputNode=-1;
	public boolean constructGraph(double [][] graph, ArrayList<String> names){
		this.graph=graph;
		if(getInputNode()==-1){
			JOptionPane.showMessageDialog(null, "The SFG doesn't contain input node!");
			return false;
		}
		
		int length = graph.length;
		sfg = new SFG(length, names);
		
		
		for (int i = 0; i < length; i++) {
			for (int j = 0; j < length; j++) {
				if(Double.compare(graph[i][j], 0)!=0){
					System.out.printf("From %d To %d Gain %f\n",(i+1),(j+1),graph[i][j]);
					sfg.addArrow(i+1, j+1, graph[i][j]);
				}
			}
		}
		return true;
	}
	
	public String printForwardPaths(int src, int dest){
		return sfg.printForwardPaths(src, dest);
	}
	
	public String printAllLoops(){
		return sfg.printAllLoops();
	}
	
	public double computeGain(int src, int dest){ // base 1
		src--;
		boolean isInput=true;
		for (int i = 0; i < graph.length; i++) {
			if(i!=src){
				if(graph[i][src]!=0)
				{
					isInput=false;
					break;
				}
			}
		}
		
		if(isInput){
			sfg.buildForwardPaths(src+1, dest);
			return 1.0*sfg.computeGain(src+1, dest)/sfg.computeDelta();
		}else{
			sfg.buildForwardPaths(getInputNode()+1, src+1);
			double deneminator = sfg.computeGain(getInputNode()+1, src+1);
			sfg.buildForwardPaths(getInputNode()+1, dest);
			double numerator = sfg.computeGain(getInputNode()+1, dest);
			return numerator/deneminator;
		}
		
	}
	
	private int getInputNode(){
		if(inputNode==-1){
			
			for (int i = 0; i < graph.length; i++) {
				boolean isInput = true;
				for (int j = 0; j < graph.length; j++) {
					if(graph[j][i]!=0){
						isInput = false;
						break;
					}
				}
				if(isInput)
					return inputNode=i;
			}
			
		}
		
		return inputNode;
	}
	
	public double[] getDeltas(){
		return sfg.getDeltas();
	}
	
}
