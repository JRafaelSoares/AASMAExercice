package exercise;

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

	public double memoryFactor = 0;

	public String decision;

	/**********************************/
	/*** A.1: Task Implementation *****/
	/**********************************/

	public HashMap<String, Utility> utilityValues = new HashMap<>();

	public String taskChosen = null;

	public double total = 0;

	public int cyclesPassed = 0;

	/**********************************/
	/******** A.2: Debugging  *********/
	/**********************************/

	public boolean debugging = true;

	public Agent(String[] options) {
		for (String option: options) {
			initializationParse(option.split("="));
		}
	}
	
	public void perceive(String input) {
		String[] values = input.split(" ");

		if(values[0].equals("A")){
			//TODO - Case with multiple utilities for multiple tasks?
			double utilityValue = Float.parseFloat(values[1].split("=")[1]);

			if(debugging) System.out.println(String.format(Locale.US,"[RATIONALE] Task: %s Old value: %.2f New Value: %.2f",this.taskChosen,this.utilityValues.get(this.taskChosen).getExpectedValue(), utilityValue));

			this.utilityValues.get(this.taskChosen).addObservation(utilityValue, this.cyclesPassed);

			this.total += utilityValue;
			if(debugging) System.out.println(String.format(Locale.US, "[RATIONALE] Total value: %.2f", this.total));
		}

		else{
			this.utilityValues.put(values[0], new Utility(Double.parseDouble(values[1].split("=")[1]), this.memoryFactor));
		}
	}
	
	public void decideAndAct() {
		this.taskChosen = maxUtilRestart();
		this.cycle--;
		this.cyclesPassed++;
		if(debugging) System.out.println(String.format(Locale.US, "[RATIONALE] Chosen Task: %s", this.taskChosen));
	}
	
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
	
	public String maxUtil(){
		String taskChosen = "blank";

		double maxValue = Float.NEGATIVE_INFINITY;
		double currentValue;
		for (String key: this.utilityValues.keySet()) {
			currentValue = this.utilityValues.get(key).getExpectedValue();
			if (currentValue > maxValue){
				maxValue = currentValue;
				taskChosen = key;
			}
			else if (currentValue == maxValue && taskChosen.compareTo(key) > 0){
				taskChosen = key;
			}
		}

		if(debugging) System.out.println(String.format(Locale.US,"[RATIONALE] Max task: %s Expected value: %.2f", taskChosen, maxValue));
		return taskChosen;
	}

	public String maxUtilRestart(){
		String maxUtilTask = maxUtil();
		if(this.taskChosen == null || this.restart == 0 || this.taskChosen.equals(maxUtilTask)){
			return maxUtilTask;
		}

		else{

			if(debugging) System.out.println(String.format(Locale.US, "[RESTART] Max Task value: %.2f  Current Task value: %.2f", this.utilityValues.get(maxUtilTask).getExpectedValue() * (this.cycle-restart), this.utilityValues.get(this.taskChosen).getExpectedValue()*this.cycle));

			double maxUtilRestartValue = calculateRestart(this.utilityValues.get(maxUtilTask), this.restart-1);
			double currentUtilValue = calculateRestart(this.utilityValues.get(this.taskChosen), 0);

			if( maxUtilRestartValue > currentUtilValue){
				if(debugging) System.out.println(String.format(Locale.US,"[RESTART] New Task: %s Old Task: %s", maxUtilTask, this.taskChosen));

				return maxUtilTask;
			}

			else if(maxUtilRestartValue == currentUtilValue){
				return maxUtilTask.compareTo(this.taskChosen) > 0 ? this.taskChosen : maxUtilTask;
			}

			else return this.taskChosen;
		}
	}

	public double calculateRestart(Utility utility, int restart){
		return utility.getExpectedValue() * (this.cycle - restart);
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

		List<String> list = new ArrayList<>(this.utilityValues.keySet());
		//sort list of Tasks
		java.util.Collections.sort(list);
		for (String key: list) {
			Utility currentUtil = this.utilityValues.get(key);
			if(currentUtil.getTimesExecuted() != 0){
				output = output.concat(String.format(Locale.US, "%s=%.2f,",key, currentUtil.getExpectedValue()));
			}

			else {
				output = output.concat(String.format("%s=NA,", key));
			}
		}
		//Removes last ","
		output = output.substring(0, output.length()-1);

		return output.concat(String.format(Locale.US,"} gain=%.2f", this.total));

	}
}
