/**
 * 
 */
package mirmagic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeSet;


/**
 * @author prussell
 * Full-featured command line parser
 * Instances are mutable
 */
public final class CommandLineParser {

	
	private boolean isParsed;
	private ArrayList<String> programDescription;

	private Map<String,String> stringArgDescriptions;
	private Map<String,String> stringListArgDescriptions;
	private Map<String,String> intArgDescriptions;
	private Map<String,String> longArgDescriptions;
	private Map<String,String> floatArgDescriptions;
	private Map<String,String> doubleArgDescriptions;
	private Map<String,String> boolArgDescriptions;	
	
	private Map<String,String> stringArgDefaults;
	private Map<String,ArrayList<String>> stringListArgDefaults;
	private Map<String,Integer> intArgDefaults;
	private Map<String,Long> longArgDefaults;
	private Map<String,Float> floatArgDefaults;
	private Map<String,Double> doubleArgDefaults;
	private Map<String,Boolean> boolArgDefaults;	
	
	private HashSet<String> requiredArgs;
	private Map<String,String> commandLineValues;
	private Map<String,ArrayList<String>> duplicateCommandLineValues;


	/**
	 * 
	 */
	public CommandLineParser() {
		isParsed = false;
		
		stringArgDescriptions = new HashMap<String,String>();
		stringListArgDescriptions = new HashMap<String,String>();
		intArgDescriptions = new HashMap<String,String>();
		longArgDescriptions = new HashMap<String,String>();
		floatArgDescriptions = new HashMap<String,String>();
		doubleArgDescriptions = new HashMap<String,String>();
		boolArgDescriptions = new HashMap<String,String>();	
		
		stringArgDefaults = new HashMap<String,String>();
		stringListArgDefaults = new HashMap<String,ArrayList<String>>();
		intArgDefaults = new HashMap<String,Integer>();
		longArgDefaults = new HashMap<String,Long>();
		floatArgDefaults = new HashMap<String,Float>();
		doubleArgDefaults = new HashMap<String,Double>();
		boolArgDefaults = new HashMap<String,Boolean>();	
		
		programDescription = new ArrayList<String>();
		requiredArgs = new HashSet<String>();
		commandLineValues = new HashMap<String,String>();
		duplicateCommandLineValues = new HashMap<String,ArrayList<String>>();
		
	}
	

	/**
	 * Sets program description to be printed as part of help menu
	 * @param description The program description
	 */
	public void setProgramDescription(String description) {
		programDescription.add(description);
	}
	
	/**
	 * Adds new string argument to set of arguments
	 * Client will crash if the same argument flag or description has already been added
	 * @param flag the command line flag for the argument
	 * @param description the description of the argument
	 * @param required whether parameter is required
	 */
	public void addStringArg(String flag, String description, boolean required) {
		enforceUniqueFlag(flag);
		enforceUniqueDescription(description);
		stringArgDescriptions.put(flag, description);
		commandLineValues.put(flag, null);
		if(required) requiredArgs.add(flag);
	}
	
	/**
	 * Adds new string argument to set of arguments and stores default
	 * Client will crash if the same argument flag or description has already been added
	 * @param flag the command line flag for the argument
	 * @param description the description of the argument
	 * @param required whether parameter is required
	 * @param def default value
	 */
	public void addStringArg(String flag, String description, boolean required, String def) {
		enforceUniqueFlag(flag);
		enforceUniqueDescription(description);
		stringArgDescriptions.put(flag, description);
		commandLineValues.put(flag, null);
		if(required) requiredArgs.add(flag);
		stringArgDefaults.put(flag, def);
	}
	
	/**
	 * Adds new string list argument to set of arguments and stores default
	 * Client will crash if the same argument flag or description has already been added
	 * @param flag the command line flag for the argument
	 * @param description the description of the argument
	 * @param required whether parameter is required
	 * @param def default value
	 */
	public void addStringListArg(String flag, String description, boolean required, ArrayList<String> def) {
		enforceUniqueFlag(flag);
		enforceUniqueDescription(description);
		stringListArgDescriptions.put(flag, description);
		commandLineValues.put(flag, null);
		if(required) requiredArgs.add(flag);
		stringListArgDefaults.put(flag, def);
	}
	
