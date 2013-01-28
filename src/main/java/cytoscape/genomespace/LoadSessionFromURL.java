package cytoscape.genomespace;

import java.io.File;
import java.util.Map;

import org.cytoscape.task.read.OpenSessionTaskFactory;
import org.cytoscape.work.swing.DialogTaskManager;
import org.genomespace.sws.GSLoadEvent;
import org.genomespace.sws.GSLoadEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadSessionFromURL implements GSLoadEventListener {
	private static final Logger logger = LoggerFactory.getLogger(LoadNetworkFromURL.class);
	private final DialogTaskManager dialogTaskManager;
	private final OpenSessionTaskFactory openSessionTaskFactory;
	private final GSUtils gsUtils;
	
	public LoadSessionFromURL(DialogTaskManager dialogTaskManager, OpenSessionTaskFactory openSessionTaskFactory, GSUtils gsUtils){
		this.dialogTaskManager = dialogTaskManager;
		this.openSessionTaskFactory = openSessionTaskFactory;
		this.gsUtils = gsUtils;
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
			File tempFile = gsUtils.downloadToTempFile(sessionURL); 
			dialogTaskManager.execute(openSessionTaskFactory.createTaskIterator(tempFile));
		} catch ( Exception e ) { e.printStackTrace(); }
	}
}
