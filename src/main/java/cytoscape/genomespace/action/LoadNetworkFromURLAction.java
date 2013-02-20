package cytoscape.genomespace.action;

import java.io.File;
import java.util.Map;

import org.cytoscape.task.read.LoadNetworkFileTaskFactory;
import org.cytoscape.work.swing.DialogTaskManager;
import org.genomespace.datamanager.core.GSDataFormat;
import org.genomespace.sws.GSLoadEvent;
import org.genomespace.sws.GSLoadEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cytoscape.genomespace.GSUtils;

public class LoadNetworkFromURLAction implements GSLoadEventListener {
	private static final Logger logger = LoggerFactory.getLogger(LoadNetworkFromURLAction.class);
	private final DialogTaskManager dialogTaskManager;
	private final LoadNetworkFileTaskFactory loadNetworkFileTaskFactory;
	private final GSUtils gsUtils;
	
	public LoadNetworkFromURLAction(DialogTaskManager dialogTaskManager, LoadNetworkFileTaskFactory loadNetworkFileTaskFactory, GSUtils gsUtils) {
		this.dialogTaskManager = dialogTaskManager;
		this.loadNetworkFileTaskFactory = loadNetworkFileTaskFactory;
		this.gsUtils = gsUtils;
	}
	
	public void onLoadEvent(GSLoadEvent event) {
		Map<String,String> params = event.getParameters();
		String netURL = params.get("network");
		loadNetwork(netURL);
	}

	public void loadNetwork(String netURL) {
		if ( netURL == null )
			return;

		try {

			final String ext = gsUtils.getExtension(netURL);
            GSDataFormat dataFormat = null; 
			if ( ext != null && ext.equalsIgnoreCase("adj") )
				dataFormat = gsUtils.findConversionFormat(gsUtils.getSession().getDataManagerClient().listDataFormats(), "xgmml");

			File tmp = gsUtils.downloadToTempFile(netURL,dataFormat);
			dialogTaskManager.execute(loadNetworkFileTaskFactory.createTaskIterator(tmp));
		} catch ( Exception e ) { e.printStackTrace(); }
	}
}