	/**
	 * Adds new string list argument to set of arguments and stores default
	 * Client will crash if the same argument flag or description has already been added
	 * @param flag the command line flag for the argument
	 * @param description the description of the argument
	 * @param required whether parameter is required
	 * @param def default value
	 */
	public void addStringListArg(String flag, String description, boolean required) {
		enforceUniqueFlag(flag);
		enforceUniqueDescription(description);
		stringListArgDescriptions.put(flag, description);
		commandLineValues.put(flag, null);
		if(required) requiredArgs.add(flag);
	}

	/**
	 * Adds new int argument to set of arguments
	 * Client will crash if the same argument flag or description has already been added
	 * @param flag the command line flag for the argument
	 * @param description the description of the argument
	 * @param required whether parameter is required
	 */	
	public void addIntArg(String flag, String description, boolean required) {
		enforceUniqueFlag(flag);
		enforceUniqueDescription(description);
		intArgDescriptions.put(flag, description);
		commandLineValues.put(flag, null);
		if(required) requiredArgs.add(flag);
	}

	/**
	 * Adds new int argument to set of arguments and stores default
	 * Client will crash if the same argument flag or description has already been added
	 * @param flag the command line flag for the argument
	 * @param description the description of the argument
	 * @param required whether parameter is required
	 * @param def default value
	 */	
	public void addIntArg(String flag, String description, boolean required, int def) {
		enforceUniqueFlag(flag);
		enforceUniqueDescription(description);
		intArgDescriptions.put(flag, description);
		commandLineValues.put(flag, null);
		if(required) requiredArgs.add(flag);
		intArgDefaults.put(flag, Integer.valueOf(def));
	}

	/**
	 * Adds new long argument to set of arguments
	 * Client will crash if the same argument flag or description has already been added
	 * @param flag the command line flag for the argument
	 * @param description the description of the argument
	 * @param required whether parameter is required
	 */	
	public void addLongArg(String flag, String description, boolean required) {
		enforceUniqueFlag(flag);
		enforceUniqueDescription(description);
		longArgDescriptions.put(flag, description);
		commandLineValues.put(flag, null);
		if(required) requiredArgs.add(flag);
	}

	/**
	 * Adds new long argument to set of arguments and stores default
	 * Client will crash if the same argument flag or description has already been added
	 * @param flag the command line flag for the argument
	 * @param description the description of the argument
	 * @param required whether parameter is required
	 * @param def default value
	 */	
	public void addLongArg(String flag, String description, boolean required, long def) {
		enforceUniqueFlag(flag);
		enforceUniqueDescription(description);
		longArgDescriptions.put(flag, description);
		commandLineValues.put(flag, null);
		if(required) requiredArgs.add(flag);
		longArgDefaults.put(flag, Long.valueOf(def));
	}

	/**
	 * Adds new float argument to set of arguments
	 * Client will crash if the same argument flag or description has already been added
	 * @param flag the command line flag for the argument
	 * @param description the description of the argument
	 * @param required whether parameter is required
	 */
	public void addFloatArg(String flag, String description, boolean required) {
		enforceUniqueFlag(flag);
		enforceUniqueDescription(description);
		floatArgDescriptions.put(flag, description);
		commandLineValues.put(flag, null);		
		if(required) requiredArgs.add(flag);
	}
	
	/**
	 * Adds new float argument to set of arguments and stores default
	 * Client will crash if the same argument flag or description has already been added
	 * @param flag the command line flag for the argument
	 * @param description the description of the argument
	 * @param required whether parameter is required
	 * @param def default value
	 */
	public void addFloatArg(String flag, String description, boolean required, float def) {
		enforceUniqueFlag(flag);
		enforceUniqueDescription(description);
		floatArgDescriptions.put(flag, description);
		commandLineValues.put(flag, null);		
		if(required) requiredArgs.add(flag);
		floatArgDefaults.put(flag, Float.valueOf(def));
	}
	
	/**
	 * Adds new double argument to set of arguments
	 * Client will crash if the same argument flag or description has already been added
	 * @param flag the command line flag for the argument
	 * @param description the description of the argument
	 * @param required whether parameter is required
	 */
	public void addDoubleArg(String flag, String description, boolean required) {
		enforceUniqueFlag(flag);
		enforceUniqueDescription(description);
		doubleArgDescriptions.put(flag, description);
		commandLineValues.put(flag, null);
		if(required) requiredArgs.add(flag);
	}

