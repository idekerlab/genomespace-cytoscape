package cytoscape.genomespace.event;

import java.io.File;
import java.net.URL;
import java.util.Map;

import javax.swing.JFrame;

import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;
import org.genomespace.client.DataManagerClient;
import org.genomespace.client.GsSession;
import org.genomespace.datamanager.core.GSDataFormat;
import org.genomespace.datamanager.core.GSFileMetadata;
import org.genomespace.sws.GSLoadEvent;
import org.genomespace.sws.GSLoadEventListener;

import cytoscape.genomespace.context.GenomeSpaceContext;
import cytoscape.genomespace.task.DeleteFileTask;
import cytoscape.genomespace.task.DownloadFileFromGenomeSpaceTask;
import cytoscape.genomespace.task.LoadFileTaskFactory;
import cytoscape.genomespace.task.SetFrameSessionTitleTask;

public class CytoscapeGSURLHandler implements GSLoadEventListener {
	private final DialogTaskManager dialogTaskManager;
	private final LoadFileTaskFactory loadNetworkFileTaskFactory;
	private final LoadFileTaskFactory loadSessionFileTaskFactory;
	private final JFrame frame;
	private final GenomeSpaceContext gsContext;
	
	public CytoscapeGSURLHandler(DialogTaskManager dialogTaskManager, LoadFileTaskFactory loadNetworkFileTaskFactory, 
			LoadFileTaskFactory loadSessionFileTaskFactory, JFrame frame, GenomeSpaceContext gsContext) {
		this.dialogTaskManager = dialogTaskManager;
		this.loadNetworkFileTaskFactory = loadNetworkFileTaskFactory;
		this.loadSessionFileTaskFactory = loadSessionFileTaskFactory;
		this.frame = frame;
		this.gsContext = gsContext;
	}
	
	public void onLoadEvent(GSLoadEvent event) {
		Map<String,String> params = event.getParameters();
		loadFromURL(params.get("network"));
	}

	public void loadFromURL(String url) {
		if ( url == null )
			return;

		try {
			if(!gsContext.loginIfNotAlready()) return;
			GsSession session = gsContext.getSession();
			DataManagerClient dmc = session.getDataManagerClient();
			final String extension = gsContext.getExtension(url);
            GSDataFormat dataFormat = null; 
			if ( extension != null && extension.equalsIgnoreCase("adj") )
				dataFormat = gsContext.findConversionFormat(gsContext.getSession().getDataManagerClient().listDataFormats(), "xgmml");
			GSFileMetadata fileMetadata = dmc.getMetadata(new URL(url));
			final String fileName = fileMetadata.getName();
			File tempFile = new File(System.getProperty("java.io.tmpdir"), fileName);
			TaskIterator ti = new TaskIterator(new DownloadFileFromGenomeSpaceTask(session, fileMetadata, dataFormat, tempFile, true));
			if(extension.equalsIgnoreCase("cys")) {
				ti.append(loadSessionFileTaskFactory.createTaskIterator(tempFile));
				ti.append(new SetFrameSessionTitleTask(frame, fileName));
			}
			else {
				ti.append(loadNetworkFileTaskFactory.createTaskIterator(tempFile));
			}
			dialogTaskManager.execute(ti);
			dialogTaskManager.execute(new TaskIterator(new DeleteFileTask(tempFile)));
		} catch ( Exception e ) { e.printStackTrace(); }
	}
}
