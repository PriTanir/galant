package edu.ncsu.csc.Galant.algorithm.code.macro;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 
 * The macros used in preprocessing. 
 */
public class Macros
	{
		public static void macros()
			{	
				
				Macro.MACROS.add(new SimpleReplacementMacro("\\bbool\\b", "boolean"){
					@Override
					public String getName() {
						return "bool";
					}
				}); 


				Macro.MACROS.add(new FetchingMacro("numOfNodes", "int"){
					@Override
					protected String includeInAlgorithm() {
						return "= graph.getNodes().size();";
					}
				});

				Macro.MACROS.add(new FetchingMacro("numOfEdges", "int"){
					@Override
					protected String includeInAlgorithm() {
						return "= graph.getEdges().size();";
					}
				});

				Macro.MACROS.add(new FetchingMacro("nodesList", "Node[]"){
					@Override
					protected String includeInAlgorithm() {
						return " = new Node[ graph.getNodes().size() ];";
					}
				});

				Macro.MACROS.add(new FetchingMacro("edgesList", "Edge[]"){
					@Override
					protected String includeInAlgorithm() {
						return " = new Edge[ graph.getEdges().size() ];";
					}
				});
				
				/*
				 * for_outgoing: iterates over the outgoing nodes and edges
				 * of a given node, based solely on source/target information
				 * and not on whether the graph is directed.
				 * 
				 * Usage: for_outgoing(<node>, <edge>, <adjacentNode>) {<code_block>}
				 * 
				 * Parameters:
				 * 
				 * node: the name of a variable of type Node. This is the starting node.
				 * 
				 * adjacentNode: a variable name. Within the code block, this can be used to refer
				 * to the current adjacent node as a Node object.
				 * 
				 * edge: a variable name. Within the code block, this can be used to refer to the
				 * current incident edge as an Edge object. <edge> connects <node> and <adjacentNode>.
				 * 
				 * code_block: a block of code that is executed for each adjacent node / incident
				 * edge of <node>. The curly braces are required.
				 */
				Macro.MACROS.add(new ParameterizedMacro("for_outgoing", 3, true){
					@Override
					protected String modifyMatch(String code, MatchResult nameMatch, String[] args, String whitespace,
						String block)
						{
							String node = args[0], edge = args[1], adjacentNode = args[2];
							return Matcher.quoteReplacement("for(Edge " + edge + " : " + node + ".getOutgoingEdges())" + whitespace +
								"{ Node " + adjacentNode + " = " + node + ".travel(" + edge + ");" + block + "}");
						}
				});

				/*
				 * for_incoming: iterates over the incoming nodes and edges
				 * of a given node (all edges in case of an undirected
				 * graph), based solely on source/target information and not
				 * on whether the graph is directed.
				 * 
				 * Usage: for_incoming(<node>, <edge>, <adjacentNode>) {<code_block>}
				 * 
				 * Parameters:
				 * 
				 * node: the name of a variable of type Node. This is the starting node.
				 * 
				 * adjacentNode: a variable name. Within the code block, this can be used to refer
				 * to the current adjacent node as a Node object.
				 * 
				 * edge: a variable name. Within the code block, this can be used to refer to the
				 * current incident edge as an Edge object. <edge> connects <node> and <adjacentNode>.
				 * 
				 * code_block: a block of code that is executed for each adjacent node / incident
				 * edge of <node>. The curly braces are required.
				 */
				Macro.MACROS.add(new ParameterizedMacro("for_incoming", 3, true){
					@Override
					protected String modifyMatch(String code, MatchResult nameMatch, String[] args, String whitespace,
						String block)
						{
							String node = args[0], edge = args[1], adjacentNode = args[2];
							return Matcher.quoteReplacement("for(Edge " + edge + " : " + node + ".getIncomingEdges())" + whitespace +
								"{ Node " + adjacentNode + " = " + node + ".travel(" + edge + ");" + block + "}");
						}
				});

				/*
				 * for_adjacent: iterates over all incident edges and
				 * adjacent nodes of a given node, but only the outgoing
				 * edges for a directed graph
				 * 
				 * Usage: for_adjacent(<node>, <edge>, <adjacentNode>) {<code_block>}
				 * 
				 * Parameters:
				 * 
				 * node: the name of a variable of type Node. This is the starting node.
				 * 
				 * adjacentNode: a variable name. Within the code block, this can be used to refer
				 * to the current adjacent node as a Node object.
				 * 
				 * edge: a variable name. Within the code block, this can be used to refer to the
				 * current incident edge as an Edge object. <edge> connects <node> and <adjacentNode>.
				 * 
				 * code_block: a block of code that is executed for each adjacent node / incident
				 * edge of <node>. The curly braces are required.
				 */
				Macro.MACROS.add(new ParameterizedMacro("for_adjacent", 3, true){
					@Override
					protected String modifyMatch(String code, MatchResult nameMatch, String[] args, String whitespace,
						String block)
						{
							String node = args[0], edge = args[1], adjacentNode = args[2];
							return Matcher.quoteReplacement("for(Edge " + edge + " : " + node + ".getIncidentEdges())" + whitespace +
								"{ Node " + adjacentNode + " = " + node + ".travel(" + edge + ");" + block + "}");
						}
				});

				/*
				 * for_nodes: iterates over all nodes in the graph.
				 * 
				 * Usage: for_nodes(<node>) <code_block>
				 * 
				 * Parameters:
				 * 
				 * node: a variable name. Within the code block, this can be used to refer
				 * to the current node as a Node object.
				 * 
				 * code_block: a block of code that is executed for each node in the graph.
				 */
				Macro.MACROS.add(new ParameterizedMacro("for_nodes", 1){
					@Override
					protected String modifyMatch(String code, MatchResult nameMatch, String[] args, String whitespace,
						String block)
						{
							return Matcher.quoteReplacement("for(Node " + args[0] + " : getNodes())");
						}
				});
				/*
				 * for_edges: iterates over all edges in the graph.
				 * 
				 * Usage: for_edges(<edge>) <code_block>
				 * 
				 * Parameters:
				 * 
				 * edge: a variable name. Within the code block, this can be used to refer
				 * to the current edge as an Edge object.
				 * 
				 * code_block: a block of code that is executed for each edge in the graph.
				 */
				Macro.MACROS.add(new ParameterizedMacro("for_edges", 1){
					@Override
					protected String modifyMatch(String code, MatchResult nameMatch, String[] args, String whitespace,
						String block)
						{
							return Matcher.quoteReplacement("for(Edge " + args[0] + " : getEdges())");
						}
				});
				
				/**
				 * This macro has been deprecated; can be re-instated if desired but support not required or necessary
				 */
				/*
				 * initializationComplete(); : marks the end of the initialization portion of the algorithm
				 * Usage:
				 * <initialization code>
				 * initializationComplete(); 
				 * <code block>
				 * 
				 * Parameters: none
				 */
                //				Macro.MACROS.add(new SimpleReplacementMacro("initializationComplete\\(\\);","ae.setInitializationComplete();"));
				

				Macro.MACROS.add(new ParameterizedMacro(MacroUtil.replaceWhitespace("function (\\S+)?  (\\S+)"), true){
							
						@Override
						public String getName() {
								return "function";
							}

						private String getObjectType(String type)
							{	
								return " " + type;
							}

						abstract class Param
							{
								public String name, declaredType, type;
								public String paramToString = "";
								public Param(int index, String[] paramStrings)
									{	
										if(paramStrings != null)
											{	

												StringBuilder stringBuilder = new StringBuilder();
												for(int i = 0; i < paramStrings.length; i++) {
													stringBuilder.append(paramStrings[i]);
													if( i < paramStrings.length - 1 ){
														stringBuilder.append(",");
													}
												}
												
												paramToString = stringBuilder.toString();

												String paramString = paramStrings[index];
												Matcher matcher = Pattern.compile("\\s+").matcher(paramString);
												StringBuilder bracketBuilder = new StringBuilder();
												int start = 0;
												while(matcher.find())
													{
														bracketBuilder.append(paramString.substring(start, matcher.end()));
														start = matcher.end();
													}
												name = paramString.substring(start);
												// trim just trailing whitespace
												declaredType = ("a" + bracketBuilder).trim().substring(1);
											}	
									}

								public String toString() 
								{	
										return paramToString;
								}	
							}

						class PairParam extends Param
							{
								/** Creates a new <code>PairParam</code>. */
								public PairParam(int index, String[] paramStrings)
									{
										super(index, paramStrings);
									}

							}
						class SingleParam extends Param
							{
								/** Creates a new <code>SingleParam</code>. */
								public SingleParam(String[] paramStrings)
									{
										super(0, paramStrings);
									}
							}
						class NoParam extends Param
							{
								/** Creates a new <code>NoParam</code>. */
								public NoParam()
									{	
										super(-1, null);
									}
							}

						@Override
						protected String modifyMatch(String code, MatchResult nameMatch, String[] args, String whitespace,
							String block)
							{
								// name
								String name = nameMatch.group(2);

								// get P from params
								Param mainParam =
									args.length == 0 ? new NoParam() : args.length == 1 ? new SingleParam(args) : new PairParam(
										0, args);

								// get R from return type
								String declaredReturnType = nameMatch.group(1);
								String returnType = declaredReturnType == null ? " void" : getObjectType(declaredReturnType);

								return Matcher.quoteReplacement("public" + returnType + " " + name + " ( " + mainParam.toString() + ")" + "{" 
									+  block + "}" ); 
							}
					});				

				/*
				 * algorithm: to indicate the start of an algorithm
				 * 
				 * Usage: algorithm{<code_block>}
				 * 
				 * code_block: a block of code that is executed for the algorithm
				 */
				 // Macro.MACROS.add(new AlgorithmMacro());	
			}
	}

//  [Last modified: 2015 07 03 at 14:46:18 GMT]
