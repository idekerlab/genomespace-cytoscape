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

import cytoscape.genomespace.task.DeleteFileTask;
import cytoscape.genomespace.task.DownloadFileFromGenomeSpaceTask;
import cytoscape.genomespace.task.SetFrameSessionTitleTask;
import cytoscape.genomespace.util.GSUtils;

public class LoadSessionFromURLAction implements GSLoadEventListener {
	private static final Logger logger = LoggerFactory.getLogger(LoadNetworkFromURLAction.class);
	private final DialogTaskManager dialogTaskManager;
	private final OpenSessionTaskFactory openSessionTaskFactory;
	private final GSUtils gsUtils;
	private final JFrame frame;
	
	public LoadSessionFromURLAction(DialogTaskManager dialogTaskManager, OpenSessionTaskFactory openSessionTaskFactory, GSUtils gsUtils, JFrame frame){
		this.dialogTaskManager = dialogTaskManager;
		this.openSessionTaskFactory = openSessionTaskFactory;
		this.gsUtils = gsUtils;
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
			GsSession session = gsUtils.getSession();
			if(!session.isLoggedIn()) return;
			DataManagerClient dmc = gsUtils.getSession().getDataManagerClient();
			GSFileMetadata fileMetadata = dmc.getMetadata(new URL(sessionURL));
			final String origFileName = fileMetadata.getName();
			final String extension = gsUtils.getExtension(origFileName);
			File tempFile = File.createTempFile("tempGS", "." + extension);
			TaskIterator ti = new TaskIterator(new DownloadFileFromGenomeSpaceTask(gsUtils, fileMetadata, tempFile, true));
			ti.append(openSessionTaskFactory.createTaskIterator(tempFile));
			ti.append(new SetFrameSessionTitleTask(frame, origFileName));
			dialogTaskManager.execute(ti);
			dialogTaskManager.execute(new TaskIterator(new DeleteFileTask(tempFile)));
		} catch ( Exception e ) { e.printStackTrace(); }
	}
}
