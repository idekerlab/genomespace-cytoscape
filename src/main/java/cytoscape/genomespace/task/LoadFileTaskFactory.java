package cytoscape.genomespace.task;

import java.io.File;

import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TunableSetter;

public class LoadFileTaskFactory {
	
	private final TaskFactory taskFactory;
	private final TunableSetter tunableSetter;
	
	public LoadFileTaskFactory(TaskFactory taskFactory, TunableSetter tunableSetter) {
		this.taskFactory = taskFactory;
		this.tunableSetter = tunableSetter;
	}
	
	public TaskIterator createTaskIterator(File file) {
		return new TaskIterator(new LoadFileTask(taskFactory, tunableSetter, file));
	}

}
