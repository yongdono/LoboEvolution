/*
    GNU GENERAL LICENSE
    Copyright (C) 2014 - 2018 Lobo Evolution

    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public
    License as published by the Free Software Foundation; either
    verion 3 of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    General License for more details.

    You should have received a copy of the GNU General Public
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    

    Contact info: ivan.difrancesco@yahoo.it
 */
package org.loboevolution.main;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.swing.SwingUtilities;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.loboevolution.clientlet.Clientlet;
import org.loboevolution.clientlet.ClientletRequest;
import org.loboevolution.clientlet.ClientletResponse;
import org.loboevolution.security.GenericLocalPermission;
import org.loboevolution.ua.NavigationEvent;
import org.loboevolution.ua.NavigationVetoException;
import org.loboevolution.ua.NavigatorEventType;
import org.loboevolution.ua.NavigatorExceptionEvent;
import org.loboevolution.ua.NavigatorFrame;
import org.loboevolution.ua.NavigatorWindow;
import org.loboevolution.util.CollectionUtilities;
import org.loboevolution.util.JoinableTask;

/**
 * Manages platform extensions.
 */
public class ExtensionManager {

	/** The Constant logger. */
	private static final Logger logger = LogManager.getLogger(ExtensionManager.class);

	/** The Constant instance. */
	private static final ExtensionManager instance = new ExtensionManager();

	/** The Constant EXT_DIR_NAME. */
	private static final String EXT_DIR_NAME = "ext";

	// Note: We do not synchronize around the extensions collection,
	// given that it is fully built in the constructor.
	/** The extension by id. */
	private final Map<String, Extension> extensionById = new HashMap<String, Extension>();

	/** The extensions. */
	private final SortedSet<Extension> extensions = new TreeSet<Extension>();

	/** The libraries. */
	private final ArrayList<Extension> libraries = new ArrayList<Extension>();

	/**
	 * Instantiates a new extension manager.
	 */
	private ExtensionManager() {
		this.createExtensions();
	}

	/**
	 * Gets the Constant instance.
	 *
	 * @return the Constant instance
	 */
	public static ExtensionManager getInstance() {
		// This security check should be enough, provided
		// ExtensionManager instances are not retained.
		SecurityManager sm = System.getSecurityManager();
		if (sm != null) {
			sm.checkPermission(GenericLocalPermission.EXT_GENERIC);
		}
		return instance;
	}

	/**
	 * Creates the extensions.
	 */
	private void createExtensions() {
		File[] extDirs;
		File[] extFiles;
		String extDirsProperty = System.getProperty("ext.dirs");
		if (extDirsProperty == null) {
			File appDir = PlatformInit.getInstance().getApplicationDirectory();
			extDirs = new File[] { new File(appDir, EXT_DIR_NAME) };
		} else {
			StringTokenizer tok = new StringTokenizer(extDirsProperty, ",");
			ArrayList<File> extDirsList = new ArrayList<File>();
			while (tok.hasMoreTokens()) {
				String token = tok.nextToken();
				extDirsList.add(new File(token.trim()));
			}
			extDirs = extDirsList.toArray(new File[0]);
		}
		String extFilesProperty = System.getProperty("ext.files");
		if (extFilesProperty == null) {
			extFiles = new File[0];
		} else {
			StringTokenizer tok = new StringTokenizer(extFilesProperty, ",");
			ArrayList<File> extFilesList = new ArrayList<File>();
			while (tok.hasMoreTokens()) {
				String token = tok.nextToken();
				extFilesList.add(new File(token.trim()));
			}
			extFiles = extFilesList.toArray(new File[0]);
		}
		this.createExtensions(extDirs, extFiles);
	}

