package com.yellowinsurance.claims.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * VULNERABILITY: XXE (XML External Entity) attack
 * This parser does NOT disable external entity processing,
 * allowing attackers to read arbitrary files from the server
 * or perform SSRF attacks via crafted XML payloads.
 *
 * Example malicious payload:
 * <?xml version="1.0"?>
 * <!DOCTYPE foo [<!ENTITY xxe SYSTEM "file:///etc/passwd">]>
 * <claim><description>&xxe;</description></claim>
 */
@Component
public class XmlParser {

    private static final Logger logger = LogManager.getLogger(XmlParser.class);

    /**
     * VULNERABILITY: XXE - external entities not disabled
     */
    public Map<String, Object> parseXml(String xmlContent) {
        Map<String, Object> result = new HashMap<>();

        try {
            // VULNERABILITY: Default DocumentBuilderFactory allows XXE
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // Should set these but intentionally NOT setting them:
            // factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            // factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            // factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xmlContent.getBytes()));

            Element root = doc.getDocumentElement();
            result.put("rootElement", root.getTagName());

            // Parse child elements
            NodeList children = root.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                if (children.item(i) instanceof Element) {
                    Element child = (Element) children.item(i);
                    result.put(child.getTagName(), child.getTextContent());
                }
            }

            result.put("parsed", true);
            logger.info("XML parsed successfully: " + result);

        } catch (Exception e) {
            result.put("parsed", false);
            result.put("error", e.getMessage());
            // VULNERABILITY: Exposing parser error details
            logger.error("XML parsing failed: " + e.getMessage(), e);
        }

        return result;
    }
}