	/**
	 * Adds new double argument to set of arguments and stores default
	 * Client will crash if the same argument flag or description has already been added
	 * @param flag the command line flag for the argument
	 * @param description the description of the argument
	 * @param required whether parameter is required
	 * @param def default value
	 */
	public void addDoubleArg(String flag, String description, boolean required, double def) {
		enforceUniqueFlag(flag);
		enforceUniqueDescription(description);
		doubleArgDescriptions.put(flag, description);
		commandLineValues.put(flag, null);
		if(required) requiredArgs.add(flag);
		doubleArgDefaults.put(flag, Double.valueOf(def));
	}

	/**
	 * Adds new boolean argument to set of arguments
	 * Client will crash if the same argument flag or description has already been added
	 * @param flag the command line flag for the argument
	 * @param description the description of the argument
	 * @param required whether parameter is required
	 */
	public void addBooleanArg(String flag, String description, boolean required) {
		enforceUniqueFlag(flag);
		enforceUniqueDescription(description);
		boolArgDescriptions.put(flag, description);
		commandLineValues.put(flag, null);
		if(required) requiredArgs.add(flag);
	}
	
	/**
	 * Adds new boolean argument to set of arguments and stores default
	 * Client will crash if the same argument flag or description has already been added
	 * @param flag the command line flag for the argument
	 * @param description the description of the argument
	 * @param required whether parameter is required
	 * @param def default value
	 */
	public void addBooleanArg(String flag, String description, boolean required, boolean def) {
		enforceUniqueFlag(flag);
		enforceUniqueDescription(description);
		boolArgDescriptions.put(flag, description);
		commandLineValues.put(flag, null);
		if(required) requiredArgs.add(flag);
		boolArgDefaults.put(flag, Boolean.valueOf(def));
	}
	
	/**
	 * Parse command arguments
	 * If command line is not in proper form, prints help menu and exits
	 * If a required argument is missing, prints help menu and exits
	 * @param args the command line arguments passed to a main program
	 * @param allowDuplicateTags Tags are allowed to be specified more than once
	 */
	public void parse(String[] args, boolean allowDuplicateTags) {
		
		isParsed = false;
				
		commandLineValues.clear();
		duplicateCommandLineValues.clear();
		int i=0;
		while(i < args.length) {
			
			// Stop when output redirection is encountered
			if(args[i].contentEquals(">") || args[i].contentEquals(">&") || args[i].contentEquals(">!") || args[i].contentEquals(">&!") || args[i].contentEquals("|") || args[i].contentEquals(">>") || args[i].contentEquals(">>&")) break;
			
			// A flag shouldn't be the last item
			if(args.length == i+1) {
				printHelpMessage();
				throw new IllegalArgumentException("Flag can't be last item on line");
			}
			
			// Make sure flag exists
			if(!hasFlag(args[i])) {
				printHelpMessage();
				throw new IllegalArgumentException("Flag not recognized: " + args[i]);
			}
			
			
			// Can't see same flag twice
			if(!allowDuplicateTags & commandLineValues.containsKey(args[i])) {
				printHelpMessage();
				throw new IllegalArgumentException("Flag specified twice:" + args[i]);
			}
			
			
			// Next item should not be a flag
			if(hasFlag(args[i+1])) {
				printHelpMessage();
				throw new IllegalArgumentException("Can't specify two flags in a row: " + args[i] + " " + args[i+1]);
			}
			
			// Add entries to map
			if (duplicateCommandLineValues.containsKey(args[i])) {
				duplicateCommandLineValues.get(args[i]).add(args[i+1]);
			}
			else if (commandLineValues.containsKey(args[i]) & allowDuplicateTags) {
				if (!duplicateCommandLineValues.containsKey(args[i])) {
					duplicateCommandLineValues.put(args[i], new ArrayList<String>());
					duplicateCommandLineValues.get(args[i]).add(commandLineValues.get(args[i]));
				}
				commandLineValues.remove(args[i]);
				duplicateCommandLineValues.get(args[i]).add(args[i+1]);
				
			} else {
				commandLineValues.put(args[i], args[i+1]);
			}
			
			// Skip to next flag
			i += 2;
						
		}
		
		// Make sure all required arguments have been provided
		for(String req : requiredArgs) {
			if(!commandLineValues.containsKey(req) && (!allowDuplicateTags || (allowDuplicateTags && !duplicateCommandLineValues.containsKey(req)))) {
				printHelpMessage();
				throw new IllegalArgumentException("Invalid command line: argument " + req + " is required");
			}
		}
		
		isParsed = true;
		
	}
	
