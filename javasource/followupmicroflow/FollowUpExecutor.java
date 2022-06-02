package followupmicroflow;

import java.util.HashMap;

import com.mendix.core.Core;
import com.mendix.core.CoreException;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixObject;

import followupmicroflow.proxies.ENU_FollowUpMicroflow_ExecutionState;
import followupmicroflow.proxies.FollowUpMicroflowLog;

public class FollowUpExecutor implements Runnable {
	private IContext context;
	private String microflow;
	private java.lang.Long timeout;
	private IMendixObject parameter;
	private FollowUpMicroflowLog fuml;
	
	public FollowUpExecutor(String microflow, IContext context, IMendixObject parameter, long timeout, FollowUpMicroflowLog fuml) {
		this.context = context;
		this.microflow = microflow;
		this.parameter = parameter;
		this.timeout = timeout;
		this.fuml = fuml;
	}
	
	public FollowUpExecutor(String microflow, IContext context, long timeout) {
		this(microflow, context, null, timeout, null);
	}
	
	@Override
	public void run() {
		Core.getLogger("FollowUpMicroflow").trace("Follow up executor started.");
		
		// Initialize sleepcounter needed for timeout
		long sleepcounter = 0;
		
		// Wait until the ActionStack is empty. Stops if ActionStack is empty or if timeout reached.
		while((timeout < 0 || timeout > sleepcounter / 1000) && context.getActionStack().empty() == false) {
			try {
				Thread.sleep(10);
				sleepcounter = sleepcounter + 10;
			} catch (InterruptedException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
		
		if(timeout >= 0 && timeout <= sleepcounter) {
			if(fuml != null) {
				fuml.setExecutionState(ENU_FollowUpMicroflow_ExecutionState.Timeout);
				try {
					Core.commit(Core.createSystemContext(), fuml.getMendixObject());
				} catch (CoreException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}
			
			throw new RuntimeException("Timeout. " + microflow + " not executed.");
		}
		
		// Writing the start log
		try {
			fuml = FUEHelpers.writeStartLog(Core.createSystemContext(), fuml);
		} catch (CoreException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage(), e);
		}
		
		// Execution of the FollowUpMicroflow
		try
		{
			if(parameter != null) {
				Core.execute(Core.createSystemContext(), microflow, true, parameter);
			}else {
				Core.execute(Core.createSystemContext(), microflow, true, new HashMap<String,Object>());
			}
		}
		catch (Exception e)
		{
			try {
				FUEHelpers.writeErrorLog(Core.createSystemContext(), fuml, e);
			} catch (CoreException e1) {
				e1.printStackTrace();
				throw new RuntimeException(e.getMessage(), e1);
			}
			
			throw new RuntimeException("Failed to run "+ microflow + " as follow up microflow. Exception: " + e.getMessage(), e);
		}
		
		// Write the finished log
		try {
			fuml = FUEHelpers.writeEndLog(Core.createSystemContext(), fuml);
		} catch (CoreException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage(), e);
		}
	}
}
