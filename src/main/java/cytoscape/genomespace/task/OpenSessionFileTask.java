package cytoscape.genomespace.task;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.task.read.OpenSessionTaskFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableSetter;

public class OpenSessionFileTask extends AbstractTask {
	
	private final CyNetworkManager netManager;
	private final CyTableManager tableManager;
	private final OpenSessionTaskFactory openSessionTaskFactory;
	private final TunableSetter tunableSetter;
	private final Map<String, Object> tunableValues;
	
	public OpenSessionFileTask(CyNetworkManager netManager, CyTableManager tableManager, OpenSessionTaskFactory openSessionTaskFactory, TunableSetter tunableSetter, File file){
		this.netManager = netManager;
		this.tableManager = tableManager;
		this.openSessionTaskFactory = openSessionTaskFactory;
		this.tunableSetter = tunableSetter;
		
		tunableValues = new HashMap<String, Object>();
		tunableValues.put("file", file);
		tunableValues.put("loadSession", true);
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if (netManager.getNetworkSet().isEmpty() && tableManager.getAllTables(false).isEmpty())
			insertTasksAfterCurrentTask(tunableSetter.createTaskIterator(openSessionTaskFactory.createTaskIterator(),tunableValues));
		else
			insertTasksAfterCurrentTask(new OpenSessionFileWithWarningTask());
	}
	
	public final class OpenSessionFileWithWarningTask extends AbstractTask {
		
		@Tunable(description="<html>Current session (all networks and tables) will be lost.<br />Do you want to continue?</html>",
				 params="ForceSetDirectly=true;ForceSetTitle=Open Session")
		public boolean loadSession;
		
		@Override
		public void run(final TaskMonitor taskMonitor) throws Exception {
			if (loadSession)
				insertTasksAfterCurrentTask(tunableSetter.createTaskIterator(openSessionTaskFactory.createTaskIterator(),tunableValues));
		}
	}
}
