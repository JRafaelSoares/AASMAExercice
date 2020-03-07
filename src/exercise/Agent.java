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

	public int currentRestart = 0;

	/**********************************/
	/******** A.2: Debugging  *********/
	/**********************************/

	public boolean debugging = true;

	public Agent(String[] options, boolean debugging) {
		for (String option: options) {
			initializationParse(option.split("="));
		}
		this.debugging = debugging;
	}
	
	public void perceive(String input) {
		String[] values = input.split(" ");

		if(values[0].equals("A")){
			double utilityValue = Double.parseDouble(values[1].split("=")[1]);
			if(debugging) System.out.println(String.format(Locale.US,"[RATIONALE] Task: %s Old value: %.2f New Value: %.2f",this.taskChosen, this.utilityValues.get(this.taskChosen).getExpectedValue(), utilityValue));

			this.utilityValues.get(this.taskChosen).addObservation(utilityValue, this.cyclesPassed);

			this.total += utilityValue;
			if(debugging) System.out.println(String.format(Locale.US, "[RATIONALE] Total value: %.2f", this.total));
		}

		else{
			this.utilityValues.put(values[0], new Utility(Double.parseDouble(values[1].split("=")[1]), this.memoryFactor));
		}
	}
	
	public void decideAndAct() {
		if(this.currentRestart > 0) this.currentRestart--;
		this.taskChosen = maxUtilRestart();

		this.cycle--;
		this.cyclesPassed++;
		if(debugging) System.out.println(String.format(Locale.US, "[RATIONALE] Chosen Task: %s", this.taskChosen));
	}
	
	public String recharge() { return getOutput(); }

	


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


			double maxUtilRestartValue =  this.utilityValues.get(maxUtilTask).simulateRestart(cycle, this.restart);   //calculateRestart(this.utilityValues.get(maxUtilTask), this.restart-1);
			double currentUtilValue = this.utilityValues.get(this.taskChosen).simulateRestart(cycle, this.currentRestart);//calculateRestart(this.utilityValues.get(this.taskChosen), 0);
			if(debugging) System.out.println(String.format(Locale.US, "[RESTART] New Task Total Value: %.2f  Current Task Total Value: %.2f", maxUtilRestartValue, currentUtilValue));

			if( maxUtilRestartValue > currentUtilValue){
				if(debugging) System.out.println(String.format(Locale.US,"[RESTART] New Current Task: %s Old Task: %s", maxUtilTask, this.taskChosen));
				this.currentRestart = this.restart;
				return maxUtilTask;
			}

			else if(maxUtilRestartValue == currentUtilValue){
				return maxUtilTask.compareTo(this.taskChosen) > 0 ? this.taskChosen : maxUtilTask;
			}

			else return this.taskChosen;
		}
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
		return String.format(Locale.US,"state={%s} gain=%.2f", getTaskValues(), this.total);

	}

	public String getTaskValues(){
		String output = "";

		List<String> list = new ArrayList<>(this.utilityValues.keySet());
		//sort list of Tasks
		java.util.Collections.sort(list);
		for (String key: list) {
			Utility currentUtil = this.utilityValues.get(key);
			if(!currentUtil.wasNotExecuted()){
				output = output.concat(String.format(Locale.US, "%s=%.2f,",key, currentUtil.getExpectedValue()));
			}

			else {
				output = output.concat(String.format("%s=NA,", key));
			}
		}
		//Removes last ","
		return output.substring(0, output.length()-1);
	}

	public double getTotal(){
		return this.total;
	}
}
