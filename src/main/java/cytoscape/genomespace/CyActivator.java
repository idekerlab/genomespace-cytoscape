package cytoscape.genomespace;

import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.task.read.LoadNetworkFileTaskFactory;
import org.cytoscape.task.read.LoadTableFileTaskFactory;
import org.cytoscape.task.read.OpenSessionTaskFactory;
import org.cytoscape.task.write.ExportNetworkViewTaskFactory;
import org.cytoscape.task.write.SaveSessionAsTaskFactory;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.work.swing.DialogTaskManager;
import org.genomespace.sws.SimpleWebServer;
import org.osgi.framework.BundleContext;

import cytoscape.genomespace.action.DeleteFileInGenomeSpaceAction;
import cytoscape.genomespace.action.LoadAttrsFromGenomeSpaceAction;
import cytoscape.genomespace.action.LoadNetworkFromGenomeSpaceAction;
import cytoscape.genomespace.action.LoadNetworkFromURLAction;
import cytoscape.genomespace.action.LoadSessionFromGenomeSpaceAction;
import cytoscape.genomespace.action.LoadSessionFromURLAction;
import cytoscape.genomespace.action.LoginToGenomeSpaceAction;
import cytoscape.genomespace.action.SaveNetworkToGenomeSpaceAction;
import cytoscape.genomespace.action.SaveSessionToGenomeSpaceAction;
import cytoscape.genomespace.action.UploadFileToGenomeSpaceAction;
import cytoscape.genomespace.ui.LaunchToolMenu;


/**
 * This class is used to instantiate your plugin. Put whatever initialization code
 * you need into the no argument constructor (the only one that will be called).
 * The actual functionality of your plugin can be in this class, but should 
 * probably be separated into separted classes that get instantiated here.
 */
public class CyActivator extends AbstractCyActivator {

	public CyActivator() {
		// Properly initializes things.
		super();
	}

