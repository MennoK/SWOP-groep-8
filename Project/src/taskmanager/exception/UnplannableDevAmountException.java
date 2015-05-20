package taskmanager.exception;

public class UnplannableDevAmountException extends RuntimeException{

	private static final long serialVersionUID = 2986885902958326213L;
	
	private int amountOfDevelopers;
	
	public UnplannableDevAmountException(int amountOfDevelopers){
		this.amountOfDevelopers = amountOfDevelopers;
	}
	
	public int getAmountOfDevelopers() {
		return amountOfDevelopers;
	}
}
