package net.sourceforge.plantuml.xmi;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import net.sourceforge.plantuml.activitydiagram3.ActivityDiagram3;
import net.sourceforge.plantuml.klimt.creole.Display;
import net.sourceforge.plantuml.version.Version;
import net.sourceforge.plantuml.xml.XmlFactories;

public abstract class XmiActivityDiagramAbstract implements XmlDiagramTransformer {
	protected final ActivityDiagram3 activityDiagram;
	protected final Document document;
	protected final Element ownedElementRoot;

	public XmiActivityDiagramAbstract(ActivityDiagram3 activityDiagram, Document document, Element ownedElementRoot) {
		super();
		this.activityDiagram = activityDiagram;
		this.document = document;
		this.ownedElementRoot = ownedElementRoot;
	}

	/**
	 * Called in constructor, must not depend on subclass state
	 * 
	 * @return
	 */
	protected String uml_version() {
		return "1.4";
	}

	public XmiActivityDiagramAbstract(ActivityDiagram3 diagram) throws ParserConfigurationException {
		this.activityDiagram = diagram;

		final DocumentBuilder builder = XmlFactories.newDocumentBuilder();
		this.document = builder.newDocument();
		document.setXmlVersion("1.0");
		document.setXmlStandalone(true);

		final Element xmi = document.createElement("XMI");
		xmi.setAttribute("xmi.version", "1.1");
		xmi.setAttribute("xmlns:UML", "href://org.omg/UML/" + uml_version());
		document.appendChild(xmi);

		final Element header = document.createElement("XMI.header");
		xmi.appendChild(header);

		header.appendChild(createXmiDocumentation());
		header.appendChild(createXmiMetamodel());

		final Element content = document.createElement("XMI.content");
		xmi.appendChild(content);

		final Element model = document.createElement("UML:Model");
		model.setAttribute("xmi.id", CucaDiagramXmiMaker.getModel(diagram));
		model.setAttribute("name", "PlantUML");
		content.appendChild(model);

		// <UML:Namespace.ownedElement>
		this.ownedElementRoot = document.createElement("UML:Namespace.ownedElement");
		model.appendChild(ownedElementRoot);
	}

	private Element createXmiDocumentation() {
		final Element documentation = document.createElement("XMI.documentation");
		final Element exporter = document.createElement("XMI.exporter");
		exporter.setTextContent("PlantUML");
		final Element exporterVersion = document.createElement("XMI.exporterVersion");
		exporterVersion.setTextContent(Version.versionString());
		documentation.appendChild(exporter);
		documentation.appendChild(exporterVersion);
		return documentation;
	}

	final protected String forXMI(String s) {
		return s.replace(':', ' ');
	}

	final protected String forXMI(Display s) {
		if (Display.isNull(s)) {
			return "";
		}
		return s.get(0).toString().replace(':', ' ');
	}

	private Element createXmiMetamodel() {
		final Element metamodel = document.createElement("XMI.metamodel");
		metamodel.setAttribute("xmi.name", "UML");
		metamodel.setAttribute("xmi.version", uml_version());
		return metamodel;
	}

	final public void transformerXml(OutputStream os) throws TransformerException, ParserConfigurationException {
		final Source source = new DOMSource(document);

		final Result resultat = new StreamResult(os);

		final Transformer transformer = XmlFactories.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
		// tf.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.transform(source, resultat);
	}

}
