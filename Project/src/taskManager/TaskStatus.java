package taskManager;



/**
 * The TaskStatus class describes an enumerator class that lists the statuses
 * that a task can have. There are four possible statuses for a project, namely
 * available, unavailable finished or failed.
 * 
 * @author Groep 8
 */
public enum TaskStatus {
	
	AVAILABLE {		
		@Override
		public TaskStatus goExecuting(){
			return TaskStatus.EXECUTING; 
		}
	}, 
	UNAVAILABLE {
		@Override
		public TaskStatus goAvailable(){
			return TaskStatus.AVAILABLE; 
		}
	}, 
	FINISHED {
		@Override
		public TaskFinishedStatus getFinishStatus(Task task) {
			if (task.wasFinishedEarly()) {
				return TaskFinishedStatus.EARLY;
			} else if (task.wasFinishedWithADelay()) {
				return TaskFinishedStatus.WITH_A_DELAY;
			} else {
				return TaskFinishedStatus.ON_TIME;
			}
		}
	}, 
	FAILED {
	}, 
	EXECUTING {
		@Override
		public TaskStatus goFinished(){
			return TaskStatus.FINISHED; 
		}
		@Override
		public TaskStatus goFailed(){
			return TaskStatus.FAILED; 
		}
	};

	public TaskFinishedStatus getFinishStatus(Task task){
		throw new IllegalStateException("The task is not finished yet");
	}
	
	public TaskStatus goExecuting(){
		throw new IllegalStateException("Task is not available");
	}
	
	public TaskStatus goAvailable(){
		return TaskStatus.UNAVAILABLE;
	}
	
	public TaskStatus goFailed(){
		throw new IllegalStateException("Task is not executing");
	}
	
	public TaskStatus goFinished(){
		throw new IllegalStateException("Task is not executing");
	}


}
