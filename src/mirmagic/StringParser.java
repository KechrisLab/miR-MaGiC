/**
 * 
 */
package mirmagic;

/**
 * @author prussell
 * Parse a string around whitespace or another specified delimiter
 * Get full array of parsed strings or individual values by index, parsed to various types
 */
public class StringParser {

	private static String whitespaceDelimiter = "\\s++";
	private String[] tokens;
	
	/**
	 * 
	 */
	public StringParser() {}
	
	/**
	 * Get the first field of a whitespace-delimited string
	 * @param s The string
	 * @return The first field
	 */
	public static String firstField(String s) {
		StringParser sp = new StringParser();
		sp.parse(s);
		return sp.asString(0);
	}
	
	/**
	 * Parses the string around whitespace and stores tokens
	 * @param s The string to parse
	 */
	public void parse(String s) {
		if(s == null) return;
		if(s.equals("")) return;
		tokens = s.split(whitespaceDelimiter);
	}
	
	/**
	 * 
	 */
	public void clear() {
		tokens = null;
	}
	
	/**
	 * Parses the string around whitespace and stores tokens
	 * @param s The string to parse
	 * @return Tokens
	 */
	public static String[] getTokens(String s) {
		return s.split(whitespaceDelimiter);
	}

	/**
	 * Get a new tab-delimited string with the first fields removed from this string
	 * @param numToRemove Number of fields to remove from beginning of string
	 * @return String with initial fields removed
	 */
	public String removeFirstTokens(int numToRemove) {
		return removeFirstTokens(numToRemove, "\t");
	}
	
	/**
	 * Get a new string with the first fields removed from this string
	 * @param numToRemove Number of fields to remove from beginning of string
	 * @param delim Delimiter to use in returned string
	 * @return String with initial fields removed
	 */
	public String removeFirstTokens(int numToRemove, String delim) {
		if(numToRemove > tokens.length - 1) {
			throw new IllegalArgumentException("Can't remove " + numToRemove + " fields from string with " + tokens.length + ".");
		}
		String rtrn = tokens[numToRemove];
		for(int i = numToRemove + 1; i < tokens.length; i++) {
			rtrn += delim + tokens[i];
		}
		return rtrn;
	}
	
	/**
	 * Parses the string around the specified delimiter and stores tokens
	 * @param s The string to parse
	 * @param regexp Regular expression for delimiter (for period use "\\.")
	 */
	public void parse(String s, String regexp) {
		if(s == null) return;
		if(s.equals("")) return;
		tokens = s.split(regexp);
	}
	
	/**
	 * Get position of the string in array
	 * @param string The string
	 * @return The first position equal to the string
	 */
	public int getIndexFor(String string) {
		for(int i=0; i < tokens.length; i++) {
			if(tokens[i].equals(string)) return i;
		}
		throw new IllegalArgumentException("String " + string + " not found.");
	}
	
	/**
	 * Get number of fields
	 * @return number of fields
	 */
	public int getFieldCount() {
		if(tokens == null) return 0;
		return tokens.length;
	}
	
	/**
	 * Gets the token at specified position
	 * @param index The position
	 * @return the desired token as a String
	 */
	public String asString(int index) {
		return tokens[index];
	}
	
	/**
	 * Gets the token at specified position and parses to an int
	 * @param index The position
	 * @return the desired token as an int
	 */
	public int asInt(int index) {
		try {
			return Integer.parseInt(tokens[index]);
		} catch (NumberFormatException e) {
			throw new NumberFormatException("Field " + index + " cannot be parsed to int: " + tokens[index]);
		}
	}
	
	/**
	 * Gets the token at specified position and parses to a long
	 * @param index The position
	 * @return the desired token as an int
	 */
	public long asLong(int index) {
		try {
			return Long.parseLong(tokens[index]);
		} catch (NumberFormatException e) {
			throw new NumberFormatException("Field " + index + " cannot be parsed to int: " + tokens[index]);
		}
	}
	
	/**
	 * Gets the token at specified position and parses to a boolean
	 * @param index The position
	 * @return the desired token as a boolean
	 */
	public boolean asBoolean(int index) {
		String token = tokens[index];
		if(token.equalsIgnoreCase("true")) return true;
		if(token.equalsIgnoreCase("t")) return true;
		if(token.equalsIgnoreCase("false")) return false;
		if(token.equalsIgnoreCase("f")) return false;
		throw new IllegalArgumentException("Invalid boolean: " + token);
	}
	
	/**
	 * Gets the token at specified position and parses to a double
	 * @param index The position
	 * @return the desired token as a double
	 */
	public double asDouble(int index) {
		try {
			return Double.parseDouble(tokens[index]);
		} catch (NumberFormatException e) {
			if(tokens[index].equals("inf")) return Double.POSITIVE_INFINITY;
			if(tokens[index].equals("-inf")) return Double.NEGATIVE_INFINITY;
			if(tokens[index].equals("nan")) return Double.NaN;
			if(tokens[index].equals("-nan")) return Double.NaN;
			throw new NumberFormatException("Field " + index + " cannot be parsed to double: " + tokens[index]);
		}
	}
	
	/**
	 * Gets the token at specified position and parses to a float
	 * @param index The position
	 * @return the desired token as a float
	 */
	public float asFloat(int index) {
		try {
			return Float.parseFloat(tokens[index]);
		} catch (NumberFormatException e) {
			throw new NumberFormatException("Field " + index + " cannot be parsed to float: " + tokens[index]);
		}
	}
	
	/**
	 * Gets the array of tokens
	 * @return all the parsed tokens as a String[]
	 */
	public String[] getStringArray() {
		return tokens;
	}
	
	
}