	/**
	 * Adds the extension.
	 *
	 * @param file
	 *            the file
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void addExtension(File file) throws IOException {
		Extension ei = new Extension(file);
		this.extensionById.put(ei.getId(), ei);
		if (ei.isLibraryOnly()) {
			libraries.add(ei);
		} else {
			extensions.add(ei);
		}
	}

	/**
	 * Creates the extensions.
	 *
	 * @param extDirs
	 *            the ext dirs
	 * @param extFiles
	 *            the ext files
	 */
	private void createExtensions(File[] extDirs, File[] extFiles) {
		Collection<Extension> extensions = this.extensions;
		Collection<Extension> libraries = this.libraries;
		Map<String, Extension> extensionById = this.extensionById;
		extensions.clear();
		libraries.clear();
		extensionById.clear();
		for (File extDir : extDirs) {
			if (!extDir.exists()) {
				logger.warn("createExtensions(): Directory '" + extDir + "' not found.");
				if (PlatformInit.getInstance().isCodeLocationDirectory()) {
					logger.warn(
							"createExtensions(): The application code location is a directory, which means the application is probably being run from an IDE. Additional setup is required. Please refer to README.txt file.");
				}
				continue;
			}
			File[] extRoots = extDir.listFiles(new ExtFileFilter());
			if (extRoots == null || extRoots.length == 0) {
				logger.warn("createExtensions(): No potential extensions found in " + extDir + " directory.");
				continue;
			}
			for (File file : extRoots) {
				try {
					this.addExtension(file);
				} catch (IOException ioe) {
					logger.error("createExtensions(): Unable to load '" + file + "'.", ioe);
				}
			}
		}
		for (File file : extFiles) {
			try {
				this.addExtension(file);
			} catch (IOException ioe) {
				logger.error("createExtensions(): Unable to load '" + file + "'.", ioe);
			}
		}

		if (this.extensionById.size() == 0) {
			logger.warn(
					"createExtensions(): No extensions found. This is indicative of a setup error. Extension directories scanned are: "
							+ Arrays.asList(extDirs) + ".");
		}

		// Get the system class loader
		ClassLoader rootClassLoader = this.getClass().getClassLoader();

		// Create class loader for extension "libraries"
		ArrayList<URL> libraryURLCollection = new ArrayList<URL>();
		for (Extension ei : libraries) {
			try {
				libraryURLCollection.add(ei.getCodeSource());
			} catch (MalformedURLException thrown) {
				logger.error( "createExtensions()", thrown);
			}
		}

		// Initialize class loader in each extension, using librariesCL as
		// the parent class loader. Extensions are initialized in parallel.
		Collection<JoinableTask> tasks = new ArrayList<JoinableTask>();
		PlatformInit pm = PlatformInit.getInstance();
		for (Extension ei : extensions) {
			final ClassLoader pcl = new URLClassLoader(libraryURLCollection.toArray(new URL[0]), rootClassLoader);
			final Extension fei = ei;
			// Initialize rest of them in parallel.
			JoinableTask task = new JoinableTask() {
				@Override
				public void execute() {
					try {
						fei.initClassLoader(pcl);
					} catch (Exception err) {
						logger.error("Unable to create class loader for " + fei + ".", err);
					}
				}

				@Override
				public String toString() {
					return "createExtensions:" + fei;
				}
			};
			tasks.add(task);
			pm.scheduleTask(task);
		}

		// Join tasks to make sure all extensions are
		// initialized at this point.
		for (JoinableTask task : tasks) {
			try {
				task.join();
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
			}
		}
	}

	/**
	 * Gets the class loader.
	 *
	 * @param extensionId
	 *            the extension id
	 * @return the class loader
	 */
	public ClassLoader getClassLoader(String extensionId) {
		Extension ei = this.extensionById.get(extensionId);
		if (ei != null) {
			return ei.getClassLoader();
		} else {
			return null;
		}
	}

	/**
	 * Inits the extensions.
	 */
	public void initExtensions() {
		Collection<JoinableTask> tasks = new ArrayList<JoinableTask>();
		PlatformInit pm = PlatformInit.getInstance();
		for (Extension ei : this.extensions) {
			final Extension fei = ei;
			JoinableTask task = new JoinableTask() {
				@Override
				public void execute() {
					fei.initExtension();
				}

				@Override
				public String toString() {
					return "initExtensions:" + fei;
				}
			};
			tasks.add(task);
			pm.scheduleTask(task);
		}
		// Join all tasks before returning
		for (JoinableTask task : tasks) {
			try {
				task.join();
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
			}
		}
	}

	/**
	 * Inits the extensions window.
	 *
	 * @param context
	 *            the context
	 */
	public void initExtensionsWindow(final NavigatorWindow context) {
		// This must be done sequentially due to menu lookup infrastructure.
		for (Extension ei : this.extensions) {
			try {
				ei.initExtensionWindow(context);
			} catch (Exception err) {
				logger.error( "initExtensionsWindow(): Extension could not properly initialize a new window.",
						err);
			}
		}
	}

	/**
	 * Shutdown extensions window.
	 *
	 * @param context
	 *            the context
	 */
	public void shutdownExtensionsWindow(final NavigatorWindow context) {
		// This must be done sequentially due to menu lookup infrastructure.
		for (Extension ei : this.extensions) {
			try {
				ei.shutdownExtensionWindow(context);
			} catch (Exception err) {
				logger.error( "initExtensionsWindow(): Extension could not properly process window shutdown.",
						err);
			}
		}
	}

	/**
	 * Gets the clientlet.
	 *
	 * @param request
	 *            the request
	 * @param response
	 *            the response
	 * @return the clientlet
	 */
	public Clientlet getClientlet(ClientletRequest request, ClientletResponse response) {
		Collection<Extension> extensions = this.extensions;
		// Call all plugins once to see if they can select the response.
		for (Extension ei : extensions) {
			try {
				Clientlet clientlet = ei.getClientlet(request, response);
				if (clientlet != null) {
					return clientlet;
				}
			} catch (Exception thrown) {
				logger.error( "getClientlet(): Extension " + ei + " threw exception.", thrown);
			}
		}

		// None handled it. Call the last resort handlers in reverse order.
		for (Extension ei : (Collection<Extension>) CollectionUtilities.reverse(extensions)) {
			try {
				Clientlet clientlet = ei.getLastResortClientlet(request, response);
				if (clientlet != null) {
					return clientlet;
				}
			} catch (Exception thrown) {
				logger.error( "getClientlet(): Extension " + ei + " threw exception.", thrown);
			}
		}
		return null;
	}

