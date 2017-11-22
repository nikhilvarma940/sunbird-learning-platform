package com.ilimi.graph.model.node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.common.exception.ResponseCode;
import com.ilimi.graph.common.mgr.BaseGraphManager;
import com.ilimi.graph.dac.enums.GraphDACParams;
import com.ilimi.graph.dac.mgr.IGraphDACSearchMgr;
import com.ilimi.graph.model.AbstractDomainObject;
import com.ilimi.graph.model.IRelation;

import scala.concurrent.Promise;

public abstract class AbstractIndexNode extends AbstractDomainObject {

    private String nodeId;
    
    public AbstractIndexNode(BaseGraphManager manager, String graphId) {
        super(manager, graphId);
    }

    public String getNodeId() {
        return nodeId;
    }
    
    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    protected void failPromise(Promise<Map<String, Object>> promise, String errorCode, String msg) {
        List<String> msgs = new ArrayList<String>();
        msgs.add(msg);
        failPromise(promise, errorCode, msgs);
    }

    protected void failPromise(Promise<Map<String, Object>> promise, String errorCode, List<String> msgs) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(errorCode, msgs);
        promise.success(map);
    }

	protected Response getNodeObject(Request req, IGraphDACSearchMgr searchMgr, String nodeId) {
        Request request = new Request(req);
        request.put(GraphDACParams.node_id.name(), nodeId);
		Response response = searchMgr.getNodeByUniqueId(request);
		return response;
    }

    protected boolean checkIfNodeExists(Promise<Map<String, Object>> promise, Throwable arg0, Object arg1, String errorCode) {
        boolean valid = false;
        if (null != arg0) {
            promise.failure(arg0);
        } else {
            if (arg1 instanceof Response) {
                Response res = (Response) arg1;
                if (manager.checkError(res)) {
                    if (!StringUtils.equals(ResponseCode.RESOURCE_NOT_FOUND.name(), res.getResponseCode().name())) {
                        failPromise(promise, errorCode, manager.getErrorMessage(res));
                    } else {
                        valid = true;
                    }
                } else {
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put(GraphDACParams.node_id.name(), getNodeId());
                    promise.success(map);
                }
            } else {
                failPromise(promise, errorCode, "Internal Error");
            }
        }
        return valid;
    }

    protected boolean validateResponse(Promise<Map<String, Object>> promise, Throwable arg0, Object arg1, String errorCode, String errorMsg) {
        boolean valid = false;
        if (null != arg0) {
            promise.failure(arg0);
        } else {
            if (arg1 instanceof Response) {
                Response res = (Response) arg1;
                if (manager.checkError(res)) {
                    failPromise(promise, errorCode, manager.getErrorMessage(res));
                } else {
                    valid = true;
                }
            } else {
                failPromise(promise, errorCode, errorMsg);
            }
        }
        return valid;
    }

    protected void createIndexNodeRelation(final Promise<Map<String, Object>> promise, Throwable arg0, Object arg1, final Request req,
            final IRelation rel, final String errorCode, String errorMsg) {
        boolean valid = validateResponse(promise, arg0, arg1, errorCode, errorMsg);
        if (valid) {
            Response res = (Response) arg1;
            String nodeId = (String) res.get(GraphDACParams.node_id.name());
            setNodeId(nodeId);
			Map<String, List<String>> messageMap = rel.validateRelation(req);
			List<String> errMessages = getErrorMessages(messageMap);
			if (null == errMessages || errMessages.isEmpty()) {
				rel.createRelation(req);
				Map<String, Object> map = new HashMap<String, Object>();
				map.put(GraphDACParams.node_id.name(), getNodeId());
				promise.success(map);
			} else {
				failPromise(promise, errorCode, errMessages);
			}

        }
    }

}
