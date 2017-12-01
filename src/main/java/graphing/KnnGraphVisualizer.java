package graphing;

import com.google.inject.Guice;
import knn.api.KnowledgeNode;
import knn.api.KnowledgeNodeNetwork;
import org.graphstream.graph.Node;
import prometheus.api.Prometheus;
import prometheus.guice.PrometheusModule;
import tags.Tag;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;

abstract class KnnGraphVisualizer extends GraphVisualizer {
    private static final int NUM_INITIAL_ACTIVE_TAGS = 5;
    private static final String KNN_STYLE_SHEET_PATH = "graphs/knn/knn_graph_style_sheet.css";
    private static final String ANIMAL_DATA_PATH = "data/animalData2.txt";
    private static final String RESET_ID = "reset";
    private static final String START_ID = "start";
    private static final String STOP_ID = "stop";
    private Set<String> activeIDs;
    boolean forceContinue = false;

    KnowledgeNodeNetwork knn;

    abstract Set<Tag> search();

    Set<Tag> getInitialActiveTags() {
        Set<Tag> activeTags = new HashSet<>();
        int i = 0;
        for (KnowledgeNode kn : knn.getKnowledgeNodes()) {
            activeTags.add(kn.getInputTag());
            if (i == NUM_INITIAL_ACTIVE_TAGS) {
                break;
            }
            i++;
        }
        return activeTags;
    }

    String getKnnDataPath() {
        return ANIMAL_DATA_PATH;
    }

    @Override
    String getStyleSheetPath() {
        return KNN_STYLE_SHEET_PATH;
    }

    @Override
    int getSleepDelay() {
        return 1500;
    }

    @Override
    void setupNodes() {
        loadKNN();
        addButtons();
        addInitialNodes();
        activateInitialNodes();
    }

    private void addButtons() {
        addButton(RESET_ID, "Reset", 0, 0, false);
        addButton(START_ID, "Start", 0, 0.5, false);
        addButton(STOP_ID, "Stop", 0, 1, true);
    }

    private void addButton(String id, String label, double x, double y, boolean clicked) {
        Node node = graph.addNode(id);
        node.addAttribute("layout.frozen");
        node.addAttribute("xy", x, y);
        if (clicked)
            node.setAttribute("ui.class", "butt_clicked");
        else
            node.setAttribute("ui.class", "butt");
        node.setAttribute("ui.label", label);
    }

    private void loadKNN() {
        Prometheus prometheus = Guice.createInjector(new PrometheusModule()).getInstance(Prometheus.class);
        knn = prometheus.getKnowledgeNodeNetwork();
        knn.loadData(getKnnDataPath());
        activeIDs = new HashSet<>();
    }

    private void addInitialNodes() {
        for (KnowledgeNode knowledgeNode : knn.getKnowledgeNodes()) {
            String inputID = knowledgeNode.getInputTag().toString();
            String knID = knowledgeNode.toString();
            graph.addEdge(inputID + knID, inputID, knID, true);
            updateNodeTagClass(inputID);
            updateNodeTagClass(knID);
            for (Tag t : knowledgeNode.getOutputTags()) {
                String outputID = t.toString();
                graph.addEdge(knID + outputID, knID, outputID, true);
                updateNodeTagClass(outputID);
            }
        }
    }

    private void activateInitialNodes() {
        for (Tag t : getInitialActiveTags()) {
            knn.addActiveTag(t);
            String id = t.toString();
            activeIDs.add(id);
            updateNodeTagClass(id);
        }
    }

    @Override
    boolean updateGraph() {
        Set<Tag> activatedTags = search();
        for (Tag t : knn.getActiveTags()) {
            String id = t.toString();
            activeIDs.add(id);
            updateNodeTagClass(id);

            KnowledgeNode kn = knn.getKnowledgeNode(t);
            if (kn != null && kn.isFired()) {
                String knID = kn.toString();
                activeIDs.add(knID);
                updateNodeTagClass(knID);
            }
        }
        return !activatedTags.isEmpty() || forceContinue;
    }

    @Override
    public void buttonPushed(String id) {
        Node node = graph.getNode(id);
        if (RESET_ID.equals(id)) {
            node.setAttribute("ui.class", "butt_clicked");
            knn.clearActiveTags();
            Set<String> prevActiveIDs = new HashSet<>(activeIDs);
            activeIDs.clear();
            for (String activeID : prevActiveIDs) {
                updateNodeTagClass(activeID);
            }
            loadKNN();
            activateInitialNodes();
        } else if (START_ID.equals(id)) {
            node.setAttribute("ui.class", "butt_clicked");
            graph.getNode(STOP_ID).setAttribute("ui.class", "butt");
            updated = true;
        } else if (STOP_ID.equals(id)) {
            node.setAttribute("ui.class", "butt_clicked");
            graph.getNode(START_ID).setAttribute("ui.class", "butt");
            updated = false;
        } else {
            node.setAttribute("ui.class", "clicked");
        }
    }

    @Override
    public void buttonReleased(String id) {
        updateNodeTagClass(id);
//        saveScreenshot("");
    }

    private void updateNodeTagClass(String id) {
        Node node = graph.getNode(id);
        if (RESET_ID.equals(id)) {
            node.setAttribute("ui.class", "butt");
        } else if (!START_ID.equals(id) && !STOP_ID.equals(id)) {
            node.addAttribute("ui.label", id);
            String nodeType = id.split("\\[")[0].toLowerCase();
            if (activeIDs.contains(id)) {
                node.setAttribute("ui.class", MessageFormat.format("active_{0}", nodeType));
            } else {
                node.setAttribute("ui.class", nodeType);
            }
        }
    }
}
