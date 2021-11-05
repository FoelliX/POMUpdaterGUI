package de.foellix.maven.pomupdater;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class VersionedPOMElement implements IElement {
	private static final Comparator<? super String> VERSION_COMPARATOR = new Comparator<String>() {
		@Override
		public int compare(String o1, String o2) {
			return o1.compareTo(o2) * -1;
		}
	};

	private String groupId;
	private String artifactId;
	private String version;

	private Node groupIdNode;
	private Node artifactIdNode;
	private Node versionNode;

	private String release;
	private String latest;
	private List<String> versions;

	public VersionedPOMElement(NodeList elements, Map<String, VariableElement> variables) {
		this.groupId = null;
		this.artifactId = null;
		this.version = null;
		this.release = null;
		this.latest = null;
		this.versions = new LinkedList<>();
		for (int i = 0; i < elements.getLength(); i++) {
			final Node element = elements.item(i);
			if (element.getNodeName().equals("groupId")) {
				this.groupId = element.getTextContent();
				this.groupIdNode = element;
			} else if (element.getNodeName().equals("artifactId")) {
				this.artifactId = element.getTextContent();
				this.artifactIdNode = element;
			} else if (element.getNodeName().equals("version")) {
				this.version = element.getTextContent();
				this.versionNode = element;
			}
		}
		if (this.groupId != null && this.artifactId != null) {
			attachMetaData();
		}
	}

	public String getIdentifier() {
		return this.groupId + ": " + this.artifactId;
	}

	@Override
	public void updateVersion(String version) {
		this.version = version;
		this.versionNode.setTextContent(version);
	}

	private void attachMetaData() {
		final String link = getLink();
		try {
			final URL url = new URL(link);
			final String metaDataXML = new String(url.openStream().readAllBytes(), StandardCharsets.UTF_8);
			IOHandler.getInstance().parseMetaData(this, metaDataXML);
		} catch (final IOException e) {
			System.err.println("\n- Keeping current information for \"" + this.getIdentifier()
					+ "\", since meta information could not be retrieved from: " + link
					+ Helper.getExceptionAppendix(e));
			if (this.version != null) {
				getVersions().add(this.version);
				setLatest(this.version);
				setRelease(this.version);
			}
		}
		this.versions.sort(VERSION_COMPARATOR);
	}

	private String getLink() {
		return "https://repo1.maven.org/maven2/" + this.groupId.replace(".", "/") + "/" + this.artifactId + "/"
				+ "maven-metadata.xml";
	}

	public String getGroupId() {
		return this.groupId;
	}

	public String getArtifactId() {
		return this.artifactId;
	}

	public String getVersion() {
		return this.version;
	}

	public String getRelease() {
		return this.release;
	}

	public String getLatest() {
		return this.latest;
	}

	public List<String> getVersions() {
		return this.versions;
	}

	public Node getGroupIdNode() {
		return this.groupIdNode;
	}

	public Node getArtifactIdNode() {
		return this.artifactIdNode;
	}

	public Node getVersionNode() {
		return this.versionNode;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public void setRelease(String release) {
		this.release = release;
	}

	public void setLatest(String latest) {
		this.latest = latest;
	}

	@Override
	public String toString() {
		return "VersionedPOMElement [groupId=" + this.groupId + ", artifactId=" + this.artifactId + ", version="
				+ this.version + ", release=" + this.release + ", latest=" + this.latest + ", versions=" + this.versions
				+ "]";
	}
}