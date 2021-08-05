package de.foellix.maven.pomupdater;

public class Helper {
	public static String getExceptionAppendix(Throwable e) {
		return " " + e.getClass().getSimpleName() + ": " + e.getMessage();
	}

	public static Object stripVariableName(String name) {
		return name.replace("$", "").replace("{", "").replace("}", "");
	}
}