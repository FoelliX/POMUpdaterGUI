package de.foellix.maven.pomupdater;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

import javafx.application.Application;

public class Main {
	private static PrintStream dummy = new PrintStream(new OutputStream() {
		@Override
		public void write(int b) throws IOException {
			// do nothing
		}
	});
	private static PrintStream err;

	public static void main(String[] args) throws FileNotFoundException {
		if (args.length > 0) {
			final File pomFile = new File(args[0]);
			if (pomFile.exists()) {
				System.out.print("Parsing POM file... ");
				final Map<String, List<VersionedPOMElement>> map = IOHandler.getInstance().parsePom(pomFile);
				System.out.println("done");

				// Ignore JavaFX unnamed module warning
				System.out.println("Starting GUI now!");
				err = System.err;
				System.setErr(dummy);
				new Thread(() -> {
					try {
						while (!GUI.started) {
							Thread.sleep(100);
						}
					} catch (final InterruptedException e) {
						e.printStackTrace();
					} finally {
						System.setErr(err);
					}
				}).start();

				// Set Map
				GUI.map = map;

				// Start GUI
				Application.launch(GUI.class, args);
			} else {
				System.err.println("Provided POM file does not exist: " + pomFile.getAbsolutePath());
			}
		} else {
			System.err.println("Please provide a POM file as launch parameter.");
		}
	}
}