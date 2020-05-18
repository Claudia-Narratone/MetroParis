package it.polito.tdp.metroparis.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.jgrapht.traverse.DepthFirstIterator;

import it.polito.tdp.metroparis.db.MetroDAO;

public class Model {
	
	private Graph<Fermata, DefaultEdge> graph;
	private List<Fermata> fermate;
	private Map<Integer, Fermata> fermateIdMap;
	
	public Model() {
		this.graph=new SimpleDirectedGraph<>(DefaultEdge.class);
		MetroDAO dao=new MetroDAO();
		this.fermate=dao.getAllFermate();
		this.fermateIdMap=new HashMap<>();
		for(Fermata f:fermate) {
			fermateIdMap.put(f.getIdFermata(), f);
		}
		
		Graphs.addAllVertices(this.graph, this.fermate);
		
		//CREAZIONE ARCHI -- metodo 1
		
		/*for(Fermata fp:fermate) {
			for(Fermata fa:fermate) {
				if(dao.fermateConnesse(fp, fa)) {
					this.graph.addEdge(fp, fa);
				}
			}
		}*/
		
		//CREAZIONE ARCHI -- metodo 2 (da un vertice trova tutti i connessi)
		
		/*for(Fermata fp:fermate) {
			List<Fermata> connesse=dao.fermateSuccessive(fp, fermateIdMap);
			
			for(Fermata fa: connesse) {
				this.graph.addEdge(fp, fa);
			}
		}*/
		
		//CREAZIONE ARCHI -- metodo 3 (chiedo al DB l'elenco degli archi)
		List<CoppiaFermate> coppie=dao.coppieFermate(fermateIdMap);
		for(CoppiaFermate c: coppie) {
			this.graph.addEdge(c.getFp(), c.getFa());
		}
		
		
		//System.out.println(this.graph);
		System.out.format("Grafo caricato con %d vertici e %d archi \n", 
				this.graph.vertexSet().size(),
				this.graph.edgeSet().size());
	}
	
	//metodo che visita l'intero grafico con strategia Breadth First e ritorna l'insieme di vertici incontrati
	public List<Fermata> visitaAmpiezza(Fermata source) {
		BreadthFirstIterator<Fermata, DefaultEdge> bfv=new BreadthFirstIterator<>(graph, source);
		List<Fermata> visita=new ArrayList<>();
		
		while(bfv.hasNext()) {
			visita.add(bfv.next());
		}
		return visita;
	}
	
	//metodo che restituisce l'albero della ricerca breadth first restituisce mappa<verticeNuovoScoperto, verticePrecedenteDaCuiArrivavo>
	public Map<Fermata, Fermata> alberoVisita(Fermata source) {
		
		Map<Fermata, Fermata> alberoMap=new HashMap<Fermata, Fermata>();
		alberoMap.put(source, null);
		
		BreadthFirstIterator<Fermata, DefaultEdge> bfv=new BreadthFirstIterator<>(graph, source);
		bfv.addTraversalListener(new TraversalListener<Fermata, DefaultEdge>() {

			@Override
			public void connectedComponentFinished(ConnectedComponentTraversalEvent e) {
			}

			@Override
			public void connectedComponentStarted(ConnectedComponentTraversalEvent e) {
			}

			@Override
			public void edgeTraversed(EdgeTraversalEvent<DefaultEdge> e) {
				//la visita sta considerando un nuovo arco
				//questo arco ha scoperto un nuovo vertice?
				//se si, provenendo da dove?
				DefaultEdge edge=e.getEdge(); //(a, b) : ho scoperto a partendo da b o viceversa?
				Fermata a= graph.getEdgeSource(edge);
				Fermata b= graph.getEdgeTarget(edge);
				if(alberoMap.containsKey(a)) {
					alberoMap.put(b, a);
				}else {
					alberoMap.put(a, b);
				}
			}

			@Override
			public void vertexTraversed(VertexTraversalEvent<Fermata> e) {
			
			}

			@Override
			public void vertexFinished(VertexTraversalEvent<Fermata> e) {
			
			}
		});
		
		while(bfv.hasNext()) {
			bfv.next();
		}
		return alberoMap;
	}
	
	//metodo che visita l'intero grafico con strategia Depth First e ritorna l'insieme di vertici incontrati
	public List<Fermata> visitaProfondita(Fermata source) {
		DepthFirstIterator<Fermata, DefaultEdge> dfv=new DepthFirstIterator<>(graph, source);
		List<Fermata> visita=new ArrayList<>();
		
		while(dfv.hasNext()) {
			visita.add(dfv.next());
		}
		return visita;
	}
	
	public static void main(String args[]) {
		Model m=new Model();
		List<Fermata> visita1=m.visitaAmpiezza(m.fermate.get(0));
		System.out.println(visita1);
		List<Fermata> visita2=m.visitaProfondita(m.fermate.get(0));
		System.out.println(visita2);
		
		Map<Fermata, Fermata> albero= m.alberoVisita(m.fermate.get(0));
		for(Fermata f: albero.keySet()) {
			System.out.format("%s <- %s \n", f, albero.get(f));
		}
	}
}