	/**
	 * Parse command arguments
	 * If command line is not in proper form, prints help menu and exits
	 * If a required argument is missing, prints help menu and exits
	 * @param args the command line arguments passed to a main program
	 */
	public void parse(String[] args) {
		parse(args,false);
	}
	
	/**
	 * Get the flags and values that were specified on the command line
	 * @return Map of flag to value
	 */
	public Map<String, String> getFlagsAndValues() {
		if(!isParsed) {
			throw new IllegalStateException("Must parse first.");
		}
		return commandLineValues;
	}
	
	
	/**
	 * Get the argument string from the command line
	 * @return The argument string
	 */
	public String getArgString() {
		return getArgString(null);
	}
	
	/**
	 * Get the argument string from the command line, possibly leaving out some arguments
	 * @param flagsToRemove Flags to leave out
	 * @return The argument string
	 */
	public String getArgString(Collection<String> flagsToRemove) {
		if(!isParsed) {
			throw new IllegalStateException("Must parse first.");
		}		
		Map<String, String> args = new HashMap<String, String>();
		args.putAll(getFlagsAndValues());
		if(flagsToRemove != null) {
			for(String flag : flagsToRemove) {
				args.remove(flag);
			}
		}
		String rtrn = "";
		for(String flag : args.keySet()) {
			rtrn += flag + " " + args.get(flag) + " ";
		}
		return rtrn;
	}
	
	/**
	 * Get value of String parameter specified by flag
	 * @param flag The command line flag for the argument
	 * @return String specified on command line or null if parameter was not specified
	 */
	public String getStringArg(String flag) {
		
		// Make sure command line has been parsed
		if(!isParsed) {
			throw new IllegalStateException("Cannot get parameter value without first calling method parse()");
		}
		
		// Make sure parameter type is correct
		if(!stringArgDescriptions.containsKey(flag)) {
			throw new IllegalArgumentException("Trying to get String value for non-String parameter " + flag);
		}
		
		if(commandLineValues.get(flag) == null) {
			if(stringArgDefaults.containsKey(flag)) return stringArgDefaults.get(flag);
		}
		
		return commandLineValues.get(flag);
	}
	
	/**
	 * Get value of String parameter specified by flag
	 * @param flag The command line flag for the argument
	 * @return String specified on command line or null if parameter was not specified
	 */
	public ArrayList<String> getStringListArg(String flag) {
		
		// Make sure command line has been parsed
		if(!isParsed) {
			throw new IllegalStateException("Cannot get parameter value without first calling method parse()");
		}
		
		// Make sure parameter type is correct
		if(!stringListArgDescriptions.containsKey(flag)) {
			throw new IllegalArgumentException("Trying to get String List value for non-String List parameter " + flag);
		}
		
		if(commandLineValues.get(flag) == null & duplicateCommandLineValues.get(flag) == null) {
			if(stringListArgDefaults.containsKey(flag)) return stringListArgDefaults.get(flag);
		} else if (commandLineValues.containsKey(flag)) {
			ArrayList<String> l = new ArrayList<String>();
			l.add(commandLineValues.get(flag));
			return l;
		} 
		
		return duplicateCommandLineValues.get(flag);
	}

	/**
	 * Get value of int parameter specified by flag
	 * @param flag The command line flag for the argument
	 * @return Integer specified on command line or null if parameter was not specified
	 */
	public int getIntArg(String flag) {
		
		// Make sure command line has been parsed
		if(!isParsed) {
			throw new IllegalStateException("Cannot get parameter value without first calling method parse()"); 
		}
		
		// Make sure parameter type is correct
		if(!intArgDescriptions.containsKey(flag)) {
			throw new IllegalArgumentException("Trying to get Integer value for non-Integer parameter " + flag); 
		}
		
		if(commandLineValues.get(flag) == null) {
			if(intArgDefaults.containsKey(flag)) return intArgDefaults.get(flag).intValue();
		}
		
		return Integer.valueOf(commandLineValues.get(flag),10).intValue();
	}

	/**
	 * Get value of long parameter specified by flag
	 * @param flag The command line flag for the argument
	 * @return Long specified on command line or null if parameter was not specified
	 */
	public long getLongArg(String flag) {
		
		// Make sure command line has been parsed
		if(!isParsed) {
			throw new IllegalStateException("Cannot get parameter value without first calling method parse()"); 
		}
		
		// Make sure parameter type is correct
		if(!longArgDescriptions.containsKey(flag)) {
			throw new IllegalArgumentException("Trying to get Long value for non-Long parameter " + flag); 
		}
		
		if(commandLineValues.get(flag) == null) {
			if(longArgDefaults.containsKey(flag)) return longArgDefaults.get(flag).longValue();
		}
		
		return Long.valueOf(commandLineValues.get(flag),10).longValue();
	}

