package com.tocchisu.movies.plugins;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Goal which downloads IMDB interfaces (Plain text files describing IMDB movies, actors, ratings, movie genres, etc.)
 * 
 * @goal download
 * 
 * @phase process-sources
 * 
 *        Usage : mvn clean install movies:download -Dmovies.directory='...'
 */
public class DownloadMojo extends AbstractMojo {
	/**
	 * URL for downloading plain text interfaces
	 */
	private static final String	IMDB_INTERFACE_URL	= "{0}/{1}.list.gz";
	// Mirrors for FTP downloads
	private static final String	DE_MIRROR			= "ftp://ftp.fu-berlin.de/pub/misc/movies/database";
	@SuppressWarnings("unused")
	private static final String	FI_MIRROR			= "ftp://ftp.funet.fi/pub/mirrors/ftp.imdb.com/pub";
	@SuppressWarnings("unused")
	private static final String	SW_MIRROR			= "ftp://ftp.sunet.se/pub/tv+movies/imdb";

	private static enum Protocol {
		FTP, HTTP
	};

	/**
	 * Location of the movie files.
	 * 
	 * @parameter expression="${movies.directory}" default-value="${basedir'/interfaces}"
	 */
	private File	targetDirectory;
	/**
	 * Should the movie files automatically be downloaded even if a movie file is already present on the file system.
	 * 
	 * @parameter expression="${movies.forceDownload}" default-value=false
	 */
	private boolean	forceDownload;

	/**
	 * Protocol used to download movies (FTP, HTTP)
	 * 
	 * @parameter expression="${movies.protocol}" default-value="ftp"
	 */
	private String	protocol;

	public void execute() throws MojoExecutionException {
		if (Protocol.valueOf(StringUtils.upperCase(protocol)) == null) {
			fail("Protocol not allowed. Available protocols are {0}", (Object[]) Protocol.values());
		}
		URL url = null;
		try {
			URL sourceURL = getSourceURL("iso-aka-titles.list.gz");
			File destinationFile = new File(getTargetDirectory(), sourceURL.getFile());
			Utils.download(sourceURL, destinationFile);
		}
		catch (IOException e) {
			fail("Error while downloading IMDB movies files from {0}", url);
		}
	}

	/**
	 * Checks existence of the directory where the movies will be downloaded. If it doesn't exist, the method will create it.
	 * 
	 * @return A safe directory location to download movies
	 * @throws MojoExecutionException
	 */
	private File getTargetDirectory() throws MojoExecutionException {
		if (getLog().isInfoEnabled()) {
			getLog().info(
					MessageFormat
							.format("Movies files will be downloaded in the directory {0}. Feel free to define a specific location to receive movies files. Usage : mvn movies:download -Dmovies.directory='...'",
									targetDirectory.getAbsoluteFile()));
		}
		if (!targetDirectory.exists()) {
			if (targetDirectory.getParentFile().canWrite()) {
				targetDirectory.mkdir();
				getLog().info(MessageFormat.format("Directory {0} has been created in order to receive movies files", targetDirectory.getAbsoluteFile()));
			}
			else {
				fail("It sounds like directory {0} cannot be accessed. Please check your permission rights on this directory or change the directory using mvn movies:download -Dmovies.directory='...' command",
						targetDirectory.getParentFile());
			}
		}
		else if (!targetDirectory.isDirectory()) {
			fail("Attempt to download movies within a file instead of a directory. Please turn {0} into a directory or change its location using mvn movies:download -Dmovies.directory='...' command ",
					targetDirectory.getAbsoluteFile());
		}
		return targetDirectory;
	}

	/**
	 * Utility method to log message errors in Maven console and fails tests execution
	 * 
	 * @param message
	 *            The message pattern to log
	 * @param params
	 *            The optional params to apply to the message pattern
	 * @throws MojoExecutionException
	 */
	private void fail(String message, Object... params) throws MojoExecutionException {
		throw new MojoExecutionException(MessageFormat.format(message, params));
	}

	private URL getSourceURL(String interfaceName) throws MalformedURLException {
		return new URL(MessageFormat.format(IMDB_INTERFACE_URL, DE_MIRROR, interfaceName));
	}
}