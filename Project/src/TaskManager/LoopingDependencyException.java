package TaskManager;

public class LoopingDependencyException extends Exception {
	public LoopingDependencyException(String message){
        super(message);
    }
}
