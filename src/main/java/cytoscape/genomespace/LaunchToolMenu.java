

package cytoscape.genomespace;


import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.genomespace.atm.model.WebToolDescriptor;
import org.genomespace.client.GsSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LaunchToolMenu extends JMenu implements MenuListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3557303633299030041L;
	private static final Logger logger = LoggerFactory.getLogger(LaunchToolMenu.class);
	private final GSUtils gsUtils;
	private final JFrame frame;
	
	public LaunchToolMenu(JMenu parent, GSUtils gsUtils, JFrame frame) {
		super("Launch");
		parent.addMenuListener(this);
		this.gsUtils = gsUtils;
		this.frame = frame;
	}

	public void menuCanceled(MenuEvent e) { 
		removeAll();
	}

	public void menuDeselected(MenuEvent e) { 
		removeAll();
	} 

	public void menuSelected(MenuEvent e) { 
		if ( gsUtils.loggedInToGS() ) {
			setEnabled(true);
			try {
				GsSession session = gsUtils.getSession();
				for ( WebToolDescriptor webTool : session.getAnalysisToolManagerClient().getWebTools() ) {
					if ( webTool.getName().equalsIgnoreCase("cytoscape") )
						continue;
					LaunchToolAction action = new LaunchToolAction(webTool, frame);
					add(new JMenuItem(action));
				}
			} catch (Exception ex) { 
				logger.warn("problem finding web tools", ex); 
			}
		} else {
			setEnabled(false);
		}
	}
}
