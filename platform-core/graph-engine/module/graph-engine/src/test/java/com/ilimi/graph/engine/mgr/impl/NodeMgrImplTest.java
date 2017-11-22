/**
 * 
 */

package com.ilimi.graph.engine.mgr.impl;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.dto.ResponseParams.StatusType;
import com.ilimi.graph.common.enums.GraphHeaderParams;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.enums.SystemNodeTypes;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.SearchCriteria;
import com.ilimi.graph.engine.loadtest.TestUtil;
import com.ilimi.graph.engine.router.GraphEngineManagers;
import com.ilimi.graph.model.node.DefinitionDTO;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

/**
 * @author pradyumna
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NodeMgrImplTest {

	static ObjectMapper mapper = new ObjectMapper();

	static String createNodeData = "{\"code\":\"dc1\",\n" +
			"   		\"mimeType\":\"application/pdf\",\n" + 
			"   		\"name\":\"TestdataNodeCreate\"}";

	static String updateNodeData = "{\"name\":\"TestdataNodeUpdate\"}";
	
	static String metadata = "{\"limit\": 50,\n" +
			"        \"status\": [\n" + 
			"          \"Live\"\n" + 
			"        ],\n" + 
			"        \"ttl\": 0.08,\n" + 
			"        \"variants\": {\n" + 
			"          \"high\": {\n" + 
			"            \"dimensions\": [\n" + 
			"              1024,\n" + 
			"              1024\n" + 
			"            ],\n" + 
			"            \"dpi\": 240\n" + 
			"          },\n" + 
			"          \"medium\": {\n" + 
			"            \"dimensions\": [\n" + 
			"              512,\n" + 
			"              512\n" + 
			"            ],\n" + 
			"            \"dpi\": 240\n" + 
			"          },\n" + 
			"          \"low\": {\n" + 
			"            \"dimensions\": [\n" + 
			"              128,\n" + 
			"              128\n" + 
			"            ],\n" + 
			"            \"dpi\": 240\n" + 
			"          }\n" + 
			"        },\n" + 
			"        \"softConstraints\": {\n" + 
			"          \"medium\": 15,\n" + 
			"          \"ageGroup\": 1,\n" + 
			"          \"gradeLevel\": 7,\n" + 
			"          \"board\": 4\n" + 
			"        },\n" + 
			"        \"versionCheckMode\": \"STRICT\",\n" + 
			"        \"allowStatusUpdate\": true,\n" + 
			"        \"fields\": [\n" + 
			"          \"identifier\",\n" + 
			"          \"name\",\n" + 
			"          \"code\"\n" + 
			"        ]\n" + 
			"      }";
	static ActorRef reqRouter = null;

	static Map<String, String> contentVersion = new HashMap<String, String>();
	static long timeout = 50000;
	protected static Timeout t = new Timeout(Duration.create(60, TimeUnit.SECONDS));

	@BeforeClass
	public static void beforeTest() {
		try {
			reqRouter = TestUtil.initReqRouter();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@AfterClass
	public static void afterTest() {
		try {
			// cleanData();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * 
	 */
	@Test
	public void testH() {
		try {
			String graphId = "domain";
			Request request = new Request();
			request.getContext().put(GraphHeaderParams.graph_id.name(), graphId);
			request.setManagerName(GraphEngineManagers.NODE_MANAGER);
			request.setOperation("deleteDataNode");
			request.put(GraphDACParams.node_id.name(), "testNode1");
			Future<Object> resp = Patterns.ask(reqRouter, request, TestUtil.timeout);
			Await.result(resp, t.duration());
			Thread.sleep(10000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * testCreateNodeSkipValidations
	 */
	@Test
	public void testA() {
		try {
			String graphId = "domain";
			String objectType = "Content";
			Map<String, Object> contentMap = mapper.readValue(createNodeData, new TypeReference<Map<String, Object>>() {
			});

			Node node = new Node();
			node.setNodeType(SystemNodeTypes.DATA_NODE.name());
			node.setIdentifier("testNode1");
			node.setMetadata(contentMap);
			node.setGraphId(graphId);
			node.setObjectType(objectType);

			Request request = new Request();
			request.getContext().put(GraphHeaderParams.graph_id.name(), graphId);
			request.setManagerName(GraphEngineManagers.NODE_MANAGER);
			request.setOperation("createDataNode");

			request.put(GraphDACParams.node.name(), node);
			request.put(GraphDACParams.object_type.name(), objectType);
			request.put(GraphDACParams.skip_validations.name(), true);

			Future<Object> response = Patterns.ask(reqRouter, request, TestUtil.timeout);

			handleFutureBlock(response, "createDataNodeWithoutValidations", GraphDACParams.node_id.name(),
					GraphDACParams.versionKey.name());
			Thread.sleep(10000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 *testCreateNodeWithValidations 
	 */
	@Test
	public void testB() {
		try {
			String graphId = "domain";
			String objectType = "Content";
			Map<String, Object> contentMap = mapper.readValue(createNodeData, new TypeReference<Map<String, Object>>() {
			});

			Node node = new Node();
			node.setNodeType("DATA_NODE");
			node.setIdentifier("testNode2");
			node.setMetadata(contentMap);
			node.setGraphId(graphId);
			node.setObjectType(objectType);

			Request request = new Request();
			request.getContext().put(GraphHeaderParams.graph_id.name(), graphId);
			request.setManagerName(GraphEngineManagers.NODE_MANAGER);
			request.setOperation("createDataNode");

			request.put(GraphDACParams.node.name(), node);
			request.put(GraphDACParams.object_type.name(), objectType);

			Future<Object> response = Patterns.ask(reqRouter, request, TestUtil.timeout);

			handleFutureBlock(response, "createDataNodeWithValidations", GraphDACParams.node_id.name(),
					GraphDACParams.versionKey.name());
			Thread.sleep(10000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testB1() {
		try {
			String graphId = "domain";
			String objectType = "Content";

			Request request = new Request();
			request.getContext().put(GraphHeaderParams.graph_id.name(), graphId);
			request.setManagerName(GraphEngineManagers.SEARCH_MANAGER);
			request.setOperation("getDataNode");
			request.put(GraphDACParams.node_id.name(), "testNode1");
			request.put(GraphDACParams.object_type.name(), objectType);

			Future<Object> response = Patterns.ask(reqRouter, request, TestUtil.timeout);

			handleFutureBlock(response, "getDataNode", null, null);
			Thread.sleep(10000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testB2() {
		try {
			String graphId = "domain";
			String objectType = "Content";

			List<String> nodeIDs = new ArrayList<String>();
			nodeIDs.add("testNode1");
			nodeIDs.add("testNode2");
			Request request = new Request();
			request.getContext().put(GraphHeaderParams.graph_id.name(), graphId);
			request.setManagerName(GraphEngineManagers.SEARCH_MANAGER);
			request.setOperation("getDataNodes");

			request.put(GraphDACParams.node_ids.name(), nodeIDs);
			request.put(GraphDACParams.object_type.name(), objectType);

			Future<Object> response = Patterns.ask(reqRouter, request, TestUtil.timeout);

			handleFutureBlock(response, "getDataNodes", null, null);
			Thread.sleep(10000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testB3() {
		try {
			String graphId = "domain";
			String objectType = "Content";
			Map<String, Object> contentMap = mapper.readValue(createNodeData, new TypeReference<Map<String, Object>>() {
			});

			Node node = new Node();
			node.setNodeType("DATA_NODE");
			node.setIdentifier("testNode3");
			node.setMetadata(contentMap);
			node.setGraphId(graphId);
			node.setObjectType(objectType);

			Request request = new Request();
			request.getContext().put(GraphHeaderParams.graph_id.name(), graphId);
			request.setManagerName(GraphEngineManagers.NODE_MANAGER);
			request.setOperation("validateNode");

			request.put(GraphDACParams.node.name(), node);

			Future<Object> response = Patterns.ask(reqRouter, request, TestUtil.timeout);

			handleFutureBlock(response, "validateNode", null, null);
			Thread.sleep(10000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testB4() {
		try {
			String graphId = "domain";
			String objectType = "Content";

			SearchCriteria sc = new SearchCriteria();
			sc.setNodeType(SystemNodeTypes.DATA_NODE.name());
			sc.setObjectType(objectType);
			sc.setResultSize(10);

			Request request = new Request();
			request.getContext().put(GraphHeaderParams.graph_id.name(), graphId);
			request.setManagerName(GraphEngineManagers.SEARCH_MANAGER);
			request.setOperation("searchNodes");

			request.put(GraphDACParams.search_criteria.name(), sc);
			request.put(GraphDACParams.get_tags.name(), true);

			Future<Object> response = Patterns.ask(reqRouter, request, TestUtil.timeout);

			handleFutureBlock(response, "searchNodes", null, null);
			Thread.sleep(10000);

			Request request1 = new Request();
			request1.getContext().put(GraphHeaderParams.graph_id.name(), graphId);
			request1.setManagerName(GraphEngineManagers.SEARCH_MANAGER);
			request1.setOperation("getNodesCount");

			request1.put(GraphDACParams.search_criteria.name(), sc);
			request1.put(GraphDACParams.get_tags.name(), true);

			Future<Object> response1 = Patterns.ask(reqRouter, request1, TestUtil.timeout);

			handleFutureBlock(response1, "getNodesCount", null, null);
			Thread.sleep(10000);

			Request request2 = new Request();
			request2.getContext().put(GraphHeaderParams.graph_id.name(), graphId);
			request2.setManagerName(GraphEngineManagers.SEARCH_MANAGER);
			request2.setOperation("getNodesByObjectType");

			request2.put(GraphDACParams.object_type.name(), "Concept");

			Future<Object> response2 = Patterns.ask(reqRouter, request2, TestUtil.timeout);

			handleFutureBlock(response2, "getNodesByObjectType", null, null);
			Thread.sleep(10000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * updateDataNode
	 */
	@Test
	public void testC() {
		try {
			String graphId = "domain";
			String objectType = "Content";
			Map<String, Object> contentMap = mapper.readValue(
					"{\"name\":\"TestdataNodeUpdate\",\"versionKey\":\"" + contentVersion.get("testNode1") + "\"}",
					new TypeReference<Map<String, Object>>() {
			});

			Node node = new Node();
			node.setNodeType("DATA_NODE");
			node.setMetadata(contentMap);
			node.setGraphId(graphId);
			node.setObjectType(objectType);

			Request request = new Request();
			request.getContext().put(GraphHeaderParams.graph_id.name(), graphId);
			request.setManagerName(GraphEngineManagers.NODE_MANAGER);
			request.setOperation("updateDataNode");
			request.put(GraphDACParams.node_id.name(), "testNode1");
			request.put(GraphDACParams.node.name(), node);
			request.put(GraphDACParams.object_type.name(), objectType);

			Future<Object> response = Patterns.ask(reqRouter, request, TestUtil.timeout);

			handleFutureBlock(response, "updateDataNode", GraphDACParams.node_id.name(),
					GraphDACParams.versionKey.name());
			Thread.sleep(10000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testD() {
		try {
			String graphId = "domain";
			Request request = new Request();
			request.getContext().put(GraphHeaderParams.graph_id.name(), graphId);
			request.setManagerName(GraphEngineManagers.NODE_MANAGER);
			request.setOperation("deleteDataNode");
			request.put(GraphDACParams.node_id.name(), "testNode2");
			Future<Object> resp = Patterns.ask(reqRouter, request, TestUtil.timeout);
	
			handleFutureBlock(resp, "deleteDataNode", null, null);
			Thread.sleep(10000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testE() {
		try {
			Request request = new Request();
			DefinitionDTO definitionDto = new DefinitionDTO();
			definitionDto.setIdentifier("DEFINITION_NODE_TEST");
			definitionDto.setObjectType("Taxonomy");
			definitionDto.setMetadata(mapper.readValue(metadata, new TypeReference<Map<String, Object>>() {
			}));
			
			request.getContext().put(GraphHeaderParams.graph_id.name(), "test");
			request.setManagerName(GraphEngineManagers.NODE_MANAGER);
			request.setOperation("saveDefinitionNode");
			request.put(GraphDACParams.definition_node.name(), definitionDto);
			Future<Object> response = Patterns.ask(reqRouter, request, t);
			handleFutureBlock(response, "saveDefinitionNode", GraphDACParams.node_id.name(), null);
			Thread.sleep(10000);

			Request request1 = new Request();
			request1.getContext().put(GraphHeaderParams.graph_id.name(), "test");
			request1.put(GraphDACParams.object_type.name(), "Taxonomy");
			request1.setManagerName(GraphEngineManagers.SEARCH_MANAGER);
			request1.setOperation("getNodeDefinition");
			Future<Object> response1 = Patterns.ask(reqRouter, request1, t);
			handleFutureBlock(response1, "getNodeDefinition", null, null);
			Thread.sleep(10000);

			Request request2 = new Request();
			request2.getContext().put(GraphHeaderParams.graph_id.name(), "test");
			request2.put(GraphDACParams.object_type.name(), "Taxonomy");
			request2.setManagerName(GraphEngineManagers.SEARCH_MANAGER);
			request2.setOperation("getNodeDefinitionFromCache");
			Future<Object> response2 = Patterns.ask(reqRouter, request2, t);
			handleFutureBlock(response2, "getNodeDefinitionFromCache", null, null);
			Thread.sleep(10000);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}

	@Test
	public void testF() {
		try {
			Request request = new Request();
			request.getContext().put(GraphHeaderParams.graph_id.name(), "domain");
			request.setManagerName(GraphEngineManagers.SEARCH_MANAGER);
			request.setOperation("getAllDefinitions");
			Future<Object> response = Patterns.ask(reqRouter, request, t);
			handleDefintionDto(response, "getAllDefinitions");
			Thread.sleep(10000);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/*@Test
	public void testE1() {
		try {
	
			Request request = new Request();
			List<MetadataDefinition> definitions = new ArrayList<MetadataDefinition>();
			MetadataDefinition metadataDef = new MetadataDefinition();
			metadataDef.setPropertyName("versionCheckMode");
			metadataDef.setDefaultValue("STRICT");
			definitions.add(metadataDef);
			request.getContext().put(GraphHeaderParams.graph_id.name(), "test");
			request.put(GraphDACParams.object_type.name(), "Taxonomy");
			request.put(GraphDACParams.metadata_definitions.name(), definitions);
			request.setManagerName(GraphEngineManagers.NODE_MANAGER);
			request.setOperation("updateDefinition");
			Future<Object> response = Patterns.ask(reqRouter, request, t);
			handleFutureBlock(response, "updateDefinition", null, null);
			Thread.sleep(10000);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}*/

	@Test
	public void testG() {
		try {
			Request request = new Request();

			request.getContext().put(GraphHeaderParams.graph_id.name(), "test");
			request.put(GraphDACParams.object_type.name(), "Taxonomy");
			request.setManagerName(GraphEngineManagers.NODE_MANAGER);
			request.setOperation("deleteDefinition");
			Future<Object> response = Patterns.ask(reqRouter, request, t);
			handleFutureBlock(response, "deleteDefinition", null, null);
			Thread.sleep(10000);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * @param response
	 * @param string
	 */
	private void handleDefintionDto(Future<Object> response, String operation) {
		try {
			Object arg1 = Await.result(response, t.duration());
			System.out.println("----------------------");
			System.out.println(operation + " response: " + arg1);
			if (arg1 instanceof Response) {
				Response ar = (Response) arg1;
				// System.out.println(ar.getResult());
				
				@SuppressWarnings("unchecked")
				List<DefinitionDTO> dtos = (List<DefinitionDTO>) ar.getResult().get("definition_nodes");
				for(DefinitionDTO dto : dtos) {
					System.out.println(dto.getIdentifier() + " : : " + dto.getObjectType());

				}
				
				System.out.println(ar.getParams());
			}
			System.out.println("----------------------");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void handleFutureBlock(Future<Object> req, String operation, String param, String versionKey) {

		try {
			Object arg1 = Await.result(req, t.duration());
			System.out.println("----------------------");
			System.out.println(operation + " response: " + arg1);
			if (arg1 instanceof Response) {
				Response ar = (Response) arg1;
				// System.out.println(ar.getResult());
				// System.out.println(ar.getParams());

				if (null != param) {
					// System.out.println(ar.get(param));
					Assert.assertTrue(StringUtils.isNotBlank((String) ar.get(param)));

					if (null != versionKey) {
						// System.out.println(ar.get(versionKey));
						contentVersion.put((String) ar.get(param), (String) ar.get(versionKey));
						// System.out.println("VersionKey : " + contentVersion.get("testNode1"));
					}

				} else {
					Assert.assertTrue(ar.getParams().getStatus().equals(StatusType.successful.name()));
				}


			} else {
			}
			System.out.println("----------------------");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
