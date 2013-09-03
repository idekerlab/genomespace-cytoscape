
package cytoscape.genomespace.action;


import java.awt.event.ActionEvent;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.cytoscape.application.swing.AbstractCyAction;
import org.genomespace.atm.model.WebToolDescriptor;
import org.genomespace.client.exceptions.GSClientException;
import org.genomespace.client.ui.BrowserLauncher;
import org.genomespace.sws.SimpleWebServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LaunchToolAction extends AbstractCyAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7876567815814811534L;
	private static final Logger logger = LoggerFactory.getLogger(LaunchToolAction.class);

	private final WebToolDescriptor webTool;
	private final JFrame frame;

	public LaunchToolAction(WebToolDescriptor webTool, JFrame frame, ImageIcon icon) {
		super(webTool.getName());
		setPreferredMenu("Apps.GenomeSpace.Launch[1]");
		putValue(SMALL_ICON, icon);

		this.webTool = webTool;
		this.frame = frame;
	}

	public void actionPerformed(ActionEvent e) {
		try {
			String launchUrl = SimpleWebServer.makeLocalLoadUrl(webTool.getReloadPort(), null);
			if (!SimpleWebServer.timedGetUrl(launchUrl)) {
				launchUrl = webTool.getBaseUrl();
				logger.info("Launch URL is: "+ launchUrl);
				BrowserLauncher.openURL(launchUrl);
			}
		} catch (GSClientException ex) {
			logger.error("Launch failed", ex);
			JOptionPane.showMessageDialog(frame, "<html>Unable to launch tool. The GenomeSpace server is inaccessible or not responding properly at this time.<br/>" +
					"Please check your Internet connection and try again.</html>", "GenomeSpace Error",
			        JOptionPane.ERROR_MESSAGE);
		}
	}
}
