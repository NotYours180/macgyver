package io.macgyver.plugin.cmdb;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;

import io.macgyver.core.Kernel;
import io.macgyver.core.resource.Resource;
import io.macgyver.core.resource.ResourceProvider;
import io.macgyver.core.util.HJson;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

public class AppDefinitionLoader extends AbstractCatalogLoader {

	public static final String APP_DEFINITION_LABEL="AppDefinition";
	public static final String APP_DEFINITION_ID="appId";
	Pattern appPattern = Pattern.compile(".*apps.*?\\/((\\S+)\\.[h]*json)");
	Logger logger = LoggerFactory.getLogger(AppDefinitionLoader.class);

	public class AppDefinitionWriter implements Action1<JsonNode> {
		@Override
		public void call(JsonNode n) {
			logger.info("app ({}): {}", n.get("appId").asText(), n);
			Preconditions.checkState(neo4j!=null,"neo4j not set");
			try {
				String cypher = "merge (a:"+APP_DEFINITION_LABEL+" {"+APP_DEFINITION_ID+":{"+APP_DEFINITION_ID+"}}) set a+={props},a.updateTs=timestamp()";
				neo4j.execCypher(cypher, APP_DEFINITION_ID, n.get(APP_DEFINITION_ID).asText(), "props", n);
			} catch (RuntimeException e) {
				logger.warn("problem processing app definition", e);
			}

		}
	}

	public class HJsonMapper implements Func1<Resource, Observable<JsonNode>> {

		@Override
		public Observable<JsonNode> call(Resource t) {
			try {

				Matcher m = appPattern.matcher(t.getPath());
				if (m.matches()) {
					ObjectNode n = (ObjectNode) HJson.parse(t.getContentAsString());

					n.put(APP_DEFINITION_ID, m.group(2));
					
					return Observable.just(n);
				}

			} catch (RuntimeException | IOException e) {
				logger.warn("could not parse hjson resource " + t, e);
			}
			return Observable.empty();
		}

	}



	public void importAll() {
		Observable.from(providers).flatMap(new ProviderMapper()).filter(new RegexResourceFilter(appPattern))
				.flatMap(new HJsonMapper()).forEach(new AppDefinitionWriter());

	}

	@Subscribe
	public void start(Kernel.ServerStartedEvent event) {
		discoverResourceProviders();
	}

}