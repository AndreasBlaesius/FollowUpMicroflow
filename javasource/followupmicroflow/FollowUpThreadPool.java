package followupmicroflow;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.mendix.core.Core;

public class FollowUpThreadPool {
	private static FollowUpThreadPool instance;
	private ThreadPoolExecutor pool;
	
	private FollowUpThreadPool() {
		Long threadPoolLimit = followupmicroflow.proxies.constants.Constants.getThreadPoolLimit();
		if(threadPoolLimit == null) {
			threadPoolLimit = new Long(0);
		}
		
		Long corePoolSize = followupmicroflow.proxies.constants.Constants.getThreadPooleCorePoolSize();
		if(corePoolSize == null) {
			corePoolSize = new Long(0);
		}
		
		this.pool = (ThreadPoolExecutor)Executors.newCachedThreadPool();
		if(corePoolSize.intValue() > 0) {
			this.pool.setCorePoolSize(corePoolSize.intValue());
		}
		if(threadPoolLimit.intValue() > 0) {
			this.pool.setMaximumPoolSize(max(threadPoolLimit.intValue(), corePoolSize.intValue()));
		}
		
		Core.getLogger("FollowUpMicroflow").info("ThreadPoolSize: " + pool.getPoolSize());
		Core.getLogger("FollowUpMicroflow").info("KeepAliveTime: " + pool.getKeepAliveTime(TimeUnit.MILLISECONDS) + "ms");
		Core.getLogger("FollowUpMicroflow").info("Maximum Pool Size: " + pool.getMaximumPoolSize());
	};
	
	/**
	 * @return Returns the maximum of two given integer values
	 */
	private int max(int intValue, int intValue2) {
		if(intValue > intValue2) {
			return intValue;
		}
		return intValue2;
	}

	/**
	 * Get the singleton instance of the FollowUpThreadPool
	 * 
	 * @return Instance of the FollowUpThreadPool
	 */
	public static FollowUpThreadPool getInstance() {
		if(instance == null) {
			Core.getLogger("FollowUpMicroflow").info("New Threadpool created.");
			instance = new FollowUpThreadPool();
		}
		return instance;
	}
	
	/**
	 * Execute an action in the thread pool
	 * 
	 * @param action An object that implements the runnable interface
	 */
	public void execute(Runnable action) {
		if(pool == null) {
			throw new RuntimeException("Threadpool not initialized.");
		}
		
		pool.execute(action);
	}
	
	/**
	 * @return Returns the number of active threads in the pool
	 */
	public int getActiveThreadCount() {
		return this.pool.getActiveCount();
	}
	
	/**
	 * @return Returns the current threadpool size
	 */
	public int getPoolSize() {
		return this.pool.getPoolSize();
	}
	
	/**
	 * @return Returns the size of the core pool. These threads will never be deleted
	 */
	public int getCorePoolSize() {
		return this.pool.getCorePoolSize();
	}
	
	/**
	 * @return Returns the maximum size of the threadpool
	 */
	public int getMaximumPoolSize() {
		return this.pool.getMaximumPoolSize();
	}
	
	/**
	 * @return Returns the number of jobs that are currently queued
	 */
	public int getQueueSize() {
		return this.pool.getQueue().size();
	}
}