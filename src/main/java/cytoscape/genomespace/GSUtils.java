package cytoscape.genomespace;


import java.awt.Dialog;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.cytoscape.property.CyProperty;
import org.genomespace.client.ConfigurationUrls;
import org.genomespace.client.DataManagerClient;
import org.genomespace.client.GsSession;
import org.genomespace.client.exceptions.AuthorizationException;
import org.genomespace.client.exceptions.GSClientException;
import org.genomespace.client.ui.GSLoginDialog;
import org.genomespace.datamanager.core.GSDataFormat;
import org.genomespace.datamanager.core.GSFileMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


final class GSUtils {
    static final Logger logger = LoggerFactory.getLogger("CyUserMessages");
	
    private final CyProperty<Properties> cytoscapePropertiesServiceRef;
    private final JFrame frame;
	private GsSession session = null;
	
	public GSUtils(CyProperty<Properties> cytoscapePropertiesServiceRef, JFrame frame) {
		this.cytoscapePropertiesServiceRef = cytoscapePropertiesServiceRef;
		this.frame = frame;
		
		initSession();
	}
	
	private void initSession() {
		try {
			String gsenv = cytoscapePropertiesServiceRef.getProperties().getProperty("genomespace.environment","test").toString();
			ConfigurationUrls.init(gsenv);
			session = new GsSession();
		} catch (Exception e) {
			throw new GSClientException("failed to create GenomeSpace session", e);
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

	public synchronized void reloginToGenomeSpace() {
		if ( session != null && session.isLoggedIn() ) {
			try { 
				session.logout();
				logger.info("Logged out of GenomeSpace");
			} catch (Exception e) { }
		}
		loginToGenomeSpace();
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
				session.login(userName, password);
				logger.info("Logged in to GenomeSpace as: " + userName);
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

	public File downloadToTempFile(String urlString) {
		return downloadToTempFile(urlString,null);
	}

	public File downloadToTempFile(String urlString, GSDataFormat format) {
		InputStream is = null;
		OutputStream os = null;
		File tempFile = null;
		try {
			URL url = new URL(urlString);
			DataManagerClient dmc = getSession().getDataManagerClient();

			if ( format == null )
				is = dmc.getInputStream(url);
			else
				is = dmc.getInputStream(dmc.getFileUrl(url,format.getUrl()));

			tempFile = File.createTempFile("tempGS","." + getExtension(url.toString()));
			os =new FileOutputStream(tempFile);
			byte buf[] = new byte[1024];
			int len;
			while( (len = is.read(buf)) > 0 )
				os.write(buf,0,len);
		} catch (Exception e) {
			throw new IllegalArgumentException("failed to load url: " + urlString, e);
		} finally {
			try { 
				if ( is != null )
					is.close();
				if ( os != null )
					os.close();
			} catch (IOException ioe) {
				throw new IllegalArgumentException("couldn't even close streams", ioe);
			}
		}

		return tempFile;
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

