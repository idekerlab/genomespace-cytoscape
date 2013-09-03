package cytoscape.genomespace.action;


import java.awt.event.ActionEvent;

import javax.swing.ImageIcon;

import org.cytoscape.application.swing.AbstractCyAction;

import cytoscape.genomespace.context.GenomeSpaceContext;


public class LoginToGenomeSpaceAction extends AbstractCyAction {
	private static final long serialVersionUID = 7577788473487659L;
	private final GenomeSpaceContext gsContext;
	
	public LoginToGenomeSpaceAction(GenomeSpaceContext gsContext, ImageIcon icon) {
		super("Login To GenomeSpace...");

		// Set the menu you'd like here.  Plugins don't need
		// to live in the Plugins menu, so choose whatever
		// is appropriate!
		setPreferredMenu("Apps.GenomeSpace");
		setMenuGravity(0.0f);
		putValue(SMALL_ICON, icon);

		this.gsContext = gsContext;
	}

	public void actionPerformed(ActionEvent e) {
		gsContext.login();	
	}
}
