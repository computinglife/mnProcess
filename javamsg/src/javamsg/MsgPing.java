/**
 * 
 */
package javamsg;

import java.lang.Thread.Builder.OfVirtual;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 
 */
public class MsgPing {

	
	private static Integer  _numThreads;
	private static Integer  _numTimes;
	
	
	private static Thread[] _threads = null;
	
	
	private static long _createStart = 0;
	private static long _createFinish = 0;
	private static long _msgstart=0;
	private static long _msgend=0;
	
	private static MsgTask _firstTask = null;
	
	
	private  static CountDownLatch _readySignal = null;
	private  static CountDownLatch _doneSignal = null;
    
	
	public static int getTotalTasks () { 
		return _numThreads;

	}
	
	public static int getTotalReps () { 
		return _numTimes;

	}
	
    public static void markThreadDone() { 
    	_doneSignal.countDown();
    }
    
    public static void markThreadReady() { 
    	_readySignal.countDown();
    }
    
	static Integer getNumber(String s) { 
		
		try
		{
			return Integer.parseInt(s);
		}
		catch (NumberFormatException num) { 
			printUsage();
			return null;
		}
	}
	
	public static void setCompleteFinishedTime() { 
		_msgend=System.nanoTime();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		//get the arguments parsed & laid out
		if (getArguments(args) !=0 ) System.exit(-1);
		
		
		_firstTask = createSetup();
		
		if (_firstTask == null) System.exit(-1);
		
		
		startMessagePassing();
		
		waitForAllThreadsToFinish();
		
		printTimeTaken();
		
		
		System.out.println("\n Done");
		
	
	}
	
	
	private static void waitForAllThreadsToStart() {

		System.out.println("Waiting for all threads to start");

		try {
			_readySignal.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(-1);
		}

		System.out.println("All threads are ready to run");
		
	}
	
	
	private static void waitForAllThreadsToFinish() {


		try {
			_doneSignal.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		setCompleteFinishedTime();
		
	}

	private static void startMessagePassing() {
		

		System.out.println("Sending message to 1st task");
		
		_msgstart = System.nanoTime();
		
		
		_firstTask.work(1);
		
	}

	private static void printTimeTaken() {
	
		long setupTimeNano=_createFinish-_createStart;
		long setupMs = TimeUnit.MILLISECONDS.convert(setupTimeNano, TimeUnit.NANOSECONDS);
		
		long msgTimeNano =_msgend-_msgstart;
		long msgMs= TimeUnit.MILLISECONDS.convert(msgTimeNano, TimeUnit.NANOSECONDS);
		
		System.out.printf("%d %d \n", setupMs, msgMs);
		
	}

	private static MsgTask createSetup() { 
		
		
		System.out.format ("Creating %d tasks for %d times message passing \n", _numThreads, _numTimes);
		_createStart = System.nanoTime();
		

		_threads = new Thread[_numThreads];
		
		OfVirtual virtBuilder = Thread.ofVirtual();
		
		
		MsgTask current_task = new MsgTask(1,_numTimes, null);
		MsgTask first_task = current_task;
		
		for ( int i =0; i < _numThreads;i++) { 
			
			MsgTask newTask = new MsgTask(i+2, _numTimes, first_task);
			
			//for the last task we don't want this to happen
			if (i < _numThreads - 1) current_task.setNext(newTask);
			
			
			try
			{
				_threads[i] = virtBuilder.start(current_task);
			}
			catch (Throwable e) { 
				e.printStackTrace();
				System.exit(-1);
			}
			
			current_task = newTask;
			
			
		}
		
		waitForAllThreadsToStart();
		
		
		_createFinish = System.nanoTime();
		
		System.out.println("Finished creating the tasks");
		
		return first_task;
		
		
	}

	private static int getArguments(String[] args) {
		if (args.length != 4) {
			System.out.printf("Recieved %d arguments \n", args.length);
			printUsage();
			return -1;
		}
		
		for (String s : args) { 
			if (s.length() <=0) {
				printUsage();
				return -1;
			}
		}
		
		if (args[0].compareToIgnoreCase("n") !=0) { 
			printUsage();
			return -1;
		}
		
		
		if (args[2].compareToIgnoreCase("m") !=0) { 
			printUsage();
			return -1;
		}
		
		_numThreads = getNumber(args[1]);
		_numTimes = getNumber(args[3]);
		
		if (_numThreads == null || _numTimes == null || _numThreads <= 0 || _numTimes <= 0) {
	        printUsage();
	        return -1;
	    }

		
		_doneSignal = new CountDownLatch(_numThreads);
		_readySignal = new CountDownLatch(_numThreads);
		
		return 0;
	}

	private static void printUsage() {
		System.out.println("Need 2 arguments --n <positive-number-of-threads> --m <positive-number-of-trips>");
	}

}
