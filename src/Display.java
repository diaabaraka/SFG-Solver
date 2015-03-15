import java.io.File;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jgraph.JGraph;
import org.jgraph.graph.DefaultCellViewFactory;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.GraphModel;
public class Display extends JFrame {
	private static final Dimension FRAME_DIM = new Dimension(1300, 700);
	private static final Dimension DRAWING_PANE = new Dimension(1165, 700);

	transient private JTextField fromField, toField, gainField, fromPath,
			toPath, nodeToDelete, startToDelete, endToDelete;
	private Hashtable<String, Integer> nodes;
	private ArrayList<double[]> edgeList;
	private ArrayList<String> nodesList;
	private int index;
	private GraphModel model;
	private GraphLayoutCache view;
	private JGraph graph;
	private DefaultGraphCell[] cells;
	private JPanel drawingPanel;
	private JScrollPane drawGraph;

	public static void main(String[] args) {
		 new Display();

	}
	
	public Display() {
		drawGraph = new JScrollPane(graph);

		index = 0;
		setTitle("SFG Solver");
		setLayout(new BorderLayout());
		setSize(FRAME_DIM);
		setLocation(10, 10);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		drawingPanel = new JPanel();
		drawingPanel.setBackground(Color.WHITE);
		drawingPanel.setPreferredSize(new Dimension(DRAWING_PANE));

		JPanel edgeJPanel = new JPanel();
		// drawingPanel.add(drawGraph);
		drawingPanel.setVisible(true);
		this.getContentPane().add(edgeJPanel, BorderLayout.WEST);
		this.getContentPane().add(drawingPanel, BorderLayout.EAST);

		edgeJPanel.setLayout(new GridLayout(17, 1));

		nodes = new Hashtable<>();
		nodesList = new ArrayList<>();
		edgeList = new ArrayList<>();

		startToDelete = new JTextField();
		endToDelete = new JTextField();
		nodeToDelete = new JTextField();
		fromField = new JTextField();
		toField = new JTextField();
		gainField = new JTextField();

		fromPath = new JTextField();
		toPath = new JTextField();

		JButton clear = new JButton("CLEAR");
		JButton load = new JButton("LOAD");
		JButton save = new JButton("SAVE");
		JButton deleteEdge = new JButton("DELETE EDGE");
		JButton deleteNode = new JButton("DELETE NODE");
		JButton enterEdge = new JButton("ADD EDGE");
		JButton showLoops = new JButton("SHOW LOOPS");
		JButton totalGain = new JButton("GAINS");
		JButton forwardPaths = new JButton("Forward Paths");
		edgeJPanel.add(fromField);
		edgeJPanel.add(toField);
		edgeJPanel.add(gainField);
		edgeJPanel.add(enterEdge);

		edgeJPanel.add(startToDelete);
		edgeJPanel.add(endToDelete);
		edgeJPanel.add(deleteEdge);

		edgeJPanel.add(nodeToDelete);
		edgeJPanel.add(deleteNode);
		
		edgeJPanel.add(fromPath);
		edgeJPanel.add(toPath);

		edgeJPanel.add(forwardPaths);
		edgeJPanel.add(totalGain);
		edgeJPanel.add(showLoops);
		edgeJPanel.add(save);
		edgeJPanel.add(load);
		edgeJPanel.add(clear);
		clear.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				nodes = new Hashtable<>();
				edgeList = new ArrayList<>();
				nodesList = new ArrayList<>();
				index = 0;
				updateDrawing();
			}
		});
		save.addActionListener(new ActionListener() {

			   @Override
			   public void actionPerformed(ActionEvent e) {

			    JFileChooser chooser = new JFileChooser();
			    FileNameExtensionFilter filter = new FileNameExtensionFilter(
			      "XML Files", "xml");
			    chooser.setFileFilter(filter);
			    int returnVal = chooser.showOpenDialog(getParent());
			    if (returnVal == JFileChooser.APPROVE_OPTION) {
			     String path = chooser.getSelectedFile().getAbsolutePath()
			       + ".xml";
			     System.out.println(path);
			     if (!save(path))
			      JOptionPane.showMessageDialog(null, "Saved");
			    }
			    // load("E:\\"+fileName);
			   }
			  });
			  load.addActionListener(new ActionListener() {

			   @Override
			   public void actionPerformed(ActionEvent e) {
			    JFileChooser chooser = new JFileChooser();
			    FileNameExtensionFilter filter = new FileNameExtensionFilter(
			      "XML Files", "xml");
			    chooser.setFileFilter(filter);
			    int openChoice = chooser.showOpenDialog(getParent());

			    if (openChoice == JFileChooser.APPROVE_OPTION) {
			     String path = chooser.getSelectedFile().getAbsolutePath();
			     System.out.println(path);
			     load(path);
			    }

			   }
			  });
		totalGain.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (nodes.size() > 0) {
					int src = 0, dest = 0;
					String from = fromPath.getText().trim();
					String to = toPath.getText().trim();
					if (!nodes.containsKey(from)) {
						JOptionPane.showMessageDialog(null,
								"INVALID START NODE !!!!");
						return;
					}

					if (!nodes.containsKey(to)) {
						JOptionPane.showMessageDialog(null,
								"INVALID END NODE!!!!");
						return;
					}
					src = nodes.get(from);
					dest = nodes.get(to);

					Solver solver = new Solver();
					double[][] mat = new double[nodes.size()][nodes.size()];
					for (double[] edge : edgeList) {
						mat[(int) edge[0]][(int) edge[1]] = edge[2];
					}
					if (!solver.constructGraph(mat, nodesList))
						return;
					double gain = solver.computeGain(src + 1, 1 + dest);
					JFrame frame = new JFrame("Total Gain");
					frame.setLocation(100, 100);
					frame.setSize(500, 500);
					double[] deltas = solver.getDeltas();
					StringBuilder output = new StringBuilder();
					output.append("Total gain(Overall Transfer Function) = ")
							.append(gain)
							.append("\n===================================\n");
					output.append("Δ = ").append(deltas[0]).append("\n");
					for (int i = 1; i < deltas.length; i++) {
						output.append("Δ").append(i).append(" = ")
								.append(deltas[i]).append("\n");
					}
					JTextArea paths = new JTextArea(output.toString());
					JScrollPane scl = new JScrollPane(paths);
					paths.setFont(new Font("MONOSPACED", Font.BOLD, 18));
					paths.setLineWrap(true);
					paths.setWrapStyleWord(true);
					paths.setEditable(false);
					frame.add(scl);
					frame.setVisible(true);
				}
			}
		});

		forwardPaths.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (nodes.size() > 0) {
					int src = 0, dest = 0;
					String from = fromPath.getText().trim();
					String to = toPath.getText().trim();
					if (!nodes.containsKey(from)) {
						JOptionPane.showMessageDialog(null,
								"INVALID START NODE !!!!");
						return;
					}

					if (!nodes.containsKey(to)) {
						JOptionPane.showMessageDialog(null,
								"INVALID END NODE !!!!");
						return;
					}
					src = nodes.get(from);
					dest = nodes.get(to);

					Solver solver = new Solver();
					double[][] mat = new double[nodes.size()][nodes.size()];
					for (double[] edge : edgeList) {
						mat[(int) edge[0]][(int) edge[1]] = edge[2];
					}
					if (!solver.constructGraph(mat, nodesList))
						return;
					String output = solver.printForwardPaths(src + 1, 1 + dest);
					JFrame frame = new JFrame("Forward Paths");
					frame.setLocation(100, 100);
					frame.setSize(500, 500);

					JTextArea paths = new JTextArea(output);
					JScrollPane scl = new JScrollPane(paths);
					paths.setFont(new Font("MONOSPACED", Font.BOLD, 18));
					paths.setLineWrap(true);
					paths.setWrapStyleWord(true);
					paths.setEditable(false);
					frame.add(scl);
					frame.setVisible(true);
				}
			}
		});

		showLoops.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (nodes.size() > 0) {
					Solver solver = new Solver();
					double[][] mat = new double[nodes.size()][nodes.size()];
					for (double[] edge : edgeList) {
						mat[(int) edge[0]][(int) edge[1]] = edge[2];
					}
					if (!solver.constructGraph(mat, nodesList))
						return;
					String output = solver.printAllLoops();
					JFrame frame = new JFrame("Loops");
					frame.setLocation(100, 100);
					frame.setSize(500, 500);

					JTextArea loops = new JTextArea(output);
					JScrollPane scl = new JScrollPane(loops);
					loops.setFont(new Font("MONOSPACED", Font.BOLD, 18));
					loops.setLineWrap(true);
					loops.setWrapStyleWord(true);
					loops.setEditable(false);
					frame.getContentPane().add(scl);
					frame.setVisible(true);
				}
			}
		});

		model = new DefaultGraphModel();
		view = new GraphLayoutCache(model, new DefaultCellViewFactory());
		graph = new JGraph(model, view);
		enterEdge.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				int from, to;
				boolean finished = false;
				String fromNode = fromField.getText().trim();
				String toNode = toField.getText().trim();
				String gain = gainField.getText().trim();
				double gainVal = 0;

				if (fromNode.trim().length() == 0) {
					JOptionPane.showMessageDialog(null,
							"START NODE IS EMPTY !!!!");

				} else if (toNode.length() == 0) {
					JOptionPane.showMessageDialog(null,
							"END NODE IS EMPTY !!!!");

				} else {
					if (gain.trim().length() == 0) {
						JOptionPane.showMessageDialog(null,
								"GAIN Field IS EMPTY !!!!");
						finished = false;

					} else {
						if (isNumeric(gain)) {
							gainVal = Double.parseDouble(gain);
							finished = true;
						} else {
							JOptionPane.showMessageDialog(null,
									"INVALID GAIN VALUE");
						}
					}
				}
				if (finished) { // if valid data

					if (nodes.containsKey(fromNode)) {
						from = nodes.get(fromNode);

					} else {
						nodes.put(fromNode, index);
						nodesList.add(fromNode);
						from = index++;
					}

					if (nodes.containsKey(toNode)) {
						to = nodes.get(toNode);
					} else {
						nodes.put(toNode, index);
						nodesList.add(toNode);
						to = index++;
					}

					boolean isExisted = false;
					double[] edg = new double[] { from, to, gainVal };
					for (double[] arr : edgeList) {
						if ((int) arr[0] == from && (int) arr[1] == to) {
							arr[2] += gainVal;
							isExisted = true;
							edg = arr;
							break;
						}
					}
					if (!isExisted)
						edgeList.add(edg);
					updateDrawing();

				}

			}
		});

		deleteNode.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {

				int indexOfNode;
				boolean finished = false;
				String node = nodeToDelete.getText().trim();
				if (node.trim().length() == 0) {
					JOptionPane.showMessageDialog(null,
							"PLEASE INSERT NODE NAME!!!!");

				} else {
					if (nodes.containsKey(node)) {
						indexOfNode = nodes.get(node);
						nodes.remove(nodesList.get(indexOfNode));
						nodesList.remove(indexOfNode);
						index--;
						for (int j = indexOfNode; j < nodesList.size(); j++) {
							nodes.put(nodesList.get(j), j);
						}
						double[] edg;
						for (int i = 0; i < edgeList.size(); i++) {
							edg = edgeList.get(i);
							if ((int) edg[0] == indexOfNode
									|| (int) edg[1] == indexOfNode) {
								edgeList.remove(i);

								i--;
							} else {
								if ((int) edg[0] > indexOfNode) {
									edg[0]--;
								}
								if ((int) edg[1] > indexOfNode) {
									edg[1]--;
								}
							}
						}
						updateDrawing();

					} else {
						JOptionPane
								.showMessageDialog(null, "NO SUCH NODE !!!!");
					}
				}
			}

		});
		deleteEdge.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {

				int from = 0, to = 0;
				boolean finished = false;
				String fromNode = startToDelete.getText().trim();
				String toNode = endToDelete.getText().trim();
				double gainVal = 0;

				if (fromNode.trim().length() == 0) {
					JOptionPane.showMessageDialog(null,
							"START NODE IS EMPTY !!!!");

				} else if (toNode.length() == 0) {
					JOptionPane.showMessageDialog(null,
							"END NODE IS EMPTY !!!!");

				} else {
					if (nodes.containsKey(fromNode)
							&& nodes.containsKey(toNode)) {
						from = nodes.get(fromNode);
						to = nodes.get(toNode);
						finished = true;

					} else {
						JOptionPane.showMessageDialog(null,
								"NO SUCH NODES !!!!");

					}
					if (finished) {
						boolean isExisted = false;
						double[] edg;
						for (int i = 0; i < edgeList.size(); i++) {
							edg = edgeList.get(i);
							if ((int) edg[0] == from && (int) edg[1] == to) {
								edgeList.remove(i);

								i--;
							}
						}

						updateDrawing();

					}

				}

			}
		});
		setVisible(true);

	}

	private void updateDrawing() {
		cells = new DefaultGraphCell[nodesList.size() + edgeList.size()];
		// System.out.println(cells.length);
		int x = 1, y = 0, cnt = 0;
		int[] loc = { drawingPanel.getWidth() / 2, 50,
				drawingPanel.getWidth() - 100 };
		for (int i = 0; i < nodesList.size(); i++) {
			cells[i] = new DefaultGraphCell(nodesList.get(i));
			cells[i].addPort();
			x = loc[cnt % 3];
			cnt = (cnt + 1) % 3;
			GraphConstants.setBounds(cells[i].getAttributes(),
					new Rectangle2D.Double(x, y += 50, 40, 20));
			GraphConstants.setGradientColor(cells[i].getAttributes(),
					Color.cyan);
			GraphConstants.setOpaque(cells[i].getAttributes(), true);
		}
		int length = nodesList.size();
		DefaultEdge e;
		for (int i = 0; i < edgeList.size(); i++) {
			cells[length + i] = e = new DefaultEdge();
			// GraphConstants.setLineStyle(e.getAttributes(),
			// GraphConstants.STYLE_BEZIER);
			// GraphConstants.setRouting(e.getAttributes(),
			// GraphConstants.ROUTING_SIMPLE);

			GraphConstants.setBendable(e.getAttributes(), true);
			int arrow = GraphConstants.ARROW_CLASSIC;
			GraphConstants.setLineEnd(e.getAttributes(), arrow);

			e.setSource(cells[(int) edgeList.get(i)[0]].getChildAt(0));
			e.setTarget(cells[(int) edgeList.get(i)[1]].getChildAt(0));
			Point2D[] point = { new Point2D.Double(GraphConstants.PERMILLE / 2,
					10) };

			GraphConstants.setExtraLabelPositions(e.getAttributes(), point);

			String t = "("
					+ (nodesList.get((int) edgeList.get(i)[0]) + " to " + (nodesList
							.get((int) edgeList.get(i)[1]))) + ")\n" + "\t["
					+ edgeList.get(i)[2] + "]";
			Object[] array = { t };
			// array[0]=temp.getEdgeGain();
			GraphConstants.setExtraLabels(e.getAttributes(), array);

		}
		drawingPanel.removeAll();
		drawingPanel.revalidate();
		drawingPanel.repaint();
		model = new DefaultGraphModel();
		view = new GraphLayoutCache(model, new DefaultCellViewFactory());
		graph = new JGraph(model, view);
		graph.getGraphLayoutCache().insert(cells);
		drawGraph = new JScrollPane(graph);
		drawGraph.setPreferredSize(DRAWING_PANE);
		// pack();
		drawGraph.setBorder(BorderFactory.createEmptyBorder());
		drawingPanel.add(drawGraph);
		drawingPanel.setVisible(true);

	}

	private boolean isNumeric(String str) {
		try {
			double d = Double.parseDouble(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	private boolean save(String fileName) {
		  DocumentBuilderFactory docFactory = DocumentBuilderFactory
		    .newInstance();
		  DocumentBuilder docBuilder;
		  try {
		   docBuilder = docFactory.newDocumentBuilder();

		   // root elements
		   Document doc = (Document) docBuilder.newDocument();
		   Element rootElement = doc.createElement("SFG");
		   doc.appendChild(rootElement);

		   for (String name : nodesList) {
		    Element node = doc.createElement("node");

		    node.setTextContent(name);
		    rootElement.appendChild(node);
		   }

		   for (double[] edge : edgeList) {
		    Element edgeElement = doc.createElement("edge");

		    edgeElement.setTextContent(nodesList.get((int) edge[0]) + " "
		      + nodesList.get((int) edge[1]) + " " + edge[2]);
		    rootElement.appendChild(edgeElement);
		   }

		   TransformerFactory transformerFactory = TransformerFactory
		     .newInstance();
		   Transformer transformer = transformerFactory.newTransformer();
		   DOMSource source = new DOMSource(doc);
		   StreamResult result = new StreamResult(new File(fileName));

		   // Output to console for testing
		   // StreamResult result = new StreamResult(System.out);

		   transformer.transform(source, result);
		   return true;

		  } catch (ParserConfigurationException e) {
		   JOptionPane.showMessageDialog(null, "Error: Graph Not Saved");
		   return false;
		  } catch (TransformerConfigurationException e) {
		   JOptionPane.showMessageDialog(null, "Error: Graph Not Saved");
		   return false;
		  } catch (Exception e) {
		   JOptionPane.showMessageDialog(null, "Error: Graph Not Saved");
		   return false;
		  }
		 }

		 private void load(String path) {

		  try {

		   nodes = new Hashtable<>();
		   nodesList = new ArrayList<>();
		   edgeList = new ArrayList<>();
		   File fXmlFile = new File(path);
		   DocumentBuilderFactory dbFactory = DocumentBuilderFactory
		     .newInstance();
		   DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		   Document doc = dBuilder.parse(fXmlFile);
		   NodeList nodesTag = doc.getElementsByTagName("node");
		   System.out.println(nodesTag.getLength());
		   for (int i = 0; i < nodesTag.getLength(); i++) {

		    Node node = nodesTag.item(i);
		    Element e = (Element) node;

		    String nString = e.getTextContent().trim();

		    System.out.println("Node");
		    nodes.put(nString, i);
		    nodesList.add(nString);
		   }

		   nodesTag = doc.getElementsByTagName("edge");
		   System.out.println(nodesTag.getLength());
		   for (int i = 0; i < nodesTag.getLength(); i++) {

		    Node node = nodesTag.item(i);
		    Element e = (Element) node;

		    String nString = e.getTextContent().trim();
		    System.out.println("Edge");
		    String[] arr = nString.split(" ");
		    edgeList.add(new double[] { (double)nodes.get(arr[0]),
		      (double)nodes.get(arr[1]), Double.parseDouble(arr[2]) });

		   }
		   updateDrawing();
		   System.out.println("Finished");

		  } catch (Exception e) {
		   // TODO: handle exception
		   JOptionPane.showMessageDialog(null, "No Such File Exists");
		  }
		 }

}
