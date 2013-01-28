package cytoscape.genomespace;


import java.awt.event.ActionEvent;

import org.cytoscape.application.swing.AbstractCyAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LoginToGenomeSpace extends AbstractCyAction {
	private static final long serialVersionUID = 7577788473487659L;
	private static final Logger logger = LoggerFactory.getLogger(LoginToGenomeSpace.class);
	private final GSUtils gsUtils;
	
	public LoginToGenomeSpace(GSUtils gsUtils) {
		super("Login To GenomeSpace...");

		// Set the menu you'd like here.  Plugins don't need
		// to live in the Plugins menu, so choose whatever
		// is appropriate!
		setPreferredMenu("File.GenomeSpace");
		this.gsUtils = gsUtils;
	}

	public void actionPerformed(ActionEvent e) {
		gsUtils.reloginToGenomeSpace();	
	}
}
