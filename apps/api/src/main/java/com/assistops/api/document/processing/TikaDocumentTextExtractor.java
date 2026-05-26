package com.assistops.api.document.processing;

import com.assistops.api.global.exception.BadRequestException;
import java.io.InputStream;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.xml.sax.SAXException;

@Component
public class TikaDocumentTextExtractor implements DocumentTextExtractor {

	private final Parser parser = new AutoDetectParser();

	@Override
	public String extract(InputStream inputStream, String contentType, String filename) {
		try {
			BodyContentHandler handler = new BodyContentHandler(-1);
			Metadata metadata = new Metadata();

			if (StringUtils.hasText(contentType)) {
				metadata.set("Content-Type", contentType);
			}
			if (StringUtils.hasText(filename)) {
				metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, filename);
			}

			parser.parse(inputStream, handler, metadata, new ParseContext());

			String text = normalize(handler.toString());

			if (!StringUtils.hasText(text)) {
				throw new BadRequestException("Extracted text is empty.");
			}

			return text;
		}
		catch (BadRequestException | DocumentProcessingException exception) {
			throw exception;
		}
		catch (TikaException | SAXException | java.io.IOException exception) {
			throw new DocumentProcessingException("Failed to extract text from document.", exception);
		}
	}

	private String normalize(String text) {
		return text
			.replace("\r\n", "\n")
			.replace('\r', '\n')
			.trim();
	}
}
