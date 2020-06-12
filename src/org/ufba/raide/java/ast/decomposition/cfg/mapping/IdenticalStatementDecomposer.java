package org.ufba.raide.java.ast.decomposition.cfg.mapping;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.ufba.raide.java.ast.decomposition.cfg.PDGNode;

public class IdenticalStatementDecomposer {

	private Map<String, Set<PDGNode>> identicalNodeMap;
	
	public IdenticalStatementDecomposer(Set<PDGNode> nodes) {
		this.identicalNodeMap = new LinkedHashMap<String, Set<PDGNode>>();
		Map<String, Set<PDGNode>> tempMap = new LinkedHashMap<String, Set<PDGNode>>();
		for(PDGNode node : nodes) {
			String nodeString = node.getASTStatement().toString();
			if(tempMap.containsKey(nodeString)) {
				Set<PDGNode> keyNodes = tempMap.get(nodeString);
				keyNodes.add(node);
			}
			else {
				Set<PDGNode> keyNodes = new LinkedHashSet<PDGNode>();
				keyNodes.add(node);
				tempMap.put(nodeString, keyNodes);
			}
		}
		for(String key : tempMap.keySet()) {
			Set<PDGNode> keyNodes = tempMap.get(key);
			if(keyNodes.size() > 1) {
				identicalNodeMap.put(key, keyNodes);
			}
		}
	}

	public Map<String, Set<PDGNode>> getIdenticalNodeMap() {
		return identicalNodeMap;
	}
}
