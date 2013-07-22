package cytoscape.genomespace.task;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TunableSetter;

public class BasicFileTask extends AbstractTask {
	
	private final TaskFactory taskFactory;
	private final TunableSetter tunableSetter;
	private final File file;
	
	public BasicFileTask(TaskFactory taskFactory, TunableSetter tunableSetter, File file){
		this.taskFactory = taskFactory;
		this.tunableSetter = tunableSetter;
		this.file = file;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		TaskIterator ti = taskFactory.createTaskIterator();
		Task task = ti.next();
		Map<String, Object> tunableValues = new HashMap<String, Object>();
		tunableValues.put("file", file);
		tunableSetter.applyTunables(task, tunableValues);
		task.run(taskMonitor);
		insertTasksAfterCurrentTask(ti);
	}

}
