package io.macgyver.plugin.cloud.aws;

import io.macgyver.neorx.rest.NeoRxClient;

import java.util.Optional;

import org.assertj.core.util.Preconditions;

import com.amazonaws.regions.Region;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeAvailabilityZonesResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class AvailabilityZoneScanner extends AWSServiceScanner {

	public AvailabilityZoneScanner(AWSServiceClient client, NeoRxClient neo4j) {
		super(client, neo4j);
	}

	@Override
	public Optional<String> computeArn(JsonNode n) {
		return Optional.empty();
	}

	@Override
	public void scan(Region region) {
		AmazonEC2Client c = getAWSServiceClient().createEC2Client(region);

		DescribeAvailabilityZonesResult result = c.describeAvailabilityZones();
		result.getAvailabilityZones().forEach(it -> {
			try {
				ObjectNode n = convertAwsObject(it, region);
				
				String cypher = "merge (x:AwsAvailabilityZone {aws_zoneName:{aws_zoneName}, aws_region:{aws_region}}) set x+={props} set x.updateTs=timestamp()";
				String mapCypher = "match (x:AwsAvailabilityZone {aws_zoneName:{aws_zoneName}, aws_region:{aws_region}}), "
						+ "(y:AwsSubnet {aws_availabilityZone:{aws_zoneName}, aws_region:{aws_region}}) merge (y)-[:RESIDES_IN]->(x)";
				NeoRxClient neoRx = getNeoRxClient();
				
				Preconditions.checkNotNull(neoRx);
				neoRx.execCypher(cypher, "aws_zoneName",n.path("aws_zoneName").asText(), "aws_region",n.path("aws_region").asText(), "props",n);
				neoRx.execCypher(mapCypher, "aws_zoneName",n.path("aws_zoneName").asText(), "aws_region",n.path("aws_region").asText(), "props",n);
			} catch (RuntimeException e) { 
				logger.warn("problem scanning availability zones",e);
			}		
		});		
	}

}
