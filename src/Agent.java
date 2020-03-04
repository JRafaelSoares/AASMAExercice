import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class Agent {

	
	/**********************************/
	/******* A: AGENT BEHAVIOR ********/
	/**********************************/

	//Agent caractheristics
	public int cycle;

	public int restart = 0;

	public float memoryFactor = 0;

	public String decision;

	/**********************************/
	/*** A.1: Task Implementation *****/
	/**********************************/
	public HashMap<String, Float> expectedValue = new HashMap<>();

	public HashMap<String, Float> realValue = new HashMap<>();

	public String taskChosen;

	public float total = 0;

	/**********************************/
	/******** A.2: Debugging  *********/
	/**********************************/

	public boolean debugging = false;

	public Agent(String[] options) {
		for (String option: options) {
			initializationParse(option.split("="));
		}
	}
	
	public void perceive(String input) {
		String[] values = input.split(" ");

		if(values[0].equals("A")){
			//TODO - Case with multiple utilities for multiple tasks?
			float utilityValue = Float.parseFloat(values[1].split("=")[1]);

			if(debugging) System.out.println(String.format(Locale.US,"Old value: %.1f New Value: %.1f", expectedValue.get(taskChosen), utilityValue));
			//Updates utility value of task
			this.expectedValue.replace(taskChosen, utilityValue);
			this.realValue.replace(taskChosen, utilityValue);


			total+= utilityValue;
		}

		else{
			//Gets value from tasks of type TX u=Y
			this.expectedValue.put(values[0], Float.parseFloat(values[1].split("=")[1]));
			if(debugging) System.out.println(String.format(Locale.US,"Task: %s Value: %.1f",values[0], Float.parseFloat(values[1].split("=")[1])));
			this.realValue.put(values[0], Float.NEGATIVE_INFINITY);

		}
	}
	
	public void decideAndAct() { maxUtil(); }
	
	public String recharge() { return getOutput(); }

	
	/******************************/
	/******* B: MAIN UTILS ********/
	/******************************/

    public static void main(String[] args) throws IOException { 
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String line = br.readLine();
		Agent agent = new Agent(line.split(" "));
		while(!(line=br.readLine()).startsWith("end")) {
			//DEBUGGING
			if(agent.debugging) System.out.println(line);
			if(line.startsWith("TIK")) agent.decideAndAct();
			else agent.perceive(line);
		}
		System.out.println(agent.recharge());
		br.close();
	}

	/******************************/
	/**** C: UTILITY FUNCTIONS*****/
	/******************************/
	
	public void maxUtil(){
		float maxValue = Float.NEGATIVE_INFINITY;
		float currentvalue;

		for (String key: this.expectedValue.keySet()) {
			currentvalue = this.expectedValue.get(key);
			if (currentvalue > maxValue){
				maxValue = currentvalue;
				this.taskChosen = key;
			}

			//TODO - Check if compareTo is correct
			else if (currentvalue == maxValue && this.taskChosen.compareTo(key) > 0){
				this.taskChosen = key;
			}
		}

		if(debugging) System.out.println(String.format(Locale.US,"Chosen task: %s Expected value: %.1f", this.taskChosen, maxValue));
	}
	
	/******************************/
	/******* D: AUX FUNCTIONS******/
	/******************************/

	public void initializationParse(String[] initialization){
		switch (initialization[0]){
			case "cycle":
				this.cycle= Integer.parseInt(initialization[1]);
				break;
			case "decision":
				this.decision = initialization[1];
				break;
			case "restart":
				this.restart = Integer.parseInt(initialization[1]);
				break;
			case "memory-factor":
				this.memoryFactor = Float.parseFloat(initialization[1]);
				break;
		}
	}

	public String getOutput(){
		String output = "state={";

		List<String> list = new ArrayList<>(this.realValue.keySet());
		//sort list of Tasks
		java.util.Collections.sort(list);
		for (String key: list) {
			float currentValue = this.realValue.get(key);
			if(currentValue != Float.NEGATIVE_INFINITY){
				output = output.concat(String.format(Locale.US, "%s=%.1f,",key, currentValue));
			}

			else {
				output = output.concat(String.format("%s=NA,", key));
			}
		}
		//Removes last ","
		output = output.substring(0, output.length()-1);

		return output.concat(String.format(Locale.US,"} gain=%.1f", this.total));

	}
}
