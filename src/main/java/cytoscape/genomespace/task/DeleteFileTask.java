package cytoscape.genomespace.task;

import java.io.File;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class DeleteFileTask extends AbstractTask {
	
	private File file;
	
	public DeleteFileTask(File file) {
		this.file = file;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		file.delete();
	}

}
