package cytoscape.genomespace.util;


import java.awt.Dialog;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.swing.JMenu;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.genomespace.atm.model.WebToolDescriptor;
import org.genomespace.client.GsSession;
import org.genomespace.client.ui.GSLoginDialog;
import org.genomespace.datamanager.core.GSDataFormat;
import org.genomespace.datamanager.core.GSFileMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cytoscape.genomespace.action.LaunchToolAction;


public final class GSUtils {
    static final Logger logger = LoggerFactory.getLogger("CyUserMessages");
    private final CySwingApplication cySwingApp;
	private GsSession session = new GsSession();
	
	public GSUtils(CySwingApplication cySwingApp) {
		this.cySwingApp = cySwingApp;
	}
	
	
//	private void updateLaunchToolActions() {
//		for(Iterator<LaunchToolAction> i = launchToolActions.iterator(); i.hasNext();){
//			cyServiceRegistrar.unregisterAllServices(i.next());
//			i.remove();
//		}
//		try {
//			if(session.isLoggedIn())
//			for ( WebToolDescriptor webTool : session.getAnalysisToolManagerClient().getWebTools() ) {
//				if ( webTool.getName().equalsIgnoreCase("cytoscape") )
//					continue;
//				LaunchToolAction action = new LaunchToolAction(webTool, cySwingApp.getJFrame());
//				cyServiceRegistrar.registerAllServices(action, new Properties());
//				launchToolActions.add(action);
//			}
//		} catch (Exception ex) { 
//			logger.warn("problem finding web tools", ex); 
//		}
//		JMenu launchMenu = cySwingApp.getJMenu("File.GenomeSpace[999].Launch");
//		if ((launchMenu != null)) {
//			launchMenu.setEnabled(launchMenu.getItemCount() > 0);
//		}
//	}

	public synchronized GsSession getSession() {
		if(!session.isLoggedIn())
			loginToGenomeSpace();
		return session;
	}

	public synchronized boolean loginToGenomeSpace() {
		final GSLoginDialog loginDialog =
			new GSLoginDialog(cySwingApp.getJFrame(), Dialog.ModalityType.APPLICATION_MODAL);
		loginDialog.setVisible(true);
		if(loginDialog.getGsSession().isLoggedIn()) {
			session = loginDialog.getGsSession();
			return true;
		}
		else {
			return false;
		}
	}

	public Map<String,GSFileMetadata> getFileNameMap(Collection<GSFileMetadata> l) {
		Map<String,GSFileMetadata> nm = new HashMap<String,GSFileMetadata>();
		for ( GSFileMetadata f : l )
			nm.put(f.getName(), f);

		return nm;
	}

	public GSDataFormat findConversionFormat(Collection<GSDataFormat> availableFormats, String targetExt) {
		if ( targetExt == null || targetExt.equals("") || availableFormats == null )
			return null;

		for ( GSDataFormat format : availableFormats ) 
			if ( targetExt.equalsIgnoreCase( format.getFileExtension() ) )
				return format;

		return null;
	}

	public String getExtension(final String fileName) {
		final int lastDotPos = fileName.lastIndexOf('.');
		return (lastDotPos == -1 ? fileName : fileName.substring(lastDotPos + 1)).toLowerCase();
	}

	public String getNetworkTitle(final String fileName) {
		final int lastDotPos = fileName.lastIndexOf('.');
		return lastDotPos == -1 ? fileName : fileName.substring(0, lastDotPos);
	}

    // Returns the directory component of "path"
    public String dirName(final String path) {
        final int lastSlashPos = path.lastIndexOf('/');
        return path.substring(0, lastSlashPos);
    }


    // Returns the basename component of "path"
    public String baseName(final String path) {
        final int lastSlashPos = path.lastIndexOf('/');
        return lastSlashPos == -1 ? path : path.substring(lastSlashPos + 1);
    }

}

