package cytoscape.genomespace;


import java.awt.Dialog;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.genomespace.atm.model.WebToolDescriptor;
import org.genomespace.client.ConfigurationUrls;
import org.genomespace.client.GsSession;
import org.genomespace.client.exceptions.AuthorizationException;
import org.genomespace.client.exceptions.GSClientException;
import org.genomespace.client.ui.GSLoginDialog;
import org.genomespace.datamanager.core.GSDataFormat;
import org.genomespace.datamanager.core.GSFileMetadata;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cytoscape.genomespace.action.LaunchToolAction;


public final class GSUtils implements BundleListener {
    static final Logger logger = LoggerFactory.getLogger("CyUserMessages");
    private final CyProperty<Properties> cytoscapePropertiesServiceRef;
    private final CyServiceRegistrar cyServiceRegistrar;
    private final JFrame frame;
    private final Set<LaunchToolAction> launchToolActions;
	private GsSession session = null;
	
	public GSUtils(CyProperty<Properties> cytoscapePropertiesServiceRef, CyServiceRegistrar cyServiceRegistrar, JFrame frame) {
		this.cytoscapePropertiesServiceRef = cytoscapePropertiesServiceRef;
		this.cyServiceRegistrar = cyServiceRegistrar;
		this.frame = frame;
		this.launchToolActions = new HashSet<LaunchToolAction>();
		initSession();
	}
	
	private void initSession() {
		try {
			String gsenv = cytoscapePropertiesServiceRef.getProperties().getProperty("genomespace.environment","dev").toString();
			ConfigurationUrls.init(gsenv);
			session = new GsSession();
			if(loggedInToGS()){
				unregisterLaunchToolActions();
				registerLaunchToolActions();
			}
		} catch (Exception e) {
			throw new GSClientException("failed to create GenomeSpace session", e);
		}
	}
	
	private void registerLaunchToolActions() {
		try {
			for ( WebToolDescriptor webTool : session.getAnalysisToolManagerClient().getWebTools() ) {
				if ( webTool.getName().equalsIgnoreCase("cytoscape") )
					continue;
				LaunchToolAction action = new LaunchToolAction(webTool, frame);
				cyServiceRegistrar.registerAllServices(action, new Properties());
				launchToolActions.add(action);
			}
		} catch (Exception ex) { 
			logger.warn("problem finding web tools", ex); 
		}
	}
	
	private void unregisterLaunchToolActions() {
		try {
			for(Iterator<LaunchToolAction> i = launchToolActions.iterator(); i.hasNext();){
				cyServiceRegistrar.unregisterAllServices(i.next());
				i.remove();
			}
		} 
		catch(Exception ex) {
			logger.warn("failed to unregister web tool service", ex); 
		}
	}

	public synchronized boolean loggedInToGS() {
		return (session != null && session.isLoggedIn()); 
	}

	public synchronized GsSession getSession() {
		if (session == null ) {
			initSession();
		}

		if (!session.isLoggedIn()) {
			try {
				if (!loginToGenomeSpace())
					throw new GSClientException("failed to login!", null);
			} catch (Exception e) {
				throw new GSClientException("failed to login", e);
			}
		}

		return session;
	}

	public synchronized boolean loginToGenomeSpace() {
		for (;;) {
			final GSLoginDialog loginDialog =
				new GSLoginDialog(frame, Dialog.ModalityType.APPLICATION_MODAL);
			loginDialog.setVisible(true);
			final String userName = loginDialog.getUsername();
			final String password = loginDialog.getPassword();
			if (userName == null || userName.isEmpty() || password == null || password.isEmpty()) {
				return false;
			}

			try {
				if(session.isLoggedIn()) {
					session.logout();
					logger.info("Logged out of GenomeSpace");
				}
				unregisterLaunchToolActions();
				session.login(userName, password);
				logger.info("Logged in to GenomeSpace as: " + userName);
				registerLaunchToolActions();
				return true;
			} catch (final AuthorizationException e) {
				JOptionPane.showMessageDialog(frame,
							      "Invalid user name or password!",
							      "Login Error",
							      JOptionPane.ERROR_MESSAGE);
				continue;
			} catch (final Exception e) {
				JOptionPane.showMessageDialog(frame,
							      e.getMessage(),
							      "Login Error",
							      JOptionPane.ERROR_MESSAGE);
				return false;
			}
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

	public void bundleChanged(BundleEvent event) {
		if(event.getType() == BundleEvent.STOPPED) {
			unregisterLaunchToolActions();
		}
	}

}

