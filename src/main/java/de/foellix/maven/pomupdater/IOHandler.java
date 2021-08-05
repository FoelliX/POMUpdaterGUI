package de.foellix.maven.pomupdater;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class IOHandler {
	public static final String PLUGINS = "plugins";
	public static final String DEPENDENCIES = "dependencies";

	private static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("dd_MM_yyyy-HH_mm_ss");

	private DocumentBuilder builder;
	private File pomFile;
	private Document pomDocument;
	final Map<String, VariableElement> variables;

	private static IOHandler instance = new IOHandler();

	private IOHandler() {
		try {
			this.builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (final ParserConfigurationException e) {
			this.builder = null;
			System.err.println("Could not setup POM parser!" + Helper.getExceptionAppendix(e));
		}
		this.variables = new HashMap<>();
	}

	public static IOHandler getInstance() {
		return instance;
	}

	public Map<String, List<VersionedPOMElement>> parsePom(File pomFile) {
		this.pomFile = pomFile;
		if (this.builder != null) {
			try {
				this.pomDocument = this.builder.parse(pomFile);
				this.variables.clear();
				final NodeList properties = this.pomDocument.getElementsByTagName("properties").item(0).getChildNodes();
				for (int i = 0; i < properties.getLength(); i++) {
					final Node property = properties.item(i);
					if (property.getNodeType() != Node.TEXT_NODE) {
						this.variables.put(property.getNodeName(),
								new VariableElement(property, property.getNodeName(), property.getTextContent()));
					}
				}
				final Map<String, List<VersionedPOMElement>> map = new HashMap<>();
				map.put(PLUGINS, new LinkedList<>());
				map.put(DEPENDENCIES, new LinkedList<>());
				parseVersionedElements(this.pomDocument.getElementsByTagName("plugin"), map.get(PLUGINS),
						this.variables);
				parseVersionedElements(this.pomDocument.getElementsByTagName("dependency"), map.get(DEPENDENCIES),
						this.variables);
				return map;
			} catch (final SAXException | IOException e) {
				System.err.println("Error while parsing POM file!" + Helper.getExceptionAppendix(e));
			}
		}
		return null;
	}

	private void parseVersionedElements(NodeList elements, List<VersionedPOMElement> list,
			Map<String, VariableElement> variables) {
		for (int i = 0; i < elements.getLength(); i++) {
			final Node element = elements.item(i);
			list.add(new VersionedPOMElement(element.getChildNodes(), variables));
		}
	}

	public void parseMetaData(VersionedPOMElement element, String metaData) {
		try {
			final Document document = this.builder.parse(new ByteArrayInputStream(metaData.getBytes()));
			final NodeList versions = document.getElementsByTagName("version");
			for (int i = 0; i < versions.getLength(); i++) {
				final Node version = versions.item(i);
				element.getVersions().add(version.getTextContent());
			}
			element.setLatest(document.getElementsByTagName("latest").item(0).getTextContent());
			element.setRelease(document.getElementsByTagName("release").item(0).getTextContent());
		} catch (final SAXException | IOException e) {
			System.err.println("Error while parsing meta data!" + Helper.getExceptionAppendix(e));
		}
	}

	public void save() {
		if (backup()) {
			try {
				final Transformer transformer = TransformerFactory.newInstance().newTransformer();
				final DOMSource source = new DOMSource(this.pomDocument);
				final FileWriter writer = new FileWriter(this.pomFile);
				final StreamResult result = new StreamResult(writer);
				transformer.transform(source, result);
				System.out.println("POM file successfully updated!");
				System.exit(0);
			} catch (final IOException | TransformerException e) {
				System.err.println(
						"Could not save POM file: " + this.pomFile.getAbsolutePath() + Helper.getExceptionAppendix(e));
			}
		} else {
			System.err.println("Could not backup old POM file. Not saving!");
		}
	}

	private boolean backup() {
		if (this.pomFile != null && this.pomFile.exists()) {
			File backupFile;
			int counter = 0;
			do {
				counter++;
				backupFile = new File(this.pomFile.getParentFile(), "pom_"
						+ DEFAULT_DATE_FORMAT.format(new Date(System.currentTimeMillis())) + "-" + counter + ".xml");
			} while (backupFile.exists());
			try {
				Files.copy(this.pomFile.toPath(), backupFile.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
				System.out.println("Backup of POM file created: " + backupFile.getAbsolutePath());
				return true;
			} catch (final IOException e) {
				return false;
			}
		}
		return false;
	}

	public Map<String, VariableElement> getVariables() {
		return this.variables;
	}
}