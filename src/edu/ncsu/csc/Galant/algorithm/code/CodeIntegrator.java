package edu.ncsu.csc.Galant.algorithm.code;

import java.util.regex.Matcher;
import edu.ncsu.csc.Galant.GraphDispatch;
import java.util.regex.Pattern;
import java.util.Scanner;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import edu.ncsu.csc.Galant.GalantPreferences;
import edu.ncsu.csc.Galant.algorithm.Algorithm;
import edu.ncsu.csc.Galant.algorithm.code.macro.Macro;
import edu.ncsu.csc.Galant.algorithm.code.macro.MalformedMacroException;
import edu.ncsu.csc.Galant.logging.LogHelper;
import edu.ncsu.csc.Galant.GalantException;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @todo Exception handling needs work, but the bigger issue is creation of a
 * separate method that runs the algorithm along with an
 * "algorithm { <body> }"
 * syntax.
 * This would mitigate the weirdness of function declarations and global
 * variables.
 *
 * <p>
 * The "pseudo-compiler" &mdash; a rather misleading name, which is why I've called it something
 * that I think is more accurate.
 * </p>
 * <p>
 * The purpose of this class is to integrate the user's algorithm code into the running application.
 * It does so with the following steps:
 * <ol>
 * <li>Replacing any macros with the equivalent Java code.</li>
 * <li>Inserting the code into a basic class structure.</li>
 * <li>Compiling the completed Java class into a .class file, which is stored in the folder defined
 * by {@link GalantPreferences#OUTPUT_DIRECTORY}.</li>
 * <li>Loading the .class file into the program so that its <code>runAlgorithm</code> method can be
 * called.</li>
 * <li>Creating an <code>Algorithm</code> from the loaded class for easy reference.
 * </ol>
 * </p>
 */
public class CodeIntegrator
	{
		public static final String METHOD_NAME = "runAlgorithm";

		public static final String PACKAGE = "edu.ncsu.csc.Galant.algorithm.code.compiled";

        /**
         * @todo These fields are initialized to meaningless values that will
         * be replaced by others later. Should be a better way.
         */
		private static final String IMPORTS_FIELD = "{Imports}";
        private static final String NAME_FIELD = "{Algorithm Name}";
        private static final String CODE_FIELD = "{User Code}";
        private static final String ALGORITHM_HEAD = "{Algorithm Head}";
        private static final String ALGORITHM_TAIL = "{Algorithm Tail}";
        private static final String ALGORITHM_BODY = "{Algorithm Body}";

        /**
         * Here is the real code that appears before and after the algorithm.
         */
        private static final String REAL_ALGORITHM_HEAD = "initialize(); AlgorithmExecutor ae = GraphDispatch.getInstance().getAlgorithmExecutor(); GraphState gs = this.getGraph().getGraphState(); ae.setAlgorithmComplete(false); synchronized(ae){ try{ae.wait(); } catch (InterruptedException e){e.printStackTrace(System.out); } }";
        private static final String REAL_ALGORITHM_TAIL = "if(gs.isLocked()) endStep(); ae.setAlgorithmComplete(true);";

		// The basic class structure into which the user's code can be inserted so it can be
		// compiled.
		//@formatter:off
		private static final String CLASS_STRUCTURE =
			"package " + PACKAGE + ";" +
			"import java.util.*;" +
			"import edu.ncsu.csc.Galant.algorithm.Algorithm;" +
			"import edu.ncsu.csc.Galant.graph.component.GraphState;" +
			"import edu.ncsu.csc.Galant.graph.component.Graph;" +
			"import edu.ncsu.csc.Galant.graph.component.Node;" +
			"import edu.ncsu.csc.Galant.graph.component.Edge;" +
			"import edu.ncsu.csc.Galant.algorithm.code.macro.Function;" +
			"import edu.ncsu.csc.Galant.algorithm.code.macro.Pair;" +
            "import edu.ncsu.csc.Galant.GalantException;" +
            "import edu.ncsu.csc.Galant.GraphDispatch;" +
            "import edu.ncsu.csc.Galant.algorithm.AlgorithmExecutor" +
			IMPORTS_FIELD +
			"public class " + NAME_FIELD + " extends Algorithm" + "{" + CODE_FIELD + "}";

		public static final String ALGORITHM_STRUCTURE = "public void run(){ try {" +
						    ALGORITHM_HEAD + ALGORITHM_BODY + ALGORITHM_TAIL + "}" + "catch (Exception e)"
                                      + " { \n if ( e instanceof GalantException )"
                                      + " { GalantException ge = (GalantException) e;"
                                      + " ge.report(\"\"); ge.display(); }"
                                      + " \n else {e.printStackTrace(System.out);}}}" ;
		//@formatter:on

		/**
		 * Converts the unmodified user algorithm code into a proper Java class, as would be found
		 * in a .java file.
		 */
		// protected so it can be accessed by tests
		protected static String toJavaClass(String algorithmName, String userCode) throws MalformedMacroException
			{
				// separate imports from main code
				// find lines starting with "import"
				Matcher matcher = Pattern.compile("import.*;").matcher(userCode);
				int splitAt = 0;
				while(matcher.find())
					splitAt = matcher.end();
				// splitAt should be 1st char in 1st line not starting with "import"
				String imports = userCode.substring(0, splitAt);
				userCode = userCode.substring(splitAt);

				/* Try remove all the comment line*/
				try {
					System.out.println("->" + removeAllComment(userCode) + "<-");
					userCode = removeAllComment(userCode);
				} catch (IOException e) {
					throw new MalformedMacroException();
				}

				// Rebuild the algorithm and add head or tail as needed

				StringBuilder sb = new StringBuilder( userCode.substring(0, userCode.indexOf("algorithm")));
				sb.append(modifyAlgorithm( REAL_ALGORITHM_HEAD,
                                           REAL_ALGORITHM_TAIL,
                                           userCode ) );
				userCode = sb.toString();

				// apply macros
				for(Macro macro : Macro.MACROS) {
					userCode = macro.applyTo(userCode); }
				// apply generated macros, removing each one so if the code is recompiled,
				// you don't end up with incorrect/duplicate macros
				while(!Macro.GENERATED_MACROS.isEmpty())
					userCode = Macro.GENERATED_MACROS.remove(0).applyTo(userCode);

				// insert into class structure
				return CLASS_STRUCTURE.replace(NAME_FIELD, algorithmName).replace(CODE_FIELD,
					userCode).replace(IMPORTS_FIELD, imports);
			}

		/**
		 * Integrates the given code into the program as a class with the given name.
		 * @param algorithmName the name of the algorithm to be integrated.
		 * @param userCode the code of the algorithm to be integrated.
		 * @return an <code>Algorithm</code> object representing the algorithm.
		 * @throws CompilationException if compiler errors occur.
		 * @throws MalformedMacroException if there are errors in macro usage.
		 */
		public static Algorithm integrateCode(String algorithmName, String userCode)
			throws CompilationException, MalformedMacroException, GalantException
			{
				// Make sure the name is a valid Java identifier
				StringBuilder nameBuilder = new StringBuilder(algorithmName.length());
				for(int i = 0; i < algorithmName.codePointCount(0, algorithmName.length()); i++)
					{
						int c = algorithmName.codePointAt(i);
						if(i == 0 ? Character.isJavaIdentifierStart(c) : Character
							.isJavaIdentifierPart(c))
							nameBuilder.appendCodePoint(c);
						else
							nameBuilder.appendCodePoint('_');
					}
				String className = nameBuilder.toString();
				String qualifiedName = PACKAGE + "." + className;

				// Replace macros and insert into class structure
				String sourceCode = toJavaClass(className, userCode);
				
                // Display source code after macro processing
                LogHelper.setEnabled( true );
				LogHelper.logDebug(sourceCode);
                LogHelper.restoreState();

				// Compile
				DiagnosticCollector<JavaFileObject> diagnostics =
					CompilerAndLoader.compile(qualifiedName, sourceCode);
				if(diagnostics != null)
					throw new CompilationException(diagnostics);
					
				// Load
				return CompilerAndLoader.loadAlgorithm(qualifiedName);
			}

		/**
		 * Modify Algorithm as needed 
		 * @param head code block to be appended before the substantial algorithm part
		 * @param tail code block to be appended after the substantial algorithm part
		 * @param userCode user code containing algorithm{}
		 */
		public static String modifyAlgorithm(String head, String tail, String userCode) throws MalformedMacroException {
			String modifiedBody ;

			try{
				modifiedBody = getCodeBlock(userCode.substring(userCode.indexOf("algorithm"), userCode.length()));
			} catch (IOException e) {
				throw new MalformedMacroException() ;
			}

			try {
				return ALGORITHM_STRUCTURE.replace(ALGORITHM_HEAD, head).replace(ALGORITHM_TAIL, tail).replace(ALGORITHM_BODY, modifiedBody);				
			} catch (NullPointerException e) {
				return ALGORITHM_STRUCTURE.replace(ALGORITHM_HEAD, "").replace(ALGORITHM_TAIL, "").replace(ALGORITHM_BODY, modifiedBody);
			}
		}

		/**
		 * Read and return code in between { and } in the given code
		 * @param code code to be proceessed 
		 */
		private static String getCodeBlock(String code) throws IOException {
			/* Convert code string to a BufferReader */
			// http://www.coderanch.com/t/519147/java/java/ignore-remove-comments-java-file
			InputStream codeInput = new ByteArrayInputStream(code.getBytes());
			BufferedReader reader = new BufferedReader(new InputStreamReader(codeInput));
			
			StringBuilder sb = new StringBuilder();
			
			/* Increment counter when meet a {, decrement it when meet a } 
			 * Stop when counter hit 0;
			 */
			int counter = 0;
			
			/* If false, means that the first { has not been read yet. This circumstance will not stop the 
			 * reader even though counter is 0;
			 */
			boolean firstEncounter = false;
			boolean startScan = false;
			
			int char1 = reader.read();
			if (char1 != -1) {
				do {
					if(char1 == '{') {
						counter ++;
						firstEncounter = true;
					} else if(char1 == '}'){
						counter --;
					}

					if(firstEncounter == true) {
						sb.append((char)char1);
						if(counter == 0) {
							break;
						}
					}

					char1 = reader.read();
					
					} while (char1 != -1);
				// use substring to ignore the first pair of { and }. They are mean for marking the start and end of algorithm {}	
			}	return sb.toString().substring(1, sb.length() - 1);
		}

		/** 
		 * Remove all the comment lines in a easy way. 
		 * Has been tested by test_comment.alg
		 * @param code code to be proceessed 
		 */
		private static String removeAllComment(String code) throws IOException {
			// http://www.coderanch.com/t/519147/java/java/ignore-remove-comments-java-file
			InputStream codeInput = new ByteArrayInputStream(code.getBytes());
			BufferedReader reader = new BufferedReader(new InputStreamReader(codeInput));
			
			StringBuilder sb = new StringBuilder();

			boolean inBlockComment = false;
   			boolean inSlashSlashComment = false;

   			int char1 = reader.read();
   			if (char1 != -1) {
   				int char2;
   				while (char1 != -1) {
		           	if ((char2 = reader.read()) == -1) {
		                sb.append((char)char1);
		                break;
		            } 
		            if (char1 == '/' && char2 == '*') {
                		inBlockComment = true;
                		char1 = reader.read();
                		continue;
            		} else if (char1 == '*' && char2 == '/') {
                		inBlockComment = false;
                		char1 = reader.read();
                		continue;
            		} else if (char1 == '/' && char2 == '/' && !inBlockComment) {
                		inSlashSlashComment = true;
                		char1 = reader.read();
                		continue;
            		}
            		if (inBlockComment) {
                		char1 = char2;
                		continue;
            		}
            		if (inSlashSlashComment) {
                		if (char2 == '\n') {
                    		inSlashSlashComment = false;
                    		sb.append((char)char2);
                    		char1 = reader.read();
                    		continue;
	                	} else if (char1 == '\n') {
	                    	inSlashSlashComment = false;
	                    	// This is where bug happens. I mistakely append char2 to the string.
	                    	// In this case, Return was captured in char1 so char 2 will the first character after Return 
	                    	// So there is a extra 'N' from Node[] parent in log when running kruskal.alg;
	                    	
	                    	// It should be char1 here. 
	                    	sb.append((char)char1);
	                    	char1 = char2;
	                    	continue;
	                	} else {
	                		/* ignore everything else than return */
	                    	char1 = reader.read();
	                    	continue;
	                	}
            		}
            		sb.append((char)char1);
            		char1 = char2;
				}
			}
			return sb.toString();
		}				
	}

//  [Last modified: 2015 07 11 at 14:44:58 GMT]
