package followupmicroflow;

import java.util.Date;
import java.util.List;

import com.mendix.core.Core;
import com.mendix.core.CoreException;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixObject;

import followupmicroflow.proxies.ENU_FollowUpMicroflow_ExecutionState;
import followupmicroflow.proxies.ErrorLog;
import followupmicroflow.proxies.FollowUpMicroflowLog;
import followupmicroflow.proxies.Microflow;

public class FUEHelpers {
	public static Microflow getMicroflow(IContext context, String microflowName) throws CoreException {
		List<IMendixObject> mfList = Core.retrieveXPathQuery(context, "//FollowUpMicroflow.Microflow[MicroflowName = \"" + microflowName + "\"]");
		Microflow mf = null;
		if(mfList.isEmpty()) {
			mf = new Microflow(context);
			mf.setMicroflowName(microflowName);
		}else {
			mf = Microflow.initialize(context, mfList.iterator().next());
		}
		return mf;
	}
	
	/**
	 * Creates FollowUpMicroflowLog object if enableLogging is set to true. It will also add initial information to the log and commit it to the database
	 * 
	 * @param context the IContext in which the action is running
	 * @param parameter Object passed to the follow up microflow. This can be any Object
	 * @param followUpMicroflow The name of the follow up microflow
	 * @param enableLogging if true, the log is created with all the information. If false, the method will return null
	 * @return returns a FollowUpMicroflowLog object if logging is enabled. Otherwise it will just return null.
	 * @throws CoreException
	 */
	public static FollowUpMicroflowLog writeInitialLog(IContext context, IMendixObject parameter, String followUpMicroflow, boolean enableLogging) throws CoreException {
		FollowUpMicroflowLog fuml = null;
		
		if(enableLogging) {
			IContext sysContext = Core.createSystemContext();
			fuml = new FollowUpMicroflowLog(sysContext);
			fuml.setRegisteredTimeStamp(new Date());
			
			if(parameter != null) {
				fuml.setParameterID(parameter.getId().toLong());
				fuml.setHasParameter(true);
				fuml.setParameterIsPersistable(parameter.getMetaObject().isPersistable());
			}
			
			// Retrieve microflow logs
			Microflow fuMicroflow = FUEHelpers.getMicroflow(sysContext, followUpMicroflow);
			Microflow callingMicroflow = FUEHelpers.getMicroflow(sysContext, context.getActionStack().elementAt(context.getActionStack().size()-2).getActionName());
			fuml.setFollowUpMicroflowLog_FollowUpMicroflow(fuMicroflow);
			fuml.setFollowUpMicroflowLog_CallingMicroflow(callingMicroflow);
			
			Core.commit(sysContext, callingMicroflow.getMendixObject());
			Core.commit(sysContext, fuMicroflow.getMendixObject());
			Core.commit(sysContext, fuml.getMendixObject());
		}
		
		return fuml;
	}
	
	/**
	 * Sets the start timestamp in the FollowUpMicroflowLog and sets the execution status to running (Will be set to executed when the execution is finished)
	 * 
	 * @param context the IContext in which the action is running
	 * @param fuml FollowUpMicroflowLog to add the information
	 * @return returns the changed Log object
	 * @throws CoreException 
	 */
	public static FollowUpMicroflowLog writeStartLog(IContext context, FollowUpMicroflowLog fuml) throws CoreException {
		if(fuml != null) {
			fuml.setExecutionStartTimeStamp(new Date());
			fuml.setExecutionState(ENU_FollowUpMicroflow_ExecutionState.Running);
			Core.commit(context, fuml.getMendixObject());
		}
		
		return fuml;
	}
	
	/**
	 * Sets the execution status to error
	 * 
	 * @param context the IContext in which the action is running
	 * @param fuml FollowUpMicroflowLog to add the information
	 * @return returns the changed Log object
	 * @throws CoreException 
	 */
	public static FollowUpMicroflowLog writeErrorLog(IContext context, FollowUpMicroflowLog fuml, Exception e) throws CoreException {
		if(fuml != null) {
			fuml.setExecutionState(ENU_FollowUpMicroflow_ExecutionState.Error);
			Core.commit(context, fuml.getMendixObject());
			
			ErrorLog errorLog = new ErrorLog(context);
			errorLog.setErrorLog_FollowUpMicroflowLog(fuml);
			errorLog.setTimeStamp(new Date());
			errorLog.setErrorMessage(e.getMessage());
			Core.commit(context, errorLog.getMendixObject());
		}
		
		return fuml;
	}
	
	/**
	 * Sets the end timestamp in the FollowUpMicroflowLog and sets the execution status to executed
	 * 
	 * @param context the IContext in which the action is running
	 * @param fuml FollowUpMicroflowLog to add the information
	 * @return returns the changed Log object
	 * @throws CoreException 
	 */
	public static FollowUpMicroflowLog writeEndLog(IContext context, FollowUpMicroflowLog fuml) throws CoreException {
		if(fuml != null) {
			fuml.setExecutionFinishedTimeStamp(new Date());
			fuml.setExecutionState(ENU_FollowUpMicroflow_ExecutionState.Executed);
			Core.commit(context, fuml.getMendixObject());
		}
		
		return fuml;
	}
	
	/**
	 * Checks if an object with the given id exists in the database
	 * 
	 * @param context the IContext in which the action is running
	 * @param objectID guid of the object
	 * @return true if object was found, otherwise false
	 * @throws CoreException
	 */
	public static boolean isObjectExisting(IContext context, long objectID) throws CoreException {
		return Core.retrieveId(context, Core.createMendixIdentifier(objectID)) != null;
	}
}
