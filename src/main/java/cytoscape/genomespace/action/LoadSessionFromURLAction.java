package cytoscape.genomespace.action;

import java.io.File;
import java.net.URL;
import java.util.Map;

import javax.swing.JFrame;

import org.cytoscape.task.read.OpenSessionTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;
import org.genomespace.client.DataManagerClient;
import org.genomespace.client.GsSession;
import org.genomespace.datamanager.core.GSFileMetadata;
import org.genomespace.sws.GSLoadEvent;
import org.genomespace.sws.GSLoadEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cytoscape.genomespace.context.GenomeSpaceContext;
import cytoscape.genomespace.task.DeleteFileTask;
import cytoscape.genomespace.task.DownloadFileFromGenomeSpaceTask;
import cytoscape.genomespace.task.SetFrameSessionTitleTask;

public class LoadSessionFromURLAction implements GSLoadEventListener {
	private static final Logger logger = LoggerFactory.getLogger(LoadNetworkFromURLAction.class);
	private final DialogTaskManager dialogTaskManager;
	private final OpenSessionTaskFactory openSessionTaskFactory;
	private final GenomeSpaceContext gsContext;
	private final JFrame frame;
	
	public LoadSessionFromURLAction(DialogTaskManager dialogTaskManager, OpenSessionTaskFactory openSessionTaskFactory, GenomeSpaceContext gsContext, JFrame frame){
		this.dialogTaskManager = dialogTaskManager;
		this.openSessionTaskFactory = openSessionTaskFactory;
		this.gsContext = gsContext;
		this.frame = frame;
	}
	public void onLoadEvent(GSLoadEvent event) {
		Map<String,String> params = event.getParameters();
		String sessionURL = params.get("session");
		loadSession(sessionURL);
	}

	public void loadSession(String sessionURL) {
		if ( sessionURL == null )
			return;

		try {
			if(!gsContext.loginIfNotAlready()) return;
			GsSession session = gsContext.getSession();
			DataManagerClient dmc = gsContext.getSession().getDataManagerClient();
			GSFileMetadata fileMetadata = dmc.getMetadata(new URL(sessionURL));
			final String origFileName = fileMetadata.getName();
			final String extension = gsContext.getExtension(origFileName);
			File tempFile = File.createTempFile("tempGS", "." + extension);
			TaskIterator ti = new TaskIterator(new DownloadFileFromGenomeSpaceTask(gsContext, fileMetadata, tempFile, true));
			ti.append(openSessionTaskFactory.createTaskIterator(tempFile));
			ti.append(new SetFrameSessionTitleTask(frame, origFileName));
			dialogTaskManager.execute(ti);
			dialogTaskManager.execute(new TaskIterator(new DeleteFileTask(tempFile)));
		} catch ( Exception e ) { e.printStackTrace(); }
	}
}
