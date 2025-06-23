package javamsg;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MsgTask implements Runnable {

	
	private volatile long _data = 0;
	private int _total = 0;
	private int _times = 0;
	
	private long _task_id=0;
	private MsgTask _nextWorker = null;
	private MsgTask _first = null;
	private boolean _lastTask = true;
	private boolean _firstTime=true;
	private boolean _done=false;
	
	private ReentrantLock _lock = new ReentrantLock();
	private Condition _condition = _lock.newCondition();
	
	private boolean _logEnabled = false;

    public void setLogEnabled(boolean enabled) {
        this._logEnabled = enabled;
    }

    public void log(String format, Object... args) {
        if (_logEnabled) {
            System.out.printf(format, args);
        }
    }
	
	MsgTask(long taskId, int totalTimes, MsgTask firstTask) { 
		
		_task_id=taskId;
		_total = totalTimes;
		
		_first = firstTask ==  null ? this:firstTask;
		 _lastTask = true; //this is the last task unless setNext task called 
		
	}
	
	public void setNext(MsgTask next) { 
		_nextWorker=next;
		_lastTask = false;
	}
	
	 void work(long input) {
		
		if (_task_id == MsgPing.getTotalTasks()) { 
			log("task id %d recieved work with input %d \n", _task_id, input );
		}
		
			
		if (_task_id == MsgPing.getTotalTasks()) { 
			log("task id %d unlocked for work \n", _task_id);
		}
		
	
		
		
		if (_task_id == MsgPing.getTotalTasks()) { 
			log("task id %d notifying with data %d times %d \n", _task_id, input, _times );
		}
		
		
		_lock.lock();
		
		
		try {
			_data=input;
			
			_condition.signal();
		}
		finally { 
			_lock.unlock();
		}
	
		
	}
	
	@Override
	public void run() {
		
		
		while(!_done) { 
				
			performRunLogic();
			_data = 0;
			goToSleep();
		
		}
			
	}

	private void performRunLogic () { 
		
		
		if (_firstTime) { 
			_firstTime = false;
			log("task id %d indicating readiness \n", _task_id );
			MsgPing.markThreadReady();
			return;
		}
		
		if (_task_id == MsgPing.getTotalTasks()) { 
			log("task id %d woke up with data %d \n", _task_id, _data );
		}
		
		
		long data = 0; 
		
		_lock.lock();
			data = _data; 
		_lock.unlock();
		
		//this should not happen and can typically happen only first time the thread wakes up
		if (data <= 0) {
				log("task id %d is going to sleep since data is 0 \n", _task_id);
			return;
		}
		
		 _times++;
		
				
		log("Iteration %d task id %d recieved data %d\n", _times, _task_id, data);
		
		
		if (_times >= _total) {

			if (_lastTask) {
				log("Finished from task ID %d \n", _task_id);
			}
			
			_done = true;
			sendWorkToNextTask(data);
			MsgPing.markThreadDone();
			return;
		}
		else if (_times < _total) {
			
			sendWorkToNextTask(data);
		}
		
		
		return;

	}
	
	private void sendWorkToNextTask(long data) {
		
		if (_lastTask) {
			log("finished %d round \n", _times);
			_first.work(1);
			return;
		}
		
		
		data++;
		log("Sending task to next worker with data %d \n", data);
		_nextWorker.work(data);
	
	}

	private void goToSleep() {
		
		_lock.lock();
	
		try {
			_condition.await();
		} 
		catch (InterruptedException e) {
			log("Exception trying to sleep in task %d  %s \n", _task_id, e);
		}
		finally {
			_lock.unlock();
		}
	}

}
