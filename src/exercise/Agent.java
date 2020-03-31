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

	public String proposedTask = null;

	public ArrayList<Utility> orderedUtilityValues = new ArrayList<>();

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
		double utilityValue = Double.parseDouble(values[1].split("=")[1]);

		if(values[0].equals("A")){
			if(debugging) System.out.println(String.format(Locale.US,"[RATIONALE] Task: %s Old value: %.2f New Value: %.2f",this.taskChosen, this.utilityValues.get(this.taskChosen).getExpectedValue(), utilityValue));

			this.utilityValues.get(this.taskChosen).addObservation(utilityValue, this.cyclesPassed);

			this.total += utilityValue;
			if(debugging) System.out.println(String.format(Locale.US, "[RATIONALE] Total value: %.2f", this.total));
		}

		else if(values[0].matches("T.*")){
			this.utilityValues.put(values[0], new Utility(utilityValue, this.memoryFactor, values[0]));
		}

		else{
			if(debugging) System.out.println(String.format("[HOMOGENEOUS] Task Observed %s Value Observed: %.2f",values[0].split("_")[1], utilityValue));

			this.utilityValues.get(values[0].split("_")[1]).addObservation(utilityValue, this.cyclesPassed);

		}
	}
	
	public void decideAndAct() {
		decide();
		act();
	}

	public ArrayList<Utility> decide(){
		this.orderedUtilityValues = maxUtilRestart();

		this.proposedTask = this.orderedUtilityValues.get(0).getTask();

		return this.orderedUtilityValues;
	}

	public void act(){

		if(!this.proposedTask.equals(this.taskChosen)){
			this.currentRestart = this.restart;
			this.taskChosen = this.proposedTask;
		}

		this.cycle--;
		this.cyclesPassed++;
		if(debugging) System.out.println(String.format(Locale.US, "[RATIONALE] Chosen Task: %s", this.taskChosen));
		if(this.currentRestart > 0) this.currentRestart--;
	}
	
	public String recharge() { return getOutput(); }

	


	/******************************/
	/**** C: UTILITY FUNCTIONS*****/
	/******************************/

	public ArrayList<Utility> maxUtil(){
		ArrayList<Utility> utilities = new ArrayList<>();
		for (String key: this.utilityValues.keySet()) {
			utilities.add(this.utilityValues.get(key));
		}

		Comparator<Utility> comparator = new Comparator<Utility>() {
			@Override
			public int compare(Utility o1, Utility o2) {
				double o1Value = o1.getExpectedValue();
				double o2Value = o2.getExpectedValue();

				if(o1Value > o2Value){
					return -1;
				}
				else if(o1Value == o2Value){
					if (o1.getTask().compareTo(o2.getTask()) > 0){
						return 1;
					}
					else return -1;
				}
				else return 1;
			}
		};

		utilities.sort(comparator);


		if(debugging) System.out.println(String.format(Locale.US,"[RATIONALE] Chosen task: %s Expected value: %.2f", utilities.get(0).getTask(), utilities.get(0).getExpectedValue()));
		return utilities;
	}

	public ArrayList<Utility> maxUtilRestart(){
		ArrayList<Utility> maxUtilArray = maxUtil();
		if(this.taskChosen == null || this.restart == 0 || this.taskChosen.equals(maxUtilArray.get(0).getTask())){
			return maxUtilArray;
		}

		else{
			RestartComparator comparator = new RestartComparator(cycle, this.restart, this.currentRestart, this.taskChosen);

			maxUtilArray.sort(comparator);

			if(debugging) System.out.println(String.format(Locale.US,"[RESTART] Max Value Task: %s Current Task: %s", maxUtilArray.get(0).getTask(), this.taskChosen));

			return maxUtilArray;
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

	public ArrayList<Utility> getOrderedUtilityValues(){
		return this.orderedUtilityValues;
	}

	public void setProposedTask(String task){
		this.proposedTask = task;
	}
}