	public void start(BundleContext bc) throws Exception {
		CySwingApplication cySwingApplication = getService(bc, CySwingApplication.class);
		CyApplicationManager cyApplicationManager = getService(bc, CyApplicationManager.class);
		CyProperty<Properties> cytoscapePropertiesServiceRef = getService(bc, CyProperty.class,
				"(cyPropertyName=commandline.props)");
		FileUtil fileUtil = getService(bc, FileUtil.class);
		DialogTaskManager dialogTaskManager = getService(bc, DialogTaskManager.class);
		LoadTableFileTaskFactory loadTableFileTaskFactory = getService(bc, LoadTableFileTaskFactory.class);
		LoadNetworkFileTaskFactory loadNetworkFileTaskFactory = getService(bc, LoadNetworkFileTaskFactory.class);
		OpenSessionTaskFactory openSessionTaskFactory = getService(bc, OpenSessionTaskFactory.class);
		SaveSessionAsTaskFactory saveSessionAsTaskFactory = getService(bc, SaveSessionAsTaskFactory.class);
		ExportNetworkViewTaskFactory exportNetworkViewTaskFactory = getService(bc, ExportNetworkViewTaskFactory.class);
		GSUtils gsUtils = new GSUtils(cytoscapePropertiesServiceRef, cySwingApplication.getJFrame());
		JFrame frame = cySwingApplication.getJFrame();
		// set up the URL loaders
		LoadNetworkFromURLAction loadNetworkURL = new LoadNetworkFromURLAction(dialogTaskManager, loadNetworkFileTaskFactory, gsUtils);
		LoadSessionFromURLAction loadSessionURL = new LoadSessionFromURLAction(dialogTaskManager, openSessionTaskFactory, gsUtils);
//		LoadCyTableFromURL loadNodeAttrURL = new LoadCyTableFromURL("node.cytable",Cytoscape.getNodeAttributes());
//		LoadCyTableFromURL loadEdgeAttrURL = new LoadCyTableFromURL("edge.cytable",Cytoscape.getEdgeAttributes());

		SimpleWebServer sws = new SimpleWebServer(60161);
		sws.registerListener(loadNetworkURL);
//		sws.registerListener(loadNodeAttrURL);
//		sws.registerListener(loadEdgeAttrURL);
		sws.registerListener(loadSessionURL);
		sws.start();

		// This action represents the actual behavior of the plugin.
		UploadFileToGenomeSpaceAction uploadAction = new UploadFileToGenomeSpaceAction(fileUtil, gsUtils, frame);
		registerService(bc,uploadAction,CyAction.class, new Properties());

		DeleteFileInGenomeSpaceAction deleteAction = new DeleteFileInGenomeSpaceAction(gsUtils, frame);
		registerService(bc,deleteAction,CyAction.class, new Properties());

		LoadNetworkFromGenomeSpaceAction loadNetworkAction = new LoadNetworkFromGenomeSpaceAction(dialogTaskManager, loadNetworkFileTaskFactory, gsUtils, frame);
		registerService(bc,loadNetworkAction,CyAction.class, new Properties());

		LoadAttrsFromGenomeSpaceAction loadAttrsAction = new LoadAttrsFromGenomeSpaceAction(dialogTaskManager, loadTableFileTaskFactory, gsUtils, frame);
		registerService(bc,loadAttrsAction,CyAction.class, new Properties());

//		LoadCyTableFromGenomeSpace loadCyTableAction = new LoadCyTableFromGenomeSpace();
//		Cytoscape.getDesktop().getCyMenus().addAction(loadCyTableAction);


		LoadSessionFromGenomeSpaceAction loadSessionAction = new LoadSessionFromGenomeSpaceAction(dialogTaskManager, openSessionTaskFactory, gsUtils, frame);
		registerService(bc,loadSessionAction,CyAction.class, new Properties());

		SaveSessionToGenomeSpaceAction saveSessionAction = new SaveSessionToGenomeSpaceAction(dialogTaskManager, saveSessionAsTaskFactory, gsUtils, frame);
		registerService(bc,saveSessionAction,CyAction.class, new Properties());

		SaveNetworkToGenomeSpaceAction saveNetworkAction = new SaveNetworkToGenomeSpaceAction(cyApplicationManager, dialogTaskManager, exportNetworkViewTaskFactory, gsUtils, frame);
		registerService(bc,saveNetworkAction,CyAction.class, new Properties());

//		LoadOntologyAndAnnotationFromGenomeSpace loadOntologyAndAnnotationFromGenomeSpace =
//			new LoadOntologyAndAnnotationFromGenomeSpace();
//		Cytoscape.getDesktop().getCyMenus().addAction(loadOntologyAndAnnotationFromGenomeSpace);

		LoginToGenomeSpaceAction loginToGenomeSpace = new LoginToGenomeSpaceAction(gsUtils);
		registerService(bc,loginToGenomeSpace,CyAction.class, new Properties());
		
		JMenu gsMenu = getMenu(cySwingApplication.getJMenuBar(), "File.GenomeSpace");
		LaunchToolMenu ltm = new LaunchToolMenu(gsMenu, gsUtils, frame);
		gsMenu.add(ltm);
		

		// load any initial arguments
		String sessionProp = cytoscapePropertiesServiceRef.getProperties().getProperty("gs.session");
		if ( sessionProp != null ) {
			loadSessionURL.loadSession(sessionProp);
		} else {
			String networkProp = cytoscapePropertiesServiceRef.getProperties().getProperty("gs.network");
			loadNetworkURL.loadNetwork(networkProp);

//			String nodeTableProp = cytoscapePropertiesServiceRef.getProperties().getProperty("node.cytable");
//			loadNodeAttrURL.loadTable(nodeTableProp);
//
//			String edgeTableProp = cytoscapePropertiesServiceRef.getProperties().getProperty("edge.cytable");
//			loadEdgeAttrURL.loadTable(edgeTableProp);
		}
	}
	
	private JMenu getMenu(JMenuBar menuBar, String name) {
		String[] path = name.split("\\.");
		if(path.length < 1) return null;
		for(int i=0; i<menuBar.getMenuCount(); i++) {
			JMenu menu = menuBar.getMenu(i);
			if(menu.getText().equals(path[0])) {
				if(path.length == 1) return menu;
				else {
					int startPos = path[0].length()+1;
					return (JMenu) getMenuItem(menu,name.substring(startPos));
				}
			}
		}
		return null;
	}
	
	private JMenu getMenu(JMenu menu, String name) {
		JMenuItem menuItem = getMenuItem(menu,name);
		if(menuItem == null || !(menuItem instanceof JMenu)) return null;
		else return (JMenu) menuItem;
	}
	
	private JMenuItem getMenuItem(JMenu menu, String name) {
		String[] path = name.split("\\.");
		if(path.length < 1) return null;
		for(int i=0; i<menu.getItemCount(); i++){
			JMenuItem menuItem = menu.getItem(i);
			if(menuItem.getText().equals(path[0])) {
				if(path.length == 1) return menuItem;
				else if(menuItem instanceof JMenu) {
					int startPos = path[0].length()+1;
					return getMenuItem((JMenu)menuItem,name.substring(startPos));
				}
				else break;
			}
		}
		return null;
	}
}
