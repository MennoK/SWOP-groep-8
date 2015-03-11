package taskManager.exception;

public class LoopingDependencyException extends Exception {
	public LoopingDependencyException(String message){
        super(message);
    }
}
