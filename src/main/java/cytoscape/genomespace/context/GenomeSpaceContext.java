package cytoscape.genomespace.context;


import java.awt.Dialog;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.application.swing.CySwingApplication;
import org.genomespace.client.GsSession;
import org.genomespace.client.ui.GSLoginDialog;
import org.genomespace.datamanager.core.GSDataFormat;
import org.genomespace.datamanager.core.GSFileMetadata;

import cytoscape.genomespace.CyActivator;


public final class GenomeSpaceContext {
    private final CySwingApplication cySwingApp;
    private final CyActivator cyActivator;
	private GsSession session = new GsSession();
	
	public GenomeSpaceContext(CySwingApplication cySwingApp, CyActivator cyActivator) {
		this.cySwingApp = cySwingApp;
		this.cyActivator = cyActivator;
	}

	public synchronized GsSession getSession() {
		return session;
	}
	
	public synchronized boolean loginIfNotAlready() {
		if(session.isLoggedIn()) return true;
		else return login();
	}

	public synchronized boolean login() {
		final GSLoginDialog loginDialog =
			new GSLoginDialog(cySwingApp.getJFrame(), Dialog.ModalityType.APPLICATION_MODAL);
		loginDialog.setVisible(true);
		if(loginDialog.getGsSession().isLoggedIn()) {
			session = loginDialog.getGsSession();
			cyActivator.updateMenus(session);
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

