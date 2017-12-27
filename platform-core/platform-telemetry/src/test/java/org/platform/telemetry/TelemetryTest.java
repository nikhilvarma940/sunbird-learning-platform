package org.platform.telemetry;

import java.util.HashMap;
import java.util.Map;

import org.platform.telemetry.dto.Actor;
import org.platform.telemetry.dto.Context;
import org.platform.telemetry.dto.Target;
import org.platform.telemetry.dto.Telemetry;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TelemetryTest {

	public static void main(String[] args) throws Exception {
		
		Actor actor = new Actor("Learning-Platform", "1.0");
		Context context = new Context("in.ekstep", "local");
		Target object = new Target("content-service", "service");
		Map<String, Object> edata = new HashMap<String, Object>();
		Telemetry tel = new Telemetry("LOG", actor, context, object, edata);
		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writeValueAsString(tel);
		System.out.println("Telemetry: "+ json);
	}
	
}
