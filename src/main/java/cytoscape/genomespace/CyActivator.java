package cytoscape.genomespace;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JMenu;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.read.LoadNetworkFileTaskFactory;
import org.cytoscape.task.read.LoadTableFileTaskFactory;
import org.cytoscape.task.read.OpenSessionTaskFactory;
import org.cytoscape.task.write.ExportNetworkViewTaskFactory;
import org.cytoscape.task.write.SaveSessionAsTaskFactory;
import org.cytoscape.work.swing.DialogTaskManager;
import org.genomespace.atm.model.WebToolDescriptor;
import org.genomespace.client.ConfigurationUrls;
import org.genomespace.client.GsSession;
import org.genomespace.sws.SimpleWebServer;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cytoscape.genomespace.action.LaunchToolAction;
import cytoscape.genomespace.action.LoadNetworkFromGenomeSpaceAction;
import cytoscape.genomespace.action.LoadSessionFromGenomeSpaceAction;
import cytoscape.genomespace.action.LoadTableFromGenomeSpaceAction;
import cytoscape.genomespace.action.LoginToGenomeSpaceAction;
import cytoscape.genomespace.action.SaveNetworkToGenomeSpaceAction;
import cytoscape.genomespace.action.SaveSessionToGenomeSpaceAction;
import cytoscape.genomespace.context.GenomeSpaceContext;
import cytoscape.genomespace.event.CytoscapeGSLoadEventListener;


/**
 * This class is used to instantiate your plugin. Put whatever initialization code
 * you need into the no argument constructor (the only one that will be called).
 * The actual functionality of your plugin can be in this class, but should 
 * probably be separated into separted classes that get instantiated here.
 */
public class CyActivator extends AbstractCyActivator {
	private static final Logger logger = LoggerFactory.getLogger(LaunchToolAction.class);
	private CySwingApplication cySwingApplication;
	private CyServiceRegistrar cyServiceRegistrar;
	private SimpleWebServer sws;
	private Set<LaunchToolAction> launchToolActions;
	
	public CyActivator() {
		// Properly initializes things.
		super();
	}

