package query_eval;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import indice.estrutura.Ocorrencia;

public class BooleanRankingModel implements RankingModel {
	
	
	HashSet<Integer> listaGlobal;
	
	public enum OPERATOR{
		AND,OR;
	}
	private OPERATOR operator;
	public BooleanRankingModel(OPERATOR op)
	{
		this.operator = op;
	}
	
	/**
	 * Retorna a lista de documentos (nao eh necessário fazer a ordenação) para a consulta  mapQueryOcur e a lista de
	 * ocorrencias de documentos lstOcorrPorTermoDocs.
	 *
	 * mapQueryOcur: Mapa de ocorrencia de termos na consulta
	 * lstOcorrPorTermoDocs: lista de ocorrencia dos termos nos documentos (apenas os termos que exitem na consulta)
	 */
	@Override
	public List<Integer> getOrderedDocs(Map<String, Ocorrencia> mapQueryOcur,
			Map<String, List<Ocorrencia>> lstOcorrPorTermoDocs) {
		
		if(this.operator == OPERATOR.AND) {
			return intersectionAll(lstOcorrPorTermoDocs);
		} else {
			return unionAll(lstOcorrPorTermoDocs);
		}
	}
	/**
	 * Faz a uniao de todos os elementos
	 * @param lstOcorrPorTermoDocs
	 * @return
	 */
	public List<Integer> unionAll(Map<String, List<Ocorrencia>> lstOcorrPorTermoDocs) {
		List<Integer> finalList = new ArrayList<>();
		HashSet<Integer> set = new HashSet<>();
		
		for(Map.Entry<String, List<Ocorrencia>> entry : lstOcorrPorTermoDocs.entrySet()) {
		    List<Ocorrencia> values = entry.getValue();
		    for (Ocorrencia ocorrencia : values) {
		    	set.add(ocorrencia.getDocId());
		    }
		}
		
		finalList.addAll(set);
		return finalList;
		
		

	}
	/**
	 * Faz a interseção de todos os elementos
	 * @param lstOcorrPorTermoDocs
	 * @return
	 */
	public List<Integer> intersectionAll(Map<String, List<Ocorrencia>> lstOcorrPorTermoDocs)
	{
		
		HashSet<Integer> set = new HashSet<>();
		List<Integer> finalList = new ArrayList<>();
		Map.Entry<String, List<Ocorrencia>> firstEntry = lstOcorrPorTermoDocs.entrySet().iterator().next();
		
		for (Ocorrencia temp : firstEntry.getValue()) {
			set.add(temp.getDocId());
		}
		
		for(Map.Entry<String, List<Ocorrencia>> entry : lstOcorrPorTermoDocs.entrySet()) {
		    List<Ocorrencia> values = entry.getValue();
		    HashSet<Integer> aux = new HashSet<>();
		    for (Ocorrencia ocorrencia : values) {
		    	aux.add(ocorrencia.getDocId());
		    }
		    set.retainAll(aux);
		}
		
		finalList.addAll(set);
		
		return finalList;
		
	}

	
	
}
