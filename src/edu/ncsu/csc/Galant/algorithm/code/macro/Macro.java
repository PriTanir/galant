package edu.ncsu.csc.Galant.algorithm.code.macro;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * Represents a macro that replaces some section of algorithm code with proper Java code.
 * </p>
 * <p>
 * For now, macros should not change the number of lines in the code, to keep the line numbers in error diagnostics correct.
 * </p>
 * <p>
 * Each macro has a <code>Pattern</code> that is used to find relevant sections of the user's code.
 * {@link Matcher#appendReplacement(StringBuffer, String)} and {@link Matcher#appendTail(StringBuffer)} are used to construct a
 * modified version of the code with a <code>StringBuffer</code>.
 * </p>
 */
public abstract class Macro
	{
		/** same-line whitespace */
		public static final String WHITESPACE = "[ \\t]";

		/**
		 * A list of all the macros that can be used in algorithm code. Note: if you want to add a macro dynamically (e.g.,
		 * from within another macro), use {@link #GENERATED_MACROS}.
		 */
		public static final List<Macro> MACROS = new ArrayList<Macro>();
		
		/** A list of macros generated by other macros. */
		// use linked list b/c each macro will be removed from the front
		public static final List<Macro> GENERATED_MACROS = new LinkedList<Macro>();

		static
			{
				Macros.macros(); // populate macro list with macros
			}

		private Pattern pattern;

		/** Creates a macro that looks for the given pattern. */
		public Macro(Pattern pattern)
			{
				this.pattern = pattern;
			}
		/** Creates a macro that looks for the pattern represented by the given regex. */
		public Macro(String regex)
			{
				this(Pattern.compile(regex));
			}

		public Pattern getPattern()
			{
				return pattern;
			}

		protected abstract boolean getIncludedInAlgorithm();

		/**
		 * <p>
		 * Subclasses should implement this method and return a modified version of the current match. The returned string will
		 * be used as a replacement string for {@link Matcher#appendReplacement(StringBuffer, String)}, with the syntax
		 * described in the javadoc for that method. So backslashes and dollar signs may need to be escaped.
		 * </p>
		 * <p>
		 * If the match is not actually correct, <code>null</code> can be returned. (This can be used with the default
		 * <code>applyTo</code> without creating an infinite loop.)
		 * </p>
		 * <p>
		 * Note: Macros may add new macros to the end of {@linkplain #MACROS the macro list} (e.g., by creating new macros),
		 * but other modifications to the list may not be supported.
		 * </p>
		 * @param code the user code on which this macro is being applied.
		 * @param match a match of this macro's pattern.
		 * @return the replacement of the matched region, or <code>null</code> if the match is not actually valid.
		 * @throws MalformedMacroException if the match is not a valid invocation of this macro, but it should have been.
		 */
		protected abstract String modify(String code, MatchResult match) throws MalformedMacroException;

		/**
		 * Applies this macro to the given code. For each match found, replaces the match with the result of
		 * {@link #modify(String, MatchResult)} (if it's not <code>null</code>), then searches for the next match starting at
		 * the index immediately after the first character of the current match in the modified code. So if the result of
		 * <code>modify</code> produces a match, that match will be included.
		 * @param code the code to apply this macro to.
		 * @return the version of the code after this macro has been applied.
		 */
		public String applyTo(String code) throws MalformedMacroException
			{
				Matcher matcher;
				int start = 0;

				if(getIncludedInAlgorithm()) {
					
					matcher = getPattern().matcher(code).region(start, code.length()); // find();
					
					matcher.find();

					matcher = Pattern.compile("^(.*?)\\;").matcher(modified);
					matcher.find();
					String replaceWith = matcher.group(0);

					// Re-read until we hit
					StringBuffer newCode = new StringBuffer();
					matcher = getPattern().matcher(code).region(start, code.length());
					String modified = modify(code, matcher);
							if(modified != null) {
								matcher.appendTail(replaceWith);
					}

				} else {
					while((matcher = getPattern().matcher(code).region(start, code.length())).find())
						{	
							StringBuffer newCode = new StringBuffer();
							String modified = modify(code, matcher);
							if(modified != null) {
								matcher.appendReplacement(newCode, modified);
							}
							matcher.appendTail(newCode);
							code = newCode.toString();
							if(matcher.start() < code.length() - 1)
								start = matcher.start() + 1;
						}
				}
				return code;
			}	
	}