	public void start(BundleContext bc) throws Exception {
		this.cySwingApplication = getService(bc, CySwingApplication.class);
		this.cyServiceRegistrar = getService(bc, CyServiceRegistrar.class);
		
		CyApplicationManager cyApplicationManager = getService(bc, CyApplicationManager.class);
		CyProperty<Properties> cytoscapePropertiesServiceRef = getService(bc, CyProperty.class,
				"(cyPropertyName=commandline.props)");
		String gsenv = cytoscapePropertiesServiceRef.getProperties().getProperty("genomespace.environment","dev").toString();
		ConfigurationUrls.init(gsenv);
		
		DialogTaskManager dialogTaskManager = getService(bc, DialogTaskManager.class);
		LoadTableFileTaskFactory loadTableFileTaskFactory = getService(bc, LoadTableFileTaskFactory.class);
		LoadNetworkFileTaskFactory loadNetworkFileTaskFactory = getService(bc, LoadNetworkFileTaskFactory.class);
		OpenSessionTaskFactory openSessionTaskFactory = getService(bc, OpenSessionTaskFactory.class);
		SaveSessionAsTaskFactory saveSessionAsTaskFactory = getService(bc, SaveSessionAsTaskFactory.class);
		ExportNetworkViewTaskFactory exportNetworkViewTaskFactory = getService(bc, ExportNetworkViewTaskFactory.class);
		JFrame frame = cySwingApplication.getJFrame();
		GenomeSpaceContext gsContext = new GenomeSpaceContext(cySwingApplication, this);
		
		// set up the URL loaders
		CytoscapeGSLoadEventListener loadEventListener = new CytoscapeGSLoadEventListener(dialogTaskManager, loadNetworkFileTaskFactory, openSessionTaskFactory, frame, gsContext);
//		LoadCyTableFromURL loadNodeAttrURL = new LoadCyTableFromURL("node.cytable",Cytoscape.getNodeAttributes());
//		LoadCyTableFromURL loadEdgeAttrURL = new LoadCyTableFromURL("edge.cytable",Cytoscape.getEdgeAttributes());
		
		sws = new SimpleWebServer(60161);
		sws.registerListener(loadEventListener);
//		sws.registerListener(loadNodeAttrURL);
//		sws.registerListener(loadEdgeAttrURL);
		sws.start();
		
		launchToolActions = new HashSet<LaunchToolAction>();

		// This action represents the actual behavior of the plugin.

		LoadNetworkFromGenomeSpaceAction loadNetworkAction = new LoadNetworkFromGenomeSpaceAction(dialogTaskManager, loadNetworkFileTaskFactory, gsContext, frame);
		registerService(bc,loadNetworkAction,CyAction.class, new Properties());

		LoadTableFromGenomeSpaceAction loadAttrsAction = new LoadTableFromGenomeSpaceAction(dialogTaskManager, loadTableFileTaskFactory, gsContext, frame);
		registerService(bc,loadAttrsAction,CyAction.class, new Properties());

//		LoadCyTableFromGenomeSpace loadCyTableAction = new LoadCyTableFromGenomeSpace();
//		Cytoscape.getDesktop().getCyMenus().addAction(loadCyTableAction);


		LoadSessionFromGenomeSpaceAction loadSessionAction = new LoadSessionFromGenomeSpaceAction(dialogTaskManager, openSessionTaskFactory, gsContext, frame);
		registerService(bc,loadSessionAction,CyAction.class, new Properties());

		SaveSessionToGenomeSpaceAction saveSessionAction = new SaveSessionToGenomeSpaceAction(dialogTaskManager, saveSessionAsTaskFactory, gsContext, frame);
		registerService(bc,saveSessionAction,CyAction.class, new Properties());

		SaveNetworkToGenomeSpaceAction saveNetworkAction = new SaveNetworkToGenomeSpaceAction(cyApplicationManager, dialogTaskManager, exportNetworkViewTaskFactory, gsContext, frame);
		registerService(bc,saveNetworkAction,CyAction.class, new Properties());
		
//		LoadOntologyAndAnnotationFromGenomeSpace loadOntologyAndAnnotationFromGenomeSpace =
//			new LoadOntologyAndAnnotationFromGenomeSpace();
//		Cytoscape.getDesktop().getCyMenus().addAction(loadOntologyAndAnnotationFromGenomeSpace);

		LoginToGenomeSpaceAction loginToGenomeSpace = new LoginToGenomeSpaceAction(gsContext);
		registerService(bc,loginToGenomeSpace,CyAction.class, new Properties());

		// load any initial arguments
		String loadURLProp = cytoscapePropertiesServiceRef.getProperties().getProperty("gs.network");
		if ( loadURLProp != null ) {
			loadEventListener.loadSession(loadURLProp);
		} else {

//			String nodeTableProp = cytoscapePropertiesServiceRef.getProperties().getProperty("node.cytable");
//			loadNodeAttrURL.loadTable(nodeTableProp);
//
//			String edgeTableProp = cytoscapePropertiesServiceRef.getProperties().getProperty("edge.cytable");
//			loadEdgeAttrURL.loadTable(edgeTableProp);
		}
		updateMenus(gsContext.getSession());
	}
	
	public void updateMenus(GsSession session) {
		for(Iterator<LaunchToolAction> i = launchToolActions.iterator(); i.hasNext();){
			cyServiceRegistrar.unregisterAllServices(i.next());
			i.remove();
		}
		try {
			if(session.isLoggedIn()) {
				for ( WebToolDescriptor webTool : session.getAnalysisToolManagerClient().getWebTools() ) {
					if ( webTool.getName().equalsIgnoreCase("cytoscape") )
						continue;
					LaunchToolAction action = new LaunchToolAction(webTool, cySwingApplication.getJFrame());
					cyServiceRegistrar.registerAllServices(action, new Properties());
					launchToolActions.add(action);
				}
			}
		} catch (Exception ex) { 
			logger.warn("problem finding web tools", ex); 
		}
		JMenu launchMenu = cySwingApplication.getJMenu("File.GenomeSpace[999].Launch");
		if ((launchMenu != null)) {
			launchMenu.setEnabled(launchMenu.getItemCount() > 0);
		}
	}
	
	public void cleanup() {
		sws.halt();
		for(LaunchToolAction action: launchToolActions){
			cyServiceRegistrar.unregisterAllServices(action);
		}
		launchToolActions.clear();
	}

	
}