	/**
	 * Handle error.
	 *
	 * @param frame
	 *            the frame
	 * @param response
	 *            the response
	 * @param exception
	 *            the exception
	 */
	public void handleError(NavigatorFrame frame, final ClientletResponse response, final Throwable exception) {
		final NavigatorExceptionEvent event = new NavigatorExceptionEvent(this, NavigatorEventType.ERROR_OCCURRED,
				frame, response, exception);

		if (SwingUtilities.isEventDispatchThread()) {
			Collection<Extension> ext = extensions;
			// Call all plugins once to see if they can select the response.
			boolean dispatched = false;
			for (Extension ei : ext) {
				if (ei.handleError(event)) {
					dispatched = true;
				}
			}
			if (!dispatched && logger.isInfoEnabled()) {
				logger.error(
						"No error handlers found for error that occurred while processing response=[" + response + "].",
						exception);
			}
		} else {
			SwingUtilities.invokeLater(() -> {
				Collection<Extension> ext = extensions;
				// Call all plugins once to see if they can select the
				// response.
				boolean dispatched = false;
				for (Extension ei : ext) {
					if (ei.handleError(event)) {
						dispatched = true;
					}
				}
				if (!dispatched && logger.isInfoEnabled()) {
					logger.error("No error handlers found for error that occurred while processing response=["
							+ response + "].", exception);
				}
			});
		}
	}

	/**
	 * Dispatch before navigate.
	 *
	 * @param event
	 *            the event
	 * @throws NavigationVetoException
	 *             the navigation veto exception
	 */
	public void dispatchBeforeNavigate(NavigationEvent event) throws NavigationVetoException {
		for (Extension ei : extensions) {
			try {
				ei.dispatchBeforeLocalNavigate(event);
			} catch (NavigationVetoException nve) {
				throw nve;
			} catch (Exception other) {
				logger.error( "dispatchBeforeNavigate(): Extension threw an unexpected exception.", other);
			}
		}
	}

	/**
	 * Dispatch before local navigate.
	 *
	 * @param event
	 *            the event
	 * @throws NavigationVetoException
	 *             the navigation veto exception
	 */
	public void dispatchBeforeLocalNavigate(NavigationEvent event) throws NavigationVetoException {
		for (Extension ei : extensions) {
			try {
				ei.dispatchBeforeLocalNavigate(event);
			} catch (NavigationVetoException nve) {
				throw nve;
			} catch (Exception other) {
				logger.error( "dispatchBeforeLocalNavigate(): Extension threw an unexpected exception.",
						other);
			}
		}
	}

	/**
	 * Dispatch before window open.
	 *
	 * @param event
	 *            the event
	 * @throws NavigationVetoException
	 *             the navigation veto exception
	 */
	public void dispatchBeforeWindowOpen(NavigationEvent event) throws NavigationVetoException {
		for (Extension ei : extensions) {
			try {
				ei.dispatchBeforeWindowOpen(event);
			} catch (NavigationVetoException nve) {
				throw nve;
			} catch (Exception other) {
				logger.error( "dispatchBeforeWindowOpen(): Extension threw an unexpected exception.", other);
			}
		}
	}

	/**
	 * Dispatch pre connection.
	 *
	 * @param connection
	 *            the connection
	 * @return the URL connection
	 */
	public URLConnection dispatchPreConnection(URLConnection connection) {
		for (Extension ei : extensions) {
			try {
				connection = ei.dispatchPreConnection(connection);
			} catch (Exception other) {
				logger.error( "dispatchPreConnection(): Extension threw an unexpected exception.", other);
			}
		}
		return connection;
	}

	/**
	 * Dispatch post connection.
	 *
	 * @param connection
	 *            the connection
	 * @return the URL connection
	 */
	public URLConnection dispatchPostConnection(URLConnection connection) {
		for (Extension ei : extensions) {
			try {
				connection = ei.dispatchPostConnection(connection);
			} catch (Exception other) {
				logger.error( "dispatchPostConnection(): Extension threw an unexpected exception.", other);
			}
		}
		return connection;
	}

	/**
	 * The Class ExtFileFilter.
	 */
	private static class ExtFileFilter implements FileFilter {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.FileFilter#accept(java.io.File)
		 */
		@Override
		public boolean accept(File file) {
			return file.isDirectory() || file.getName().toLowerCase().endsWith(".jar");
		}
	}
}
