package cytoscape.genomespace.task;

import java.io.File;

import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TunableSetter;

public class ImportFileTaskFactory implements FileTaskFactory {
	
	private final TaskFactory taskFactory;
	private final TunableSetter tunableSetter;
	
	public ImportFileTaskFactory(TaskFactory taskFactory, TunableSetter tunableSetter) {
		this.taskFactory = taskFactory;
		this.tunableSetter = tunableSetter;
	}
	
	public TaskIterator createTaskIterator(File file) {
		return new TaskIterator(new ImportFileTask(taskFactory, tunableSetter, file));
	}

}
