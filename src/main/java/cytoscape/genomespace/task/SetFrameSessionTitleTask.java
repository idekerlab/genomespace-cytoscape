package cytoscape.genomespace.task;

import javax.swing.JFrame;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class SetFrameSessionTitleTask extends AbstractTask {
	
	private JFrame frame;
	private String sessionFileName;
	
	public SetFrameSessionTitleTask(JFrame frame, String sessionFileName) {
		this.frame = frame;
		this.sessionFileName = sessionFileName;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		frame.setTitle("Session: " + sessionFileName);
	}

}
