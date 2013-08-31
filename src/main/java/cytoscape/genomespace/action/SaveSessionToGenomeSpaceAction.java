package cytoscape.genomespace.action;


import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.task.write.SaveSessionAsTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;
import org.genomespace.client.DataManagerClient;
import org.genomespace.client.GsSession;
import org.genomespace.client.exceptions.GSClientException;
import org.genomespace.client.ui.GSFileBrowserDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cytoscape.genomespace.context.GenomeSpaceContext;
import cytoscape.genomespace.task.DeleteFileTask;
import cytoscape.genomespace.task.SetFrameSessionTitleTask;
import cytoscape.genomespace.task.UploadFileToGenomeSpaceTask;


/**
 * A simple action.  Change the names as appropriate and
 * then fill in your expected behavior in the actionPerformed()
 * method.
 */
public class SaveSessionToGenomeSpaceAction extends AbstractCyAction {
	private static final long serialVersionUID = 9988760123456789L;
	private static final Logger logger = LoggerFactory.getLogger(SaveSessionToGenomeSpaceAction.class);
	private final DialogTaskManager dialogTaskManager;
	private final SaveSessionAsTaskFactory saveSessionAsTaskFactory;
	private final GenomeSpaceContext gsContext;
	private final JFrame frame;
	
	
	public SaveSessionToGenomeSpaceAction(DialogTaskManager dialogTaskManager, SaveSessionAsTaskFactory saveSessionAsTaskFactory, GenomeSpaceContext gsContext, JFrame frame) {
		// Give your action a name here
		super("Save to GenomeSpace...");

		// Set the menu you'd like here.  Plugins don't need
		// to live in the Plugins menu, so choose whatever
		// is appropriate!
		setPreferredMenu("File");
		setMenuGravity(3.2f);
		this.dialogTaskManager = dialogTaskManager;
		this.saveSessionAsTaskFactory = saveSessionAsTaskFactory;
		this.gsContext = gsContext;
		this.frame = frame;
	}

	public void actionPerformed(ActionEvent e) {
		try {
			if(!gsContext.loginIfNotAlready()) return;
			final GsSession session = gsContext.getSession();
			final DataManagerClient dataManagerClient = session.getDataManagerClient();

			final List<String> acceptableExtensions = new ArrayList<String>();
			acceptableExtensions.add("cys");
			final GSFileBrowserDialog dialog =
				new GSFileBrowserDialog(frame, dataManagerClient,
							acceptableExtensions,
							GSFileBrowserDialog.DialogType.SAVE_AS_DIALOG, "Save Session to GenomeSpace");
			String saveFileName = dialog.getSaveFileName();
			
			if (saveFileName == null)
				return;

			// Create Task
			final String baseName = gsContext.baseName(saveFileName);
			final File tempFile = new File(System.getProperty("java.io.tmpdir"), baseName);
			TaskIterator ti = saveSessionAsTaskFactory.createTaskIterator(tempFile);
			ti.append(new UploadFileToGenomeSpaceTask(session, tempFile, saveFileName));
			ti.append(new SetFrameSessionTitleTask(frame, baseName));
			ti.append(new TaskIterator(new DeleteFileTask(tempFile)));
			dialogTaskManager.execute(ti);
		} catch (GSClientException ex) {
			logger.error("GenomeSpace failed", ex);
			JOptionPane.showMessageDialog(frame, "<html>The GenomeSpace server is inaccessible or not responding properly at this time.<br/>" +
					"Please check your Internet connection and try again.</html>", "GenomeSpace Error",
			        JOptionPane.ERROR_MESSAGE);
		}
	}
}
