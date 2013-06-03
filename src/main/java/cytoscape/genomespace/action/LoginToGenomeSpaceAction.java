package cytoscape.genomespace.action;


import java.awt.event.ActionEvent;

import org.cytoscape.application.swing.AbstractCyAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cytoscape.genomespace.GSUtils;


public class LoginToGenomeSpaceAction extends AbstractCyAction {
	private static final long serialVersionUID = 7577788473487659L;
	private static final Logger logger = LoggerFactory.getLogger(LoginToGenomeSpaceAction.class);
	private final GSUtils gsUtils;
	
	public LoginToGenomeSpaceAction(GSUtils gsUtils) {
		super("Login To GenomeSpace...");

		// Set the menu you'd like here.  Plugins don't need
		// to live in the Plugins menu, so choose whatever
		// is appropriate!
		setPreferredMenu("File.GenomeSpace[999]");
		this.gsUtils = gsUtils;
	}

	public void actionPerformed(ActionEvent e) {
		gsUtils.loginToGenomeSpace();	
	}
}
