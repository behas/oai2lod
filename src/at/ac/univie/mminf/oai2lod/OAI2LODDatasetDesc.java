package at.ac.univie.mminf.oai2lod;

import java.util.Map;

import org.joseki.DatasetDesc;

import com.hp.hpl.jena.query.DataSource;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Resource;


public class OAI2LODDatasetDesc extends DatasetDesc {

	private DataSource dataset;
	
	public OAI2LODDatasetDesc(DataSource dataset) {
		super(null);
		this.dataset = dataset;
	}

	public Dataset getDataset() {
		return this.dataset;
	}

	public void clearDataset() {
		this.dataset = null;
	}

	public void freeDataset() {
		this.dataset = null;
	}

	public void setDefaultGraph(Resource dftGraph) {
		throw new RuntimeException("OAI2LODDatasetDesc.setDefaultGraph is not implemented");
	}
	
	public Resource getDefaultGraph() {
		throw new RuntimeException("OAI2LODDatasetDesc.getDefaultGraph is not implemented");
	}

	public void addNamedGraph(String uri, Resource r) {
		throw new RuntimeException("OAI2LODDatasetDesc.addNamedGraph is not implemented");
	}

	public Map getNamedGraphs() {
		throw new RuntimeException("OAI2LODDatasetDesc.getNamedGraphs is not implemented");
	}

	public String toString() {
		return "OAI2LODDatasetDesc(" + this.dataset + ")";
	}
}
