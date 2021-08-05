package de.foellix.maven.pomupdater;

import org.w3c.dom.Node;

public class VariableElement implements IElement {
	private String variable;
	private String version;

	private Node node;

	public VariableElement(Node element, String variable, String version) {
		this.variable = variable;
		this.version = version;
		this.node = element;
	}

	@Override
	public void updateVersion(String version) {
		this.version = version;
		this.node.setTextContent(version);
	}

	public String getVariable() {
		return this.variable;
	}

	public String getVersion() {
		return this.version;
	}

	public void setVariable(String variable) {
		this.variable = variable;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public String toString() {
		return "VariableElement [variable=" + this.variable + ", version=" + this.version + "]";
	}
}