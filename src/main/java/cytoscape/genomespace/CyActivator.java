package cytoscape.genomespace;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.CyVersion;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.io.write.CyNetworkViewWriterManager;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.read.LoadNetworkFileTaskFactory;
import org.cytoscape.task.read.LoadTableFileTaskFactory;
import org.cytoscape.task.read.OpenSessionTaskFactory;
import org.cytoscape.task.write.SaveSessionAsTaskFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.TunableSetter;
import org.cytoscape.work.swing.DialogTaskManager;
import org.genomespace.atm.model.WebToolDescriptor;
import org.genomespace.client.GsSession;
import org.genomespace.sws.SimpleWebServer;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cytoscape.genomespace.action.ExportNetworkToGenomeSpaceAction;
import cytoscape.genomespace.action.ExportNetworkViewToGenomeSpaceAction;
import cytoscape.genomespace.action.ImportNetworkFromGenomeSpaceAction;
import cytoscape.genomespace.action.ImportTableFromGenomeSpaceAction;
import cytoscape.genomespace.action.LaunchToolAction;
import cytoscape.genomespace.action.LoginToGenomeSpaceAction;
import cytoscape.genomespace.action.OpenSessionFromGenomeSpaceAction;
import cytoscape.genomespace.action.SaveSessionToGenomeSpaceAction;
import cytoscape.genomespace.context.GenomeSpaceContext;
import cytoscape.genomespace.event.CytoscapeGSURLHandler;
import cytoscape.genomespace.task.FileTaskFactory;
import cytoscape.genomespace.task.ImportFileTaskFactory;
import cytoscape.genomespace.task.OpenSessionFileTaskFactory;


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
	private ImageIcon icon;
	private SimpleWebServer sws;
	private Set<LaunchToolAction> launchToolActions;
	
	public CyActivator() {
		// Properly initializes things.
		super();
	}

	public void start(final BundleContext bc) throws Exception {
		this.cySwingApplication = getService(bc, CySwingApplication.class);
		this.cyServiceRegistrar = getService(bc, CyServiceRegistrar.class);
		this.icon = new ImageIcon(getClass().getResource("/images/genomespace_icon.gif"));
		
		final CyApplicationManager cyApplicationManager = getService(bc, CyApplicationManager.class);
		final CyNetworkManager cyNetworkManager = getService(bc, CyNetworkManager.class);
		final CyNetworkViewManager cyNetworkViewManager = getService(bc, CyNetworkViewManager.class);
		final CyTableManager cyTableManager = getService(bc, CyTableManager.class);
		final CyProperty<Properties> cytoscapePropertiesServiceRef = getService(bc, CyProperty.class,
				"(cyPropertyName=commandline.props)");
		
		final DialogTaskManager dialogTaskManager = getService(bc, DialogTaskManager.class);
		final TunableSetter tunableSetter = getService(bc, TunableSetter.class);
		
		// These wrapper classes are ugly hacks to work around breakage
		// in several import/export task factories on some Cytoscape releases 
		// and abstract this workaround from the actions
		// For fixed Cytoscape releases, we'll use the task factories directly
		
		FileTaskFactory loadTableFileTaskFactory = new ImportFileTaskFactory
				(getService(bc, LoadTableFileTaskFactory.class), tunableSetter);
		
		FileTaskFactory loadNetworkFileTaskFactory = new ImportFileTaskFactory
				(getService(bc, LoadNetworkFileTaskFactory.class), tunableSetter);
		
		FileTaskFactory openSessionTaskFactory = new OpenSessionFileTaskFactory
				(cyNetworkManager, cyTableManager, getService(bc, OpenSessionTaskFactory.class), tunableSetter);
		
		SaveSessionAsTaskFactory saveSessionAsTaskFactory = getService(bc, SaveSessionAsTaskFactory.class);
		CyNetworkViewWriterManager cyNetworkViewWriterManager = getService(bc, CyNetworkViewWriterManager.class);
		JFrame frame = cySwingApplication.getJFrame();
		GenomeSpaceContext gsContext = new GenomeSpaceContext(cySwingApplication, this);
		
		// set up the URL loaders
		CytoscapeGSURLHandler gsUrlHandler = new CytoscapeGSURLHandler(dialogTaskManager, loadNetworkFileTaskFactory, openSessionTaskFactory, frame, gsContext);
		
		sws = new SimpleWebServer(60161);
		sws.registerListener(gsUrlHandler);
		sws.start();
		
		launchToolActions = new HashSet<LaunchToolAction>();

		ImportNetworkFromGenomeSpaceAction importNetworkFromGenomeSpaceAction = new ImportNetworkFromGenomeSpaceAction(dialogTaskManager, loadNetworkFileTaskFactory, gsContext, bc, frame, icon);
		registerService(bc,importNetworkFromGenomeSpaceAction,CyAction.class, new Properties());

		ImportTableFromGenomeSpaceAction importTableFromGenomeSpaceAction = new ImportTableFromGenomeSpaceAction(dialogTaskManager, loadTableFileTaskFactory, gsContext, bc, frame, icon);
		registerService(bc,importTableFromGenomeSpaceAction,CyAction.class, new Properties());
		
		ExportNetworkToGenomeSpaceAction exportNetworkToGenomeSpaceAction = new ExportNetworkToGenomeSpaceAction(cyApplicationManager, cyNetworkViewManager, dialogTaskManager, cyNetworkViewWriterManager, gsContext, frame, icon);
		registerService(bc,exportNetworkToGenomeSpaceAction,CyAction.class, new Properties());

		ExportNetworkViewToGenomeSpaceAction exportNetworkViewToGenomeSpaceAction = new ExportNetworkViewToGenomeSpaceAction(cyApplicationManager, cyNetworkViewManager, dialogTaskManager, cyNetworkViewWriterManager, gsContext, frame, icon);
		registerService(bc,exportNetworkViewToGenomeSpaceAction,CyAction.class, new Properties());
		
		OpenSessionFromGenomeSpaceAction openSessionFromGenomeSpaceAction = new OpenSessionFromGenomeSpaceAction(dialogTaskManager, openSessionTaskFactory, gsContext, frame, icon);
		registerService(bc,openSessionFromGenomeSpaceAction,CyAction.class, new Properties());

		SaveSessionToGenomeSpaceAction saveSessionToGenomeSpaceAction = new SaveSessionToGenomeSpaceAction(dialogTaskManager, saveSessionAsTaskFactory, gsContext, frame, icon);
		registerService(bc,saveSessionToGenomeSpaceAction,CyAction.class, new Properties());

		LoginToGenomeSpaceAction loginToGenomeSpaceAction = new LoginToGenomeSpaceAction(gsContext, icon);
		registerService(bc,loginToGenomeSpaceAction,CyAction.class, new Properties());

		// load any initial arguments
		String fileUrl = cytoscapePropertiesServiceRef.getProperties().getProperty("gs.url");
		if ( fileUrl != null ) {
			gsUrlHandler.loadFromURL(fileUrl);
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
					if ( webTool.getName().startsWith("Cytoscape") )
						continue;
					LaunchToolAction action = new LaunchToolAction(webTool, cySwingApplication.getJFrame(), icon);
					cyServiceRegistrar.registerAllServices(action, new Properties());
					launchToolActions.add(action);
				}
			}
		} catch (Exception ex) { 
			logger.warn("problem finding web tools", ex); 
		}
		JMenu launchMenu = cySwingApplication.getJMenu("Apps.GenomeSpace.Launch");
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
