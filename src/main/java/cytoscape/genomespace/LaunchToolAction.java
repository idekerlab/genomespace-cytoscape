
package cytoscape.genomespace;


import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.genomespace.atm.model.WebToolDescriptor;
import org.genomespace.client.ui.BrowserLauncher;
import org.genomespace.sws.SimpleWebServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LaunchToolAction extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7876567815814811534L;
	private static final Logger logger = LoggerFactory.getLogger(LaunchToolAction.class);

	private final WebToolDescriptor webTool;
	private final JFrame frame;

	public LaunchToolAction(WebToolDescriptor webTool, JFrame frame) {
		super(webTool.getName());
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
		} catch (Exception ex) {
			logger.error("Launch failed", ex);
			JOptionPane.showMessageDialog(frame,
						      ex.getMessage(), "GenomeSpace Error",
						      JOptionPane.ERROR_MESSAGE);
		}
	}
}
