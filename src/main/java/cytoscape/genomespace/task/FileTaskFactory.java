package cytoscape.genomespace.task;

import java.io.File;

import org.cytoscape.work.TaskIterator;

public interface FileTaskFactory {
	public TaskIterator createTaskIterator(File file);
}
