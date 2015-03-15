import java.util.*;

import javax.xml.stream.events.Namespace;


public class SFG {

	private ArrayList<Edge> graph[];
	private ArrayList<Path> forwardPaths, individualLoops;
	private boolean[] visited;
	private ArrayList<ArrayList<Integer>> allLoops;
	private Hashtable<String, Boolean> orignal;
	private double deltaM[];
	private ArrayList<String> nodesName;

	@SuppressWarnings("unchecked")
	public SFG(int nodes, ArrayList<String> names) {
		graph = new ArrayList[nodes];
		for (int i = 0; i < nodes; i++) {
			graph[i] = new ArrayList<>();
		}
		visited = new boolean[nodes];
		nodesName = names;
	}

	public double[] getDeltas(){
		return deltaM;
	}
	
	public String printAllLoops() {
		if (allLoops == null) {
			constructLoops();
		}

		int level = 1;
		StringBuilder output = new StringBuilder();
		for (ArrayList<Integer> loop : allLoops) {
			
			output.append(level).append(" Untouched Loops:\n");
			if (!loop.isEmpty()) {
				int cnt=1;
				for (int i = 0; i < loop.size(); i += level) {
					output.append("Loop #").append(cnt++).append(": ");
					output.append(individualLoops.get(loop.get(i)).getPath());
					double gain = individualLoops.get(loop.get(i)).getGain();
					for (int j = 1; j < level; j++) {
						output.append(" AND ").append(individualLoops.get(loop.get(i + j)).getPath());
						gain*=individualLoops.get(loop.get(i+j)).getGain();
					}
					output.append(" (And its/their total Gain = ").append(gain);
					output.append(")\n");
				}
			}
			level++;
			output.append("====================================\n");
		}
		return output.toString();
	}

	public double computeGain(int src, int dest) {
		
		
		
		if (deltaM == null) {
			deltaM = new double[forwardPaths.size()+1];
			int path = 0;

			double ans = 0;
			for (Path p : forwardPaths) {
				deltaM[path+1]=computeDelta(path);
				ans += (deltaM[path+1] * p.getGain());
				path++;
			}
			return ans;
		}else{
			double ans = 0;
			int path=0;
			for (Path p : forwardPaths) {
				ans += (deltaM[++path] * p.getGain());
			}
			return ans;
		}
	}

	private double computeDelta(int num) {

		

		int sign = -1;
		double delta = 1L;
		
		if(allLoops==null){
			printAllLoops();
		}
		
		orignal = new Hashtable<>(); // remove list
		String[] remove = forwardPaths.get(num).getPath().split(" ");

		for (String a : remove) {
			orignal.put(a, true);
		}
		
		int levels = allLoops.size();
		System.out.println("Delta "+(num+1));
		for (int level = 0; level < levels; level++) {

			ArrayList<Integer> cur = allLoops.get(level);
			double brackerGain=0.0;
			for (int i = 0; i < cur.size(); i += (level + 1)) {
				double termGain = 1.0;
				for (int j = 0; j <= level; j++) {
					if (isTouched(individualLoops.get(cur.get(i + j)).getPath()
							.split(" "))) {
						termGain = 0.0;
						break;
					}else{
						System.out.println("UnTouched : "+individualLoops.get(cur.get(i+j)).getPath());
					}
					termGain *= individualLoops.get(cur.get(i + j)).getGain();
				}
				
				brackerGain+=termGain;
				
			}
			delta += (sign * brackerGain);
			sign *= -1;
		}
		System.out.println("Delta "+(num+1)+" = "+delta);
		return delta;
	}

	public double computeDelta() {

		constructLoops();

		int sign = -1;
		double delta = 1L;
		int levels = allLoops.size();
		for (int level = 0; level < levels; level++) {

			ArrayList<Integer> cur = allLoops.get(level);
			
			double bracketGain = 0.0;
			for (int i = 0; i < cur.size(); i += (level + 1)) {
				double termGain=individualLoops.get(cur.get(i)).getGain();
				for (int j = 1; j <= level; j++) {
					termGain *= individualLoops.get(cur.get(i + j)).getGain();
				}

				bracketGain += termGain;
			}
			
			delta+=(sign*bracketGain);
			sign *= -1;
		}

		return deltaM[0]=delta;
	}

	private void constructLoops() {
		if (allLoops == null) {
			allLoops = new ArrayList<>();
			// Start adding the first level of n'th non-touching loops
			ArrayList<Integer> individual;
			constructIndividualLoops();
			allLoops.add(individual = new ArrayList<Integer>());
			for (int i = 0; i < individualLoops.size(); i++) {
				individual.add(i);
			}
			combination(1);
		}

	}

	public void addArrow(int from, int to, double gain) { // from/to base 1
		graph[--from].add(new Edge(--to, gain));
	}

