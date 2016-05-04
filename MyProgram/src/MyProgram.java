import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;


public class MyProgram {

	public static int exceptionMode; //A check to see if the parser should try ignore any errors. This is initially 0
	public static String currentState="S";
	public static Stack<String[]> ruleIndex= new Stack<String[]>();
	public static String[] toPush={"S", "1"};
	public static int currentRuleIndex=0;
	public static List<String> format= new ArrayList<String>();
	public static List<List<String>> parseArrays= new ArrayList<>();
	public static List<List<String>> rulesArrays= new ArrayList<>();
	
	public static void main(String[] args) {
		//No argument provided
		if (args.length==0){
			System.out.println("No string provided");
			System.exit(1);
		}
		List<String> input= new ArrayList<String>();
		input= separateInput(args[0]);
		if (args.length>1){
			if (args[1].equals("-e")){
				exceptionMode=1;
			}
		}	
		initialiseGrammar();
			ruleIndex.push(toPush);
			currentRuleIndex=0; //ie the first index of the rule S
		for(int i=0; i<input.size(); i++){
			
			String temp=input.get(i);
			boolean isValid=checkInput(temp);
			if (isValid=false){
				if (exceptionMode==0){
					System.exit(1);
				}
				else{
				System.out.println("COUDLN'T FIND VALUE, trying next value");
				}
			}
			else{
				System.out.println("System parsed "+temp);
			}
		}
		
	}
	
	//Separates the args into an ArrayList of each individual input command
	public static List<String> separateInput(String argsIn){
		List<String> input= new ArrayList<String>();
		input.addAll((Arrays.asList(argsIn.split(" "))));
		return input;
	}
	
	
	/*Checks if the provided input can be reached from the current state. This uses strings that consist of the Rows
	 * from the derived parse tables. This part of the program is therefore specific to the grammar hard coded in.
	 * TODO make a method that checks if the next character of the current rule is a non terminal or not, this can be used then to provide
	 * the expected message.
	*/
	public static Boolean checkInput(String command){
	int indexToLookUp=format.indexOf(command);
	//Firstly Checks that the provided string is even an accepted string
	//System.out.println(indexToLookUp + " index"+ " command "+ command +" "+ currentState);
	if (indexToLookUp==-1){
		//TODO add more error message
		System.out.println("NOT A TERMINAL WITHIN THIS GRAMMAR");
		return false;
		}
	
		List<String> rule=getRule(currentState);
		//System.out.println("The rule being returned is " +rule.get(0));
		String currentPart= rule.get(currentRuleIndex+1);
		System.out.println("The current Part "+ currentPart);
		//This checks firstly to see if the current string is the command trying to parse
		if (currentPart.equals(command)){
			//This means the program is at the end of the current rule and its time to go back to the previous rule
			if (rule.size()==currentRuleIndex+2){
				popStack();
			}
			else{
				currentRuleIndex++;}
			return true;
		}
		List<String> firsts=getFirst(currentPart);
		//This will only return true if currentPart is a terminal but not the terminal being parsed currently, therfore getFirst() 
		//Won't be able to find the correct String to return and will return Null.
		if (firsts==null){
			//TODO add in the extension part of the issue, also provide feedback on what symbol the parser was expecting.
			System.out.println("Error couldn't parse that string");
			return false;
		}
		//This means that the parser must go to another rule, meaning the current rule and the index must be pushed to the stack.
		else{
			String nextTerminal=firsts.get(indexToLookUp);
			pushStack(nextTerminal);
			if (checkInput(command)==true){
				return false;
			}
			else return false;
		}
	}
	
	
	/*Returns a string which has the rule in which each terminal can be reached in a first look ahead. The variable which 
	 * corresponds with each index is specified in the format comment line. Null if that terminal cannot be reached in a first
	 * look ahead from that rule. Basically these strings are rows from the parse table.
	*/
	public static List<String> getFirst(String variable){
			
		for (int i=0; i<parseArrays.size(); i++){
			if (parseArrays.get(i).get(0).equals(variable)){
				return parseArrays.get(i);
			}
		}
			return null;
	}
	
	public static List<String> getRule(String variable){
		System.out.println("the variable "+ variable);
		for (int i=0; i<rulesArrays.size(); i++){
			if (parseArrays.get(i).get(0).equals(variable)){
				return rulesArrays.get(i);
			}
		}
			return null;
		
	}
	/*
	 * Pops the stack, sets global variable currentState to the popped state or rule, and also sets
	 * the global variable currentRuleIndex which is the index of that rule the program was up before 
	 * going to a new rule
	 */
	public static void popStack(){
		int check;
		//basically this loop will keep popping if the parser was on a variable that was the end of that rule.
		do{
			//this means the parser is back to the first
			if(currentState.equals("S")){
				System.out.println("Parse complete, input was successful");
				System.exit(0);
			}
		String [] temp=ruleIndex.pop();
		currentState=temp[0];
		currentRuleIndex=Integer.parseInt(temp[1]);
		check= getRule(currentState).size();
		}while(check==currentRuleIndex+1);
	}
	
	public static void pushStack(String variable){
		System.out.println("pushStack variable" +variable);
		toPush[0]=currentState;
		toPush[1]=Integer.toString(currentRuleIndex);
		
		//Sets the new current State and its index to 0
		currentState=variable;
		currentRuleIndex=0;
	}
	
	/*
	 * This method initialises the parseArrays, and also the ruleArray from the txt files they are in.
	 */
	public static void initialiseGrammar(){
		Scanner inFile;
		List<String> inputDump=new ArrayList<String>();
		try{
			inFile= new Scanner(new FileReader("parseTable.txt"));
			while (inFile.hasNextLine()){
				inputDump.add(inFile.nextLine()); 
			 }
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		for (int i=0; i<inputDump.size(); i++){
		List<String> temp= new ArrayList<String>();
		temp=Arrays.asList(inputDump.get(i).split(","));
			//special case just to deal with the format line
			if (i==0){
			format=temp;
			}
			else{
				parseArrays.add(temp);
			}
		}
		try{
			inputDump=new ArrayList<String>();
			inFile= new Scanner(new FileReader("rules.txt"));
			while (inFile.hasNextLine()){
				inputDump.add(inFile.nextLine()); 
			 }
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		for (int i=0; i<inputDump.size(); i++){
			List<String> temp= new ArrayList<String>();
			temp=Arrays.asList(inputDump.get(i).split(","));
			rulesArrays.add(temp);
		}
		
		
		/*
		for (int i=0; i<parseArrays.size();i++){
			for (int w=0; w<parseArrays.size();w++){
			System.out.print(parseArrays.get(i).get(w));
			}
			System.out.println();
			System.out.println();
		}
		*/
	}

}
