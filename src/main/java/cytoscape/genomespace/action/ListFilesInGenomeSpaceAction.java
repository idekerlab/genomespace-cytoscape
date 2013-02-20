package cytoscape.genomespace.action;


import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;

import org.cytoscape.application.swing.AbstractCyAction;
import org.genomespace.client.DataManagerClient;
import org.genomespace.client.GsSession;
import org.genomespace.datamanager.core.GSFileMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cytoscape.genomespace.GSUtils;
import cytoscape.genomespace.filechoosersupport.GenomeSpaceTree;


/**
 * A simple action.  Change the names as appropriate and
 * then fill in your expected behavior in the actionPerformed()
 * method.
 */
public class ListFilesInGenomeSpaceAction extends AbstractCyAction {
	private static final long serialVersionUID = 1234487711999989L;
	private static final Logger logger = LoggerFactory.getLogger(ListFilesInGenomeSpaceAction.class);
	private final GSUtils gsUtils;
	private final JFrame frame;
	
	public ListFilesInGenomeSpaceAction(GSUtils gsUtils, JFrame frame) {
		// Give your action a name here
		super("List Available Files in GenomeSpace");

		// Set the menu you'd like here.  Plugins don't need
		// to live in the Plugins menu, so choose whatever
		// is appropriate!
		setPreferredMenu("File.GenomeSpace");
		this.gsUtils = gsUtils;
		this.frame = frame;
	}

	public void actionPerformed(ActionEvent e) {
		try {
			GsSession client = gsUtils.getSession();
			DataManagerClient dmc = client.getDataManagerClient();

			// list the files present for this user
			displayTree(dmc);
		} catch (Exception ex) {
			logger.error("GenomeSpace failed",ex);
		}
	}

	private void displayTree(final DataManagerClient dataManagerClient) {
		final GenomeSpaceTree tree = new GenomeSpaceTree(dataManagerClient);
		JScrollPane scrollPane = new JScrollPane(tree);
		scrollPane.setPreferredSize(new Dimension(350, 600));
		JPanel jp = new JPanel();
		jp.add(scrollPane);
		JOptionPane.showMessageDialog(frame, jp);
	}
}


class MyCellRenderer extends JLabel implements ListCellRenderer {
	final static ImageIcon regularFileIcon = new ImageIcon("images/regularFile.png");
	final static ImageIcon directoryIcon = new ImageIcon("images/directory.png");

	// This is the only method defined by ListCellRenderer.
	// We just reconfigure the JLabel each time we're called.

	public Component getListCellRendererComponent(JList list,              // the list
						      Object value,            // value to display
						      int index,               // cell index
						      boolean isSelected,      // is the cell selected
						      boolean cellHasFocus)    // does the cell have focus
	{
		final GSFileMetadata fileMetadata = (GSFileMetadata)value;
		setText(fileMetadata.getName() + " (" + fileMetadata.getOwner() + ")" + fileMetadata.getSize());
		setIcon(fileMetadata.isDirectory() ? directoryIcon : regularFileIcon);

		if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}

		setEnabled(list.isEnabled());
		setFont(list.getFont());
		setOpaque(true);

		return this;
	}

}