	public String printForwardPaths(int src, int dest) {
		if (forwardPaths == null)
			buildForwardPaths(src, dest);
		int i = 1;
		StringBuilder output = new StringBuilder();
		for (Path path : forwardPaths) {
			output.append("Forward Path #").append(i++).append(": ").append(path.getPath());
			output.append(" AND Gain = ").append(path.getGain()).append("\n");
		}
		return output.toString();
	}

	public void buildForwardPaths(int src, int dest) {
		forwardPaths = new ArrayList<>();
		visited[src-1]=true;
		DFS(src - 1, dest - 1, 1L, nodesName.get(src-1));
		visited[src-1]=false;
		deltaM = null;
	}

	private void constructIndividualLoops() {
		if (individualLoops == null) {

			ArrayList<Path> temp = forwardPaths;
			int prev;
			forwardPaths = new ArrayList<>();
			for (int src = 0; src < graph.length; src++) {
				visited[src] = true;
				for (Edge node : graph[src]) {

					visited[node.getTo()] = true;

					prev = forwardPaths.size();
					DFS(node.getTo(),
							src,
							1L * node.getGain(),
							nodesName.get(src) + " "
									+ nodesName.get(node.getTo()));

					visited[node.getTo()] = false;

					if (prev < forwardPaths.size()) // new paths were added
						checkRepeated(prev); // remove repeated paths

				}
				visited[src] = false;
			}

			individualLoops = forwardPaths;
			forwardPaths = temp;

		}
	}

	private void checkRepeated(int prev) {
		Path recent, tPath;
		int PREV = prev;
		Stack<Integer> removeList = new Stack<>(); // repeated paths to be
													// removed

		for (; prev < forwardPaths.size(); prev++) {

			String[] recentPath = (recent = forwardPaths.get(prev)).getPath()
					.split(" ");
			orignal = new Hashtable<>();
			for (int i = 0; i < recentPath.length; i++) {
				orignal.put(recentPath[i], true);
			}

			String[] tempStrings;
			for (int i = 0; i < PREV; i++) {

				tempStrings = (tPath = forwardPaths.get(i)).getPath()
						.split(" ");

				if (recent.getGain() == tPath.getGain() && isEqual(tempStrings)) {
					removeList.push(prev);
					break;
				}
			}
		}

		while (!removeList.isEmpty()) {
			forwardPaths.remove((int) removeList.pop());
		}
	}

	private boolean isEqual(String[] b) {

		if (orignal.size() != (b.length - 1)) // because "b" has the src node
												// twice
			return false;

		for (int i = 0; i < b.length; i++) {
			if (!orignal.containsKey(b[i]))
				return false;
		}
		return true;
	}

	private void DFS(int cur, int dest, double gain, String path) {
		if (cur == dest) {
			if (!path.equals(Integer.toString(dest + 1)))
				forwardPaths.add(new Path(gain, path));
			return;
		}

		int to;
		for (Edge node : graph[cur]) {
			to = node.getTo();

			if (to == dest || !visited[to]) {
				visited[to] = true;
				DFS(to, dest, gain * node.getGain(),
						path + " " + nodesName.get(to));
				visited[to] = false;
			}
		}
	}

	private void combination(int level) { // level is in base "1"
		// starting building the (level) non-touching loops
		// from the (level-1) non-touching loops

		if (level > allLoops.size())
			return;

		ArrayList<Integer> curLevel = allLoops.get(level - 1);
		ArrayList<Integer> zeroLevel = allLoops.get(0);

		int length = allLoops.get(level - 1).size(); // length of the (level)
														// list
		int zeroLenght = zeroLevel.size();
		for (int i = 0; i < length; i += level) {

			for (int k = i + level; k < zeroLenght; k++) {

				boolean touched = false;
				for (int j = 0; j < level; j++) {

					if (isTouched(individualLoops.get(zeroLevel.get(k)),
							individualLoops.get(curLevel.get(i + j)))) {
						touched = true;
						break;
					}

				}
				if (!touched) { // if not touched then add this combination
					ArrayList<Integer> nextLevel = new ArrayList<>();
					if (allLoops.size() == level)
						allLoops.add(nextLevel = new ArrayList<Integer>());
					else
						nextLevel = allLoops.get(level);
					// check if this combination of paths were already taken

					for (int j = 0; j < level; j++) {
						nextLevel.add(zeroLevel.get(i + j));
					}
					nextLevel.add(zeroLevel.get(k));

				}
			}
		}
		combination(level + 1);
	}

	private boolean isTouched(Path a, Path b) {

		String aa[] = a.getPath().split(" ");
		String[] bb = b.getPath().split(" ");

		orignal = new Hashtable<>();
		for (int i = 0; i < aa.length; i++) {
			orignal.put(aa[i], true);
		}

		for (int i = 0; i < bb.length; i++) {
			if (orignal.containsKey(bb[i]))
				return true;
		}
		return false;
	}

	private boolean isTouched(String[] a) {

		for (int i = 0; i < a.length; i++) {
			if (orignal.containsKey(a[i]))
				return true;
		}
		return false;
	}
}