	/**
	 * Get value of float parameter specified by flag
	 * @param flag The command line flag for the argument
	 * @return Float specified on command line or null if parameter was not specified
	 */
	public float getFloatArg(String flag) {
		
		// Make sure command line has been parsed
		if(!isParsed) {
			throw new IllegalStateException("Cannot get parameter value without first calling method parse()"); 
		}
		
		// Make sure parameter type is correct
		if(!floatArgDescriptions.containsKey(flag)) {
			throw new IllegalArgumentException("Trying to get Float value for non-Float parameter " + flag); 
		}
		
		if(commandLineValues.get(flag) == null) {
			if(floatArgDefaults.containsKey(flag)) return floatArgDefaults.get(flag).floatValue();
		}
		
		return Float.valueOf(commandLineValues.get(flag)).floatValue();
	}

	/**
	 * Get value of double parameter specified by flag
	 * @param flag The command line flag for the argument
	 * @return Double specified on command line or null if parameter was not specified
	 */
	public double getDoubleArg(String flag) {
		
		// Make sure command line has been parsed
		if(!isParsed) {
			throw new IllegalStateException("Cannot get parameter value without first calling method parse()"); 
		}
		
		// Make sure parameter type is correct
		if(!doubleArgDescriptions.containsKey(flag)) {
			throw new IllegalArgumentException("Trying to get Double value for non-Double parameter " + flag); 
		}
		
		if(commandLineValues.get(flag) == null) {
			if(doubleArgDefaults.containsKey(flag)) return doubleArgDefaults.get(flag).doubleValue();
		}
		
		return Double.valueOf(commandLineValues.get(flag)).doubleValue();
	}

	/**
	 * Get value of boolean parameter specified by flag
	 * @param flag The command line flag for the argument
	 * @return Boolean specified on command line or null if parameter was not specified
	 */
	public boolean getBooleanArg(String flag) {
		
		// Make sure command line has been parsed
		if(!isParsed) {
			throw new IllegalStateException("Cannot get parameter value without first calling method parse()"); 
		}
		
		// Make sure parameter type is correct
		if(!boolArgDescriptions.containsKey(flag)) {
			throw new IllegalArgumentException("Trying to get Boolean value for non-Boolean parameter " + flag); 
		}
		
		if(commandLineValues.get(flag) == null) {
			if(boolArgDefaults.containsKey(flag)) return boolArgDefaults.get(flag).booleanValue();
		}
		
		return Boolean.valueOf(commandLineValues.get(flag)).booleanValue();
	}
	
	
	
	/**
	 * Prints program description plus argument flags and descriptions
	 */
	public void printHelpMessage() {
		System.err.println();
		if(!programDescription.isEmpty()) {
			for(String s : programDescription) System.err.println(s + "\n");
			System.err.println();
		}
		
		TreeSet<String> args = new TreeSet<String>();
		
		for(String key : stringArgDescriptions.keySet()) {
			String msg = key + " <String>\t" + stringArgDescriptions.get(key);
			if(requiredArgs.contains(key)) msg += " (required)\n";
			else msg += " (default=" + stringArgDefaults.get(key) + ")\n";
			args.add(msg); 
		}
		for(String key : stringListArgDescriptions.keySet()) {
			String msg = key + " <String (repeatable)>\t" + stringListArgDescriptions.get(key);
			if(requiredArgs.contains(key)) msg += " (required)\n";
			else msg += " (default=" + stringArgDefaults.get(key) + ")\n";
			args.add(msg);
		}
		for(String key : intArgDescriptions.keySet()) {
			String msg = key + " <int>\t" + intArgDescriptions.get(key);
			if(requiredArgs.contains(key)) msg += " (required)\n";
			else msg += " (default=" + intArgDefaults.get(key) + ")\n";
			args.add(msg); 
		}
		for(String key : longArgDescriptions.keySet()) {
			String msg = key + " <long int>\t" + longArgDescriptions.get(key);
			if(requiredArgs.contains(key)) msg += " (required)\n";
			else msg += " (default=" + longArgDefaults.get(key) + ")\n";
			args.add(msg); 
		}
		for(String key : floatArgDescriptions.keySet()) {
			String msg = key + " <float>\t" + floatArgDescriptions.get(key);
			if(requiredArgs.contains(key)) msg += " (required)\n";
			else msg += " (default=" + floatArgDefaults.get(key) + ")\n";
			args.add(msg); 
		}
		for(String key : doubleArgDescriptions.keySet()) {
			String msg = key + " <double>\t" + doubleArgDescriptions.get(key);
			if(requiredArgs.contains(key)) msg += " (required)\n";
			else msg += " (default=" + doubleArgDefaults.get(key) + ")\n";
			args.add(msg); 
		}
		for(String key : boolArgDescriptions.keySet()) {
			String msg = key + " <boolean>\t" + boolArgDescriptions.get(key);
			if(requiredArgs.contains(key)) msg += " (required)\n";
			else msg += " (default=" + boolArgDefaults.get(key) + ")\n";
			args.add(msg); 
		}

		for(String s : args) {
			System.err.println(s);
		}
		System.err.println();
	
	}
	
