// This file was generated by Mendix Modeler.
//
// WARNING: Only the following code will be retained when actions are regenerated:
// - the import list
// - the code between BEGIN USER CODE and END USER CODE
// - the code between BEGIN EXTRA CODE and END EXTRA CODE
// Other code you write will be lost the next time you deploy the project.
// Special characters, e.g., é, ö, à, etc. are supported in comments.

package followupmicroflow.actions;

import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.webui.CustomJavaAction;
import followupmicroflow.FUEHelpers;
import followupmicroflow.FollowUpExecutor;
import followupmicroflow.FollowUpThreadPool;
import followupmicroflow.proxies.FollowUpMicroflowLog;
import com.mendix.systemwideinterfaces.core.IMendixObject;

/**
 * Use this action to register a follow up microflow. This follow up microflow will run after the calling microflow cascade finished (It waits for the ActionStack to be empty).
 * The follow up microflow will run in it's own transaction. While it is running, other users are alreay able to see the result of the calling microflow. If the follow up microflow throws an exception, the calling microflows changes will not be rolled back (Keep that in mind when using this).
 * 
 * You can pass an object as parameter, define a timeout and enable or disable logging.
 */
public class RegisterFollowUpMicroflow extends CustomJavaAction<java.lang.Boolean>
{
	private java.lang.String followUpMicroflow;
	private java.lang.Long timeout;
	private IMendixObject parameter;
	private java.lang.Boolean EnableLogging;

	public RegisterFollowUpMicroflow(IContext context, java.lang.String followUpMicroflow, java.lang.Long timeout, IMendixObject parameter, java.lang.Boolean EnableLogging)
	{
		super(context);
		this.followUpMicroflow = followUpMicroflow;
		this.timeout = timeout;
		this.parameter = parameter;
		this.EnableLogging = EnableLogging;
	}

	@Override
	public java.lang.Boolean executeAction() throws Exception
	{
		// BEGIN USER CODE
		
		// Write initial log if enabled
		FollowUpMicroflowLog fuml = FUEHelpers.writeInitialLog(getContext(), parameter, followUpMicroflow, EnableLogging);
		
		// Set timeout to -1 if empty (Easier to process in following actions)
		if(timeout == null) {
			timeout = new Long(-1);
		}
		
		
		// Start background process that waits for the ActionStack to be empty before executing the follow up microflow
		FollowUpThreadPool.getInstance().execute(new FollowUpExecutor(followUpMicroflow, getContext(), parameter, timeout, fuml));
		
		return true;
		// END USER CODE
	}

	/**
	 * Returns a string representation of this action
	 */
	@Override
	public java.lang.String toString()
	{
		return "RegisterFollowUpMicroflow";
	}

	// BEGIN EXTRA CODE
	// END EXTRA CODE
}
