package com.cafedefinca.backend.util;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

public class EnteroEstrictoDeserializer extends ValueDeserializer<Integer> {
	@Override
	public Integer deserialize(JsonParser parser, DeserializationContext contexto) throws JacksonException {
		if (!parser.isExpectedNumberIntToken()) {
			return contexto.reportInputMismatch(Integer.class, "Se esperaba un número entero, sin decimales.");
		}
		return parser.getIntValue();
	}
}