	/**
	 * Checks if flag has already been added
	 * @param flag
	 * @return true if and only if flag has already been used
	 */
	private boolean hasFlag(String flag) {
		return (stringListArgDescriptions.containsKey(flag) || stringArgDescriptions.containsKey(flag) || intArgDescriptions.containsKey(flag) || longArgDescriptions.containsKey(flag) || floatArgDescriptions.containsKey(flag) || doubleArgDescriptions.containsKey(flag) || boolArgDescriptions.containsKey(flag));
	}
	
	/**
	 * Whether the flag is associated with an int argument and was specified on the command line
	 * @param flag The flag
	 * @return Whether the flag is present
	 */
	public boolean hasIntFlag(String flag) {
		return intArgDescriptions.containsKey(flag) && commandLineValues.containsKey(flag);
	}
	
	/**
	 * Whether the flag is associated with a long argument and was specified on the command line
	 * @param flag The flag
	 * @return Whether the flag is present
	 */
	public boolean hasLongFlag(String flag) {
		return longArgDescriptions.containsKey(flag) && commandLineValues.containsKey(flag);
	}
	
	/**
	 * Whether the flag is associated with a float argument and was specified on the command line
	 * @param flag The flag
	 * @return Whether the flag is present
	 */
	public boolean hasFloatFlag(String flag) {
		return floatArgDescriptions.containsKey(flag) && commandLineValues.containsKey(flag);
	}
	
	/**
	 * Whether the flag is associated with a double argument and was specified on the command line
	 * @param flag The flag
	 * @return Whether the flag is present
	 */
	public boolean hasDoubleFlag(String flag) {
		return doubleArgDescriptions.containsKey(flag) && commandLineValues.containsKey(flag);
	}
	
	/**
	 * Whether the flag is associated with a boolean argument and was specified on the command line
	 * @param flag The flag
	 * @return Whether the flag is present
	 */
	public boolean hasBooleanFlag(String flag) {
		return boolArgDescriptions.containsKey(flag) && commandLineValues.containsKey(flag);
	}
	
	/**
	 * Whether the flag is associated with a string argument and was specified on the command line
	 * @param flag The flag
	 * @return Whether the flag is present
	 */
	public boolean hasStringFlag(String flag) {
		return stringArgDescriptions.containsKey(flag) && commandLineValues.containsKey(flag);
	}
	
	/**
	 * Checks if description has already been added
	 * @param description
	 * @return true if and only if description has already been used
	 */
	private boolean hasDescription(String description) {
		return (stringArgDescriptions.containsValue(description) || intArgDescriptions.containsValue(description) || longArgDescriptions.containsValue(description) || floatArgDescriptions.containsValue(description) || doubleArgDescriptions.containsValue(description) || boolArgDescriptions.containsValue(description));
	}
	
	/**
	 * Causes client to crash with error message if argument flag has already been used
	 * @param flag
	 */
	private void enforceUniqueFlag(String flag) {
		if(hasFlag(flag)) {
			throw new IllegalStateException("Flag " + flag + " has already been used."); 
		}
	}
	
	/**
	 * Causes client to crash with error message if argument description has already been used
	 * @param description
	 */
	private void enforceUniqueDescription(String description) {
		if(hasDescription(description)) {
			throw new IllegalStateException("Description " + description + " has already been used."); 
		}
	}
	
	
	
}
