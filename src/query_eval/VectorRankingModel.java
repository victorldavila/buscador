package query_eval;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import indice.estrutura.Ocorrencia;

public class VectorRankingModel implements RankingModel {
	private IndicePreCompModelo idxPrecompVals;

	public static double tf(int freqTerm) {
		return (1 + Math.log(freqTerm));
	}

	public static double idf(int numDocs, int numDocsTerm) {
		return Math.log(numDocs / numDocsTerm);
	}

	public static double tfIdf(int numDocs, int freqTerm, int numDocsTerm) {
		return tf(freqTerm) * idf(numDocs, numDocsTerm);
	}

	public VectorRankingModel(IndicePreCompModelo idxPrecomp) {
		this.idxPrecompVals = idxPrecomp;
	}

	/**
	 * Retorna uma lista ordenada de documentos usando o tf xidf. Para isso,
	 * para cada termo da consulta, calcule o w_iq (peso do termo i na consulta
	 * q) e o w_ij e, logo apos, acumule o w_iq x w_ij no peso do documento
	 * (dj_weight). Depois de ter o peso de cada documento, divida pela norma do
	 * documento (use o idxPrecompVals.getNormaDocumento) Apos ter o peso para
	 * cada documento, rode o UtilQuery.getOrderedList para retornar a lista de
	 * documentos ordenados pela consulta
	 */
	@Override
	public List<Integer> getOrderedDocs(Map<String, Ocorrencia> mapQueryOcur,
                                      Map<String, List<Ocorrencia>> lstOcorrPorTermoDocs) {

    Map<Integer, Double> dj_weight = sumWiqTimesWij(mapQueryOcur, lstOcorrPorTermoDocs);

    return UtilQuery.getOrderedList(dj_weight);
	}

  private Map<Integer, Double> sumWiqTimesWij(Map<String, Ocorrencia> mapQueryOcur,
                              Map<String, List<Ocorrencia>> lstOcorrPorTermoDocs) {

    Map<Integer, Double> dj_weight = new HashMap<Integer, Double>();

		double normaQuery = 0;//calculo norma
		Map<Integer, Double> normaDocumento = new HashMap<>();//calculo norma;

	  for (String key : mapQueryOcur.keySet()){
      List<Ocorrencia> listOcorrenciaPorDoc = lstOcorrPorTermoDocs.get(key);
      Ocorrencia ocorrenciaPorQuery = mapQueryOcur.get(key);

      double wiq = tfIdf(idxPrecompVals.getNumDocumentos(),
      ocorrenciaPorQuery.getFreq(),
      listOcorrenciaPorDoc.size());

      normaQuery += Math.pow(wiq, 2);

			normaDocumento.put(ocorrenciaPorQuery.getDocId(), (double) 0);

      for (Ocorrencia ocorrenciaPorTermo : listOcorrenciaPorDoc) {
        double wij = tfIdf(idxPrecompVals.getNumDocumentos(),
        ocorrenciaPorTermo.getFreq(),
        listOcorrenciaPorDoc.size());

        normaDocumento.put(ocorrenciaPorQuery.getDocId(), normaDocumento.get(ocorrenciaPorQuery.getDocId()) + Math.pow(wij, 2)) ;

        if (dj_weight.containsKey(key)) {
          double sum = dj_weight.get(key);

          dj_weight.put(ocorrenciaPorQuery.getDocId(), sum + (wij * wiq));
        } else {
          dj_weight.put(ocorrenciaPorQuery.getDocId(), (wij * wiq));
        }
      }
    }

    return updateSimilarity(dj_weight, normaQuery, normaDocumento);
  }

  private Map<Integer, Double> updateSimilarity(Map<Integer, Double> djWeight, double normaQuerry, Map<Integer, Double> normaDoc) {
    for (Integer docId : djWeight.keySet()) {
      djWeight.put(docId,
          djWeight.get(docId) / (normaDoc.get(docId) * normaQuerry));
    }

    return djWeight;
  }
}
