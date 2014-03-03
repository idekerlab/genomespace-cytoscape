package cytoscape.genomespace.task;

import java.io.File;

import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.task.read.OpenSessionTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TunableSetter;

public class OpenSessionFileTaskFactory implements FileTaskFactory {
	
	private final CyNetworkManager netManager;
	private final CyTableManager tableManager;
	private final OpenSessionTaskFactory openSessionTaskFactory;
	private final TunableSetter tunableSetter;
	
	public OpenSessionFileTaskFactory(CyNetworkManager netManager, CyTableManager tableManager, OpenSessionTaskFactory openSessionTaskFactory, TunableSetter tunableSetter) {
		this.netManager = netManager;
		this.tableManager = tableManager;
		this.openSessionTaskFactory = openSessionTaskFactory;
		this.tunableSetter = tunableSetter;
	}

	public TaskIterator createTaskIterator(File file) {
		return new TaskIterator(new OpenSessionFileTask(netManager, tableManager, openSessionTaskFactory, tunableSetter, file));
	}

}
